package fuzs.examplemod.neoforge.client;

import fuzs.examplemod.ExampleMod;
import fuzs.examplemod.client.ExampleModClient;
import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;

@Mod(value = ExampleMod.MOD_ID, dist = Dist.CLIENT)
public class ExampleModNeoForgeClient {

    public ExampleModNeoForgeClient() {
        ClientModConstructor.construct(ExampleMod.MOD_ID, ExampleModClient::new);
    }
}
