package net.torocraft.torohealth.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Gui;
import net.torocraft.torohealth.ToroHealth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class InGameHudMixin {

  @Inject(method = "render", at = @At("RETURN"))
  private void render(PoseStack matrixStack, float partial, CallbackInfo info) {
    ToroHealth.HUD.draw(matrixStack, ToroHealth.CONFIG);
  }

}
