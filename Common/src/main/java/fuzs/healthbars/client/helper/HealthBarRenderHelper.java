package fuzs.healthbars.client.helper;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import fuzs.healthbars.HealthBars;
import fuzs.healthbars.client.handler.GuiRenderingHandler;
import fuzs.healthbars.config.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.LivingEntity;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class HealthBarRenderHelper {

    public static void renderHealthBar(GuiGraphics guiGraphics, int posX, int posY, float partialTick, HealthTracker healthTracker, LivingEntity entity, int barWidth, ClientConfig.BarColors barColors) {

        float barProgress = healthTracker.getBarProgress(partialTick);
        float backgroundBarProgress = healthTracker.getBackgroundBarProgress(partialTick);
        BossEvent.BossBarColor barColor = HealthBarHelper.getBarColor(entity, barColors);
        ClientConfig.NotchedStyle notchedStyle = barColors.notchedStyle;

        renderHealthBar(HealthBarHelper.getBarSprite(BossEvent.BossBarColor.WHITE, true), guiGraphics, posX, posY,
                barWidth, 1.0F
        );
        renderHealthBar(HealthBarHelper.getBarSprite(barColor, true), guiGraphics, posX, posY, barWidth,
                backgroundBarProgress
        );
        if (notchedStyle != ClientConfig.NotchedStyle.NONE) {
            renderHealthBar(HealthBarHelper.getOverlaySprite(BossEvent.BossBarOverlay.NOTCHED_12, true), guiGraphics,
                    posX, posY, barWidth,
                    notchedStyle == ClientConfig.NotchedStyle.COLORED ? backgroundBarProgress : 1.0F
            );
        }
        renderHealthBar(HealthBarHelper.getBarSprite(barColor, false), guiGraphics, posX, posY, barWidth, barProgress);
        if (notchedStyle != ClientConfig.NotchedStyle.NONE) {
            renderHealthBar(HealthBarHelper.getOverlaySprite(BossEvent.BossBarOverlay.NOTCHED_12, false), guiGraphics,
                    posX, posY, barWidth, barProgress
            );
        }
    }

    private static void renderHealthBar(ResourceLocation resourceLocation, GuiGraphics guiGraphics, int posX, int posY, int width, float percentage) {

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        posX -= (width / 2);
        guiGraphics.blitSprite(resourceLocation, posX, posY, (int) (width * percentage), 5);
        guiGraphics.pose().translate(0.0F, 0.0F, 0.03F);
    }

    public static void renderHealthBarDecorations(GuiGraphics guiGraphics, int posX, int posY, Font font, @Nullable RenderType backgroundRenderType, HealthTracker healthTracker, int barWidth, boolean dropShadow, int packedLight) {

        if (backgroundRenderType != null) {
            int backgroundColor = Minecraft.getInstance().options.getBackgroundColor(0.25F);
            renderHealthBarDecorations(posX, posY, font, healthTracker, barWidth,
                    (int x, int y, int width, int height) -> {
                        fill(backgroundRenderType, guiGraphics.pose(), guiGraphics.bufferSource(), packedLight, x - 1,
                                y - 1, x + width + 1, y + height + 1, -0.03F, backgroundColor
                        );
                    }, (int x, int y, int width, int height) -> {
                        fill(backgroundRenderType, guiGraphics.pose(), guiGraphics.bufferSource(), packedLight, x - 1,
                                y - 1, x + width + 1, y + height + 1, -0.03F, backgroundColor
                        );
                    }
            );
        }

        renderHealthBarDecorations(posX, posY, font, healthTracker, barWidth, (int x, int y, int width, int height) -> {
            guiGraphics.drawString(font, healthTracker.getData().displayName(), x, y, -1, dropShadow);
        }, (int x, int y, int width, int height) -> {
            GuiRenderingHandler.renderHealthComponent(guiGraphics, new MutableInt(x), new MutableInt(y), font,
                    healthTracker, dropShadow,
                    HealthBars.CONFIG.get(ClientConfig.class).level.renderSpriteComponent
            );
            guiGraphics.flush();
        });
    }

    static void renderHealthBarDecorations(int posX, int posY, Font font, HealthTracker healthTracker, int barWidth, TextElementRenderer titleRenderer, TextElementRenderer healthRenderer) {

        boolean renderTitleComponent = HealthBars.CONFIG.get(
                ClientConfig.class).level.renderTitleComponent;
        boolean renderHealthComponent = HealthBars.CONFIG.get(
                ClientConfig.class).level.renderHealthComponent;

        int offsetY = font.lineHeight + 2;
        int titleComponentWidth = font.width(healthTracker.getData().displayName());
        int healthComponentWidth = GuiRenderingHandler.getHealthComponentWidth(healthTracker, font,
                HealthBars.CONFIG.get(ClientConfig.class).level.renderSpriteComponent
        );

        if (renderTitleComponent && renderHealthComponent && titleComponentWidth <
                barWidth - 2 - healthComponentWidth - GuiRenderingHandler.TEXT_TO_SPRITE_GAP * 2) {

            posY -= offsetY;
            posX -= (barWidth / 2);
            titleRenderer.renderAtPosition(posX + 1, posY, titleComponentWidth, font.lineHeight - 1);
            posX += barWidth - 1 - healthComponentWidth;
            healthRenderer.renderAtPosition(posX, posY, healthComponentWidth, font.lineHeight - 1);
        } else {

            if (renderHealthComponent) {
                posY -= offsetY;
                healthRenderer.renderAtPosition(posX - healthComponentWidth / 2, posY, healthComponentWidth,
                        font.lineHeight - 1
                );
            }

            if (renderTitleComponent) {
                posY -= offsetY;
                titleRenderer.renderAtPosition(posX - titleComponentWidth / 2, posY, titleComponentWidth,
                        font.lineHeight - 1
                );
            }
        }
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

    @FunctionalInterface
    interface TextElementRenderer {

        void renderAtPosition(int x, int y, int width, int height);
    }
}
