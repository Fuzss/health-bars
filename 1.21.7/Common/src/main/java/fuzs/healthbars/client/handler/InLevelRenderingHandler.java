package fuzs.healthbars.client.handler;

import com.mojang.blaze3d.vertex.PoseStack;
import fuzs.healthbars.HealthBars;
import fuzs.healthbars.client.gui.LevelGraphics;
import fuzs.healthbars.client.helper.*;
import fuzs.healthbars.client.renderer.ModRenderType;
import fuzs.healthbars.config.ClientConfig;
import fuzs.puzzleslib.api.client.renderer.v1.RenderPropertyKey;
import fuzs.puzzleslib.api.core.v1.utility.ResourceLocationHelper;
import fuzs.puzzleslib.api.event.v1.core.EventResult;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.function.Function;

public class InLevelRenderingHandler {
    public static final ResourceLocation GUI_SHEET = ResourceLocationHelper.withDefaultNamespace(
            "textures/atlas/gui.png");
    private static final RenderPropertyKey<HealthTrackerRenderState> HEALTH_TRACKER_PROPERTY = new RenderPropertyKey<>(
            HealthBars.id("health_tracker"));

    private static boolean isRenderingInGui;

    public static void setIsRenderingInGui(boolean isRenderingInGui) {
        InLevelRenderingHandler.isRenderingInGui = isRenderingInGui;
    }

    public static void onExtractRenderState(Entity entity, EntityRenderState renderState, float partialTick) {
        if (entity instanceof LivingEntity livingEntity && canBarRender(livingEntity, partialTick)) {
            HealthTracker healthTracker = HealthTracker.getHealthTracker(livingEntity, false);
            if (healthTracker != null) {
                RenderPropertyKey.set(renderState,
                        HEALTH_TRACKER_PROPERTY,
                        HealthTrackerRenderState.extractRenderState(healthTracker,
                                livingEntity,
                                partialTick,
                                HealthBars.CONFIG.get(ClientConfig.class).level.barColors));
                if (renderState.nameTag == null) {
                    // we must force the name tag to render, as the name tag render event does not run unless this is set
                    renderState.nameTag = CommonComponents.EMPTY;
                    renderState.nameTagAttachment = entity.getAttachments()
                            .getNullable(EntityAttachment.NAME_TAG, 0, entity.getViewYRot(partialTick));
                }
            }
        }
    }

    @SuppressWarnings("ConstantValue")
    private static boolean canBarRender(LivingEntity livingEntity, float partialTick) {
        if (!HealthBars.CONFIG.get(ClientConfig.class).anyRendering.get() ||
                !HealthBars.CONFIG.get(ClientConfig.class).levelRendering || isRenderingInGui) {
            return false;
        } else if (livingEntity.isAlive() && HealthBars.CONFIG.get(ClientConfig.class).isEntityAllowed(livingEntity)) {

            Minecraft minecraft = Minecraft.getInstance();
            Vec3 nameTagAttachment = livingEntity.getAttachments()
                    .getNullable(EntityAttachment.NAME_TAG, 0, livingEntity.getViewYRot(partialTick));
            // other mods might be rendering this mob without a level in some menu, so camera is null then
            if (nameTagAttachment != null && minecraft.getEntityRenderDispatcher().camera != null) {
                return EntityVisibilityHelper.isEntityVisible(minecraft,
                        livingEntity,
                        partialTick,
                        HealthBars.CONFIG.get(ClientConfig.class).level.pickedEntity);
            }
        }

        return false;
    }

