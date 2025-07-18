package fuzs.healthbars.client.handler;

import fuzs.healthbars.HealthBars;
import fuzs.healthbars.client.gui.GraphicsComponent;
import fuzs.healthbars.client.helper.*;
import fuzs.healthbars.config.AnchorPoint;
import fuzs.healthbars.config.ClientConfig;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.LivingEntity;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.function.Function;

public class GuiRenderingHandler {
    static final int MOB_SELECTION_SIZE = 42;
    static final int MOB_SELECTION_BORDER_SIZE = 4;
    static final float MOB_TITLE_SCALE = 1.5F;
    public static final int GUI_SPRITE_SIZE = 9;
    public static final int TEXT_TO_SPRITE_GAP = 2;
    public static final ResourceLocation MOB_SELECTION_SPRITE = HealthBars.id("mob_selection");
    public static final ResourceLocation HEART_CONTAINER_SPRITE = ResourceLocation.withDefaultNamespace(
            "hud/heart/container");
    public static final ResourceLocation HEART_FULL_SPRITE = ResourceLocation.withDefaultNamespace("hud/heart/full");
    public static final ResourceLocation ARMOR_FULL_SPRITE = ResourceLocation.withDefaultNamespace("hud/armor_full");

    public static void renderHealthBar(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {

        if (!HealthBars.CONFIG.get(ClientConfig.class).anyRendering.get()
                || !HealthBars.CONFIG.get(ClientConfig.class).guiRendering) {
            return;
        }

        if (PickEntityHandler.getCrosshairPickEntity() instanceof LivingEntity livingEntity && HealthBars.CONFIG.get(
                ClientConfig.class).isEntityAllowed(livingEntity)) {

            Minecraft minecraft = Minecraft.getInstance();
            float partialTick = deltaTracker.getGameTimeDeltaPartialTick(false);
            HealthTracker healthTracker = HealthTracker.getHealthTracker(livingEntity, false);
            if (healthTracker != null && EntityVisibilityHelper.isEntityVisible(minecraft,
                    livingEntity,
                    partialTick,
                    true)) {
                ClientConfig.Gui config = HealthBars.CONFIG.get(ClientConfig.class).gui;
                HealthTrackerRenderState renderState = HealthTrackerRenderState.extractRenderState(healthTracker,
                        livingEntity,
                        partialTick,
                        config.barColors);
                int barWidth = HealthBarHelper.getBarWidth(config, renderState);
                AnchorPoint anchorPoint = config.anchorPoint;
                AnchorPoint.Positioner positioner = anchorPoint.createPositioner(guiGraphics.guiWidth(),
                        guiGraphics.guiHeight(),
                        MOB_SELECTION_SIZE + 5 + barWidth,
                        MOB_SELECTION_SIZE);
                MutableInt posX = new MutableInt(positioner.getPosX(config.offsetWidth));
                MutableInt posY = new MutableInt(positioner.getPosY(config.offsetHeight));

                if (config.renderEntityDisplay) {
                    if (anchorPoint.isRight()) {
                        posX.add(barWidth + 5);
                    }
                    guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED,
                            MOB_SELECTION_SPRITE,
                            posX.intValue(),
                            posY.intValue(),
                            MOB_SELECTION_SIZE,
                            MOB_SELECTION_SIZE);
                    renderEntityDisplay(guiGraphics, posX, posY, renderState, livingEntity);
                    if (anchorPoint.isRight()) {
                        posX.subtract(barWidth + 5);
                    } else {
                        posX.add(MOB_SELECTION_SIZE + 5);
                    }
                }

                Font font = minecraft.font;
                int posXOffset = 0;
                if (anchorPoint.isRight()) {
                    FormattedCharSequence formattedCharSequence = getMobTitleComponent(font,
                            renderState,
                            MOB_TITLE_SCALE);
                    posXOffset = barWidth - 2 - (int) (font.width(formattedCharSequence) * MOB_TITLE_SCALE);
                    posX.add(posXOffset);
                }
                renderMobTitleComponent(guiGraphics, posX, posY, font, renderState);
                if (anchorPoint.isRight()) {
                    posX.subtract(posXOffset);
                }

                posY.add(MOB_SELECTION_SIZE / 2);
                GraphicsComponent graphicsComponent = new GraphicsComponent.Gui(guiGraphics);
                renderHealthBar(graphicsComponent, posX, posY, font, renderState, barWidth);
                if (config.renderAttributeComponents) {
                    posY.add(8);
                    if (anchorPoint.isRight()) {
                        posXOffset = barWidth - 2 - getHealthComponentWidth(renderState, font, true)
                                - getArmorComponentWidth(renderState, font);
                        posX.add(posXOffset);
                    }
                    renderHealthComponent(graphicsComponent, posX, posY, font, renderState);
                    renderArmorComponent(guiGraphics, posX, posY, font, renderState);
                    if (anchorPoint.isRight()) {
                        posX.subtract(posXOffset);
                    }
                }
            }
        }
    }

