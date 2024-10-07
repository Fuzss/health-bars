package fuzs.immersivedamageindicators.client.helper;

import fuzs.immersivedamageindicators.ImmersiveDamageIndicators;
import fuzs.immersivedamageindicators.config.ClientConfig;
import fuzs.immersivedamageindicators.init.ModRegistry;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public class HealthTracker {
    static final int MAX_HEALTH_DELAY = 30;
    static final int MAX_HEALTH_DELAY_PROGRESS = 10;
    static final int MAX_HEALTH_DELAY_FREEZE_TICKS = 200;

    private EntityDataCache entityDataCache = EntityDataCache.EMPTY;
    private float maxHealth = -1;
    private float health;
    private float lastHealth;
    private int healthDelay;
    private float lastHealthDelta;
    private int healthDelayFreezeTicks;

    public static HealthTracker getHealthTracker(LivingEntity livingEntity, boolean createIfAbsent) {
        if (createIfAbsent && !ModRegistry.HEALTH_TRACKER_ATTACHMENT_TYPE.has(livingEntity)) {
            HealthTracker healthTracker = new HealthTracker();
            ModRegistry.HEALTH_TRACKER_ATTACHMENT_TYPE.set(livingEntity, healthTracker);
            return healthTracker;
        } else {
            return ModRegistry.HEALTH_TRACKER_ATTACHMENT_TYPE.get(livingEntity);
        }
    }

    /**
     * @return the health change that occurred during this tick
     */
    public int getLastHealthDelta() {
        return Mth.ceil(Math.abs(this.lastHealthDelta)) * Mth.sign(this.lastHealthDelta);
    }

    /**
     * @return the health change that occurred since {@link #lastHealth} was last updated
     */
    public int getHealthDelta() {
        float healthDelta = this.health - this.lastHealth;
        return Mth.ceil(Math.abs(healthDelta)) * Mth.sign(healthDelta);
    }

    public void tick(LivingEntity livingEntity) {
        this.lastHealthDelta = 0.0F;
        // serves as a timeout for mobs that have their health constantly change, like the wither which is regenerating continuously
        if (this.healthDelayFreezeTicks > 0) {
            this.healthDelayFreezeTicks--;
        }
        float health = Mth.clamp(livingEntity.getHealth(), 0.0F, livingEntity.getMaxHealth());
        if (this.maxHealth == -1) {
            this.health = this.lastHealth = health;
        } else if (health != this.health) {
            this.lastHealth = this.getLastHealthProgress(1.0F);
            this.lastHealthDelta = health - this.health;
            this.health = health;
            this.healthDelay =
                    health > 0.0F && this.healthDelayFreezeTicks > 0 ? MAX_HEALTH_DELAY : MAX_HEALTH_DELAY_PROGRESS;
        } else if (this.healthDelay > 0) {
            this.healthDelay--;
        } else {
            this.lastHealth = health;
            this.healthDelayFreezeTicks = MAX_HEALTH_DELAY_FREEZE_TICKS;
        }
        this.maxHealth = livingEntity.getMaxHealth();
        this.entityDataCache = EntityDataCache.of(livingEntity);
    }

    public EntityDataCache getData() {
        return this.entityDataCache;
    }

    public float getHealthProgress() {
        return Mth.clamp(this.health / this.maxHealth, 0.0F, 1.0F);
    }

    private float getLastHealthProgress(float partialTick) {
        float delta = Mth.clamp(1.0F - (this.healthDelay - partialTick) / MAX_HEALTH_DELAY_PROGRESS, 0.0F, 1.0F);
        return Mth.lerp(delta, this.lastHealth, this.health);
    }

    public float getBarProgress(float partialTick) {
        if (this.lastHealth < this.health) {
            return this.getLastHealthProgress(partialTick) / this.maxHealth;
        } else {
            return this.health / this.maxHealth;
        }
    }

    public float getBackgroundBarProgress(float partialTick) {
        if (this.lastHealth > this.health) {
            return this.getLastHealthProgress(partialTick) / this.maxHealth;
        } else {
            return this.health / this.maxHealth;
        }
    }

    public record EntityDataCache(Component displayName, int health, int maxHealth, int armorValue, double renderOffset) {
        public static final EntityDataCache EMPTY = new EntityDataCache(CommonComponents.EMPTY, 0, 0, 0, 0.0F);

        public static EntityDataCache of(LivingEntity livingEntity) {
            ClientConfig.Gui config = ImmersiveDamageIndicators.CONFIG.get(ClientConfig.class).gui;
            double renderOffset = config.mobRenderOffsets.<Double>getOptional(livingEntity.getType(), 0).orElse(0.0);
            return new EntityDataCache(livingEntity.getDisplayName(), Mth.ceil(livingEntity.getHealth()), Mth.ceil(livingEntity.getMaxHealth()), livingEntity.getArmorValue(), renderOffset);
        }

        public Component getHealthComponent() {
            return Component.literal(this.health + "/" + this.maxHealth);
        }
    }
}
