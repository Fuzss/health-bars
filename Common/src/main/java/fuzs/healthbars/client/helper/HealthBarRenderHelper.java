package fuzs.healthbars.client.helper;

import fuzs.healthbars.HealthBars;
import fuzs.healthbars.client.gui.GraphicsLayer;
import fuzs.healthbars.client.handler.GuiRenderingHandler;
import fuzs.healthbars.client.renderer.entity.state.HealthTrackerRenderState;
import fuzs.healthbars.config.ClientConfig;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.BossEvent;

import java.util.function.Function;

public class HealthBarRenderHelper {

    public static void submitHealthBar(GraphicsLayer graphicsLayer, Function<ResourceLocation, RenderType> renderTypeGetter, int posX, int posY, HealthTrackerRenderState renderState, int color, int lightCoords) {
        submitHealthBar(graphicsLayer,
                renderTypeGetter,
                HealthBarHelper.getBarSprite(BossEvent.BossBarColor.WHITE, true),
                posX,
                posY,
                renderState.barWidth,
                1.0F,
                color,
                lightCoords);
        submitHealthBar(graphicsLayer,
                renderTypeGetter,
                HealthBarHelper.getBarSprite(renderState.barColor, true),
                posX,
                posY,
                renderState.barWidth,
                renderState.backgroundBarProgress,
                color,
                lightCoords);
        if (renderState.notchedStyle != ClientConfig.NotchedStyle.NONE) {
            submitHealthBar(graphicsLayer,
                    renderTypeGetter,
                    HealthBarHelper.getOverlaySprite(BossEvent.BossBarOverlay.NOTCHED_12, true),
                    posX,
                    posY,
                    renderState.barWidth,
                    renderState.notchedStyle == ClientConfig.NotchedStyle.COLORED ? renderState.backgroundBarProgress :
                            1.0F,
                    color,
                    lightCoords);
        }

        submitHealthBar(graphicsLayer,
                renderTypeGetter,
                HealthBarHelper.getBarSprite(renderState.barColor, false),
                posX,
                posY,
                renderState.barWidth,
                renderState.barProgress,
                color,
                lightCoords);
        if (renderState.notchedStyle != ClientConfig.NotchedStyle.NONE) {
            submitHealthBar(graphicsLayer,
                    renderTypeGetter,
                    HealthBarHelper.getOverlaySprite(BossEvent.BossBarOverlay.NOTCHED_12, false),
                    posX,
                    posY,
                    renderState.barWidth,
                    renderState.barProgress,
                    color,
                    lightCoords);
        }
    }

    private static void submitHealthBar(GraphicsLayer graphicsLayer, Function<ResourceLocation, RenderType> renderTypeGetter, ResourceLocation resourceLocation, int posX, int posY, int width, float progress, int color, int lightCoords) {
        graphicsLayer.blitSprite(renderTypeGetter,
                resourceLocation,
                posX - (width / 2),
                posY,
                (int) (width * progress),
                5,
                color,
                lightCoords);
        graphicsLayer.translate(0.03F);
    }

    public static void submitHealthBarDecorations(GraphicsLayer graphicsLayer, int posX, int posY, Font font, HealthTrackerRenderState renderState, int color, Font.DisplayMode displayMode, int backgroundColor, int lightCoords, int outlineColor) {
        int offsetY = font.lineHeight + 2;
        Component component = GuiRenderingHandler.getHealthComponent(renderState);
        if (singleLineOnly(font, component, renderState)) {
            posY -= offsetY;
            posX -= (renderState.barWidth / 2);
            graphicsLayer.drawString(font,
                    renderState.displayName,
                    posX + 1,
                    posY,
                    color,
                    renderState.drawShadow(),
                    displayMode,
                    backgroundColor,
                    lightCoords,
                    outlineColor);
            posX += renderState.barWidth - 1 - font.width(component);
            graphicsLayer.drawString(font,
                    component,
                    posX,
                    posY,
                    color,
                    renderState.drawShadow(),
                    displayMode,
                    backgroundColor,
                    lightCoords,
                    outlineColor);
        } else {
            if (HealthBars.CONFIG.get(ClientConfig.class).level.renderAttributeComponents) {
                posY -= offsetY;
                graphicsLayer.drawCenteredString(font,
                        component,
                        posX,
                        posY,
                        color,
                        renderState.drawShadow(),
                        displayMode,
                        backgroundColor,
                        lightCoords,
                        outlineColor);
            }

            if (HealthBars.CONFIG.get(ClientConfig.class).level.renderTitleComponent) {
                posY -= offsetY;
                graphicsLayer.drawCenteredString(font,
                        renderState.displayName,
                        posX,
                        posY,
                        color,
                        renderState.drawShadow(),
                        displayMode,
                        backgroundColor,
                        lightCoords,
                        outlineColor);
            }
        }
    }

    private static boolean singleLineOnly(Font font, Component component, HealthTrackerRenderState renderState) {
        if (!HealthBars.CONFIG.get(ClientConfig.class).level.renderTitleComponent) {
            return false;
        } else if (!HealthBars.CONFIG.get(ClientConfig.class).level.renderAttributeComponents) {
            return false;
        } else if (font.width(renderState.displayName) < renderState.barWidth - 2 - font.width(component) - 4) {
            return true;
        } else {
            return false;
        }
    }
}
