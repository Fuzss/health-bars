package fuzs.immersivedamageindicators.client.handler;

import com.mojang.blaze3d.vertex.PoseStack;
import fuzs.immersivedamageindicators.ImmersiveDamageIndicators;
import fuzs.immersivedamageindicators.client.helper.EntityVisibilityHelper;
import fuzs.immersivedamageindicators.client.helper.HealthBarHelper;
import fuzs.immersivedamageindicators.client.helper.HealthTracker;
import fuzs.immersivedamageindicators.client.helper.CustomGuiGraphics;
import fuzs.immersivedamageindicators.client.renderer.ModRenderType;
import fuzs.immersivedamageindicators.config.ClientConfig;
import fuzs.puzzleslib.api.event.v1.core.EventResult;
import fuzs.puzzleslib.api.event.v1.data.DefaultedValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import fuzs.immersivedamageindicators.client.helper.HealthBarRenderHelper;

public class InLevelRenderingHandler {
    private static boolean isRenderingInInventory;

    public static void setIsRenderingInInventory(boolean isRenderingInInventory) {
        InLevelRenderingHandler.isRenderingInInventory = isRenderingInInventory;
    }

    @SuppressWarnings("ConstantValue")
    public static EventResult onRenderNameTag(Entity entity, DefaultedValue<Component> content, EntityRenderer<?> entityRenderer, PoseStack poseStack, int packedLight, float partialTick) {
        if (isRenderingInInventory) {
            return EventResult.DENY;
        } else if (entity instanceof LivingEntity livingEntity && livingEntity.isAlive()) {
            HealthTracker healthTracker = HealthTracker.getHealthTracker(livingEntity, false);
            if (healthTracker != null) {

                ClientConfig.Level config = ImmersiveDamageIndicators.CONFIG.get(
                        ClientConfig.class).level;
                Minecraft minecraft = Minecraft.getInstance();
                EntityRenderDispatcher dispatcher = entityRenderer.entityRenderDispatcher;
                Vec3 vec3 = entity.getAttachments().getNullable(EntityAttachment.NAME_TAG, 0,
                        entity.getViewYRot(partialTick)
                );
                // other mods might be rendering this mob without a level in some menu, so camera is null then
                if (vec3 != null && dispatcher.camera != null &&
                        dispatcher.camera.getEntity() instanceof LivingEntity && EntityVisibilityHelper.isEntityVisible(
                        minecraft.level, livingEntity, minecraft.player, partialTick, dispatcher, config.pickedEntity)) {
                    poseStack.pushPose();
                    poseStack.translate(vec3.x, vec3.y + 0.5, vec3.z);
                    poseStack.mulPose(dispatcher.cameraOrientation());
                    float plaqueScale = getPlaqueScale(livingEntity, dispatcher, minecraft.player);
                    // x and z are flipped as of 1.21
                    poseStack.scale(0.025F * plaqueScale, -0.025F * plaqueScale, 0.025F * plaqueScale);
                    int heightOffset = "deadmau5".equals(content.get().getString()) ? -13 : -3;
                    if (!config.renderNameComponent && ((EntityRenderer<Entity>) entityRenderer).shouldShowName(entity)) {
                        heightOffset -= 13;
                    }
                    heightOffset += config.offsetHeight;

                    int barWidth = HealthBarHelper.getBarWidth(config, healthTracker);

                    RenderType renderType = RenderType.textBackgroundSeeThrough();
                    CustomGuiGraphics guiGraphics = new CustomGuiGraphics(poseStack).setAlpha(0.125F)
                            .setBlitOffset(0.01F)
                            .setRenderType(ModRenderType.ICON_SEE_THROUGH).setFontDisplayMode(Font.DisplayMode.SEE_THROUGH);

                    if (config.fullBrightness != ClientConfig.FullBrightRendering.ALWAYS) {
                        guiGraphics.setPackedLight(packedLight);
                    }

                    if (config.fullBrightness == ClientConfig.FullBrightRendering.ALWAYS) {
                        guiGraphics.setAlpha(1.0F);
                    }

                    boolean renderBackground = config.renderBackground;
                    if (config.behindWalls) {
                        HealthBarRenderHelper.renderHealthBar(healthTracker, guiGraphics, livingEntity, 0, heightOffset + 8,
                                barWidth, partialTick, config.barColors
                        );
                        HealthBarRenderHelper.renderHealthBarDecorations(renderType, guiGraphics, 0, heightOffset + 8, minecraft.font,
                                healthTracker, barWidth, !config.renderBackground, renderBackground
                        );
                        guiGraphics.flush();
                    }

                    guiGraphics = new CustomGuiGraphics(poseStack).setAlpha(1.0F).setBlitOffset(0.0F).setRenderType(ModRenderType.ICON).setFontDisplayMode(
                            Font.DisplayMode.NORMAL);

                    if (config.fullBrightness == ClientConfig.FullBrightRendering.NEVER) {
                        guiGraphics.setPackedLight(packedLight);
                    }

                    if (false && config.behindWalls && config.fullBrightness == ClientConfig.FullBrightRendering.ALWAYS) {
                        guiGraphics.setRenderType(ModRenderType.ICON_SEE_THROUGH).setFontDisplayMode(Font.DisplayMode.SEE_THROUGH).setBlitOffset(0.125F);
                        renderType = RenderType.textBackgroundSeeThrough();
                    } else {
                        renderType = RenderType.textBackground();
                        renderBackground = false;
                    }

                    HealthBarRenderHelper.renderHealthBar(healthTracker, guiGraphics, livingEntity, 0, heightOffset + 8,
                            barWidth, partialTick, config.barColors
                    );
                    HealthBarRenderHelper.renderHealthBarDecorations(renderType, guiGraphics, 0, heightOffset + 8, minecraft.font,
                            healthTracker, barWidth, !config.renderBackground, renderBackground
                    );
                    guiGraphics.flush();

                    poseStack.popPose();
                }

                return config.renderNameComponent ? EventResult.DENY : EventResult.PASS;
            }
        }

        return EventResult.PASS;
    }

    private static float getPlaqueScale(LivingEntity targetEntity, EntityRenderDispatcher dispatcher, Player player) {
        float plaqueScale = (float) ImmersiveDamageIndicators.CONFIG.get(ClientConfig.class).level.plaqueScale;
        if (ImmersiveDamageIndicators.CONFIG.get(ClientConfig.class).level.scaleWithDistance) {
            double distanceSqr = dispatcher.distanceToSqr(targetEntity);
            double entityInteractionRange = player.entityInteractionRange();
            double scaleRatio = Mth.clamp((distanceSqr - Math.pow(entityInteractionRange / 2.0, 2.0)) /
                    (Math.pow(entityInteractionRange * 2.0, 2.0) / 2.0), 0.0, 2.0);
            plaqueScale *= (float) (1.0 + scaleRatio);
        }

        return plaqueScale;
    }
}
