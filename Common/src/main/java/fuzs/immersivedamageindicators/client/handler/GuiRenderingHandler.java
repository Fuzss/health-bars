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
import net.torocraft.torohealth.bars.HealthBarRenderer;
import org.apache.commons.lang3.mutable.MutableInt;

public class GuiRenderingHandler {
    private static final int FRAME_SIZE = 42;
    static final int FRAME_BORDER_SIZE = 4;
    static final int GUI_SPRITE_SIZE = 9;
    static final float MOB_TITLE_SCALE = 1.5F;
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
                ClientConfig.Hud config = ImmersiveDamageIndicators.CONFIG.get(ClientConfig.class).hud;
                int barWidth = HealthBarHelper.getBarWidth(config, healthTracker);
                AnchorPoint.Positioner positioner = config.anchorPoint.createPositioner(guiGraphics.guiWidth(),
                        guiGraphics.guiHeight(), FRAME_SIZE + 5 + barWidth, FRAME_SIZE
                );
                MutableInt posX = new MutableInt(positioner.getPosX(config.offsetX));
                MutableInt posY = new MutableInt(positioner.getPosY(config.offsetY));
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(0.0F, 0.0F, 3000.0F);
                RenderSystem.enableBlend();
                RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                        GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO,
                        GlStateManager.DestFactor.ONE
                );
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                guiGraphics.blitSprite(HOTBAR_OFFHAND_LEFT_LOCATION, posX.intValue(), posY.intValue(), FRAME_SIZE,
                        FRAME_SIZE
                );
                InLevelRenderingHandler.setIsRenderingInInventory(true);

                // these are similar to the player size values
                float scaleWidth = 0.8F / livingEntity.getBbWidth();
                float scaleHeight = 1.8F / livingEntity.getBbHeight();
                int scale = (int) (Math.min(scaleWidth, scaleHeight) * 30.0F);

                float yOffset = 0.5F - (scaleHeight - 0.8F) * 0.15F;
                if (config.mobOffsetOverrides.contains(livingEntity.getType())) {
                    yOffset += config.mobOffsetOverrides.<Double>get(livingEntity.getType(), 0);
                }

                InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, posX.intValue() + FRAME_BORDER_SIZE,
                        posY.intValue() + FRAME_BORDER_SIZE, posX.intValue() + FRAME_SIZE - FRAME_BORDER_SIZE,
                        posY.intValue() + FRAME_SIZE - FRAME_BORDER_SIZE, scale, yOffset,
                        posX.intValue() + 70 * (config.anchorPoint.isRight() ? -1 : 1), posY.intValue() + 10,
                        livingEntity
                );
                InLevelRenderingHandler.setIsRenderingInInventory(false);

                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

                renderMobTitleComponent(guiGraphics, posX, posY, minecraft.font, healthTracker);
                RenderSystem.enableBlend();
                renderHealthBar(guiGraphics, posX, posY, partialTick, minecraft.font, healthTracker, livingEntity,
                        barWidth
                );
                renderHealthComponent(guiGraphics, posX, posY, minecraft.font, healthTracker);
                renderArmorComponent(guiGraphics, posX, posY, minecraft.font, healthTracker);

                RenderSystem.disableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                guiGraphics.pose().popPose();
            }
        }
    }

    private static void renderMobTitleComponent(GuiGraphics guiGraphics, MutableInt posX, MutableInt posY, Font font, HealthTracker healthTracker) {
        posX.add(FRAME_SIZE + 5);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(MOB_TITLE_SCALE, MOB_TITLE_SCALE, MOB_TITLE_SCALE);
        FormattedCharSequence formattedCharSequence = getMobTitleComponent(font, healthTracker, MOB_TITLE_SCALE);
        guiGraphics.drawString(font, formattedCharSequence, (int) (posX.intValue() / MOB_TITLE_SCALE),
                (int) ((posY.intValue() + 5) / MOB_TITLE_SCALE), -1, true
        );
        guiGraphics.pose().popPose();
    }

    private static FormattedCharSequence getMobTitleComponent(Font font, HealthTracker healthTracker, float titleScale) {
        int barWidth = HealthBarHelper.getBarWidth(
                ImmersiveDamageIndicators.CONFIG.get(ClientConfig.class).hud, healthTracker);
        float maxWidth = (barWidth - 2) / titleScale - font.width(CommonComponents.ELLIPSIS);
        Component component = healthTracker.getDisplayName();
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
        ClientConfig.Hud config = ImmersiveDamageIndicators.CONFIG.get(ClientConfig.class).hud;
        posY.add(FRAME_SIZE / 2);
        HealthBarRenderer.renderHealthBar(healthTracker, guiGraphics, livingEntity,
                posX.intValue() - 1 + barWidth / 2, posY.intValue(), barWidth, partialTick, config.barColors
        );
        drawDamageNumber(guiGraphics.pose(), guiGraphics.bufferSource(), font, healthTracker.getHealthDelta(),
                posX.intValue() - 1 + (int) (barWidth * healthTracker.getHealthProgress()), posY.intValue() - 1,
                15728880, config.damageValues
        );
        guiGraphics.flush();
    }

    private static void renderHealthComponent(GuiGraphics guiGraphics, MutableInt posX, MutableInt posY, Font font, HealthTracker healthTracker) {
        posY.add(8);
        Component component = Component.literal(healthTracker.getHealth() + "/" + healthTracker.getMaxHealth());
        guiGraphics.drawString(font, component, posX.intValue(), posY.intValue(), -1, true);
        posX.add(font.width(component) + 2);
        guiGraphics.blitSprite(HEART_CONTAINER_SPRITE, posX.intValue(), posY.intValue(), GUI_SPRITE_SIZE,
                GUI_SPRITE_SIZE
        );
        guiGraphics.blitSprite(HEART_FULL_SPRITE, posX.intValue(), posY.intValue(), GUI_SPRITE_SIZE, GUI_SPRITE_SIZE);
    }

    private static void renderArmorComponent(GuiGraphics guiGraphics, MutableInt posX, MutableInt posY, Font font, HealthTracker healthTracker) {
        if (healthTracker.getArmorValue() > 0) {
            posX.add(GUI_SPRITE_SIZE + 4);
            Component component = Component.literal("|");
            guiGraphics.drawString(font, component, posX.intValue(), posY.intValue(), -1, true);
            posX.add(font.width(component) + 4);
            component = Component.literal(String.valueOf(healthTracker.getArmorValue()));
            guiGraphics.drawString(font, component, posX.intValue(), posY.intValue(), -1, true);
            posX.add(font.width(component) + 2);
            guiGraphics.blitSprite(ARMOR_FULL_SPRITE, posX.intValue(), posY.intValue(), GUI_SPRITE_SIZE,
                    GUI_SPRITE_SIZE
            );
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
