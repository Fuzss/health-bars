package fuzs.healthbars.common.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

public class DamageValueParticle extends Particle {
    protected int damageValue;

    public DamageValueParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        super(level, x, y, z);
        this.setParticleSpeed(xSpeed, ySpeed, zSpeed);
        this.hasPhysics = false;
        this.gravity = 0.25F;
        this.lifetime = 50;
    }

    @Override
    public ParticleRenderType getGroup() {
        return DamageValueParticleGroup.GROUP;
    }

    public void setDamageValue(int damageValue) {
        this.damageValue = damageValue;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, RandomSource randomSource) {
            return new DamageValueParticle(level, x, y, z, xSpeed, ySpeed, zSpeed);
        }
    }
}
