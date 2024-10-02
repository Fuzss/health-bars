package fuzs.immersivedamageindicators.client.handler;

import com.mojang.blaze3d.systems.RenderSystem;
import fuzs.immersivedamageindicators.ImmersiveDamageIndicators;
import fuzs.immersivedamageindicators.client.helper.EntityVisibilityHelper;
import fuzs.immersivedamageindicators.client.helper.HealthBarHelper;
import fuzs.immersivedamageindicators.client.helper.HealthTracker;
import fuzs.immersivedamageindicators.config.ClientConfig;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.torocraft.torohealth.bars.HealthBarRenderer;
import fuzs.immersivedamageindicators.client.helper.GuiGraphicsHelper;

public class GuiRenderingHandler {
    private static final int FRAME_SIZE = 42;
    private static final ResourceLocation HOTBAR_OFFHAND_LEFT_SPRITE = ResourceLocation.withDefaultNamespace(
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
                GuiGraphicsHelper.blitNineSliced(guiGraphics, HOTBAR_OFFHAND_LEFT_SPRITE, posX, posY, FRAME_SIZE,
                        FRAME_SIZE, 4, 4, 4, 4, 22, 22, 0, 1, 29, 24
                );
                InLevelRenderingHandler.setIsRenderingInInventory(true);
                InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, posX + 4, posY + 4,
                        posX + FRAME_SIZE - 4, posY + FRAME_SIZE - 4, 25, 0.0625F * 8.0F, 80, 20, livingEntity
                );
                InLevelRenderingHandler.setIsRenderingInInventory(false);

                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                RenderSystem.enableBlend();

                posX += FRAME_SIZE + 5;
                guiGraphics.pose().pushPose();
                guiGraphics.pose().scale(1.5F, 1.5F, 1.5F);
                Component component = healthTracker.getDisplayName();
                guiGraphics.drawString(minecraft.font, component, posX * 2 / 3, (posY + 5) * 2 / 3,
                        -1, true
                );
                guiGraphics.pose().popPose();

                RenderSystem.enableBlend();

                posY += FRAME_SIZE / 2;
                int scale = Math.max(Mth.ceil(minecraft.font.width(component) * 1.5F / 45.0F), HealthBarHelper.getBarScaleFromHealth(healthTracker.getHealth()));
                int barWidth = HealthBarHelper.getBarWidthByScale(Mth.clamp(scale, 2, 4));
                HealthBarRenderer.render(healthTracker, guiGraphics.pose(), livingEntity, posX - 1 + barWidth / 2, posY,
                        barWidth, false
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
            int fontColor;
            if (damageAmount > 0) {
                fontColor = ImmersiveDamageIndicators.CONFIG.get(ClientConfig.class).particle.healColor;
            } else {
                fontColor = ImmersiveDamageIndicators.CONFIG.get(ClientConfig.class).particle.damageColor;
            }
            String s = Integer.toString(Math.abs(damageAmount));
//            guiGraphics.drawString(font, s, posX - font.width(s) / 2, posY, fontColor, true);
            FormattedCharSequence text = Component.literal(s).getVisualOrderText();
            font.drawInBatch8xOutline(text, posX - font.width(s) / 2, posY, fontColor, 0,
                    guiGraphics.pose().last().pose(), guiGraphics.bufferSource(), 15728880
            );
            guiGraphics.flush();
        }
    }
}
