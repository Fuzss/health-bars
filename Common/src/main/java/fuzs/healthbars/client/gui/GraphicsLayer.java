package fuzs.healthbars.client.gui;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import fuzs.healthbars.client.renderer.rendertype.ModRenderTypes;
import fuzs.puzzleslib.common.api.client.gui.v2.GuiGraphicsHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;

public interface GraphicsLayer {
    int PACKED_LIGHT = 0XF000F0;

    void translate(float zOffset);

    @Nullable RenderType getTextBackgroundRenderType(Font.DisplayMode displayMode);

    void fill(@Nullable RenderType renderType, int minX, int minY, int maxX, int maxY, float zOffset, int color, int packedLight);

    void text(Font font, Component text, int x, int y, int color, boolean drawShadow, Font.DisplayMode displayMode, int packedLight, int outlineColor);

    default void centeredText(Font font, Component text, int x, int y, int color, boolean drawShadow, Font.DisplayMode displayMode, int packedLight, int outlineColor) {
        this.text(font, text, x - font.width(text) / 2, y, color, drawShadow, displayMode, packedLight, outlineColor);
    }

    default void text(Font font, Component text, int x, int y, int color, boolean drawShadow, Font.DisplayMode displayMode, int backgroundColor, int packedLight, int outlineColor) {
        if (backgroundColor != 0) {
            this.fill(this.getTextBackgroundRenderType(displayMode),
                    x - 2,
                    y - 2,
                    x + font.width(text) + 2,
                    y + font.lineHeight + (drawShadow ? 1 : 0),
                    -0.03F,
                    backgroundColor,
                    packedLight);
        }

        this.text(font, text, x, y, color, drawShadow, displayMode, packedLight, outlineColor);
    }

    default void centeredText(Font font, Component text, int x, int y, int color, boolean drawShadow, Font.DisplayMode displayMode, int backgroundColor, int packedLight, int outlineColor) {
        this.text(font,
                text,
                x - font.width(text) / 2,
                y,
                color,
                drawShadow,
                displayMode,
                backgroundColor,
                packedLight,
                outlineColor);
    }

    void text8xOutline(Font font, Component text, int x, int y, int color, int packedLight);

    void blitSprite(@Nullable Function<Identifier, RenderType> renderTypeGetter, Identifier identifier, int x, int y, int width, int height, int color, int packedLight);

    record Gui(GuiGraphicsExtractor guiGraphics) implements GraphicsLayer {

        @Override
        public void translate(float zOffset) {
            // NO-OP
        }

        @Override
        public @Nullable RenderType getTextBackgroundRenderType(Font.DisplayMode displayMode) {
            return null;
        }

        @Override
        public void fill(@Nullable RenderType renderType, int minX, int minY, int maxX, int maxY, float zOffset, int color, int packedLight) {
            Preconditions.checkArgument(renderType == null);
            Preconditions.checkArgument(packedLight == PACKED_LIGHT);
            this.guiGraphics.fill(minX, minY, maxX, maxY, color);
        }

        @Override
        public void text(Font font, Component text, int x, int y, int color, boolean drawShadow, Font.DisplayMode displayMode, int packedLight, int outlineColor) {
            Preconditions.checkArgument(displayMode == Font.DisplayMode.NORMAL);
            Preconditions.checkArgument(packedLight == PACKED_LIGHT);
            this.guiGraphics.text(font, text, x, y, color, drawShadow);
        }

        @Override
        public void text8xOutline(Font font, Component text, int x, int y, int color, int packedLight) {
            Preconditions.checkArgument(packedLight == PACKED_LIGHT);
            GuiGraphicsHelper.drawInBatch8xOutline(this.guiGraphics, font, text, x, y, color, ARGB.opaque(0));
        }

