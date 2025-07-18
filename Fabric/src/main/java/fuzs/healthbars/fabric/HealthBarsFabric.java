package fuzs.healthbars.fabric;

import fuzs.healthbars.HealthBars;
import fuzs.puzzleslib.api.core.v1.ModConstructor;
import net.fabricmc.api.ModInitializer;

public class HealthBarsFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        ModConstructor.construct(HealthBars.MOD_ID, HealthBars::new);
    }
}
