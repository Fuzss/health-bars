package fuzs.immersivedamageindicators.client;

import com.mojang.blaze3d.vertex.PoseStack;
import fuzs.immersivedamageindicators.ImmersiveDamageIndicators;
import fuzs.immersivedamageindicators.config.ClientConfig;
import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import fuzs.puzzleslib.api.client.event.v1.ClientTickEvents;
import fuzs.puzzleslib.api.client.event.v1.gui.RenderGuiEvents;
import fuzs.puzzleslib.api.client.event.v1.renderer.RenderLevelEvents;
import fuzs.puzzleslib.api.client.event.v1.renderer.RenderLivingEvents;
import fuzs.puzzleslib.api.event.v1.entity.EntityTickEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.torocraft.torohealth.bars.BarStates;
import net.torocraft.torohealth.bars.HealthBarRenderer;
import net.torocraft.torohealth.bars.HealthTracker;
import net.torocraft.torohealth.bars.ParticleRenderer;
import net.torocraft.torohealth.display.HudRenderer;
import net.torocraft.torohealth.util.RayTraceGetter;
import org.joml.Matrix4f;

public class ImmersiveDamageIndicatorsClient implements ClientModConstructor {
    public static final HudRenderer HUD_RENDERER = new HudRenderer();
    public static final RayTraceGetter RAYTRACE = new RayTraceGetter();

    @Override
    public void onConstructMod() {
        registerEventHandlers();
    }

    private static void registerEventHandlers() {
        RenderGuiEvents.AFTER.register((Gui gui, GuiGraphics guiGraphics, DeltaTracker deltaTracker) -> {
            HUD_RENDERER.draw(gui, guiGraphics);
        });
        ClientTickEvents.END.register((Minecraft minecraft) -> {
            if (minecraft.level != null && !minecraft.isPaused()) {
                int distance = ImmersiveDamageIndicators.CONFIG.get(ClientConfig.class).hud.distance;
                LivingEntity entityInCrosshair = ImmersiveDamageIndicatorsClient.RAYTRACE.getEntityInCrosshair(1.0F,
                        distance
                );
                ImmersiveDamageIndicatorsClient.HUD_RENDERER.setEntity(entityInCrosshair);
//                BarStates.tick();
                ImmersiveDamageIndicatorsClient.HUD_RENDERER.tick();
            }
        });
        RenderLivingEvents.AFTER.register(ImmersiveDamageIndicatorsClient::onAfterRenderEntity);
        RenderLevelEvents.AFTER_TRANSLUCENT.register(
                (LevelRenderer levelRenderer, Camera camera, GameRenderer gameRenderer, DeltaTracker deltaTracker, PoseStack poseStack, Matrix4f projectionMatrix, Frustum frustum, ClientLevel level) -> {
                    HealthBarRenderer.renderInWorld(poseStack, camera);
                    ParticleRenderer.renderParticles(poseStack, camera);
                });
        EntityTickEvents.END.register(entity -> {
            if (entity instanceof LivingEntity livingEntity) {
                HealthTracker.getHealthTracker(livingEntity).tick();
            }
        });
    }

    static <T extends LivingEntity, M extends EntityModel<T>> void onAfterRenderEntity(T entity, LivingEntityRenderer<T, M> renderer, float partialTick, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight) {
        HealthBarRenderer.prepareRenderInWorld(entity);
    }
}
