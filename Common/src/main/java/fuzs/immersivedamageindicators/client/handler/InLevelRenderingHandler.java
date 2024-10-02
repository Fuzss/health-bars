package fuzs.immersivedamageindicators.client.handler;

import com.mojang.blaze3d.vertex.PoseStack;
import fuzs.immersivedamageindicators.ImmersiveDamageIndicators;
import fuzs.immersivedamageindicators.client.helper.EntityVisibilityHelper;
import fuzs.immersivedamageindicators.client.helper.HealthBarHelper;
import fuzs.immersivedamageindicators.client.helper.HealthTracker;
import fuzs.immersivedamageindicators.config.ClientConfig;
import fuzs.puzzleslib.api.event.v1.core.EventResult;
import fuzs.puzzleslib.api.event.v1.data.DefaultedValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.torocraft.torohealth.bars.HealthBarRenderer;

public class InLevelRenderingHandler {
    private static boolean isRenderingInInventory;

    public static void setIsRenderingInInventory(boolean isRenderingInInventory) {
        InLevelRenderingHandler.isRenderingInInventory = isRenderingInInventory;
    }

    @SuppressWarnings("ConstantValue")
    public static EventResult onRenderNameTag(Entity entity, DefaultedValue<Component> content, EntityRenderer<?> entityRenderer, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, float partialTick) {
        if (!isRenderingInInventory && entity instanceof LivingEntity livingEntity) {
            Minecraft minecraft = Minecraft.getInstance();
            EntityRenderDispatcher dispatcher = entityRenderer.entityRenderDispatcher;
            Vec3 vec3 = entity.getAttachments().getNullable(EntityAttachment.NAME_TAG, 0,
                    entity.getViewYRot(partialTick)
            );
            // other mods might be rendering this mob without a level in some menu, so camera is null then
            if (vec3 != null && dispatcher.camera != null && dispatcher.camera.getEntity() instanceof LivingEntity &&
                    EntityVisibilityHelper.isEntityVisible(minecraft.level, livingEntity, minecraft.player, partialTick,
                            dispatcher, false
                    )) {
                poseStack.pushPose();
                poseStack.translate(vec3.x, vec3.y + 0.5, vec3.z);
                poseStack.mulPose(dispatcher.cameraOrientation());
                float plaqueScale = getPlaqueScale(livingEntity, dispatcher, minecraft.player);
                // x and z are flipped as of 1.21
                poseStack.scale(0.025F * plaqueScale, -0.025F * plaqueScale, -0.025F * plaqueScale);
                int heightOffset = computeHeightOffset(livingEntity, content.get(), plaqueScale, minecraft.font);
//                renderAllPlaques(livingEntity, poseStack, multiBufferSource, packedLight, heightOffset, minecraft.font);

                HealthTracker healthTracker = HealthTracker.getHealthTracker(livingEntity, false);
                if (healthTracker != null) {
                    HealthBarRenderer.render(healthTracker, poseStack, livingEntity, 0, 0, HealthBarHelper.getBarWidthByScale(2), true);
                }

                poseStack.popPose();
            }
        }

        return EventResult.PASS;
    }

    private static float getPlaqueScale(LivingEntity targetEntity, EntityRenderDispatcher dispatcher, Player player) {
        float plaqueScale = (float) ImmersiveDamageIndicators.CONFIG.get(ClientConfig.class).plaqueScale;
        if (ImmersiveDamageIndicators.CONFIG.get(ClientConfig.class).scaleWithDistance) {
            double distanceSqr = dispatcher.distanceToSqr(targetEntity);
            double entityInteractionRange = player.entityInteractionRange();
            double scaleRatio = Mth.clamp(
                    (distanceSqr - Math.pow(entityInteractionRange / 2.0, 2.0)) / (Math.pow(entityInteractionRange * 2.0, 2.0) / 2.0), 0.0, 2.0);
            plaqueScale *= (float) (1.0 + scaleRatio);
        }

        return plaqueScale;
    }

    private static int computeHeightOffset(LivingEntity livingEntity, Component nameComponent, float plaqueScale, Font font) {
        int heightOffset = "deadmau5".equals(nameComponent.getString()) ? -13 : -3;
//        heightOffset -= (int) (MobPlaques.CONFIG.get(ClientConfig.class).heightOffset * (0.5F / plaqueScale));
//        if (MobPlaques.CONFIG.get(ClientConfig.class).renderBelowNameTag) {
//            heightOffset += (int) (23 * (0.5F / plaqueScale));
//        } else {
//            int plaquesHeight = getPlaquesHeight(font, livingEntity);
//            heightOffset -= (int) ((plaquesHeight + PLAQUE_VERTICAL_DISTANCE) * (0.5F / plaqueScale));
//        }
        return heightOffset;
    }
}
