package fuzs.healthbars.common.client.handler;

import com.mojang.blaze3d.vertex.PoseStack;
import fuzs.healthbars.common.HealthBars;
import fuzs.healthbars.common.client.gui.GraphicsLayer;
import fuzs.healthbars.common.client.helper.EntityVisibilityHelper;
import fuzs.healthbars.common.client.helper.HealthBarRenderHelper;
import fuzs.healthbars.common.client.renderer.entity.state.HealthTrackerRenderState;
import fuzs.healthbars.common.config.ClientConfig;
import fuzs.healthbars.common.world.entity.HealthTracker;
import fuzs.puzzleslib.common.api.client.renderer.v1.RenderStateExtraData;
import fuzs.puzzleslib.common.api.event.v1.core.EventResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;
import java.util.Optional;

public class InLevelRenderingHandler {
    private static final ContextKey<Optional<HealthTrackerRenderState>> HEALTH_TRACKER_PROPERTY = new ContextKey<>(
            HealthBars.id("health_tracker"));

    private static boolean isRenderingInGui;

    public static void setIsRenderingInGui(boolean isRenderingInGui) {
        InLevelRenderingHandler.isRenderingInGui = isRenderingInGui;
    }

    public static void onExtractEntityRenderState(Entity entity, EntityRenderState entityRenderState, float partialTick) {
        if (entity instanceof LivingEntity livingEntity && canBarRender(livingEntity, partialTick)) {
            HealthTracker healthTracker = HealthTracker.getHealthTracker(livingEntity, false);
            if (healthTracker != null) {
                HealthTrackerRenderState renderState = HealthTrackerRenderState.extractRenderState(healthTracker,
                        livingEntity,
                        partialTick,
                        HealthBars.CONFIG.get(ClientConfig.class).level);
                RenderStateExtraData.set(entityRenderState, HEALTH_TRACKER_PROPERTY, Optional.of(renderState));
                if (entityRenderState.nameTag == null) {
                    // we must force the name tag to render, as the name tag render event does not run unless this is set
                    entityRenderState.nameTag = CommonComponents.EMPTY;
                    entityRenderState.nameTagAttachment = entity.getAttachments()
                            .getNullable(EntityAttachment.NAME_TAG, 0, entity.getViewYRot(partialTick));
                }
            }
        }
    }

    private static boolean canBarRender(LivingEntity livingEntity, float partialTick) {
        if (!HealthBars.CONFIG.get(ClientConfig.class).anyRendering.get()
                || !HealthBars.CONFIG.get(ClientConfig.class).levelRendering || isRenderingInGui) {
            return false;
        } else if (livingEntity.isAlive() && HealthBars.CONFIG.get(ClientConfig.class).isEntityAllowed(livingEntity)) {
            Minecraft minecraft = Minecraft.getInstance();
            Vec3 nameTagAttachment = livingEntity.getAttachments()
                    .getNullable(EntityAttachment.NAME_TAG, 0, livingEntity.getViewYRot(partialTick));
            // other mods might be rendering this mob without a level in some menu, so the camera is null then
            if (nameTagAttachment != null && minecraft.getEntityRenderDispatcher().camera != null) {
                return EntityVisibilityHelper.isEntityVisible(livingEntity,
                        partialTick,
                        HealthBars.CONFIG.get(ClientConfig.class).level.pickedEntity);
            }
        }

        return false;
    }

    public static EventResult onSubmitNameTag(EntityRenderer<?, ?> entityRenderer, EntityRenderState entityRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        Component component = entityRenderState.nameTag;
        Optional<HealthTrackerRenderState> optional = RenderStateExtraData.getOrDefault(entityRenderState,
                HEALTH_TRACKER_PROPERTY,
                Optional.empty());
        if (component != null && optional.isPresent()) {
            poseStack.pushPose();
            Vec3 vec3 = entityRenderState.nameTagAttachment;
            if (vec3 != null) {
                poseStack.translate(vec3.x, vec3.y + 0.5, vec3.z);
            }

            poseStack.mulPose(cameraRenderState.orientation);
            float renderScale = getRenderScale(entityRenderState.distanceToCameraSq);
            poseStack.scale(0.025F * renderScale, -0.025F * renderScale, 0.025F * renderScale);

            HealthTrackerRenderState renderState = optional.get();
            ClientConfig.Level config = HealthBars.CONFIG.get(ClientConfig.class).level;
            int posY = config.heightOffset;
            if (Objects.equals("deadmau5", component.getString())) {
                posY -= 10;
            }

            if (!config.renderTitleComponent && component != CommonComponents.EMPTY) {
                posY -= 13;
            }

            int lightCoords = config.fullBrightness ? GraphicsLayer.PACKED_LIGHT : entityRenderState.lightCoords;
            GraphicsLayer graphicsLayer = new GraphicsLayer.Level(poseStack, submitNodeCollector);
            if (config.behindWalls) {
                submitHealthBar(graphicsLayer,
                        0,
                        posY,
                        renderState,
                        ARGB.white(0.125F),
                        Font.DisplayMode.SEE_THROUGH,
                        renderState.backgroundColor,
                        lightCoords,
                        entityRenderState.outlineColor);
            }

            submitHealthBar(graphicsLayer,
                    0,
                    posY,
                    renderState,
                    -1,
                    Font.DisplayMode.NORMAL,
                    config.behindWalls ? 0 : renderState.backgroundColor,
                    lightCoords,
                    entityRenderState.outlineColor);
            poseStack.popPose();
            // when the component is empty, rendering has been forced by us and vanilla should not be allowed to proceed
            if (config.renderTitleComponent || component == CommonComponents.EMPTY) {
                return EventResult.INTERRUPT;
            }
        }

        return EventResult.PASS;
    }

    private static float getRenderScale(double distanceToCameraSq) {
        float renderScale = (float) HealthBars.CONFIG.get(ClientConfig.class).level.renderScale;
        if (HealthBars.CONFIG.get(ClientConfig.class).level.scaleWithDistance) {
            double entityInteractionRange = Minecraft.getInstance().player.entityInteractionRange();
            double scaleRatio = Mth.clamp((distanceToCameraSq - Math.pow(entityInteractionRange / 2.0, 2.0)) / (
                    Math.pow(entityInteractionRange * 2.0, 2.0) / 2.0), 0.0, 2.0);
            renderScale *= (float) (1.0 + scaleRatio);
        }

        return renderScale;
    }

    private static void submitHealthBar(GraphicsLayer graphicsLayer, int posX, int posY, HealthTrackerRenderState renderState, int color, Font.DisplayMode displayMode, int backgroundColor, int lightCoords, int outlineColor) {
        HealthBarRenderHelper.submitHealthBar(graphicsLayer,
                displayMode == Font.DisplayMode.SEE_THROUGH ? RenderTypes::textSeeThrough : RenderTypes::text,
                posX,
                posY,
                renderState,
                color,
                lightCoords);
        HealthBarRenderHelper.submitHealthBarDecorations(graphicsLayer,
                posX,
                posY,
                Minecraft.getInstance().font,
                renderState,
                color,
                displayMode,
                backgroundColor,
                lightCoords,
                outlineColor);
    }
}
