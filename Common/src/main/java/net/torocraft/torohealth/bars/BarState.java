package net.torocraft.torohealth.bars;

import fuzs.immersivedamageindicators.ImmersiveDamageIndicators;
import fuzs.immersivedamageindicators.config.ClientConfig;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public class BarState {
    private static final int HEALTH_INDICATOR_DELAY = 10;

    public final LivingEntity entity;
    public float health;
    public float previousHealth;
    public float previousHealthDisplay;
    public float previousHealthDelay;
    public int lastDmg;
    public int lastDmgCumulative;
    public float lastHealth;
    public float lastDmgDelay;
    private float animationSpeed = 0.0F;

    public BarState(LivingEntity entity) {
        this.entity = entity;
    }

    public void tick() {
        this.health = Math.min(this.entity.getHealth(), this.entity.getMaxHealth());
        this.incrementTimers();

        if (this.lastHealth < 0.1) {
            this.reset();

        } else if (this.lastHealth != this.health) {
            this.handleHealthChange();

        } else if (this.lastDmgDelay == 0.0F) {
            this.reset();
        }

        this.updateAnimations();
    }

    private void reset() {
        this.lastHealth = this.health;
        this.lastDmg = 0;
        this.lastDmgCumulative = 0;
    }

    private void incrementTimers() {
        if (this.lastDmgDelay > 0) {
            this.lastDmgDelay--;
        }
        if (this.previousHealthDelay > 0) {
            this.previousHealthDelay--;
        }
    }

    private void handleHealthChange() {
        this.lastDmg = Mth.ceil(this.lastHealth) - Mth.ceil(this.health);
        this.lastDmgCumulative += this.lastDmg;

        this.lastDmgDelay = HEALTH_INDICATOR_DELAY * 2;
        this.lastHealth = this.health;
        if (ImmersiveDamageIndicators.CONFIG.get(ClientConfig.class).particle.show) {
            BarStates.PARTICLES.add(new BarParticle(this.entity, this.lastDmg));
        }
    }

    private void updateAnimations() {
        if (this.previousHealthDelay > 0) {
            float diff = this.previousHealthDisplay - this.health;
            if (diff > 0) {
                this.animationSpeed = diff / 10.0F;
            }
        } else if (this.previousHealthDelay < 1 && this.previousHealthDisplay > this.health) {
            this.previousHealthDisplay -= this.animationSpeed;
        } else {
            this.previousHealthDisplay = this.health;
            this.previousHealth = this.health;
            this.previousHealthDelay = HEALTH_INDICATOR_DELAY;
        }
    }
}
