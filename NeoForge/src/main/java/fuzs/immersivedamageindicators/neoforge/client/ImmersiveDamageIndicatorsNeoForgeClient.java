package fuzs.immersivedamageindicators.neoforge.client;

import fuzs.immersivedamageindicators.ImmersiveDamageIndicators;
import fuzs.immersivedamageindicators.client.ImmersiveDamageIndicatorsClient;
import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;

@Mod(value = ImmersiveDamageIndicators.MOD_ID, dist = Dist.CLIENT)
public class ImmersiveDamageIndicatorsNeoForgeClient {

    public ImmersiveDamageIndicatorsNeoForgeClient() {
        ClientModConstructor.construct(ImmersiveDamageIndicators.MOD_ID, ImmersiveDamageIndicatorsClient::new);
    }
}
