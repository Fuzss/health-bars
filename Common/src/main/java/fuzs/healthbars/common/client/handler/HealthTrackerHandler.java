package fuzs.healthbars.common.client.handler;

import fuzs.healthbars.common.HealthBars;
import fuzs.healthbars.common.client.particle.DamageValueParticle;
import fuzs.healthbars.common.config.ClientConfig;
import fuzs.healthbars.common.init.ModRegistry;
import fuzs.healthbars.common.world.entity.HealthTracker;
import fuzs.puzzleslib.common.api.client.util.v1.ClientParticleHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class HealthTrackerHandler {

    public static void onEndEntityTick(Entity entity) {
        if (entity.level().isClientSide() && entity instanceof LivingEntity livingEntity) {
            HealthTracker healthTracker = HealthTracker.getHealthTracker(livingEntity, true);
            healthTracker.tick(livingEntity);
            int healthDelta = healthTracker.getLastHealthDelta();
            if (healthDelta != 0 && HealthBars.CONFIG.get(ClientConfig.class).level.damageValues.renderDamageValues) {
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
        Vec3 cameraLocation = minecraft.gameRenderer.getMainCamera().position();
        double offsetBy = entity.getBbWidth();
        Vec3 offset = cameraLocation.subtract(entityLocation).normalize().scale(offsetBy);
        Vec3 pos = entityLocation.add(offset);
        double xd = entity.getRandom().nextGaussian() * 0.04;
        double yd = 0.10 + (entity.getRandom().nextGaussian() * 0.05);
        double zd = entity.getRandom().nextGaussian() * 0.04;
        Particle particle = ClientParticleHelper.addParticle((ClientLevel) entity.level(),
                ModRegistry.DAMAGE_VALUE_PARTICLE_TYPE.value(),
                pos.x(),
                pos.y(),
                pos.z(),
                xd,
                yd,
                zd);
        if (particle != null) {
            ((DamageValueParticle) particle).setDamageValue(healthDelta);
        }
    }
}
