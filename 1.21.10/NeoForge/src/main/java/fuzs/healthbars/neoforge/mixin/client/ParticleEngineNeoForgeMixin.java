package fuzs.healthbars.neoforge.mixin.client;

import fuzs.healthbars.client.particle.DamageValueParticleGroup;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.particle.ParticleRenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Deprecated
@Mixin(ParticleEngine.class)
abstract class ParticleEngineNeoForgeMixin {

    @Inject(method = "createParticleGroup", at = @At("HEAD"), cancellable = true)
    private void createParticleGroup(ParticleRenderType renderType, CallbackInfoReturnable<ParticleGroup<?>> callback) {
        // TODO move this to Puzzles Lib
        if (renderType == DamageValueParticleGroup.GROUP) {
            callback.setReturnValue(new DamageValueParticleGroup(ParticleEngine.class.cast(this)));
        }
    }
}
