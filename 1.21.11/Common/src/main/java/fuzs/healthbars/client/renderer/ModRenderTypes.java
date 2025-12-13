package fuzs.healthbars.client.renderer;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import fuzs.healthbars.HealthBars;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;

public final class ModRenderTypes {
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
     * @see net.minecraft.client.renderer.rendertype.RenderTypes#TEXT_BACKGROUND
     */
    private static final RenderType TEXT_BACKGROUND = RenderType.create(HealthBars.id("text_background").toString(),
            RenderSetup.builder(TEXT_BACKGROUND_PIPELINE).useLightmap().sortOnUpload().createRenderSetup());

    private ModRenderTypes() {
        // NO-OP
    }

    public static RenderType textBackground() {
        return TEXT_BACKGROUND;
    }
}
