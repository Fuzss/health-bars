package fuzs.immersivedamageindicators.client;

import fuzs.immersivedamageindicators.client.handler.GuiRenderingHandler;
import fuzs.immersivedamageindicators.client.handler.InLevelRenderingHandler;
import fuzs.immersivedamageindicators.client.handler.PickEntityHandler;
import fuzs.immersivedamageindicators.client.helper.HealthTracker;
import fuzs.immersivedamageindicators.init.ModRegistry;
import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import fuzs.puzzleslib.api.client.core.v1.context.ParticleProvidersContext;
import fuzs.puzzleslib.api.client.event.v1.ClientTickEvents;
import fuzs.puzzleslib.api.client.event.v1.gui.RenderGuiEvents;
import fuzs.puzzleslib.api.client.event.v1.renderer.GameRenderEvents;
import fuzs.puzzleslib.api.client.event.v1.renderer.RenderNameTagCallback;
import fuzs.puzzleslib.api.client.particle.v1.ClientParticleHelper;
import fuzs.puzzleslib.api.event.v1.entity.EntityTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import fuzs.immersivedamageindicators.client.particle.DamageValueParticle;

public class ImmersiveDamageIndicatorsClient implements ClientModConstructor {

    @Override
    public void onConstructMod() {
        registerEventHandlers();
    }

    private static void registerEventHandlers() {
        GameRenderEvents.BEFORE.register(PickEntityHandler::onBeforeGameRender);
        ClientTickEvents.START.register(PickEntityHandler::onStartClientTick);
        RenderNameTagCallback.EVENT.register(InLevelRenderingHandler::onRenderNameTag);
        RenderGuiEvents.AFTER.register(GuiRenderingHandler::onAfterRenderGui);
        EntityTickEvents.END.register(entity -> {
            if (entity.level().isClientSide && entity instanceof LivingEntity livingEntity) {
                HealthTracker healthTracker = HealthTracker.getHealthTracker(livingEntity, true);
                healthTracker.tick(livingEntity);
                int healthDelta = healthTracker.getLastHealthDelta();
                if (healthDelta != 0) {
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
        });
    }

    @Override
    public void onRegisterParticleProviders(ParticleProvidersContext context) {
        context.registerParticleProvider(ModRegistry.DAMAGE_VALUE_PARTICLE_TYPE.value(), new DamageValueParticle.Provider());
    }
}