    private static void renderEntityDisplay(GuiGraphics guiGraphics, MutableInt posX, MutableInt posY, HealthTrackerRenderState renderState, LivingEntity livingEntity) {
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
        InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics,
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

    private static void renderMobTitleComponent(GuiGraphics guiGraphics, MutableInt posX, MutableInt posY, Font font, HealthTrackerRenderState renderState) {
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().scale(MOB_TITLE_SCALE, MOB_TITLE_SCALE);
        FormattedCharSequence formattedCharSequence = getMobTitleComponent(font, renderState, MOB_TITLE_SCALE);
        guiGraphics.drawString(font,
                formattedCharSequence,
                (int) (posX.intValue() / MOB_TITLE_SCALE),
                (int) ((posY.intValue() + 5) / MOB_TITLE_SCALE),
                -1,
                true);
        guiGraphics.pose().popMatrix();
    }

    private static FormattedCharSequence getMobTitleComponent(Font font, HealthTrackerRenderState renderState, float titleScale) {
        int barWidth = HealthBarHelper.getBarWidth(HealthBars.CONFIG.get(ClientConfig.class).gui, renderState);
        float maxWidth = (barWidth - 2) / titleScale - font.width(CommonComponents.ELLIPSIS);
        Component component = renderState.displayName;
        if (font.width(component) > maxWidth) {
            Component ellipsis = Component.empty().append(CommonComponents.ELLIPSIS).withStyle(component.getStyle());
            FormattedText formattedText = FormattedText.composite(font.substrByWidth(component, (int) maxWidth),
                    ellipsis);
            return Language.getInstance().getVisualOrder(formattedText);
        } else {
            return component.getVisualOrderText();
        }
    }

    private static void renderHealthBar(GraphicsComponent graphicsComponent, MutableInt posX, MutableInt posY, Font font, HealthTrackerRenderState renderState, int barWidth) {
        ClientConfig.Gui config = HealthBars.CONFIG.get(ClientConfig.class).gui;
        HealthBarRenderHelper.renderHealthBar(graphicsComponent,
                null,
                posX.intValue() - 1 + barWidth / 2,
                posY.intValue(),
                renderState,
                barWidth,
                -1,
                GraphicsComponent.PACKED_LIGHT);
        if (config.damageValues.renderDamageValues) {
            drawDamageNumber(graphicsComponent,
                    font,
                    renderState.healthData,
                    posX.intValue() - 1 + (int) (barWidth * renderState.healthProgress),
                    posY.intValue() - 1,
                    GraphicsComponent.PACKED_LIGHT,
                    config.damageValues);
        }
    }

    private static void renderHealthComponent(GraphicsComponent graphicsComponent, MutableInt posX, MutableInt posY, Font font, HealthTrackerRenderState renderState) {
        renderHealthComponent(graphicsComponent,
                null,
                posX,
                posY,
                font,
                renderState,
                true,
                true,
                -1,
                Font.DisplayMode.NORMAL,
                GraphicsComponent.PACKED_LIGHT);
    }

    public static void renderHealthComponent(GraphicsComponent graphicsComponent, Function<ResourceLocation, RenderType> renderTypeGetter, MutableInt posX, MutableInt posY, Font font, HealthTrackerRenderState renderState, boolean dropShadow, boolean renderSprite, int color, Font.DisplayMode fontDisplayMode, int packedLight) {
        Component component = renderState.healthComponent;
        int x = posX.intValue();
        int y = posY.intValue();
        graphicsComponent.drawString(font, component, x, y, color, dropShadow, fontDisplayMode, packedLight);
        posX.add(font.width(component));
        if (renderSprite) {
            posX.add(TEXT_TO_SPRITE_GAP);
            graphicsComponent.blitSprite(renderTypeGetter,
                    HEART_CONTAINER_SPRITE,
                    posX.intValue(),
                    posY.intValue(),
                    GUI_SPRITE_SIZE,
                    GUI_SPRITE_SIZE,
                    color,
                    packedLight);
            graphicsComponent.blitSprite(renderTypeGetter,
                    HEART_FULL_SPRITE,
                    posX.intValue(),
                    posY.intValue(),
                    GUI_SPRITE_SIZE,
                    GUI_SPRITE_SIZE,
                    color,
                    packedLight);
            posX.add(GUI_SPRITE_SIZE);
        }
    }

    public static int getHealthComponentWidth(HealthTrackerRenderState renderState, Font font, boolean renderSprite) {
        MutableInt posX = new MutableInt();
        Component component = renderState.healthComponent;
        posX.add(font.width(component));
        if (renderSprite) {
            posX.add(TEXT_TO_SPRITE_GAP);
            posX.add(GUI_SPRITE_SIZE);
        }
        return posX.intValue();
    }

    private static void renderArmorComponent(GuiGraphics guiGraphics, MutableInt posX, MutableInt posY, Font font, HealthTrackerRenderState renderState) {
        if (renderState.armorValue > 0) {
            posX.add(TEXT_TO_SPRITE_GAP * 2);
            Component component = Component.literal("|");
            guiGraphics.drawString(font, component, posX.intValue(), posY.intValue(), -1, true);
            posX.add(font.width(component) + TEXT_TO_SPRITE_GAP * 2);
            component = Component.literal(String.valueOf(renderState.armorValue));
            guiGraphics.drawString(font, component, posX.intValue(), posY.intValue(), -1, true);
            posX.add(font.width(component) + TEXT_TO_SPRITE_GAP);
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED,
                    ARMOR_FULL_SPRITE,
                    posX.intValue(),
                    posY.intValue(),
                    GUI_SPRITE_SIZE,
                    GUI_SPRITE_SIZE);
            posX.add(GUI_SPRITE_SIZE);
        }
    }

