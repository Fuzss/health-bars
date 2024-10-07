package fuzs.healthbars.client.handler;

import com.mojang.blaze3d.vertex.PoseStack;
import fuzs.healthbars.HealthBars;
import fuzs.healthbars.client.gui.CustomGuiGraphics;
import fuzs.healthbars.client.helper.EntityVisibilityHelper;
import fuzs.healthbars.client.helper.HealthBarHelper;
import fuzs.healthbars.client.helper.HealthBarRenderHelper;
import fuzs.healthbars.client.helper.HealthTracker;
import fuzs.healthbars.config.ClientConfig;
import fuzs.puzzleslib.api.event.v1.core.EventResult;
import fuzs.puzzleslib.api.event.v1.data.DefaultedValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
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
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

public class InLevelRenderingHandler {
    private static boolean isRenderingInInventory;

    public static void setIsRenderingInInventory(boolean isRenderingInInventory) {
        InLevelRenderingHandler.isRenderingInInventory = isRenderingInInventory;
    }

    @SuppressWarnings("ConstantValue")
    public static EventResult onRenderNameTag(Entity entity, DefaultedValue<Component> content, EntityRenderer<?> entityRenderer, PoseStack poseStack, int packedLight, float partialTick) {

        if (!HealthBars.CONFIG.get(ClientConfig.class).anyRendering.get() || !HealthBars.CONFIG.get(
                ClientConfig.class).levelRendering) {
            return EventResult.PASS;
        }

        if (isRenderingInInventory) {

            return EventResult.DENY;
        } else if (entity instanceof LivingEntity livingEntity && livingEntity.isAlive() && HealthBars.CONFIG.get(
                ClientConfig.class).isEntityAllowed(livingEntity)) {

            HealthTracker healthTracker = HealthTracker.getHealthTracker(livingEntity, false);
            if (healthTracker != null) {

                ClientConfig.Level config = HealthBars.CONFIG.get(ClientConfig.class).level;
                Minecraft minecraft = Minecraft.getInstance();
                EntityRenderDispatcher dispatcher = entityRenderer.entityRenderDispatcher;
                Vec3 vec3 = entity.getAttachments().getNullable(EntityAttachment.NAME_TAG, 0,
                        entity.getViewYRot(partialTick)
                );
                // other mods might be rendering this mob without a level in some menu, so camera is null then
                if (vec3 != null && dispatcher.camera != null &&
                        dispatcher.camera.getEntity() instanceof LivingEntity) {
                    if (EntityVisibilityHelper.isEntityVisible(minecraft.level, livingEntity, minecraft.player,
                            partialTick, dispatcher, config.pickedEntity
                    )) {

                        poseStack.pushPose();

                        poseStack.translate(vec3.x, vec3.y + 0.5, vec3.z);
                        poseStack.mulPose(dispatcher.cameraOrientation());
                        float renderScale = getRenderScale(livingEntity, dispatcher, minecraft.player);
                        // x and z are flipped as of 1.21
                        poseStack.scale(0.025F * renderScale, -0.025F * renderScale, 0.025F * renderScale);

                        int heightOffset = "deadmau5".equals(content.get().getString()) ? -13 : -3;
                        if (!config.renderTitleComponent && ((EntityRenderer<Entity>) entityRenderer).shouldShowName(
                                entity)) {
                            heightOffset -= 13;
                        }
                        heightOffset += config.offsetHeight;

                        if (config.behindWalls) {
                            renderHealthBar(poseStack, partialTick, config.fullBrightness ? 15728880 : packedLight,
                                    healthTracker, livingEntity, heightOffset, minecraft.font,
                                    CustomGuiGraphics::createSeeThrough, RenderType.textBackgroundSeeThrough()
                            );
                        }

                        renderHealthBar(poseStack, partialTick, config.fullBrightness ? 15728880 : packedLight,
                                healthTracker, livingEntity, heightOffset, minecraft.font, CustomGuiGraphics::create,
                                !config.behindWalls ? RenderType.textBackground() : null
                        );

                        poseStack.popPose();
                    }
                }

                return config.renderTitleComponent ? EventResult.DENY : EventResult.PASS;
            }
        }

        return EventResult.PASS;
    }

    private static void renderHealthBar(PoseStack poseStack, float partialTick, int packedLight, HealthTracker healthTracker, LivingEntity livingEntity, int heightOffset, Font font, BiFunction<PoseStack, Integer, GuiGraphics> factory, @Nullable RenderType renderType) {
        ClientConfig.Level config = HealthBars.CONFIG.get(ClientConfig.class).level;
        int barWidth = HealthBarHelper.getBarWidth(config, healthTracker);
        GuiGraphics guiGraphics = factory.apply(poseStack, packedLight);
        HealthBarRenderHelper.renderHealthBar(guiGraphics, 0, heightOffset + 8, partialTick, healthTracker,
                livingEntity, barWidth, config.barColors
        );
        HealthBarRenderHelper.renderHealthBarDecorations(guiGraphics, 0, heightOffset + 8, font,
                config.renderBackground ? renderType : null, healthTracker, barWidth, !config.renderBackground,
                packedLight
        );
    }

    private static float getRenderScale(LivingEntity targetEntity, EntityRenderDispatcher dispatcher, Player player) {
        float renderScale = (float) HealthBars.CONFIG.get(ClientConfig.class).level.renderScale;
        if (HealthBars.CONFIG.get(ClientConfig.class).level.scaleWithDistance) {
            double distanceToEntity = dispatcher.distanceToSqr(targetEntity);
            double entityInteractionRange = player.entityInteractionRange();
            double scaleRatio = Mth.clamp((distanceToEntity - Math.pow(entityInteractionRange / 2.0, 2.0)) /
                    (Math.pow(entityInteractionRange * 2.0, 2.0) / 2.0), 0.0, 2.0);
            renderScale *= (float) (1.0 + scaleRatio);
        }

        return renderScale;
    }
}
