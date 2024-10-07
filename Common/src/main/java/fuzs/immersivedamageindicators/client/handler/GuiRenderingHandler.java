package fuzs.immersivedamageindicators.client.handler;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import fuzs.immersivedamageindicators.ImmersiveDamageIndicators;
import fuzs.immersivedamageindicators.client.helper.EntityVisibilityHelper;
import fuzs.immersivedamageindicators.client.helper.HealthBarHelper;
import fuzs.immersivedamageindicators.client.helper.HealthTracker;
import fuzs.immersivedamageindicators.config.AnchorPoint;
import fuzs.immersivedamageindicators.config.ClientConfig;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import fuzs.immersivedamageindicators.client.helper.HealthBarRenderHelper;
import org.apache.commons.lang3.mutable.MutableInt;

public class GuiRenderingHandler {
    static final int FRAME_SIZE = 42;
    static final int FRAME_BORDER_SIZE = 4;
    static final float MOB_TITLE_SCALE = 1.5F;
    public static final int GUI_SPRITE_SIZE = 9;
    public static final int TEXT_TO_SPRITE_GAP = 2;
    private static final ResourceLocation HOTBAR_OFFHAND_LEFT_LOCATION = ImmersiveDamageIndicators.id("mob_selection");
    public static final ResourceLocation HEART_CONTAINER_SPRITE = ResourceLocation.withDefaultNamespace(
            "hud/heart/container");
    public static final ResourceLocation HEART_FULL_SPRITE = ResourceLocation.withDefaultNamespace("hud/heart/full");
    private static final ResourceLocation ARMOR_FULL_SPRITE = ResourceLocation.withDefaultNamespace("hud/armor_full");

