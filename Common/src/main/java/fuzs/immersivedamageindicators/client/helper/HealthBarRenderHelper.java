package fuzs.immersivedamageindicators.client.helper;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import fuzs.immersivedamageindicators.ImmersiveDamageIndicators;
import fuzs.immersivedamageindicators.client.handler.GuiRenderingHandler;
import fuzs.immersivedamageindicators.config.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.LivingEntity;
import org.apache.commons.lang3.mutable.MutableInt;
import org.joml.Matrix4f;

public class HealthBarRenderHelper {

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

    public static void renderHealthBarDecorations(RenderType renderType, CustomGuiGraphics guiGraphics, int posX, int posY, Font font, HealthTracker healthTracker, int healthBarWidth, boolean dropShadow, boolean renderBackground) {

        int backgroundColor = Minecraft.getInstance().options.getBackgroundColor(0.25F);

        boolean renderMobTitleComponent = ImmersiveDamageIndicators.CONFIG.get(
                ClientConfig.class).level.renderNameComponent;
        boolean renderHealthComponent = ImmersiveDamageIndicators.CONFIG.get(
                ClientConfig.class).level.renderHealthComponent;
        boolean renderSpriteComponent = ImmersiveDamageIndicators.CONFIG.get(
                ClientConfig.class).level.renderSpriteComponent;

        Component displayName = healthTracker.getData().displayName();
        int healthComponentWidth = GuiRenderingHandler.getHealthComponentWidth(healthTracker, font,
                renderSpriteComponent
        );

        int offsetY = font.lineHeight + 2;
        int minMobTitleWidth = healthBarWidth;
        // one pixel gap on both ends
        minMobTitleWidth -= 2;
        // the heart text & sprite component
        minMobTitleWidth -= healthComponentWidth;
        // the minimum gap between title text and heart text
        minMobTitleWidth -= GuiRenderingHandler.TEXT_TO_SPRITE_GAP * 2;

        if (renderMobTitleComponent && renderHealthComponent && font.width(displayName) <
                minMobTitleWidth) {

            posY -= offsetY;
            posX -= (healthBarWidth / 2);

            int healthComponentStart = healthBarWidth - 1 - healthComponentWidth;
            if (renderBackground) {
                fill(renderType, guiGraphics.pose(), guiGraphics.bufferSource(), guiGraphics.getPackedLight(), posX, posY - 1, posX + 2 + font.width(displayName), posY + font.lineHeight, -0.03F, backgroundColor);
                fill(renderType, guiGraphics.pose(), guiGraphics.bufferSource(), guiGraphics.getPackedLight(), posX + healthComponentStart - 1, posY - 1, posX + healthComponentStart + 1 + healthComponentWidth, posY + font.lineHeight, -0.03F, backgroundColor);
            }

            guiGraphics.drawString(font, displayName, posX + 1, posY, -1, dropShadow);
            posX += healthComponentStart;


            GuiRenderingHandler.renderHealthComponent(guiGraphics, new MutableInt(posX), new MutableInt(posY), font,
                    healthTracker, dropShadow, renderSpriteComponent
            );
        } else {

            if (renderBackground) {

                int posYBackground = posY;

                if (renderHealthComponent) {
                    posYBackground -= offsetY;
                    fill(renderType, guiGraphics.pose(), guiGraphics.bufferSource(), guiGraphics.getPackedLight(), posX  - healthComponentWidth / 2 - 1, posYBackground - 1, posX + healthComponentWidth / 2 + 1, posYBackground + font.lineHeight, -0.03F, backgroundColor);
                }
                if (renderMobTitleComponent) {
                    posYBackground -= offsetY;
                    fill(renderType, guiGraphics.pose(), guiGraphics.bufferSource(), guiGraphics.getPackedLight(), posX - font.width(displayName) / 2 - 1, posYBackground - 1, posX + font.width(displayName) / 2 + 1, posYBackground + font.lineHeight, -0.03F, backgroundColor);
                }
            }

            if (renderHealthComponent) {
                posY -= offsetY;
                GuiRenderingHandler.renderHealthComponent(guiGraphics,
                        new MutableInt(posX - healthComponentWidth / 2),
                        new MutableInt(posY), font, healthTracker, dropShadow, renderSpriteComponent
                );
            }

            if (renderMobTitleComponent) {
                posY -= offsetY;
                guiGraphics.drawString(font, displayName,
                        posX - font.width(displayName) / 2, posY, -1, dropShadow
                );
            }
        }
    }

    private static void renderHealthBar(ResourceLocation resourceLocation, GuiGraphics guiGraphics, int posX, int posY, int width, float percentage) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        posX -= (width / 2);
        guiGraphics.blitSprite(resourceLocation, posX, posY, (int) (width * percentage), 5);
        guiGraphics.pose().translate(0.0F, 0.0F, 0.03F);
    }

    public static void fill(RenderType renderType, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int minX, int minY, int maxX, int maxY, float zOffset, int color) {
        Matrix4f matrix4f = poseStack.last().pose();
        if (minX < maxX) {
            int tmp = minX;
            minX = maxX;
            maxX = tmp;
        }
        if (minY < maxY) {
            int tmp = minY;
            minY = maxY;
            maxY = tmp;
        }
        VertexConsumer bufferBuilder = bufferSource.getBuffer(renderType);
        bufferBuilder.addVertex(matrix4f, minX, minY, zOffset).setColor(color).setLight(packedLight);
        bufferBuilder.addVertex(matrix4f, minX, maxY, zOffset).setColor(color).setLight(packedLight);
        bufferBuilder.addVertex(matrix4f, maxX, maxY, zOffset).setColor(color).setLight(packedLight);
        bufferBuilder.addVertex(matrix4f, maxX, minY, zOffset).setColor(color).setLight(packedLight);
    }
}
