package fuzs.healthbars.fabric.client;

import fuzs.healthbars.HealthBars;
import fuzs.healthbars.client.HealthBarsClient;
import fuzs.healthbars.client.particle.DamageValueParticleGroup;
import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleRendererRegistry;

public class HealthBarsFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientModConstructor.construct(HealthBars.MOD_ID, HealthBarsClient::new);
        // TODO move this to Puzzles Lib
        ParticleRendererRegistry.register(DamageValueParticleGroup.GROUP, DamageValueParticleGroup::new);
    }
}
