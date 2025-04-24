package fuzs.healthbars.client.renderer;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import fuzs.healthbars.HealthBars;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderType;

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

    private ModRenderType(String string, int i, boolean bl, boolean bl2, Runnable runnable, Runnable runnable2) {
        super(string, i, bl, bl2, runnable, runnable2);
    }

    public static RenderType textBackground() {
        return TEXT_BACKGROUND;
    }
}
