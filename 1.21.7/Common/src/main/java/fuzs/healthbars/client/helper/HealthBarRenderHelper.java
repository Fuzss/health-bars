package fuzs.healthbars.client.helper;

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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.BossEvent;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.function.Function;

public class HealthBarRenderHelper {

    public static void renderHealthBar(GuiGraphics guiGraphics, Function<ResourceLocation, RenderType> renderTypeGetter, int posX, int posY, HealthTrackerRenderState renderState, int barWidth, int color) {

        renderHealthBar(guiGraphics,
                renderTypeGetter,
                HealthBarHelper.getBarSprite(BossEvent.BossBarColor.WHITE, true),
                posX,
                posY,
                barWidth,
                1.0F,
                color);
        renderHealthBar(guiGraphics,
                renderTypeGetter,
                HealthBarHelper.getBarSprite(renderState.barColor, true),
                posX,
                posY,
                barWidth,
                renderState.backgroundBarProgress,
                color);
        if (renderState.notchedStyle != ClientConfig.NotchedStyle.NONE) {
            renderHealthBar(guiGraphics,
                    renderTypeGetter,
                    HealthBarHelper.getOverlaySprite(BossEvent.BossBarOverlay.NOTCHED_12, true),
                    posX,
                    posY,
                    barWidth,
                    renderState.notchedStyle == ClientConfig.NotchedStyle.COLORED ? renderState.backgroundBarProgress :
                            1.0F,
                    color);
        }
        renderHealthBar(guiGraphics,
                renderTypeGetter,
                HealthBarHelper.getBarSprite(renderState.barColor, false),
                posX,
                posY,
                barWidth,
                renderState.barProgress,
                color);
        if (renderState.notchedStyle != ClientConfig.NotchedStyle.NONE) {
            renderHealthBar(guiGraphics,
                    renderTypeGetter,
                    HealthBarHelper.getOverlaySprite(BossEvent.BossBarOverlay.NOTCHED_12, false),
                    posX,
                    posY,
                    barWidth,
                    renderState.barProgress,
                    color);
        }
    }

    private static void renderHealthBar(GuiGraphics guiGraphics, Function<ResourceLocation, RenderType> renderTypeGetter, ResourceLocation resourceLocation, int posX, int posY, int width, float percentage, int color) {
        posX -= (width / 2);
        guiGraphics.blitSprite(renderTypeGetter, resourceLocation, posX, posY, (int) (width * percentage), 5, color);
        guiGraphics.pose().translate(0.0F, 0.0F, 0.03F);
    }

    public static void renderHealthBarDecorations(GuiGraphics guiGraphics, Function<ResourceLocation, RenderType> renderTypeGetter, int posX, int posY, Font font, @Nullable RenderType textBackground, HealthTrackerRenderState renderState, int barWidth, int color, boolean dropShadow, Font.DisplayMode fontDisplayMode, int packedLight) {

        if (textBackground != null) {
            int backgroundColor = Minecraft.getInstance().options.getBackgroundColor(0.25F);
            renderHealthBarDecorations(posX,
                    posY,
                    font,
                    renderState,
                    barWidth,
                    (int x, int y, int width, int height) -> {
                        fill(textBackground,
                                guiGraphics.pose(),
                                guiGraphics.bufferSource,
                                packedLight,
                                x - 1,
                                y - 1,
                                x + width + 1,
                                y + height + 1,
                                -0.03F,
                                backgroundColor);
                    },
                    (int x, int y, int width, int height) -> {
                        fill(textBackground,
                                guiGraphics.pose(),
                                guiGraphics.bufferSource,
                                packedLight,
                                x - 1,
                                y - 1,
                                x + width + 1,
                                y + height + 1,
                                -0.03F,
                                backgroundColor);
                    });
        }

        renderHealthBarDecorations(posX, posY, font, renderState, barWidth, (int x, int y, int width, int height) -> {
            drawString(guiGraphics,
                    font,
                    renderState.displayName,
                    x,
                    y,
                    color,
                    dropShadow,
                    fontDisplayMode,
                    packedLight);
        }, (int x, int y, int width, int height) -> {
            GuiRenderingHandler.renderHealthComponent(guiGraphics,
                    renderTypeGetter,
                    new MutableInt(x),
                    new MutableInt(y),
                    font,
                    renderState,
                    dropShadow,
                    HealthBars.CONFIG.get(ClientConfig.class).level.renderSpriteComponent,
                    color,
                    fontDisplayMode,
                    packedLight);
        });
    }

    private static void renderHealthBarDecorations(int posX, int posY, Font font, HealthTrackerRenderState renderState, int barWidth, TextElementRenderer titleRenderer, TextElementRenderer healthRenderer) {

        boolean renderTitleComponent = HealthBars.CONFIG.get(ClientConfig.class).level.renderTitleComponent;
        boolean renderHealthComponent = HealthBars.CONFIG.get(ClientConfig.class).level.renderHealthComponent;

        int offsetY = font.lineHeight + 2;
        int titleComponentWidth = font.width(renderState.displayName);
        int healthComponentWidth = GuiRenderingHandler.getHealthComponentWidth(renderState,
                font,
                HealthBars.CONFIG.get(ClientConfig.class).level.renderSpriteComponent);

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
                healthRenderer.renderAtPosition(posX - healthComponentWidth / 2,
                        posY,
                        healthComponentWidth,
                        font.lineHeight - 1);
            }

            if (renderTitleComponent) {
                posY -= offsetY;
                titleRenderer.renderAtPosition(posX - titleComponentWidth / 2,
                        posY,
                        titleComponentWidth,
                        font.lineHeight - 1);
            }
        }
    }

    public static void drawString(GuiGraphics guiGraphics, Font font, Component text, int x, int y, int color, boolean dropShadow, Font.DisplayMode fontDisplayMode, int packedLight) {
        drawString(guiGraphics, font, text.getVisualOrderText(), x, y, color, dropShadow, fontDisplayMode, packedLight);
    }

    public static void drawString(GuiGraphics guiGraphics, Font font, FormattedCharSequence text, int x, int y, int color, boolean dropShadow, Font.DisplayMode fontDisplayMode, int packedLight) {
        font.drawInBatch(text,
                x,
                y,
                color,
                dropShadow,
                guiGraphics.pose().last().pose(),
                guiGraphics.bufferSource,
                fontDisplayMode,
                0,
                packedLight);
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
