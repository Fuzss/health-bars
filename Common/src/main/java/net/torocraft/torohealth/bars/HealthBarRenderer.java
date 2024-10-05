package net.torocraft.torohealth.bars;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import fuzs.immersivedamageindicators.ImmersiveDamageIndicators;
import fuzs.immersivedamageindicators.client.handler.GuiRenderingHandler;
import fuzs.immersivedamageindicators.client.helper.HealthBarHelper;
import fuzs.immersivedamageindicators.client.helper.HealthTracker;
import fuzs.immersivedamageindicators.client.helper.MutableGuiGraphics;
import fuzs.immersivedamageindicators.client.renderer.ModRenderType;
import fuzs.immersivedamageindicators.config.ClientConfig;
import fuzs.puzzleslib.api.client.gui.v2.components.GuiGraphicsHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.LivingEntity;

public class HealthBarRenderer {

    public static void renderHealthBar(HealthTracker healthTracker, PoseStack poseStack, LivingEntity entity, int posX, int posY, int width, boolean inWorld, float partialTick) {

        float barProgress = healthTracker.getBarProgress(partialTick);
        float backgroundBarProgress = healthTracker.getBackgroundBarProgress(partialTick);
        BossEvent.BossBarColor barColor = HealthBarHelper.getBarColor(entity);
        ClientConfig.NotchedStyle notchedStyle = ImmersiveDamageIndicators.CONFIG.get(
                ClientConfig.class).barColors.notchedStyle;

        renderHealthBar(HealthBarHelper.getBarSprite(BossEvent.BossBarColor.WHITE, true), poseStack, posX, posY, width, 1.0F);
        renderHealthBar(HealthBarHelper.getBarSprite(barColor, true), poseStack, posX, posY, width, backgroundBarProgress);
        if (notchedStyle != ClientConfig.NotchedStyle.NONE) {
            renderHealthBar(HealthBarHelper.getOverlaySprite(BossEvent.BossBarOverlay.NOTCHED_12, true), poseStack, posX,
                    posY, width, notchedStyle == ClientConfig.NotchedStyle.COLORED ? backgroundBarProgress : 1.0F
            );
        }
        renderHealthBar(HealthBarHelper.getBarSprite(barColor, false), poseStack, posX, posY, width, barProgress);
        if (notchedStyle != ClientConfig.NotchedStyle.NONE) {
            renderHealthBar(HealthBarHelper.getOverlaySprite(BossEvent.BossBarOverlay.NOTCHED_12, false), poseStack, posX,
                    posY, width, barProgress
            );
        }

        if (inWorld) {
            Font font = Minecraft.getInstance().font;

            float f = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
            int alpha = (int)(f * 255.0F) << 24;
            if (font.width(healthTracker.getDisplayName()) < width / 2) {

                posX -= (width / 2);
                posY -= font.lineHeight + 1;
                font.drawInBatch(healthTracker.getDisplayName(), posX + 1, posY, -1, true, poseStack.last()
                        .pose(), Minecraft.getInstance().renderBuffers().bufferSource(), Font.DisplayMode.POLYGON_OFFSET, 0, 15728880);


                Component component = Component.literal(healthTracker.getHealth() + "/" + healthTracker.getMaxHealth());
                posX += width - 1 - font.width(component) - 2 - 9;
                GuiGraphics guiGraphics = GuiGraphicsHelper.create(poseStack);
                guiGraphics.drawString(font, component, posX, posY, -1, true);
                posX += font.width(component) + 2;
                guiGraphics.blitSprite(GuiRenderingHandler.HEART_CONTAINER_SPRITE, posX, posY, 9, 9);
                guiGraphics.blitSprite(GuiRenderingHandler.HEART_FULL_SPRITE, posX, posY, 9, 9);
            } else {
                posY -= (font.lineHeight + 1) * 2;
                font.drawInBatch(healthTracker.getDisplayName(), posX - font.width(healthTracker.getDisplayName()) / 2, posY, -1, true, poseStack.last()
                        .pose(), Minecraft.getInstance().renderBuffers().bufferSource(), Font.DisplayMode.POLYGON_OFFSET, 0, 15728880);

                posY += (font.lineHeight + 1) * 1;
                Component component = Component.literal(healthTracker.getHealth() + "/" + healthTracker.getMaxHealth());
                posX -= (font.width(component) + 2 + 9) / 2;
                GuiGraphics guiGraphics = GuiGraphicsHelper.create(poseStack);
                guiGraphics.drawString(font, component, posX, posY, -1, true);
                posX += font.width(component) + 2;
                guiGraphics.blitSprite(GuiRenderingHandler.HEART_CONTAINER_SPRITE, posX, posY, 9, 9);
                guiGraphics.blitSprite(GuiRenderingHandler.HEART_FULL_SPRITE, posX, posY, 9, 9);

            }

        }
    }

    private static void renderHealthBar(ResourceLocation resourceLocation, PoseStack poseStack, int posX, int posY, int width, float percentage) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        posX -= (width / 2);
        MutableGuiGraphics guiGraphics = new MutableGuiGraphics(poseStack).setAlpha(0.125F)
                .setBlitOffset(0.01F)
                .setRenderType(ModRenderType.ICON_SEE_THROUGH);
        guiGraphics.blitSprite(resourceLocation, posX, posY, (int) (width * percentage), 5);
        guiGraphics.setAlpha(1.0F).setBlitOffset(0.0F).setRenderType(ModRenderType.ICON);
        guiGraphics.blitSprite(resourceLocation, posX, posY, (int) (width * percentage), 5);
        poseStack.translate(0.0F, 0.0F, 0.03F);
    }
}
