package net.torocraft.torohealth.bars;

import fuzs.immersivedamageindicators.init.ModRegistry;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public class HealthTracker {
    private final LivingEntity entity;
    private float maxHealth;
    private float health;
    private float lastHealth;
    private int healthDelay;

    public HealthTracker(LivingEntity livingEntity) {
        this.entity = livingEntity;
        this.maxHealth = this.getMaxHealth();
        this.health = this.lastHealth = this.getHealth();
    }

    public static HealthTracker getHealthTracker(LivingEntity livingEntity) {
        if (!ModRegistry.HEALTH_TRACKER_ATTACHMENT_TYPE.has(livingEntity)) {
            HealthTracker healthTracker = new HealthTracker(livingEntity);
            ModRegistry.HEALTH_TRACKER_ATTACHMENT_TYPE.set(livingEntity, healthTracker);
            return healthTracker;
        } else {
            return ModRegistry.HEALTH_TRACKER_ATTACHMENT_TYPE.get(livingEntity);
        }
    }

    public void tick() {
        float health = this.getHealth();
        if (health != this.health) {
            this.lastHealth = this.getLastHealthProgress(1.0F);
            this.healthDelay = 30;
            this.health = health;
        } else if (this.healthDelay > 0) {
            this.healthDelay--;
        } else {
            this.lastHealth = health;
        }
        this.maxHealth = this.getMaxHealth();
    }

    private float getHealth() {
        return Mth.clamp(this.entity.getHealth(), 0.0F, this.entity.getMaxHealth());
    }

    private float getMaxHealth() {
        return this.entity.getMaxHealth();
    }

    private float getLastHealthProgress(float partialTick) {
        float delta = Mth.clamp(1.0F - (this.healthDelay - partialTick) / 10.0F, 0.0F, 1.0F);
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
