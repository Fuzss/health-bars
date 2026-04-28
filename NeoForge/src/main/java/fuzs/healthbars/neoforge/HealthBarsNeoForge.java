package fuzs.healthbars.neoforge;

import fuzs.healthbars.common.HealthBars;
import fuzs.puzzleslib.common.api.core.v1.ModConstructor;
import net.neoforged.fml.common.Mod;

@Mod(HealthBars.MOD_ID)
public class HealthBarsNeoForge {

    public HealthBarsNeoForge() {
        ModConstructor.construct(HealthBars.MOD_ID, HealthBars::new);
    }
}