    public static void onAfterRenderGui(Gui gui, GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Entity entity = PickEntityHandler.getCrosshairPickEntity();
        if (entity instanceof LivingEntity livingEntity) {
            Minecraft minecraft = gui.minecraft;
            float partialTick = deltaTracker.getGameTimeDeltaPartialTick(true);
            HealthTracker healthTracker = HealthTracker.getHealthTracker(livingEntity, false);
            if (healthTracker != null && EntityVisibilityHelper.isEntityVisible(minecraft.level, livingEntity,
                    minecraft.player, partialTick, minecraft.getEntityRenderDispatcher(), true
            )) {
                ClientConfig.Gui config = ImmersiveDamageIndicators.CONFIG.get(ClientConfig.class).gui;
                int barWidth = HealthBarHelper.getBarWidth(config, healthTracker);
                AnchorPoint.Positioner positioner = config.anchorPoint.createPositioner(guiGraphics.guiWidth(),
                        guiGraphics.guiHeight(), FRAME_SIZE + 5 + barWidth, FRAME_SIZE
                );
                MutableInt posX = new MutableInt(positioner.getPosX(config.offsetWidth));
                MutableInt posY = new MutableInt(positioner.getPosY(config.offsetHeight));
                guiGraphics.pose().pushPose();
                // draw above all other gui layers, since 1.21 they are all separated by z-offset, so this needs to be quite a lot
                guiGraphics.pose().translate(0.0F, 0.0F, 3000.0F);
                RenderSystem.enableBlend();
                RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                        GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO,
                        GlStateManager.DestFactor.ONE
                );
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

                if (config.renderEntityDisplay) {
                    guiGraphics.blitSprite(HOTBAR_OFFHAND_LEFT_LOCATION, posX.intValue(), posY.intValue(), FRAME_SIZE,
                            FRAME_SIZE
                    );
                    renderEntityDisplay(guiGraphics, posX, posY, healthTracker, livingEntity);
                    posX.add(FRAME_SIZE + 5);
                }

                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

                renderMobTitleComponent(guiGraphics, posX, posY, minecraft.font, healthTracker);
                RenderSystem.enableBlend();
                posY.add(FRAME_SIZE / 2);
                renderHealthBar(guiGraphics, posX, posY, partialTick, minecraft.font, healthTracker, livingEntity,
                        barWidth
                );
                if (config.renderAttributeComponents) {
                    posY.add(8);
                    renderHealthComponent(guiGraphics, posX, posY, minecraft.font, healthTracker, true, true);
                    renderArmorComponent(guiGraphics, posX, posY, minecraft.font, healthTracker);
                }

                RenderSystem.disableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                guiGraphics.pose().popPose();
            }
        }
    }

    private static void renderEntityDisplay(GuiGraphics guiGraphics, MutableInt posX, MutableInt posY, HealthTracker healthTracker, LivingEntity livingEntity) {
        // these are similar to the player size values
        float scaleWidth = 0.8F / livingEntity.getBbWidth();
        float scaleHeight = 1.8F / livingEntity.getBbHeight();
        int scale = (int) (Math.min(scaleWidth, scaleHeight) * 30.0F);
        float yOffset = 0.5F - (scaleHeight - 0.8F) * 0.15F + (float) healthTracker.getData().renderOffset();

        int x1 = posX.intValue() + FRAME_BORDER_SIZE;
        int y1 = posY.intValue() + FRAME_BORDER_SIZE;
        int x2 = posX.intValue() + FRAME_SIZE - FRAME_BORDER_SIZE;
        int y2 = posY.intValue() + FRAME_SIZE - FRAME_BORDER_SIZE;
        int mouseX = posX.intValue() + 70 * (ImmersiveDamageIndicators.CONFIG.get(
                ClientConfig.class).gui.anchorPoint.isRight() ? -1 : 1);
        int mouseY = posY.intValue() + 10;

        InLevelRenderingHandler.setIsRenderingInInventory(true);
        InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, x1, y1, x2, y2, scale, yOffset, mouseX, mouseY,
                livingEntity
        );
        InLevelRenderingHandler.setIsRenderingInInventory(false);
    }

    private static void renderMobTitleComponent(GuiGraphics guiGraphics, MutableInt posX, MutableInt posY, Font font, HealthTracker healthTracker) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(MOB_TITLE_SCALE, MOB_TITLE_SCALE, MOB_TITLE_SCALE);
        FormattedCharSequence formattedCharSequence = getMobTitleComponent(font, healthTracker, MOB_TITLE_SCALE);
        guiGraphics.drawString(font, formattedCharSequence, (int) (posX.intValue() / MOB_TITLE_SCALE),
                (int) ((posY.intValue() + 5) / MOB_TITLE_SCALE), -1, true
        );
        guiGraphics.pose().popPose();
    }

    private static FormattedCharSequence getMobTitleComponent(Font font, HealthTracker healthTracker, float titleScale) {
        int barWidth = HealthBarHelper.getBarWidth(ImmersiveDamageIndicators.CONFIG.get(ClientConfig.class).gui,
                healthTracker
        );
        float maxWidth = (barWidth - 2) / titleScale - font.width(CommonComponents.ELLIPSIS);
        Component component = healthTracker.getData().displayName();
        if (font.width(component) > maxWidth) {
            Component ellipsis = Component.empty().append(CommonComponents.ELLIPSIS).withStyle(component.getStyle());
            FormattedText formattedText = FormattedText.composite(font.substrByWidth(component, (int) maxWidth),
                    ellipsis
            );
            return Language.getInstance().getVisualOrder(formattedText);
        } else {
            return component.getVisualOrderText();
        }
    }

    private static void renderHealthBar(GuiGraphics guiGraphics, MutableInt posX, MutableInt posY, float partialTick, Font font, HealthTracker healthTracker, LivingEntity livingEntity, int barWidth) {
        ClientConfig.Gui config = ImmersiveDamageIndicators.CONFIG.get(ClientConfig.class).gui;
        HealthBarRenderHelper.renderHealthBar(healthTracker, guiGraphics, livingEntity, posX.intValue() - 1 + barWidth / 2,
                posY.intValue(), barWidth, partialTick, config.barColors
        );
        if (config.damageValues.renderDamageValues) {
            drawDamageNumber(guiGraphics.pose(), guiGraphics.bufferSource(), font, healthTracker.getHealthDelta(),
                    posX.intValue() - 1 + (int) (barWidth * healthTracker.getHealthProgress()), posY.intValue() - 1,
                    15728880, config.damageValues
            );
        }
        guiGraphics.flush();
    }

    public static void renderHealthComponent(GuiGraphics guiGraphics, MutableInt posX, MutableInt posY, Font font, HealthTracker healthTracker, boolean dropShadow, boolean renderSprite) {
        Component component = healthTracker.getData().getHealthComponent();
        guiGraphics.drawString(font, component, posX.intValue(), posY.intValue(), -1, dropShadow);
        posX.add(font.width(component));
        if (renderSprite) {
            posX.add(TEXT_TO_SPRITE_GAP);
            guiGraphics.blitSprite(HEART_CONTAINER_SPRITE, posX.intValue(), posY.intValue(), GUI_SPRITE_SIZE,
                    GUI_SPRITE_SIZE
            );
            guiGraphics.blitSprite(HEART_FULL_SPRITE, posX.intValue(), posY.intValue(), GUI_SPRITE_SIZE, GUI_SPRITE_SIZE);
            posX.add(GUI_SPRITE_SIZE);
        }
    }

    public static int getHealthComponentWidth(HealthTracker healthTracker, Font font, boolean renderSprite) {
        int width = font.width(healthTracker.getData().getHealthComponent());
        if (renderSprite) {
            return width + GuiRenderingHandler.TEXT_TO_SPRITE_GAP +
                    GuiRenderingHandler.GUI_SPRITE_SIZE;
        } else {
            return width;
        }
    }

    private static void renderArmorComponent(GuiGraphics guiGraphics, MutableInt posX, MutableInt posY, Font font, HealthTracker healthTracker) {
        if (healthTracker.getData().armorValue() > 0) {
            posX.add(TEXT_TO_SPRITE_GAP * 2);
            Component component = Component.literal("|");
            guiGraphics.drawString(font, component, posX.intValue(), posY.intValue(), -1, true);
            posX.add(font.width(component) + TEXT_TO_SPRITE_GAP * 2);
            component = Component.literal(String.valueOf(healthTracker.getData().armorValue()));
            guiGraphics.drawString(font, component, posX.intValue(), posY.intValue(), -1, true);
            posX.add(font.width(component) + TEXT_TO_SPRITE_GAP);
            guiGraphics.blitSprite(ARMOR_FULL_SPRITE, posX.intValue(), posY.intValue(), GUI_SPRITE_SIZE,
                    GUI_SPRITE_SIZE
            );
            posX.add(GUI_SPRITE_SIZE);
        }
    }

    public static void drawDamageNumber(PoseStack poseStack, MultiBufferSource bufferSource, Font font, int damageAmount, int posX, int posY, int packedLight, ClientConfig.DamageValues damageValues) {
        if (damageAmount != 0) {
            int fontColor = damageValues.getTextColor(damageAmount);
            String s = Integer.toString(Math.abs(damageAmount));
            int stringWidth = font.width(s) / 2;
            if (damageValues.strongTextOutline) {
                FormattedCharSequence text = Language.getInstance().getVisualOrder(FormattedText.of(s));
                font.drawInBatch8xOutline(text, posX - stringWidth, posY, fontColor, 0, poseStack.last().pose(),
                        bufferSource, packedLight
                );
            } else {
                font.drawInBatch(s, posX - stringWidth, posY, fontColor, true, poseStack.last().pose(), bufferSource,
                        Font.DisplayMode.NORMAL, 0, packedLight, font.isBidirectional()
                );
            }
        }
    }
}
