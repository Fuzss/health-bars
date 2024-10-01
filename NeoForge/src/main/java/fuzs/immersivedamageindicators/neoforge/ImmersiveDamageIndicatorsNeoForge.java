package fuzs.immersivedamageindicators.neoforge;

import fuzs.immersivedamageindicators.ImmersiveDamageIndicators;
import fuzs.puzzleslib.api.core.v1.ModConstructor;
import net.neoforged.fml.common.Mod;

@Mod(ImmersiveDamageIndicators.MOD_ID)
public class ImmersiveDamageIndicatorsNeoForge {

    public ImmersiveDamageIndicatorsNeoForge() {
        ModConstructor.construct(ImmersiveDamageIndicators.MOD_ID, ImmersiveDamageIndicators::new);
    }
}
