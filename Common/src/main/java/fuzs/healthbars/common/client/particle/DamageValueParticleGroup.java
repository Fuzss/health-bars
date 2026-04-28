package fuzs.healthbars.common.client.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import fuzs.healthbars.common.HealthBars;
import fuzs.healthbars.common.client.gui.GraphicsLayer;
import fuzs.healthbars.common.client.handler.GuiRenderingHandler;
import fuzs.healthbars.common.config.ClientConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.ParticleGroupRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class DamageValueParticleGroup extends ParticleGroup<DamageValueParticle> {
    public static final ParticleRenderType GROUP = new ParticleRenderType(HealthBars.id("damage_values").toString());

    public DamageValueParticleGroup(ParticleEngine particleEngine) {
        super(particleEngine);
    }

    @Override
    public ParticleGroupRenderState extractRenderState(Frustum frustum, Camera camera, float partialTick) {
        return new GroupRenderState(this.particles.stream().map((DamageValueParticle particle) -> {
            return ParticleRenderState.fromParticle(particle, camera, partialTick);
        }).toList());
    }

    record ParticleRenderState(int damageValue, Vec3 position) {

        public static ParticleRenderState fromParticle(DamageValueParticle particle, Camera camera, float partialTick) {
            double x = Mth.lerp(partialTick, particle.xo, particle.x);
            double y = Mth.lerp(partialTick, particle.yo, particle.y);
            double z = Mth.lerp(partialTick, particle.zo, particle.z);
            return new ParticleRenderState(particle.damageValue, new Vec3(x, y, z).subtract(camera.position()));
        }
    }

    record GroupRenderState(List<ParticleRenderState> states) implements ParticleGroupRenderState {

        @Override
        public void submit(SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
            PoseStack poseStack = new PoseStack();
            for (ParticleRenderState renderState : this.states) {
                poseStack.pushPose();
                poseStack.translate(renderState.position().x(), renderState.position().y(), renderState.position().z());
                poseStack.mulPose(cameraRenderState.orientation);
                poseStack.scale(0.025F, -0.025F, 0.025F);
                ClientConfig.DamageValues damageValues = HealthBars.CONFIG.get(ClientConfig.class).level.damageValues;
                GraphicsLayer graphicsLayer = new GraphicsLayer.Level(poseStack, submitNodeCollector);
                GuiRenderingHandler.submitDamageScore(graphicsLayer,
                        Minecraft.getInstance().font,
                        renderState.damageValue(),
                        0,
                        0,
                        GraphicsLayer.PACKED_LIGHT,
                        damageValues);
                poseStack.popPose();
            }
        }
    }
}
