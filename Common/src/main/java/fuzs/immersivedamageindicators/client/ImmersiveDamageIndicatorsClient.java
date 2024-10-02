package fuzs.immersivedamageindicators.client;

import com.mojang.blaze3d.vertex.PoseStack;
import fuzs.immersivedamageindicators.client.handler.GuiRenderingHandler;
import fuzs.immersivedamageindicators.client.handler.InLevelRenderingHandler;
import fuzs.immersivedamageindicators.client.handler.PickEntityHandler;
import fuzs.immersivedamageindicators.client.helper.HealthTracker;
import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import fuzs.puzzleslib.api.client.event.v1.ClientTickEvents;
import fuzs.puzzleslib.api.client.event.v1.gui.RenderGuiEvents;
import fuzs.puzzleslib.api.client.event.v1.renderer.GameRenderEvents;
import fuzs.puzzleslib.api.client.event.v1.renderer.RenderLevelEvents;
import fuzs.puzzleslib.api.client.event.v1.renderer.RenderNameTagCallback;
import fuzs.puzzleslib.api.event.v1.entity.EntityTickEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.entity.LivingEntity;
import net.torocraft.torohealth.bars.BarParticle;
import net.torocraft.torohealth.bars.ParticleRenderer;
import org.joml.Matrix4f;

public class ImmersiveDamageIndicatorsClient implements ClientModConstructor {

    @Override
    public void onConstructMod() {
        registerEventHandlers();
    }

    private static void registerEventHandlers() {
        GameRenderEvents.BEFORE.register(PickEntityHandler::onBeforeGameRender);
        ClientTickEvents.START.register(PickEntityHandler::onStartClientTick);
        RenderNameTagCallback.EVENT.register(InLevelRenderingHandler::onRenderNameTag);
        RenderGuiEvents.AFTER.register(GuiRenderingHandler::onAfterRenderGui);
        EntityTickEvents.END.register(entity -> {
            if (entity.level().isClientSide && entity instanceof LivingEntity livingEntity) {
                HealthTracker healthTracker = HealthTracker.getHealthTracker(livingEntity, true);
                healthTracker.tick(livingEntity);
                int healthDelta = healthTracker.getLastHealthDelta();
                if (healthDelta != 0) {
                    ParticleRenderer.PARTICLES.add(new BarParticle(livingEntity, healthDelta));
                }
            }
        });
        // TODO remove these
        ClientTickEvents.END.register((Minecraft minecraft) -> {
            if (minecraft.level != null && !minecraft.isPaused()) {
                ParticleRenderer.tick();
            }
        });
        RenderLevelEvents.AFTER_TRANSLUCENT.register(
                (LevelRenderer levelRenderer, Camera camera, GameRenderer gameRenderer, DeltaTracker deltaTracker, PoseStack poseStack, Matrix4f projectionMatrix, Frustum frustum, ClientLevel level) -> {
                    ParticleRenderer.renderParticles(poseStack, camera);
                });
    }
}
