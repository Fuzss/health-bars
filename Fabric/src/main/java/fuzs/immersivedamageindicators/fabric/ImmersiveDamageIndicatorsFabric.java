package fuzs.immersivedamageindicators.fabric;

import fuzs.immersivedamageindicators.ImmersiveDamageIndicators;
import fuzs.puzzleslib.api.core.v1.ModConstructor;
import net.fabricmc.api.ModInitializer;

public class ImmersiveDamageIndicatorsFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        ModConstructor.construct(ImmersiveDamageIndicators.MOD_ID, ImmersiveDamageIndicators::new);
    }
}