        @Override
        public void blitSprite(@Nullable Function<Identifier, RenderType> renderTypeGetter, Identifier identifier, int x, int y, int width, int height, int color, int packedLight) {
            Preconditions.checkArgument(renderTypeGetter == null);
            Preconditions.checkArgument(packedLight == PACKED_LIGHT);
            this.guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, identifier, x, y, width, height, color);
        }
    }

    class Level implements GraphicsLayer {
        private final PoseStack poseStack;
        private final SubmitNodeCollector nodeCollector;

        public Level(PoseStack poseStack, SubmitNodeCollector nodeCollector) {
            this.poseStack = poseStack;
            this.nodeCollector = nodeCollector;
        }

        @Override
        public void translate(float zOffset) {
            this.poseStack.translate(0.0F, 0.0F, zOffset);
        }

        @Override
        public RenderType getTextBackgroundRenderType(Font.DisplayMode displayMode) {
            return displayMode == Font.DisplayMode.SEE_THROUGH ? RenderTypes.textBackgroundSeeThrough() :
                    ModRenderTypes.textBackground();
        }

        @Override
        public void fill(@Nullable RenderType renderType, int minX, int minY, int maxX, int maxY, float zOffset, int color, int packedLight) {
            Objects.requireNonNull(renderType, "render type is null");
            this.nodeCollector.order(-1)
                    .submitCustomGeometry(this.poseStack,
                            renderType,
                            (PoseStack.Pose pose, VertexConsumer vertexConsumer) -> {
                                Matrix4f matrix4f = pose.pose();
                                vertexConsumer.addVertex(matrix4f, minX, minY, zOffset)
                                        .setColor(color)
                                        .setLight(packedLight);
                                vertexConsumer.addVertex(matrix4f, minX, maxY, zOffset)
                                        .setColor(color)
                                        .setLight(packedLight);
                                vertexConsumer.addVertex(matrix4f, maxX, maxY, zOffset)
                                        .setColor(color)
                                        .setLight(packedLight);
                                vertexConsumer.addVertex(matrix4f, maxX, minY, zOffset)
                                        .setColor(color)
                                        .setLight(packedLight);
                            });
        }

        @Override
        public void text(Font font, Component text, int x, int y, int color, boolean drawShadow, Font.DisplayMode displayMode, int packedLight, int outlineColor) {
            this.nodeCollector.order(1)
                    .submitText(this.poseStack,
                            x,
                            y,
                            text.getVisualOrderText(),
                            drawShadow,
                            displayMode,
                            packedLight,
                            color,
                            0,
                            outlineColor);
        }

        @Override
        public void text8xOutline(Font font, Component text, int x, int y, int color, int packedLight) {
            this.nodeCollector.order(1)
                    .submitText(this.poseStack,
                            x,
                            y,
                            text.getVisualOrderText(),
                            false,
                            Font.DisplayMode.NORMAL,
                            packedLight,
                            color,
                            0,
                            ARGB.opaque(0));
        }

        @SuppressWarnings("DataFlowIssue")
        @Override
        public void blitSprite(@Nullable Function<Identifier, RenderType> renderTypeGetter, Identifier identifier, int x, int y, int width, int height, int color, int packedLight) {
            Objects.requireNonNull(renderTypeGetter, "render type getter is null");
            // set the gui render state to null, so we get a NullPointerException whenever it is used, which are all the calls we need to redirect to the node collector
            GuiGraphicsExtractor guiGraphics = new GuiGraphicsExtractor(Minecraft.getInstance(), null, -1, -1) {
                @Override
                public void innerBlit(RenderPipeline pipeline, Identifier atlasLocation, int x1, int x2, int y1, int y2, float minU, float maxU, float minV, float maxV, int color) {
                    Preconditions.checkArgument(pipeline == null);
                    Preconditions.checkArgument(Objects.equals(atlasLocation, Sheets.GUI_SHEET));
                    RenderType renderType = renderTypeGetter.apply(atlasLocation);
                    Level.this.nodeCollector.submitCustomGeometry(Level.this.poseStack,
                            renderType,
                            (PoseStack.Pose pose, VertexConsumer vertexConsumer) -> {
                                Matrix4f matrix4f = pose.pose();
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
                            });
                }

                /**
                 * Vanilla has refactored this method in 1.21.9 to use a separate {@link net.minecraft.client.gui.render.state.TiledBlitRenderState}.
                 * <p>
                 * This copies the old implementation from 1.21.8, so everything can still run through {@link #innerBlit(RenderPipeline, Identifier, int, int, int, int, float, float, float, float, int)}
                 */
                @Override
                public void blitTiledSprite(RenderPipeline pipeline, TextureAtlasSprite sprite, int x, int y, int width, int height, int u, int v, int spriteWidth, int spriteHeight, int textureWidth, int textureHeight, int color) {
                    if (width > 0 && height > 0) {
                        if (spriteWidth > 0 && spriteHeight > 0) {
                            for (int i = 0; i < width; i += spriteWidth) {
                                int j = Math.min(spriteWidth, width - i);
                                for (int k = 0; k < height; k += spriteHeight) {
                                    int l = Math.min(spriteHeight, height - k);
                                    this.blitSprite(pipeline,
                                            sprite,
                                            textureWidth,
                                            textureHeight,
                                            u,
                                            v,
                                            x + i,
                                            y + k,
                                            j,
                                            l,
                                            color);
                                }
                            }
                        } else {
                            throw new IllegalArgumentException(
                                    "Tiled sprite texture size must be positive, got " + spriteWidth + "x"
                                            + spriteHeight);
                        }
                    }
                }
            };
            // the pipeline is set to null on purpose, so we can check in our implementation that the call is indeed coming from us
            guiGraphics.blitSprite(null, identifier, x, y, width, height, color);
        }
    }
}
