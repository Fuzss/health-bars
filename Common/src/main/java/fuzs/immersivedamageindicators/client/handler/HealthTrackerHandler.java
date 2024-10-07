package fuzs.immersivedamageindicators.client.handler;

import fuzs.immersivedamageindicators.ImmersiveDamageIndicators;
import fuzs.immersivedamageindicators.client.helper.HealthTracker;
import fuzs.immersivedamageindicators.client.particle.DamageValueParticle;
import fuzs.immersivedamageindicators.config.ClientConfig;
import fuzs.immersivedamageindicators.init.ModRegistry;
import fuzs.puzzleslib.api.client.particle.v1.ClientParticleHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class HealthTrackerHandler {

    public static void onEndEntityTick(Entity entity) {
        if (entity.level().isClientSide && entity instanceof LivingEntity livingEntity) {
            HealthTracker healthTracker = HealthTracker.getHealthTracker(livingEntity, true);
            healthTracker.tick(livingEntity);
            int healthDelta = healthTracker.getLastHealthDelta();
            if (healthDelta != 0 && ImmersiveDamageIndicators.CONFIG.get(ClientConfig.class).level.damageValues.renderDamageValues) {
                addDamageValueParticle(entity, healthDelta);
            }
        }
    }

    /**
     * Copied from <a
     * href="https://github.com/ToroCraft/ToroHealth/blob/master/src/main/java/net/torocraft/torohealth/bars/BarParticle.java">ToroHealth</a>.
     */
    private static void addDamageValueParticle(Entity entity, int healthDelta) {
        Minecraft minecraft = Minecraft.getInstance();
        Vec3 entityLocation = entity.position().add(0.0F, entity.getBbHeight() / 2.0F, 0.0F);
        Vec3 cameraLocation = minecraft.gameRenderer.getMainCamera().getPosition();
        double offsetBy = entity.getBbWidth();
        Vec3 offset = cameraLocation.subtract(entityLocation).normalize().scale(offsetBy);
        Vec3 pos = entityLocation.add(offset);
        double xd = entity.getRandom().nextGaussian() * 0.04;
        double yd = 0.10 + (entity.getRandom().nextGaussian() * 0.05);
        double zd = entity.getRandom().nextGaussian() * 0.04;
        Particle particle = ClientParticleHelper.addParticle(entity.level(),
                ModRegistry.DAMAGE_VALUE_PARTICLE_TYPE.value(), pos.x(), pos.y(), pos.z(), xd, yd, zd
        );
        if (particle != null) {
            ((DamageValueParticle) particle).setDamageValue(healthDelta);
        }
    }
}
