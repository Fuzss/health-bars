package fuzs.healthbars.client;

import fuzs.healthbars.HealthBars;
import fuzs.healthbars.client.handler.*;
import fuzs.healthbars.client.particle.DamageValueParticle;
import fuzs.healthbars.client.particle.DamageValueParticleGroup;
import fuzs.healthbars.client.renderer.ModRenderTypes;
import fuzs.healthbars.init.ModRegistry;
import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import fuzs.puzzleslib.api.client.core.v1.context.GuiLayersContext;
import fuzs.puzzleslib.api.client.core.v1.context.KeyMappingsContext;
import fuzs.puzzleslib.api.client.core.v1.context.ParticleProvidersContext;
import fuzs.puzzleslib.api.client.core.v1.context.RenderPipelinesContext;
import fuzs.puzzleslib.api.client.event.v1.ClientTickEvents;
import fuzs.puzzleslib.api.client.event.v1.renderer.ExtractRenderStateCallback;
import fuzs.puzzleslib.api.client.event.v1.renderer.GameRenderEvents;
import fuzs.puzzleslib.api.client.event.v1.renderer.SubmitNameTagCallback;
import fuzs.puzzleslib.api.event.v1.entity.EntityTickEvents;

public class HealthBarsClient implements ClientModConstructor {

    @Override
    public void onConstructMod() {
        registerEventHandlers();
    }

    private static void registerEventHandlers() {
        GameRenderEvents.BEFORE.register(PickEntityHandler::onBeforeGameRender);
        ClientTickEvents.START.register(PickEntityHandler::onStartClientTick);
        ExtractRenderStateCallback.EVENT.register(InLevelRenderingHandler::onExtractRenderState);
        SubmitNameTagCallback.EVENT.register(InLevelRenderingHandler::onSubmitNameTag);
        EntityTickEvents.END.register(HealthTrackerHandler::onEndEntityTick);
    }

    @Override
    public void onRegisterParticleProviders(ParticleProvidersContext context) {
        context.registerParticleProvider(ModRegistry.DAMAGE_VALUE_PARTICLE_TYPE.value(),
                new DamageValueParticle.Provider());
        context.registerParticleRenderType(DamageValueParticleGroup.GROUP, DamageValueParticleGroup::new);
    }

    @Override
    public void onRegisterKeyMappings(KeyMappingsContext context) {
        KeyBindingHandler.onRegisterKeyMappings(context);
    }

    @Override
    public void onRegisterRenderPipelines(RenderPipelinesContext context) {
        context.registerRenderPipeline(ModRenderTypes.TEXT_BACKGROUND_PIPELINE);
    }

    @Override
    public void onRegisterGuiLayers(GuiLayersContext context) {
        context.registerGuiLayer(GuiLayersContext.BOSS_BAR,
                HealthBars.id("health_bar"),
                GuiRenderingHandler::submitHealthBar);
    }
}
