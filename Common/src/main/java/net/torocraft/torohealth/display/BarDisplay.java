package net.torocraft.torohealth.display;

import com.mojang.blaze3d.systems.RenderSystem;
import fuzs.immersivedamageindicators.config.ClientConfig;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.torocraft.torohealth.bars.BarState;
import net.torocraft.torohealth.bars.HealthBarRenderer;

public class BarDisplay {

    private String getEntityName(LivingEntity entity) {
        return entity.getDisplayName().getString();
    }

    public void draw(GuiGraphics guiGraphics, Font font, LivingEntity entity) {
        int xOffset = 0;

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();

        BarState barState = HealthBarRenderer.render(guiGraphics.pose(), entity, 63, 14, 130, false);

        if (HealthBarRenderer.barConfig().damageNumberType.equals(ClientConfig.NumberType.CUMULATIVE)) {
            HealthBarRenderer.drawDamageNumber(guiGraphics, font, barState.lastDmgCumulative, 63, 14, 130);
        } else if (HealthBarRenderer.barConfig().damageNumberType.equals(ClientConfig.NumberType.LAST)) {
            HealthBarRenderer.drawDamageNumber(guiGraphics, font, barState.lastDmg, 63, 14, 130);
        }

        String name = this.getEntityName(entity);
        int healthMax = Mth.ceil(entity.getMaxHealth());
        int healthCur = Math.min(Mth.ceil(entity.getHealth()), healthMax);
        String healthText = healthCur + "/" + healthMax;
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        guiGraphics.drawString(font, name, xOffset, (int) 2, 16777215);

        guiGraphics.drawString(font, name, xOffset, 2, 16777215, true);
        xOffset += font.width(name) + 5;

        this.renderHeartIcon(guiGraphics, xOffset, 1);
        xOffset += 10;

        guiGraphics.drawString(font, healthText, xOffset, 2, 0xe0e0e0, true);
        xOffset += font.width(healthText) + 5;

        int armorValue = entity.getArmorValue();
        if (armorValue > 0) {
            this.renderArmorIcon(guiGraphics, xOffset, 1);
            xOffset += 10;
            guiGraphics.drawString(font, String.valueOf(armorValue), xOffset, 2, 0xe0e0e0, true);
        }
    }

    private static final ResourceLocation HEART_CONTAINER_SPRITE = ResourceLocation.withDefaultNamespace(
            "hud/heart/container");
    private static final ResourceLocation HEART_FULL_SPRITE = ResourceLocation.withDefaultNamespace("hud/heart/full");
    private static final ResourceLocation ARMOR_FULL_SPRITE = ResourceLocation.withDefaultNamespace("hud/armor_full");

    private void renderArmorIcon(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.blitSprite(ARMOR_FULL_SPRITE, x, y, 9, 9);
    }

    private void renderHeartIcon(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.blitSprite(HEART_CONTAINER_SPRITE, x, y, 9, 9);
        guiGraphics.blitSprite(HEART_FULL_SPRITE, x, y, 50, 9, 9);
    }
}
