package fuzs.healthbars.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import fuzs.healthbars.client.renderer.ModRenderType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.Objects;
import java.util.function.Function;

/**
 * A hacky extension to {@link GuiGraphics} that allows setting a custom render type and a few other properties.
 */
public class CustomGuiGraphics extends GuiGraphics {
    private final Function<ResourceLocation, RenderType> renderType;
    private final int alpha;
    private final int packedLight;
    private final float blitOffset;
    private final Font.DisplayMode fontDisplayMode;

    public CustomGuiGraphics(PoseStack poseStack, Function<ResourceLocation, RenderType> renderType, int alpha, int packedLight, float blitOffset, Font.DisplayMode fontDisplayMode) {
        this(poseStack.last().pose(), renderType, alpha, packedLight, blitOffset, fontDisplayMode);
    }

    public CustomGuiGraphics(Matrix4f matrix4f, Function<ResourceLocation, RenderType> renderType, int alpha, int packedLight, float blitOffset, Font.DisplayMode fontDisplayMode) {
        super(Minecraft.getInstance(), Minecraft.getInstance().renderBuffers().bufferSource());
        this.renderType = renderType;
        this.alpha = alpha;
        this.packedLight = packedLight;
        this.blitOffset = blitOffset;
        this.fontDisplayMode = fontDisplayMode;
        this.pose().mulPose(matrix4f);
    }

    public static CustomGuiGraphics createSeeThrough(PoseStack poseStack, int packedLight) {
        return new CustomGuiGraphics(poseStack, ModRenderType.ICON_SEE_THROUGH, 0x20, packedLight, 0.01F,
                Font.DisplayMode.SEE_THROUGH
        );
    }

    public static CustomGuiGraphics create(PoseStack poseStack, int packedLight) {
        return new CustomGuiGraphics(poseStack, ModRenderType.ICON, 0xFF, packedLight, 0.0F, Font.DisplayMode.NORMAL);
    }

    @Override
    public int drawString(Font font, @Nullable String text, int x, int y, int color, boolean dropShadow) {
        if (text == null) {
            return 0;
        } else {
            int stringWidth = font.drawInBatch(text, x, y, FastColor.ARGB32.color(this.alpha, color), dropShadow,
                    this.pose().last().pose(), this.bufferSource(), this.fontDisplayMode, 0, this.packedLight,
                    font.isBidirectional()
            );
            this.flush();
            return stringWidth;
        }
    }

    @Override
    public int drawString(Font font, FormattedCharSequence text, int x, int y, int color, boolean dropShadow) {
        int stringWidth = font.drawInBatch(text, x, y, FastColor.ARGB32.color(this.alpha, color), dropShadow,
                this.pose().last().pose(), this.bufferSource(), this.fontDisplayMode, 0, this.packedLight
        );
        this.flush();
        return stringWidth;
    }

    @Override
    protected void innerBlit(ResourceLocation atlasLocation, int x1, int x2, int y1, int y2, int blitOffset, float minU, float maxU, float minV, float maxV) {
        Objects.requireNonNull(this.renderType, "render type is null");
        RenderSystem.enableBlend();
        Matrix4f matrix4f = this.pose().last().pose();
        VertexConsumer bufferBuilder = ((MultiBufferSource) this.bufferSource()).getBuffer(
                this.renderType.apply(atlasLocation));
        bufferBuilder.addVertex(matrix4f, x1, y1, this.blitOffset)
                .setColor(FastColor.ARGB32.color(this.alpha, -1))
                .setUv(minU, minV)
                .setLight(this.packedLight);
        bufferBuilder.addVertex(matrix4f, x1, y2, this.blitOffset)
                .setColor(FastColor.ARGB32.color(this.alpha, -1))
                .setUv(minU, maxV)
                .setLight(this.packedLight);
        bufferBuilder.addVertex(matrix4f, x2, y2, this.blitOffset)
                .setColor(FastColor.ARGB32.color(this.alpha, -1))
                .setUv(maxU, maxV)
                .setLight(this.packedLight);
        bufferBuilder.addVertex(matrix4f, x2, y1, this.blitOffset)
                .setColor(FastColor.ARGB32.color(this.alpha, -1))
                .setUv(maxU, minV)
                .setLight(this.packedLight);
        RenderSystem.disableBlend();
    }

    @Override
    protected void innerBlit(ResourceLocation atlasLocation, int x1, int x2, int y1, int y2, int blitOffset, float minU, float maxU, float minV, float maxV, float red, float green, float blue, float alpha) {
        Objects.requireNonNull(this.renderType, "render type is null");
        RenderSystem.enableBlend();
        Matrix4f matrix4f = this.pose().last().pose();
        VertexConsumer bufferBuilder = ((MultiBufferSource) this.bufferSource()).getBuffer(
                this.renderType.apply(atlasLocation));
        bufferBuilder.addVertex(matrix4f, x1, y1, this.blitOffset).setUv(minU, minV).setColor(red, green, blue,
                this.alpha / 255.0F
        ).setLight(this.packedLight);
        bufferBuilder.addVertex(matrix4f, x1, y2, this.blitOffset).setUv(minU, maxV).setColor(red, green, blue,
                this.alpha / 255.0F
        ).setLight(this.packedLight);
        bufferBuilder.addVertex(matrix4f, x2, y2, this.blitOffset).setUv(maxU, maxV).setColor(red, green, blue,
                this.alpha / 255.0F
        ).setLight(this.packedLight);
        bufferBuilder.addVertex(matrix4f, x2, y1, this.blitOffset).setUv(maxU, minV).setColor(red, green, blue,
                this.alpha / 255.0F
        ).setLight(this.packedLight);
        RenderSystem.disableBlend();
    }
}
