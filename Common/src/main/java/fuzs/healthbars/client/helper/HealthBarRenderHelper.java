package fuzs.healthbars.client.helper;

import fuzs.healthbars.HealthBars;
import fuzs.healthbars.client.gui.GraphicsComponent;
import fuzs.healthbars.client.handler.GuiRenderingHandler;
import fuzs.healthbars.config.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.BossEvent;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class HealthBarRenderHelper {

    public static void renderHealthBar(GraphicsComponent graphicsComponent, Function<ResourceLocation, RenderType> renderTypeGetter, int posX, int posY, HealthTrackerRenderState renderState, int barWidth, int color, int packedLight) {

        renderHealthBar(graphicsComponent,
                renderTypeGetter,
                HealthBarHelper.getBarSprite(BossEvent.BossBarColor.WHITE, true),
                posX,
                posY,
                barWidth,
                1.0F,
                color,
                packedLight);
        renderHealthBar(graphicsComponent,
                renderTypeGetter,
                HealthBarHelper.getBarSprite(renderState.barColor, true),
                posX,
                posY,
                barWidth,
                renderState.backgroundBarProgress,
                color,
                packedLight);
        if (renderState.notchedStyle != ClientConfig.NotchedStyle.NONE) {
            renderHealthBar(graphicsComponent,
                    renderTypeGetter,
                    HealthBarHelper.getOverlaySprite(BossEvent.BossBarOverlay.NOTCHED_12, true),
                    posX,
                    posY,
                    barWidth,
                    renderState.notchedStyle == ClientConfig.NotchedStyle.COLORED ? renderState.backgroundBarProgress :
                            1.0F,
                    color,
                    packedLight);
        }
        renderHealthBar(graphicsComponent,
                renderTypeGetter,
                HealthBarHelper.getBarSprite(renderState.barColor, false),
                posX,
                posY,
                barWidth,
                renderState.barProgress,
                color,
                packedLight);
        if (renderState.notchedStyle != ClientConfig.NotchedStyle.NONE) {
            renderHealthBar(graphicsComponent,
                    renderTypeGetter,
                    HealthBarHelper.getOverlaySprite(BossEvent.BossBarOverlay.NOTCHED_12, false),
                    posX,
                    posY,
                    barWidth,
                    renderState.barProgress,
                    color,
                    packedLight);
        }
    }

    private static void renderHealthBar(GraphicsComponent graphicsComponent, Function<ResourceLocation, RenderType> renderTypeGetter, ResourceLocation resourceLocation, int posX, int posY, int width, float percentage, int color, int packedLight) {
        posX -= (width / 2);
        graphicsComponent.blitSprite(renderTypeGetter,
                resourceLocation,
                posX,
                posY,
                (int) (width * percentage),
                5,
                color,
                packedLight);
        graphicsComponent.translate(0.03F);
    }

    public static void renderHealthBarDecorations(GraphicsComponent graphicsComponent, Function<ResourceLocation, RenderType> renderTypeGetter, int posX, int posY, Font font, @Nullable RenderType textBackground, HealthTrackerRenderState renderState, int barWidth, int color, boolean dropShadow, Font.DisplayMode fontDisplayMode, int packedLight) {

        if (textBackground != null) {
            int backgroundColor = Minecraft.getInstance().options.getBackgroundColor(0.25F);
            renderHealthBarDecorations(posX,
                    posY,
                    font,
                    renderState,
                    barWidth,
                    (int x, int y, int width, int height) -> {
                        graphicsComponent.fill(textBackground,
                                x - 1,
                                y - 1,
                                x + width + 1,
                                y + height + 1,
                                -0.03F,
                                backgroundColor,
                                packedLight);
                    },
                    (int x, int y, int width, int height) -> {
                        graphicsComponent.fill(textBackground,
                                x - 1,
                                y - 1,
                                x + width + 1,
                                y + height + 1,
                                -0.03F,
                                backgroundColor,
                                packedLight);
                    });
        }

        renderHealthBarDecorations(posX, posY, font, renderState, barWidth, (int x, int y, int width, int height) -> {
            graphicsComponent.drawString(font,
                    renderState.displayName,
                    x,
                    y,
                    color,
                    dropShadow,
                    fontDisplayMode,
                    packedLight);
        }, (int x, int y, int width, int height) -> {
            GuiRenderingHandler.renderHealthComponent(graphicsComponent,
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

        if (renderTitleComponent && renderHealthComponent && titleComponentWidth
                < barWidth - 2 - healthComponentWidth - GuiRenderingHandler.TEXT_TO_SPRITE_GAP * 2) {

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

    @FunctionalInterface
    interface TextElementRenderer {

        void renderAtPosition(int x, int y, int width, int height);
    }
}
