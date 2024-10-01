package net.torocraft.torohealth.display;

import com.mojang.blaze3d.systems.RenderSystem;
import fuzs.immersivedamageindicators.ImmersiveDamageIndicators;
import fuzs.immersivedamageindicators.config.ClientConfig;
import fuzs.immersivedamageindicators.config.ClientConfig.AnchorPoint;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class HudRenderer {
    private static final ResourceLocation BACKGROUND_TEXTURE = ImmersiveDamageIndicators.id(
            "textures/gui/default_skin_basic.png");

    private final EntityDisplay entityDisplay = new EntityDisplay();
    private final BarDisplay barDisplay = new BarDisplay();
    private LivingEntity entity;
    private int age;

    public void draw(Gui gui, GuiGraphics guiGraphics) {
        if (gui.getDebugOverlay().showDebugScreen()) return;
        float x = this.determineX(guiGraphics.guiWidth());
        float y = this.determineY(guiGraphics.guiHeight());
        this.draw(gui, guiGraphics, x, y, this.config().scale);
    }

    private int determineX(int guiWidth) {
        int x = this.config().x;
        AnchorPoint anchor = this.config().anchorPoint;
        return switch (anchor) {
            case BOTTOM_CENTER, TOP_CENTER -> (guiWidth / 2) + x;
            case BOTTOM_RIGHT, TOP_RIGHT -> (guiWidth) + x;
            default -> x;
        };
    }

    private int determineY(int guiHeight) {
        int y = this.config().y;
        AnchorPoint anchor = this.config().anchorPoint;
        return switch (anchor) {
            case BOTTOM_CENTER, BOTTOM_LEFT, BOTTOM_RIGHT -> y + guiHeight;
            default -> y;
        };
    }

    public void tick() {
        this.age++;
    }

    public void setEntity(LivingEntity entity) {
        if (entity != null) {
            this.age = 0;
        }

        if (entity == null && this.age > this.config().hideDelay) {
            this.setEntityWork(null);
        }

        if (entity != null && entity != this.entity) {
            this.setEntityWork(entity);
        }
    }

    private void setEntityWork(LivingEntity entity) {
        this.entity = entity;
        this.entityDisplay.setEntity(entity);
    }

    public LivingEntity getEntity() {
        return this.entity;
    }

    private void draw(Gui gui, GuiGraphics guiGraphics, float x, float y, float scale) {
        if (this.entity == null) {
            return;
        }

        if (this.config().onlyWhenHurt && this.entity.getHealth() >= this.entity.getMaxHealth()) {
            return;
        }

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(scale, scale, scale);
        guiGraphics.pose().translate(x - 10, y - 10, 0);
        if (this.config().showSkin) {
            this.drawBackground(guiGraphics);
        }
        guiGraphics.pose().translate(10, 10, 0);
        if (this.config().showEntity) {
            this.entityDisplay.draw(guiGraphics, scale);
        }
        guiGraphics.pose().translate(44, 0, 0);
        if (this.config().showBar) {
            this.barDisplay.draw(guiGraphics, gui.getFont(), this.entity);
        }
        guiGraphics.pose().popPose();
    }

    private void drawBackground(GuiGraphics guiGraphics) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int w = 160, h = 60;
        guiGraphics.blit(BACKGROUND_TEXTURE, 0, 0, 0.0F, 0.0F, w, h, w, h);
    }

    public ClientConfig.Hud config() {
        return ImmersiveDamageIndicators.CONFIG.get(ClientConfig.class).hud;
    }
}
