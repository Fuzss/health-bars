package fuzs.immersivedamageindicators.client;

import fuzs.immersivedamageindicators.client.handler.GuiRenderingHandler;
import fuzs.immersivedamageindicators.client.handler.HealthTrackerHandler;
import fuzs.immersivedamageindicators.client.handler.InLevelRenderingHandler;
import fuzs.immersivedamageindicators.client.handler.PickEntityHandler;
import fuzs.immersivedamageindicators.client.particle.DamageValueParticle;
import fuzs.immersivedamageindicators.init.ModRegistry;
import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import fuzs.puzzleslib.api.client.core.v1.context.ParticleProvidersContext;
import fuzs.puzzleslib.api.client.event.v1.ClientTickEvents;
import fuzs.puzzleslib.api.client.event.v1.gui.RenderGuiEvents;
import fuzs.puzzleslib.api.client.event.v1.renderer.GameRenderEvents;
import fuzs.puzzleslib.api.client.event.v1.renderer.RenderNameTagCallback;
import fuzs.puzzleslib.api.event.v1.entity.EntityTickEvents;

public class ImmersiveDamageIndicatorsClient implements ClientModConstructor {

    @Override
    public void onConstructMod() {
        registerEventHandlers();
    }

    private static void registerEventHandlers() {
        GameRenderEvents.BEFORE.register(PickEntityHandler::onBeforeGameRender);
        ClientTickEvents.START.register(PickEntityHandler::onStartClientTick);
        RenderNameTagCallback.EVENT.register(
                (entity, content, entityRenderer, poseStack, packedLight, partialTick, partialTick2) -> InLevelRenderingHandler.onRenderNameTag(
                        entity, content, entityRenderer, poseStack, partialTick, partialTick2));
        RenderGuiEvents.AFTER.register(GuiRenderingHandler::onAfterRenderGui);
        EntityTickEvents.END.register(HealthTrackerHandler::onEndEntityTick);
    }

    @Override
    public void onRegisterParticleProviders(ParticleProvidersContext context) {
        context.registerParticleProvider(ModRegistry.DAMAGE_VALUE_PARTICLE_TYPE.value(),
                new DamageValueParticle.Provider()
        );
    }
}
