package fuzs.healthbars.fabric.client;

import fuzs.healthbars.HealthBars;
import fuzs.healthbars.client.HealthBarsClient;
import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import net.fabricmc.api.ClientModInitializer;

public class HealthBarsFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientModConstructor.construct(HealthBars.MOD_ID, HealthBarsClient::new);
    }
}
