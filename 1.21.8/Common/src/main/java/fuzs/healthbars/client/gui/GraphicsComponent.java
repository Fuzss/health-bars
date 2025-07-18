package fuzs.healthbars.client.gui;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import fuzs.healthbars.client.handler.InLevelRenderingHandler;
import fuzs.puzzleslib.api.client.gui.v2.GuiGraphicsHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import org.joml.Matrix4f;

import java.util.function.Function;

public interface GraphicsComponent {
    int PACKED_LIGHT = 0XF000F0;

    void translate(float zOffset);

    void fill(RenderType renderType, int minX, int minY, int maxX, int maxY, float zOffset, int color, int packedLight);

    void drawString(Font font, Component text, int x, int y, int color, boolean drawShadow, Font.DisplayMode displayMode, int packedLight);

    void drawString8xOutline(Font font, Component text, int x, int y, int color, int packedLight);

    void blitSprite(Function<ResourceLocation, RenderType> renderTypeGetter, ResourceLocation resourceLocation, int x, int y, int width, int height, int color, int packedLight);

    class Gui implements GraphicsComponent {
        private final GuiGraphics guiGraphics;

        public Gui(GuiGraphics guiGraphics) {
            this.guiGraphics = guiGraphics;
        }

        @Override
        public void translate(float zOffset) {
            // NO-OP
        }

        @Override
        public void fill(RenderType renderType, int minX, int minY, int maxX, int maxY, float zOffset, int color, int packedLight) {
            Preconditions.checkArgument(renderType == null);
            Preconditions.checkArgument(packedLight == PACKED_LIGHT);
            this.guiGraphics.fill(minX, minY, maxX, maxY, color);
        }

        @Override
        public void drawString(Font font, Component text, int x, int y, int color, boolean drawShadow, Font.DisplayMode displayMode, int packedLight) {
            Preconditions.checkArgument(displayMode == Font.DisplayMode.NORMAL);
            Preconditions.checkArgument(packedLight == PACKED_LIGHT);
            this.guiGraphics.drawString(font, text, x, y, color, drawShadow);
        }

        @Override
        public void drawString8xOutline(Font font, Component text, int x, int y, int color, int packedLight) {
            Preconditions.checkArgument(packedLight == PACKED_LIGHT);
            GuiGraphicsHelper.drawInBatch8xOutline(this.guiGraphics, font, text, x, y, color, ARGB.opaque(0));
        }

        @Override
        public void blitSprite(Function<ResourceLocation, RenderType> renderTypeGetter, ResourceLocation resourceLocation, int x, int y, int width, int height, int color, int packedLight) {
            Preconditions.checkArgument(renderTypeGetter == null);
            Preconditions.checkArgument(packedLight == PACKED_LIGHT);
            this.guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, resourceLocation, x, y, width, height, color);
        }
    }

    class Level implements GraphicsComponent {
        private final PoseStack poseStack;
        private final MultiBufferSource bufferSource;

        public Level(PoseStack poseStack, MultiBufferSource bufferSource) {
            this.poseStack = poseStack;
            this.bufferSource = bufferSource;
        }

        @Override
        public void translate(float zOffset) {
            this.poseStack.translate(0.0F, 0.0F, zOffset);
        }

        @Override
        public void fill(RenderType renderType, int minX, int minY, int maxX, int maxY, float zOffset, int color, int packedLight) {
            Matrix4f matrix4f = this.poseStack.last().pose();
            if (minX < maxX) {
                int tmp = minX;
                minX = maxX;
                maxX = tmp;
            }

            if (minY < maxY) {
                int tmp = minY;
                minY = maxY;
                maxY = tmp;
            }

            VertexConsumer bufferBuilder = this.bufferSource.getBuffer(renderType);
            bufferBuilder.addVertex(matrix4f, minX, minY, zOffset).setColor(color).setLight(packedLight);
            bufferBuilder.addVertex(matrix4f, minX, maxY, zOffset).setColor(color).setLight(packedLight);
            bufferBuilder.addVertex(matrix4f, maxX, maxY, zOffset).setColor(color).setLight(packedLight);
            bufferBuilder.addVertex(matrix4f, maxX, minY, zOffset).setColor(color).setLight(packedLight);
        }

        @Override
        public void drawString(Font font, Component text, int x, int y, int color, boolean drawShadow, Font.DisplayMode displayMode, int packedLight) {
            font.drawInBatch(text,
                    x,
                    y,
                    color,
                    drawShadow,
                    this.poseStack.last().pose(),
                    this.bufferSource,
                    displayMode,
                    0,
                    packedLight);
        }

        @Override
        public void drawString8xOutline(Font font, Component text, int x, int y, int color, int packedLight) {
            font.drawInBatch8xOutline(text.getVisualOrderText(),
                    x,
                    y,
                    color,
                    ARGB.opaque(0),
                    this.poseStack.last().pose(),
                    this.bufferSource,
                    packedLight);
        }

        @Override
        public void blitSprite(Function<ResourceLocation, RenderType> renderTypeGetter, ResourceLocation resourceLocation, int x, int y, int width, int height, int color, int packedLight) {
            GuiGraphics guiGraphics = new GuiGraphics(Minecraft.getInstance(), null) {

                @Override
                protected void innerBlit(RenderPipeline pipeline, ResourceLocation atlasLocation, int x1, int x2, int y1, int y2, float minU, float maxU, float minV, float maxV, int color) {
                    Preconditions.checkArgument(pipeline == null);
                    Preconditions.checkArgument(atlasLocation.equals(InLevelRenderingHandler.GUI_SHEET));
                    RenderType renderType = renderTypeGetter.apply(atlasLocation);
                    Matrix4f matrix4f = Level.this.poseStack.last().pose();
                    VertexConsumer vertexConsumer = Level.this.bufferSource.getBuffer(renderType);
                    vertexConsumer.addVertex(matrix4f, x1, y1, 0.0F)
                            .setUv(minU, minV)
                            .setColor(color)
                            .setLight(packedLight);
                    vertexConsumer.addVertex(matrix4f, x1, y2, 0.0F)
                            .setUv(minU, maxV)
                            .setColor(color)
                            .setLight(packedLight);
                    vertexConsumer.addVertex(matrix4f, x2, y2, 0.0F)
                            .setUv(maxU, maxV)
                            .setColor(color)
                            .setLight(packedLight);
                    vertexConsumer.addVertex(matrix4f, x2, y1, 0.0F)
                            .setUv(maxU, minV)
                            .setColor(color)
                            .setLight(packedLight);
                }
            };
            guiGraphics.blitSprite(null, resourceLocation, x, y, width, height, color);
        }
    }
}
