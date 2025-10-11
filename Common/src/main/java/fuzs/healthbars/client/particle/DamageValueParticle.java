package fuzs.healthbars.client.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import fuzs.healthbars.HealthBars;
import fuzs.healthbars.client.gui.GraphicsComponent;
import fuzs.healthbars.client.handler.GuiRenderingHandler;
import fuzs.healthbars.config.ClientConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

public class DamageValueParticle extends Particle {
    private int damageValue;

    public DamageValueParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        super(level, x, y, z);
        this.setParticleSpeed(xSpeed, ySpeed, zSpeed);
        this.hasPhysics = false;
        this.gravity = 0.25F;
        this.lifetime = 50;
    }

    public void setDamageValue(int damageValue) {
        this.damageValue = damageValue;
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
        // NO-OP
    }

    @Override
    public void renderCustom(PoseStack poseStack, MultiBufferSource bufferSource, Camera camera, float partialTick) {
        this.renderRotatedQuad(poseStack, bufferSource, camera, camera.rotation(), partialTick);
    }

    protected void renderRotatedQuad(PoseStack poseStack, MultiBufferSource bufferSource, Camera camera, Quaternionf quaternion, float partialTick) {
        Vec3 vec3 = camera.getPosition();
        float x = (float) (Mth.lerp(partialTick, this.xo, this.x) - vec3.x());
        float y = (float) (Mth.lerp(partialTick, this.yo, this.y) - vec3.y());
        float z = (float) (Mth.lerp(partialTick, this.zo, this.z) - vec3.z());
        this.renderRotatedQuad(poseStack, bufferSource, quaternion, x, y, z);
    }

    protected void renderRotatedQuad(PoseStack poseStack, MultiBufferSource bufferSource, Quaternionf quaternion, float x, float y, float z) {
        Font font = Minecraft.getInstance().font;
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.mulPose(quaternion);
        poseStack.scale(0.025F, -0.025F, 0.025F);
        ClientConfig.DamageValues damageValues = HealthBars.CONFIG.get(ClientConfig.class).level.damageValues;
        GraphicsComponent graphicsComponent = new GraphicsComponent.Level(poseStack, bufferSource);
        GuiRenderingHandler.drawDamageNumber(graphicsComponent,
                font,
                this.damageValue,
                0,
                0,
                GraphicsComponent.PACKED_LIGHT,
                damageValues);
        poseStack.popPose();
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.CUSTOM;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new DamageValueParticle(level, x, y, z, xSpeed, ySpeed, zSpeed);
        }
    }
}
