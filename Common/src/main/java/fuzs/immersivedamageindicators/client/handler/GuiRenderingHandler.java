package fuzs.immersivedamageindicators.client.handler;

import com.mojang.blaze3d.systems.RenderSystem;
import fuzs.immersivedamageindicators.ImmersiveDamageIndicators;
import fuzs.immersivedamageindicators.client.helper.EntityVisibilityHelper;
import fuzs.immersivedamageindicators.client.helper.GuiGraphicsHelper;
import fuzs.immersivedamageindicators.client.helper.HealthBarHelper;
import fuzs.immersivedamageindicators.client.helper.HealthTracker;
import fuzs.immersivedamageindicators.config.ClientConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.torocraft.torohealth.bars.HealthBarRenderer;

public class GuiRenderingHandler {
    private static final int FRAME_SIZE = 42;
    private static final ResourceLocation HOTBAR_OFFHAND_LEFT_SPRITE = ResourceLocation.withDefaultNamespace(
            "hud/hotbar_offhand_left");
    private static final ResourceLocation HOTBAR_OFFHAND_LEFT_LOCATION = ResourceLocation.withDefaultNamespace(
            "textures/gui/sprites/hud/hotbar_offhand_left.png");
    private static final ResourceLocation HEART_CONTAINER_SPRITE = ResourceLocation.withDefaultNamespace(
            "hud/heart/container");
    private static final ResourceLocation HEART_FULL_SPRITE = ResourceLocation.withDefaultNamespace("hud/heart/full");
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
                int posX = 9;
                int posY = 9;
                guiGraphics.pose().pushPose();
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                GuiGraphicsHelper.blitNineSliced(guiGraphics, HOTBAR_OFFHAND_LEFT_LOCATION, posX, posY, FRAME_SIZE,
                        FRAME_SIZE, 4, 4, 4, 4, 22, 22, 0, 1, 29, 24
                );
                guiGraphics.blitSprite(HOTBAR_OFFHAND_LEFT_SPRITE, 29, 24, 0, 1, posX - 5, posY + FRAME_SIZE - 13, 22,
                        22
                );
                ItemStack itemStack = HealthBarHelper.getDisplayItem(livingEntity).getDefaultInstance();
                guiGraphics.renderFakeItem(itemStack, posX - 5 + 3, posY + FRAME_SIZE - 13 + 3);
                InLevelRenderingHandler.setIsRenderingInInventory(true);
                InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, posX + 4, posY + 4,
                        posX + FRAME_SIZE - 4, posY + FRAME_SIZE - 4, 25, 0.0625F * 8.0F, 80, 20, livingEntity
                );
                InLevelRenderingHandler.setIsRenderingInInventory(false);

                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                RenderSystem.enableBlend();

                Component component = healthTracker.getDisplayName();
                int barScale = Math.max(Mth.ceil(minecraft.font.width(component) * 1.5F / 45.0F),
                        HealthBarHelper.getBarScaleFromHealth(healthTracker.getHealth())
                );
                int minBarWidth = ImmersiveDamageIndicators.CONFIG.get(ClientConfig.class).barColors.minBarWidth;
                int barWidth = HealthBarHelper.getBarWidthByScale(Mth.clamp(barScale, Math.min(4, minBarWidth), 4));

                posX += FRAME_SIZE + 5;
                guiGraphics.pose().pushPose();
                guiGraphics.pose().scale(1.5F, 1.5F, 1.5F);
                int maxWidth = (barWidth - 2) * 2 / 3 - minecraft.font.width(CommonComponents.ELLIPSIS);
                FormattedText formattedText;
                if (minecraft.font.width(component) * 3 / 2 > maxWidth) {
                    Component ellipsis = Component.empty().append(CommonComponents.ELLIPSIS).withStyle(component.getStyle());
                    formattedText = FormattedText.composite(minecraft.font.substrByWidth(component, maxWidth), ellipsis);
                } else {
                    formattedText = component;
                }
                guiGraphics.drawString(minecraft.font, Language.getInstance().getVisualOrder(formattedText), posX * 2 / 3, (posY + 5) * 2 / 3, -1, true);
                guiGraphics.pose().popPose();

                RenderSystem.enableBlend();

                posY += FRAME_SIZE / 2;
                HealthBarRenderer.render(healthTracker, guiGraphics.pose(), livingEntity, posX - 1 + barWidth / 2, posY,
                        barWidth, false, partialTick
                );
                drawDamageNumber(guiGraphics, minecraft.font, healthTracker.getHealthDelta(),
                        posX - 1 + (int) (barWidth * healthTracker.getHealthProgress()), posY - 1
                );

                posY += 8;
                component = Component.literal(healthTracker.getHealth() + "/" + healthTracker.getMaxHealth());
                guiGraphics.drawString(minecraft.font, component, posX, posY, -1, true);
                posX += minecraft.font.width(component) + 2;
                guiGraphics.blitSprite(HEART_CONTAINER_SPRITE, posX, posY, 9, 9);
                guiGraphics.blitSprite(HEART_FULL_SPRITE, posX, posY, 9, 9);

                if (healthTracker.getArmorValue() > 0) {

                    posX += 9 + 4;
                    component = Component.literal("|");
                    guiGraphics.drawString(minecraft.font, component, posX, posY, -1, true);
                    posX += minecraft.font.width(component) + 4;
                    component = Component.literal(String.valueOf(healthTracker.getArmorValue()));
                    guiGraphics.drawString(minecraft.font, component, posX, posY, -1, true);
                    posX += minecraft.font.width(component) + 2;
                    guiGraphics.blitSprite(ARMOR_FULL_SPRITE, posX, posY, 9, 9);
                }

                guiGraphics.pose().popPose();
            }
        }
    }

    public static void drawDamageNumber(GuiGraphics guiGraphics, Font font, int damageAmount, int posX, int posY) {
        if (damageAmount != 0) {
            int fontColor = getTextColor(damageAmount);
            String s = Integer.toString(Math.abs(damageAmount));
//            guiGraphics.drawString(font, s, posX - font.width(s) / 2, posY, fontColor, true);
            FormattedCharSequence text = Component.literal(s).getVisualOrderText();
            font.drawInBatch8xOutline(text, posX - font.width(s) / 2, posY, fontColor, 0,
                    guiGraphics.pose().last().pose(), guiGraphics.bufferSource(), 15728880
            );
            guiGraphics.flush();
        }
    }

    public static int getTextColor(int damageAmount) {
        ChatFormatting chatFormatting;
        if (damageAmount > 0) {
            chatFormatting = ImmersiveDamageIndicators.CONFIG.get(ClientConfig.class).particleColors.healColor;
        } else {
            chatFormatting = ImmersiveDamageIndicators.CONFIG.get(ClientConfig.class).particleColors.damageColor;
        }
        return chatFormatting.isColor() ? chatFormatting.getColor() : -1;
    }
}