    public static EventResult onRenderNameTag(EntityRenderState renderState, Component component, EntityRenderer<?, ?> entityRenderer, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {

        if (RenderPropertyKey.has(renderState, HEALTH_TRACKER_PROPERTY)) {

            ClientConfig.Level config = HealthBars.CONFIG.get(ClientConfig.class).level;
            Minecraft minecraft = Minecraft.getInstance();
            HealthTrackerRenderState healthTracker = RenderPropertyKey.get(renderState, HEALTH_TRACKER_PROPERTY);

            poseStack.pushPose();

            Vec3 vec3 = renderState.nameTagAttachment;
            if (vec3 != null) {
                poseStack.translate(vec3.x, vec3.y + 0.5, vec3.z);
            }
            poseStack.mulPose(entityRenderer.entityRenderDispatcher.cameraOrientation());
            float renderScale = getRenderScale(renderState.distanceToCameraSq, minecraft.player);
            // x and z are flipped as of 1.21
            poseStack.scale(0.025F * renderScale, -0.025F * renderScale, 0.025F * renderScale);

            int heightOffset = "deadmau5".equals(component.getString()) ? -13 : -3;
            if (!config.renderTitleComponent && component != CommonComponents.EMPTY) {
                heightOffset -= 13;
            }
            heightOffset += config.offsetHeight;
            int packedLightForRendering = config.fullBrightness ? 0XF000F0 : packedLight;
            GuiGraphics guiGraphics = new LevelGraphics(poseStack, packedLightForRendering);

            if (config.behindWalls) {
                poseStack.pushPose();
                poseStack.translate(0.0F, 0.0F, 0.01F);
                renderHealthBar(guiGraphics,
                        packedLightForRendering,
                        healthTracker,
                        heightOffset,
                        minecraft.font,
                        ModRenderType::textSeeThrough,
                        RenderType.textBackgroundSeeThrough(),
                        ARGB.white(0.125F),
                        Font.DisplayMode.SEE_THROUGH);
                poseStack.popPose();
            }

            renderHealthBar(guiGraphics,
                    packedLightForRendering,
                    healthTracker,
                    heightOffset,
                    minecraft.font,
                    ModRenderType::text,
                    !config.behindWalls ? ModRenderType.textBackground() : null,
                    -1,
                    Font.DisplayMode.NORMAL);

            poseStack.popPose();

            // when the component is empty rendering has been forced by us and vanilla should not be allowed to proceed
            if (config.renderTitleComponent || component == CommonComponents.EMPTY) {
                return EventResult.INTERRUPT;
            }
        }

        return EventResult.PASS;
    }

    private static float getRenderScale(double distanceToCameraSq, Player player) {
        float renderScale = (float) HealthBars.CONFIG.get(ClientConfig.class).level.renderScale;
        if (HealthBars.CONFIG.get(ClientConfig.class).level.scaleWithDistance) {
            double entityInteractionRange = player.entityInteractionRange();
            double scaleRatio = Mth.clamp((distanceToCameraSq - Math.pow(entityInteractionRange / 2.0, 2.0)) /
                    (Math.pow(entityInteractionRange * 2.0, 2.0) / 2.0), 0.0, 2.0);
            renderScale *= (float) (1.0 + scaleRatio);
        }

        return renderScale;
    }

    private static void renderHealthBar(GuiGraphics guiGraphics, int packedLight, HealthTrackerRenderState renderState, int heightOffset, Font font, Function<ResourceLocation, RenderType> renderTypeGetter, @Nullable RenderType textBackground, int color, Font.DisplayMode fontDisplayMode) {
        ClientConfig.Level config = HealthBars.CONFIG.get(ClientConfig.class).level;
        int barWidth = HealthBarHelper.getBarWidth(config, renderState);
        HealthBarRenderHelper.renderHealthBar(guiGraphics,
                renderTypeGetter,
                0,
                heightOffset + 8,
                renderState,
                barWidth,
                color);
        HealthBarRenderHelper.renderHealthBarDecorations(guiGraphics,
                renderTypeGetter,
                0,
                heightOffset + 8,
                font,
                config.renderBackground ? textBackground : null,
                renderState,
                barWidth,
                color,
                !config.renderBackground,
                fontDisplayMode,
                packedLight);
    }

    public static void onRenderLevelAfterEntities(LevelRenderer levelRenderer, Camera camera, GameRenderer gameRenderer, DeltaTracker deltaTracker, PoseStack poseStack, Matrix4f projectionMatrix, Frustum frustum, ClientLevel level) {
        MultiBufferSource.BufferSource bufferSource = gameRenderer.getMinecraft().renderBuffers().bufferSource();
        // manually call BufferSource::endBatch, otherwise this is called very often and causes extreme lag
        bufferSource.endBatch(ModRenderType.text(GUI_SHEET));
        bufferSource.endBatch(ModRenderType.textSeeThrough(GUI_SHEET));
        bufferSource.endBatch(ModRenderType.textBackground());
        bufferSource.endBatch(ModRenderType.textBackgroundSeeThrough());
    }
}
