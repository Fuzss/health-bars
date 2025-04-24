package fuzs.healthbars.neoforge.client;

import fuzs.healthbars.HealthBars;
import fuzs.healthbars.client.HealthBarsClient;
import fuzs.healthbars.data.client.ModLanguageProvider;
import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import fuzs.puzzleslib.neoforge.api.data.v2.core.DataProviderHelper;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;

@Mod(value = HealthBars.MOD_ID, dist = Dist.CLIENT)
public class HealthBarsNeoForgeClient {

    public HealthBarsNeoForgeClient() {
        ClientModConstructor.construct(HealthBars.MOD_ID, HealthBarsClient::new);
        DataProviderHelper.registerDataProviders(HealthBars.MOD_ID, ModLanguageProvider::new);
    }
}
