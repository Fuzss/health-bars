package fuzs.examplemod.neoforge;

import fuzs.examplemod.ExampleMod;
import fuzs.puzzleslib.api.core.v1.ModConstructor;
import net.neoforged.fml.common.Mod;

@Mod(ExampleMod.MOD_ID)
public class ExampleModNeoForge {

    public ExampleModNeoForge() {
        ModConstructor.construct(ExampleMod.MOD_ID, ExampleMod::new);
    }
}
