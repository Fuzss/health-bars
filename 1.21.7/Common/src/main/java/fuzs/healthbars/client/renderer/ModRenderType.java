package fuzs.healthbars.client.renderer;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import fuzs.healthbars.HealthBars;
import fuzs.healthbars.client.handler.InLevelRenderingHandler;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

/**
 * We use custom render types for everything, so we can manually call
 * {@link net.minecraft.client.renderer.MultiBufferSource.BufferSource#endBatch(RenderType)}.
 */
public abstract class ModRenderType extends RenderType {
    /**
     * Disable depth write as it prevents water behind the text background from rendering.
     *
     * @see RenderPipelines#TEXT_BACKGROUND
     */
    public static final RenderPipeline TEXT_BACKGROUND_PIPELINE = RenderPipeline.builder(RenderPipelines.TEXT_SNIPPET,
                    RenderPipelines.FOG_SNIPPET)
            .withLocation(HealthBars.id("pipeline/text_background"))
            .withVertexShader("core/rendertype_text_background")
            .withFragmentShader("core/rendertype_text_background")
            .withSampler("Sampler2")
            .withDepthWrite(false)
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR_LIGHTMAP, VertexFormat.Mode.QUADS)
            .build();
    /**
     * @see RenderType#TEXT
     */
    private static final Function<ResourceLocation, RenderType> TEXT = Util.memoize((ResourceLocation resourceLocation) -> create(
            HealthBars.id("text").toString(),
            786432,
            false,
            false,
            RenderPipelines.TEXT,
            CompositeState.builder()
                    .setTextureState(new TextureStateShard(resourceLocation, false))
                    .setLightmapState(LIGHTMAP)
                    .createCompositeState(false)));
    /**
     * @see RenderType#TEXT_SEE_THROUGH
     */
    private static final Function<ResourceLocation, RenderType> TEXT_SEE_THROUGH = Util.memoize((ResourceLocation resourceLocation) -> create(
            HealthBars.id("text_see_through").toString(),
            1536,
            false,
            false,
            RenderPipelines.TEXT_SEE_THROUGH,
            CompositeState.builder()
                    .setTextureState(new TextureStateShard(resourceLocation, false))
                    .setLightmapState(LIGHTMAP)
                    .createCompositeState(false)));
    /**
     * @see RenderType#TEXT_BACKGROUND
     */
    private static final RenderType TEXT_BACKGROUND = create(HealthBars.id("text_background").toString(),
            1536,
            false,
            true,
            TEXT_BACKGROUND_PIPELINE,
            CompositeState.builder()
                    .setTextureState(NO_TEXTURE)
                    .setLightmapState(LIGHTMAP)
                    .createCompositeState(false));
    /**
     * @see RenderType#TEXT_BACKGROUND_SEE_THROUGH
     */
    private static final RenderType TEXT_BACKGROUND_SEE_THROUGH = create(HealthBars.id("text_background_see_through")
                    .toString(),
            1536,
            false,
            true,
            RenderPipelines.TEXT_BACKGROUND_SEE_THROUGH,
            RenderType.CompositeState.builder()
                    .setTextureState(NO_TEXTURE)
                    .setLightmapState(LIGHTMAP)
                    .createCompositeState(false));

    private ModRenderType(String string, int i, boolean bl, boolean bl2, Runnable runnable, Runnable runnable2) {
        super(string, i, bl, bl2, runnable, runnable2);
    }

    public static RenderType text(ResourceLocation location) {
        return TEXT.apply(location);
    }

    public static RenderType textSeeThrough(ResourceLocation location) {
        return TEXT_SEE_THROUGH.apply(location);
    }

    public static RenderType textGuiSheet() {
        return text(InLevelRenderingHandler.GUI_SHEET);
    }

    public static RenderType textSeeThroughGuiSheet() {
        return textSeeThrough(InLevelRenderingHandler.GUI_SHEET);
    }

    public static RenderType textBackground() {
        return TEXT_BACKGROUND;
    }

    public static RenderType textBackgroundSeeThrough() {
        return TEXT_BACKGROUND_SEE_THROUGH;
    }
}
