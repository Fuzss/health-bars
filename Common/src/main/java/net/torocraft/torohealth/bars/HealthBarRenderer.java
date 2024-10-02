package net.torocraft.torohealth.bars;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import fuzs.immersivedamageindicators.ImmersiveDamageIndicators;
import fuzs.immersivedamageindicators.client.helper.HealthBarHelper;
import fuzs.immersivedamageindicators.client.helper.HealthTracker;
import fuzs.immersivedamageindicators.config.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.LivingEntity;
import fuzs.immersivedamageindicators.client.helper.GuiGraphicsHelper;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

public class HealthBarRenderer {

    private static ClientConfig.Particle particleConfig() {
        return ImmersiveDamageIndicators.CONFIG.get(ClientConfig.class).particle;
    }

    public static void render(HealthTracker healthTracker, PoseStack poseStack, LivingEntity entity, int x, int y, int width, boolean inWorld) {

        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

        float barProgress = healthTracker.getBarProgress(
                Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false));
        float backgroundBarProgress = healthTracker.getBackgroundBarProgress(
                Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false));
        int backgroundColor = ImmersiveDamageIndicators.CONFIG.get(ClientConfig.class).barColors.backgroundColor;
        float backgroundDim = (float) ImmersiveDamageIndicators.CONFIG.get(ClientConfig.class).barColors.backgroundDim;
        int color = HealthBarHelper.pickBarColor(entity);

        Matrix4f matrix4f = poseStack.last().pose();
        drawBar(ImmersiveDamageIndicators.id("boss_bar/white_background"), matrix4f, x, y, width, 1.0F,
                backgroundColor, inWorld, 1.0F
        );
        drawBar(ImmersiveDamageIndicators.id("boss_bar/white_progress"), matrix4f, x, y, width,
                backgroundBarProgress, color, inWorld, backgroundDim
        );
        drawBar(ImmersiveDamageIndicators.id("boss_bar/notched_12_progress"), matrix4f, x, y, width,
                backgroundBarProgress, color, inWorld, backgroundDim
        );
        drawBar(ImmersiveDamageIndicators.id("boss_bar/white_progress"), matrix4f, x, y, width, barProgress, color,
                inWorld, 1.0F
        );
        drawBar(ImmersiveDamageIndicators.id("boss_bar/notched_12_progress"), matrix4f, x, y, width, barProgress,
                color, inWorld, 1.0F
        );
    }

    public static void drawDamageNumber(GuiGraphics guiGraphics, Font font, int damageAmount, int posX, int posY, int width) {
        if (damageAmount != 0) {
            String s = Integer.toString(Math.abs(damageAmount));
            int stringWidth = font.width(s);
            int color = damageAmount > 0 ? particleConfig().healColor : particleConfig().damageColor;
            guiGraphics.drawString(font, s, posX + (width / 2) - stringWidth, posY + 5, color, true);
        }
    }

    public static void drawDamageNumber(PoseStack poseStack, Font font, int damageAmount, int posX, int posY, int width) {
        if (damageAmount != 0) {
            String s = Integer.toString(Math.abs(damageAmount));
            int stringWidth = font.width(s);
            int color = damageAmount > 0 ? particleConfig().healColor : particleConfig().damageColor;
            drawString(poseStack, font, s, posX + (width / 2) - stringWidth, posY + 5, color, true);
        }
    }

    public static void drawString(PoseStack poseStack, Font font, String text, int x, int y, int color, boolean dropShadow) {
        MultiBufferSource.BufferSource multiBufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        font.drawInBatch(text, x, y, color, dropShadow, poseStack.last().pose(), multiBufferSource,
                Font.DisplayMode.POLYGON_OFFSET, 0, 15728880, font.isBidirectional()
        );
    }

    private static void drawBar(ResourceLocation resourceLocation, Matrix4f matrix4f, int posX, int posY, int width, float percentage, int color, boolean inWorld, float dimFactor) {

        float r = FastColor.ARGB32.red(color) / 255.0F * dimFactor;
        float g = FastColor.ARGB32.green(color) / 255.0F * dimFactor;
        float b = FastColor.ARGB32.blue(color) / 255.0F * dimFactor;

        RenderSystem.setShaderColor(r, g, b, 1.0F);

        posX -= (width / 2);
        GuiGraphics guiGraphics = GuiGraphicsHelper.create(matrix4f);

        guiGraphics.blitSprite(resourceLocation, posX, posY, (int) (width * percentage), 5);


        matrix4f.translate(0.0F, 0.0F, 0.03F * (inWorld ? -1 : 1));
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

    }
}
