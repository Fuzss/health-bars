package fuzs.healthbars.common.client;

import fuzs.healthbars.common.HealthBars;
import fuzs.healthbars.common.client.handler.*;
import fuzs.healthbars.common.client.particle.DamageValueParticle;
import fuzs.healthbars.common.client.particle.DamageValueParticleGroup;
import fuzs.healthbars.common.init.ModRegistry;
import fuzs.puzzleslib.common.api.client.core.v1.ClientModConstructor;
import fuzs.puzzleslib.common.api.client.core.v1.context.GuiLayersContext;
import fuzs.puzzleslib.common.api.client.core.v1.context.KeyMappingsContext;
import fuzs.puzzleslib.common.api.client.core.v1.context.ParticleProvidersContext;
import fuzs.puzzleslib.common.api.client.event.v1.ClientTickEvents;
import fuzs.puzzleslib.common.api.client.event.v1.renderer.ComputeFieldOfViewCallback;
import fuzs.puzzleslib.common.api.client.event.v1.renderer.ExtractEntityRenderStateCallback;
import fuzs.puzzleslib.common.api.client.event.v1.renderer.SubmitNameTagCallback;
import fuzs.puzzleslib.common.api.event.v1.entity.EntityTickEvents;

public class HealthBarsClient implements ClientModConstructor {

    @Override
    public void onConstructMod() {
        registerEventHandlers();
    }

    private static void registerEventHandlers() {
        ComputeFieldOfViewCallback.EVENT.register(PickEntityHandler::onComputeFieldOfView);
        ClientTickEvents.START.register(PickEntityHandler::onStartClientTick);
        ExtractEntityRenderStateCallback.EVENT.register(InLevelRenderingHandler::onExtractEntityRenderState);
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
    public void onRegisterGuiLayers(GuiLayersContext context) {
        context.registerGuiLayer(GuiLayersContext.BOSS_BAR,
                HealthBars.id("health_bar"),
                GuiRenderingHandler::submitHealthBar);
    }
}
