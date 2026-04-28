package fuzs.healthbars.client.handler;

import fuzs.healthbars.HealthBars;
import fuzs.healthbars.client.gui.GraphicsLayer;
import fuzs.healthbars.client.helper.EntityVisibilityHelper;
import fuzs.healthbars.client.helper.HealthBarRenderHelper;
import fuzs.healthbars.client.renderer.entity.state.HealthTrackerRenderState;
import fuzs.healthbars.config.ClientConfig;
import fuzs.healthbars.world.entity.HealthTracker;
import fuzs.puzzleslib.common.api.client.gui.v2.AnchorPoint;
import fuzs.puzzleslib.common.api.util.v1.ComponentHelper;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.data.AtlasIds;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.objects.AtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class GuiRenderingHandler {
    private static final int MOB_SELECTION_SIZE = 42;
    private static final int MOB_SELECTION_BORDER_SIZE = 4;
    private static final float MOB_TITLE_SCALE = 1.5F;
    private static final Component SEPARATOR_COMPONENT = Component.literal(" \u25C7 ");
    private static final Identifier MOB_SELECTION_SPRITE = HealthBars.id("mob_selection");

    public static void submitHealthBar(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker) {
        if (!HealthBars.CONFIG.get(ClientConfig.class).anyRendering.get()
                || !HealthBars.CONFIG.get(ClientConfig.class).guiRendering) {
            return;
        }

        if (PickEntityHandler.getCrosshairPickEntity() instanceof LivingEntity livingEntity && HealthBars.CONFIG.get(
                ClientConfig.class).isEntityAllowed(livingEntity)) {

            float partialTick = deltaTracker.getGameTimeDeltaPartialTick(false);
            HealthTracker healthTracker = HealthTracker.getHealthTracker(livingEntity, false);
            if (healthTracker != null && EntityVisibilityHelper.isEntityVisible(livingEntity, partialTick, true)) {
                ClientConfig.Gui config = HealthBars.CONFIG.get(ClientConfig.class).gui;
                HealthTrackerRenderState renderState = HealthTrackerRenderState.extractRenderState(healthTracker,
                        livingEntity,
                        partialTick,
                        config);

                AnchorPoint.Positioner positioner = config.anchorPoint.createPositioner(guiGraphics.guiWidth(),
                        guiGraphics.guiHeight(),
                        MOB_SELECTION_SIZE + 5 + renderState.barWidth,
                        MOB_SELECTION_SIZE);
                MutableInt posX = new MutableInt(positioner.getPosX(config.offsetWidth));
                MutableInt posY = new MutableInt(positioner.getPosY(config.offsetHeight));
                boolean isRight = config.anchorPoint.isRight();
                if (config.renderEntityDisplay) {
                    submitEntityDisplay(guiGraphics, posX, posY, renderState, livingEntity, isRight);
                }

                Font font = Minecraft.getInstance().font;
                GraphicsLayer.Gui graphicsLayer = new GraphicsLayer.Gui(guiGraphics);
                submitTitleComponent(graphicsLayer, posX, posY, font, renderState, isRight);
                submitHealthBar(graphicsLayer, posX, posY, font, renderState, config);
                if (config.renderAttributeComponents) {
                    submitAttributesComponent(graphicsLayer, posX, posY, font, renderState, isRight);
                }
            }
        }
    }

    private static void submitEntityDisplay(GuiGraphicsExtractor guiGraphics, MutableInt posX, MutableInt posY, HealthTrackerRenderState renderState, LivingEntity livingEntity, boolean isRight) {
        if (isRight) {
            posX.add(renderState.barWidth + 5);
        }

        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED,
                MOB_SELECTION_SPRITE,
                posX.intValue(),
                posY.intValue(),
                MOB_SELECTION_SIZE,
                MOB_SELECTION_SIZE);
        submitEntityDisplay(guiGraphics, posX, posY, renderState, livingEntity);
        if (isRight) {
            posX.subtract(renderState.barWidth + 5);
        } else {
            posX.add(MOB_SELECTION_SIZE + 5);
        }
    }

    private static void submitEntityDisplay(GuiGraphicsExtractor guiGraphics, MutableInt posX, MutableInt posY, HealthTrackerRenderState renderState, LivingEntity livingEntity) {
        // these are similar to the player size values
        float scaleWidth = 0.8F / livingEntity.getBbWidth();
        float scaleHeight = 1.8F / livingEntity.getBbHeight();
        int scale = (int) (Math.min(scaleWidth, scaleHeight) * 30.0F);
        float yOffset = 0.5F - (scaleHeight - 0.8F) * 0.15F + (float) renderState.renderOffset;

        int x1 = posX.intValue() + MOB_SELECTION_BORDER_SIZE;
        int y1 = posY.intValue() + MOB_SELECTION_BORDER_SIZE;
        int x2 = posX.intValue() + MOB_SELECTION_SIZE - MOB_SELECTION_BORDER_SIZE;
        int y2 = posY.intValue() + MOB_SELECTION_SIZE - MOB_SELECTION_BORDER_SIZE;
        int mouseX = posX.intValue() + MOB_SELECTION_SIZE / 2 + (70 - MOB_SELECTION_SIZE / 2) * (
                HealthBars.CONFIG.get(ClientConfig.class).gui.anchorPoint.isRight() ? -1 : 1);
        int mouseY = posY.intValue() + 10;

        InLevelRenderingHandler.setIsRenderingInGui(true);
        InventoryScreen.extractEntityInInventoryFollowsMouse(guiGraphics,
                x1,
                y1,
                x2,
                y2,
                scale,
                yOffset,
                mouseX,
                mouseY,
                livingEntity);
        InLevelRenderingHandler.setIsRenderingInGui(false);
    }

    private static void submitTitleComponent(GraphicsLayer.Gui graphicsLayer, MutableInt posX, MutableInt posY, Font font, HealthTrackerRenderState renderState, boolean isRight) {
        Component component = getTitleComponent(renderState, font);
        int offsetX;
        if (isRight) {
            offsetX = renderState.barWidth - 2 - (int) (font.width(component) * MOB_TITLE_SCALE) - Mth.floor(
                    MOB_TITLE_SCALE);
        } else {
            offsetX = Mth.floor(MOB_TITLE_SCALE);
        }

        posX.add(offsetX);
        graphicsLayer.guiGraphics().pose().pushMatrix();
        graphicsLayer.guiGraphics().pose().scale(MOB_TITLE_SCALE, MOB_TITLE_SCALE);
        int x = (int) (posX.intValue() / MOB_TITLE_SCALE);
        int y = (int) ((posY.intValue() + 5) / MOB_TITLE_SCALE);
        graphicsLayer.text(font,
                component,
                x,
                y,
                -1,
                renderState.drawShadow(),
                Font.DisplayMode.NORMAL,
                renderState.backgroundColor,
                GraphicsLayer.PACKED_LIGHT,
                0);
        graphicsLayer.guiGraphics().pose().popMatrix();
        posX.subtract(offsetX);
    }

    private static void submitHealthBar(GraphicsLayer graphicsLayer, MutableInt posX, MutableInt posY, Font font, HealthTrackerRenderState renderState, ClientConfig.Gui config) {
        posY.add(MOB_SELECTION_SIZE / 2);
        HealthBarRenderHelper.submitHealthBar(graphicsLayer,
                null,
                posX.intValue() - 1 + renderState.barWidth / 2,
                posY.intValue(),
                renderState,
                -1,
                GraphicsLayer.PACKED_LIGHT);
        if (config.damageValues.renderDamageValues) {
            submitDamageScore(graphicsLayer,
                    font,
                    renderState.healthData,
                    posX.intValue() - 1 + (int) (renderState.barWidth * renderState.healthProgress),
                    posY.intValue() - 1,
                    GraphicsLayer.PACKED_LIGHT,
                    config.damageValues);
        }
    }

    private static void submitAttributesComponent(GraphicsLayer.Gui graphicsLayer, MutableInt posX, MutableInt posY, Font font, HealthTrackerRenderState renderState, boolean isRight) {
        posY.add(10);
        Component component = getAttributesComponent(renderState, isRight);
        int offsetX;
        if (isRight) {
            offsetX = renderState.barWidth - 2 - font.width(component);
        } else {
            offsetX = 0;
        }

        posX.add(offsetX);
        graphicsLayer.text(font,
                component,
                posX.intValue(),
                posY.intValue(),
                -1,
                renderState.drawShadow(),
                Font.DisplayMode.NORMAL,
                renderState.backgroundColor,
                GraphicsLayer.PACKED_LIGHT,
                0);
        posX.subtract(offsetX);
    }

    private static Component getTitleComponent(HealthTrackerRenderState renderState, Font font) {
        int maxWidth = (int) ((renderState.barWidth - 2.0F) / MOB_TITLE_SCALE) - font.width(CommonComponents.ELLIPSIS);
        Component component = renderState.displayName;
        if (font.width(renderState.displayName) > maxWidth) {
            Style style = component.getStyle();
            component = ComponentHelper.getAsComponent(font.substrByWidth(component, maxWidth));
            return Component.empty().append(component).append(CommonComponents.ELLIPSIS).withStyle(style);
        } else {
            return renderState.displayName;
        }
    }

    private static Component getAttributesComponent(HealthTrackerRenderState renderState, boolean isRight) {
        List<Component> list = new ArrayList<>();
        list.add(getHealthComponent(renderState));
        if (renderState.armorValue > 0) {
            list.add(getArmorComponent(renderState));
        }

        if (renderState.toughnessValue > 0) {
            list.add(getArmorToughnessComponent(renderState));
        }

        return list.stream().reduce((Component o1, Component o2) -> {
            return Component.empty().append(isRight ? o2 : o1).append(SEPARATOR_COMPONENT).append(isRight ? o1 : o2);
        }).orElse(CommonComponents.EMPTY);
    }

    public static Component getHealthComponent(HealthTrackerRenderState renderState) {
        return getComponent(renderState.getHealthComponent(), renderState.healthSprite);
    }

    private static Component getArmorComponent(HealthTrackerRenderState renderState) {
        return getComponent(Component.literal(String.valueOf(renderState.armorValue)), renderState.armorSprite);
    }

    private static Component getArmorToughnessComponent(HealthTrackerRenderState renderState) {
        return getComponent(Component.literal(String.valueOf(renderState.toughnessValue)), renderState.toughnessSprite);
    }

    private static Component getComponent(Component component, @Nullable Identifier identifier) {
        MutableComponent mutableComponent = Component.empty().append(component);
        if (identifier != null) {
            return mutableComponent.append(CommonComponents.SPACE)
                    .append(Component.object(new AtlasSprite(AtlasIds.GUI, identifier)).withColor(-1));
        } else {
            return mutableComponent;
        }
    }

    public static void submitDamageScore(GraphicsLayer graphicsLayer, Font font, int damageAmount, int posX, int posY, int packedLight, ClientConfig.DamageValues damageValues) {
        if (damageAmount != 0) {
            int fontColor = damageValues.getTextColor(damageAmount);
            Component text = Component.literal(Integer.toString(Math.abs(damageAmount)));
            int stringWidth = font.width(text) / 2;
            if (damageValues.strongTextOutline) {
                graphicsLayer.text8xOutline(font, text, posX - stringWidth, posY, fontColor, packedLight);
            } else {
                graphicsLayer.text(font,
                        text,
                        posX - stringWidth,
                        posY,
                        fontColor,
                        true,
                        Font.DisplayMode.NORMAL,
                        packedLight,
                        0);
            }
        }
    }
}
