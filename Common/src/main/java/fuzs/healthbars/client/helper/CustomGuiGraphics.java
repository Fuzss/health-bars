package fuzs.healthbars.client.helper;

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

public class CustomGuiGraphics extends GuiGraphics {
    @Nullable
    private Function<ResourceLocation, RenderType> renderType;
    private int alpha = 0xFF;
    private int packedLight = 15728880;
    private float blitOffset;
    private Font.DisplayMode fontDisplayMode = Font.DisplayMode.NORMAL;

    public CustomGuiGraphics(PoseStack poseStack) {
        this(poseStack.last().pose());
    }

    public CustomGuiGraphics(Matrix4f matrix4f) {
        super(Minecraft.getInstance(), Minecraft.getInstance().renderBuffers().bufferSource());
        this.pose().mulPose(matrix4f);
    }

    public static CustomGuiGraphics createSeeThrough(PoseStack poseStack) {
        CustomGuiGraphics guiGraphics = new CustomGuiGraphics(poseStack);
        guiGraphics.renderType = ModRenderType.ICON_SEE_THROUGH;
        guiGraphics.alpha = 0x20;
        guiGraphics.blitOffset = 0.01F;
        guiGraphics.fontDisplayMode = Font.DisplayMode.SEE_THROUGH;
        return guiGraphics;
    }

    public int getPackedLight() {
        return this.packedLight;
    }

    public CustomGuiGraphics setRenderType(Function<ResourceLocation, RenderType> renderType) {
        this.renderType = renderType;
        return this;
    }

    public CustomGuiGraphics setAlpha(float alpha) {
        this.alpha = FastColor.as8BitChannel(alpha);
        return this;
    }

    public CustomGuiGraphics setPackedLight(int packedLight) {
        this.packedLight = packedLight;
        return this;
    }

    public CustomGuiGraphics setBlitOffset(float blitOffset) {
        this.blitOffset = blitOffset;
        return this;
    }

    public CustomGuiGraphics addBlitOffset(float blitOffset) {
        this.blitOffset += blitOffset;
        return this;
    }

    public CustomGuiGraphics setFontDisplayMode(Font.DisplayMode fontDisplayMode) {
        this.fontDisplayMode = fontDisplayMode;
        return this;
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
        VertexConsumer bufferBuilder = ((MultiBufferSource) this.bufferSource()).getBuffer(this.renderType.apply(atlasLocation));
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
        VertexConsumer bufferBuilder = ((MultiBufferSource) this.bufferSource()).getBuffer(this.renderType.apply(atlasLocation));
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
