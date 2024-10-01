package fuzs.immersivedamageindicators.fabric.client;

import fuzs.immersivedamageindicators.ImmersiveDamageIndicators;
import fuzs.immersivedamageindicators.client.ImmersiveDamageIndicatorsClient;
import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import net.fabricmc.api.ClientModInitializer;

public class ImmersiveDamageIndicatorsFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientModConstructor.construct(ImmersiveDamageIndicators.MOD_ID, ImmersiveDamageIndicatorsClient::new);
    }
}
