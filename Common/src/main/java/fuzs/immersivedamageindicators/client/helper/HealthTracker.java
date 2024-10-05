package fuzs.immersivedamageindicators.client.helper;

import fuzs.immersivedamageindicators.init.ModRegistry;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public class HealthTracker {
    static final int MAX_HEALTH_DELAY = 30;
    static final int MAX_HEALTH_DELAY_PROGRESS = 10;
    static final int MAX_HEALTH_DELAY_FREEZE_TICKS = 200;

    @Nullable
    private Component displayName;
    private float maxHealth;
    private int armorValue;
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
        if (this.displayName == null) {
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
        this.displayName = livingEntity.getDisplayName();
        this.maxHealth = livingEntity.getMaxHealth();
        this.armorValue = livingEntity.getArmorValue();
    }

    public Component getDisplayName() {
        return this.displayName != null ? this.displayName : CommonComponents.EMPTY;
    }

    public int getHealth() {
        return Mth.ceil(this.health);
    }

    public int getMaxHealth() {
        return Mth.ceil(this.maxHealth);
    }

    public int getArmorValue() {
        return this.armorValue;
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
}
