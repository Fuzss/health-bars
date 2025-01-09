package fuzs.healthbars.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

import java.util.function.Function;

public final class LevelGraphics extends GuiGraphics {
    private final int packedLight;

    public LevelGraphics(PoseStack poseStack, int packedLight) {
        super(Minecraft.getInstance(), Minecraft.getInstance().renderBuffers().bufferSource());
        this.packedLight = packedLight;
        this.pose().mulPose(poseStack.last().pose());
    }

    @Override
    protected void innerBlit(Function<ResourceLocation, RenderType> renderTypeGetter, ResourceLocation atlasLocation, int x1, int x2, int y1, int y2, float minU, float maxU, float minV, float maxV, int color) {
        RenderType renderType = renderTypeGetter.apply(atlasLocation);
        Matrix4f matrix4f = this.pose().last().pose();
        this.drawSpecial((MultiBufferSource bufferSource) -> {
            VertexConsumer vertexConsumer = bufferSource.getBuffer(renderType);
            vertexConsumer.addVertex(matrix4f, x1, y1, 0.0F)
                    .setUv(minU, minV)
                    .setColor(color)
                    .setLight(this.packedLight);
            vertexConsumer.addVertex(matrix4f, x1, y2, 0.0F)
                    .setUv(minU, maxV)
                    .setColor(color)
                    .setLight(this.packedLight);
            vertexConsumer.addVertex(matrix4f, x2, y2, 0.0F)
                    .setUv(maxU, maxV)
                    .setColor(color)
                    .setLight(this.packedLight);
            vertexConsumer.addVertex(matrix4f, x2, y1, 0.0F)
                    .setUv(maxU, minV)
                    .setColor(color)
                    .setLight(this.packedLight);
        });
    }
}
