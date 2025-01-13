package net.torocraft.torohealth.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.torocraft.torohealth.bars.HealthBarRenderer;
import net.torocraft.torohealth.bars.ParticleRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class WorldRendererMixin {

  @Shadow
  private EntityRenderDispatcher entityRenderDispatcher;

  @Inject(method = "renderEntity", at = @At(value = "RETURN"))
  private void renderEntity(Entity entity, double x, double y, double z, float g,
      PoseStack matrix, MultiBufferSource v, CallbackInfo info) {
    if (entity instanceof LivingEntity) {
      HealthBarRenderer.prepareRenderInWorld((LivingEntity) entity);
    }
  }

  @Inject(method = "render", at = @At(value = "RETURN"))
  private void render(PoseStack matrices, float tickDelta, long limitTime,
      boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer,
      LightTexture lightmapTextureManager, Matrix4f matrix, CallbackInfo info) {
    HealthBarRenderer.renderInWorld(matrices, camera);
    ParticleRenderer.renderParticles(matrices, camera);
  }

}
