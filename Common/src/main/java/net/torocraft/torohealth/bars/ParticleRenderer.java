package net.torocraft.torohealth.bars;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import fuzs.immersivedamageindicators.ImmersiveDamageIndicators;
import fuzs.immersivedamageindicators.config.ClientConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class ParticleRenderer {
    public static List<BarParticle> PARTICLES = new ArrayList<>();

    public static void renderParticles(PoseStack poseStack, Camera camera) {
        for (BarParticle particle : PARTICLES) {
            renderParticle(poseStack, particle, camera);
        }
    }

    private static void renderParticle(PoseStack poseStack, BarParticle particle, Camera camera) {

        float scaleToGui = 0.025f;

        Minecraft client = Minecraft.getInstance();
        float tickDelta = client.getTimer().getGameTimeDeltaPartialTick(false);

        double x = Mth.lerp(tickDelta, particle.xPrev, particle.x);
        double y = Mth.lerp(tickDelta, particle.yPrev, particle.y);
        double z = Mth.lerp(tickDelta, particle.zPrev, particle.z);

        Vec3 camPos = camera.getPosition();
        double camX = camPos.x;
        double camY = camPos.y;
        double camZ = camPos.z;

        poseStack.pushPose();
        poseStack.translate(x - camX, y - camY, z - camZ);
        poseStack.mulPose(Axis.YP.rotationDegrees(-camera.getYRot()));
        poseStack.mulPose(Axis.XP.rotationDegrees(camera.getXRot()));
        poseStack.scale(-scaleToGui, -scaleToGui, scaleToGui);

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

        HealthBarRenderer.drawDamageNumber(poseStack, Minecraft.getInstance().font, particle.damage, 0, 0, 10);

        RenderSystem.disableBlend();

        poseStack.popPose();
    }

    public static void tick() {
        PARTICLES.forEach(p -> p.tick());
        PARTICLES.removeIf(p -> p.age > 50);
    }
}
