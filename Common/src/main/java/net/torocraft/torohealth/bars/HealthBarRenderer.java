package net.torocraft.torohealth.bars;

import com.mojang.blaze3d.systems.RenderSystem;
import fuzs.immersivedamageindicators.client.handler.GuiRenderingHandler;
import fuzs.immersivedamageindicators.client.helper.HealthBarHelper;
import fuzs.immersivedamageindicators.client.helper.HealthTracker;
import fuzs.immersivedamageindicators.config.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.LivingEntity;

public class HealthBarRenderer {

    public static void renderHealthBar(HealthTracker healthTracker, GuiGraphics guiGraphics, LivingEntity entity, int posX, int posY, int healthBarWidth, float partialTick, ClientConfig.BarColors barColors) {

        float barProgress = healthTracker.getBarProgress(partialTick);
        float backgroundBarProgress = healthTracker.getBackgroundBarProgress(partialTick);
        BossEvent.BossBarColor barColor = HealthBarHelper.getBarColor(entity, barColors);
        ClientConfig.NotchedStyle notchedStyle = barColors.notchedStyle;

        renderHealthBar(HealthBarHelper.getBarSprite(BossEvent.BossBarColor.WHITE, true), guiGraphics, posX, posY,
                healthBarWidth, 1.0F
        );
        renderHealthBar(HealthBarHelper.getBarSprite(barColor, true), guiGraphics, posX, posY, healthBarWidth,
                backgroundBarProgress
        );
        if (notchedStyle != ClientConfig.NotchedStyle.NONE) {
            renderHealthBar(HealthBarHelper.getOverlaySprite(BossEvent.BossBarOverlay.NOTCHED_12, true), guiGraphics,
                    posX, posY, healthBarWidth,
                    notchedStyle == ClientConfig.NotchedStyle.COLORED ? backgroundBarProgress : 1.0F
            );
        }
        renderHealthBar(HealthBarHelper.getBarSprite(barColor, false), guiGraphics, posX, posY, healthBarWidth,
                barProgress
        );
        if (notchedStyle != ClientConfig.NotchedStyle.NONE) {
            renderHealthBar(HealthBarHelper.getOverlaySprite(BossEvent.BossBarOverlay.NOTCHED_12, false), guiGraphics,
                    posX, posY, healthBarWidth, barProgress
            );
        }
    }

    public static void renderHealthBarDecorations(HealthTracker healthTracker, GuiGraphics guiGraphics, int posX, int posY, int healthBarWidth) {
        Font font = Minecraft.getInstance().font;
        int offsetY = font.lineHeight + 2;

        float f = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
        int alpha = (int) (f * 255.0F) << 24;
        if (font.width(healthTracker.getDisplayName()) < healthBarWidth / 2) {

            posX -= (healthBarWidth / 2);
            posY -= offsetY;

            guiGraphics.drawString(font, healthTracker.getDisplayName(), posX + 1, posY, -1, true);

            Component component = Component.literal(healthTracker.getHealth() + "/" + healthTracker.getMaxHealth());
            posX += healthBarWidth - 1 - font.width(component) - 2 - 9;
            guiGraphics.drawString(font, component, posX, posY, -1, true);
            posX += font.width(component) + 2;
            guiGraphics.blitSprite(GuiRenderingHandler.HEART_CONTAINER_SPRITE, posX, posY, 9, 9);
            guiGraphics.blitSprite(GuiRenderingHandler.HEART_FULL_SPRITE, posX, posY, 9, 9);
        } else {
            posY -= offsetY * 2;

            guiGraphics.drawString(font, healthTracker.getDisplayName(), posX - font.width(healthTracker.getDisplayName()) / 2,
                    posY, -1, true);

            posY += offsetY;
            Component component = Component.literal(healthTracker.getHealth() + "/" + healthTracker.getMaxHealth());
            posX -= (font.width(component) + 2 + 9) / 2;
            guiGraphics.drawString(font, component, posX, posY, -1, true);
            posX += font.width(component) + 2;
            guiGraphics.blitSprite(GuiRenderingHandler.HEART_CONTAINER_SPRITE, posX, posY, 9, 9);
            guiGraphics.blitSprite(GuiRenderingHandler.HEART_FULL_SPRITE, posX, posY, 9, 9);

        }
    }

    private static void renderHealthBar(ResourceLocation resourceLocation, GuiGraphics guiGraphics, int posX, int posY, int width, float percentage) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        posX -= (width / 2);
        guiGraphics.blitSprite(resourceLocation, posX, posY, (int) (width * percentage), 5);
        guiGraphics.pose().translate(0.0F, 0.0F, 0.03F);
    }
}