    private static int getArmorComponentWidth(HealthTrackerRenderState renderState, Font font) {
        if (renderState.armorValue > 0) {
            MutableInt posX = new MutableInt();
            posX.add(TEXT_TO_SPRITE_GAP * 2);
            Component component = Component.literal("|");
            posX.add(font.width(component) + TEXT_TO_SPRITE_GAP * 2);
            component = Component.literal(String.valueOf(renderState.armorValue));
            posX.add(font.width(component) + TEXT_TO_SPRITE_GAP);
            posX.add(GUI_SPRITE_SIZE);
            return posX.intValue();
        } else {
            return 0;
        }
    }

    public static void drawDamageNumber(GraphicsComponent graphicsComponent, Font font, int damageAmount, int posX, int posY, int packedLight, ClientConfig.DamageValues damageValues) {
        if (damageAmount != 0) {
            int fontColor = damageValues.getTextColor(damageAmount);
            Component text = Component.literal(Integer.toString(Math.abs(damageAmount)));
            int stringWidth = font.width(text) / 2;
            if (damageValues.strongTextOutline) {
                graphicsComponent.drawString8xOutline(font, text, posX - stringWidth, posY, fontColor, packedLight);
            } else {
                graphicsComponent.drawString(font,
                        text,
                        posX - stringWidth,
                        posY,
                        fontColor,
                        true,
                        Font.DisplayMode.NORMAL,
                        packedLight);
            }
        }
    }
}
