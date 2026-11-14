package fuzs.healthbars.common.client.renderer.rendertype;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import fuzs.healthbars.common.HealthBars;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.oit.OitPipelineSet;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

import java.util.function.Function;

public final class ModRenderTypes {
    /**
     * OIT equivalent of {@link RenderPipelines#TEXT_SEE_THROUGH}, required since custom geometry drawn with a render
     * type that has blending enabled is routed to the improved transparency phase. Vanilla see-through text avoids
     * that by using a dedicated render pass, which is not available for custom geometry.
     */
    public static final OitPipelineSet TEXT_SEE_THROUGH_OIT_PIPELINES = OitPipelineSet.builder("text_see_through",
                    RenderPipeline.builder()
                            .withBindGroupLayout(BindGroupLayouts.PROJECTION)
                            .withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
                            .withBindGroupLayout(BindGroupLayouts.SAMPLER0)
                            .withVertexShader("core/text")
                            .withFragmentShader("core/text")
                            .withShaderDefine("IS_SEE_THROUGH")
                            .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR)
                            .withPrimitiveTopology(PrimitiveTopology.QUADS)
                            .withDepthStencilState(DepthStencilState.DEFAULT))
            .withoutDepthTest()
            .build();
    /**
     * @see net.minecraft.client.renderer.rendertype.RenderTypes#textSeeThrough(Identifier)
     */
    private static final Function<Identifier, RenderType> TEXT_SEE_THROUGH = Util.memoize(texture ->
            RenderType.create(HealthBars.id("text_see_through").toString(),
                    RenderSetup.builder(RenderPipelines.TEXT_SEE_THROUGH)
                            .setOitPipelines(TEXT_SEE_THROUGH_OIT_PIPELINES)
                            .withTexture("Sampler0", texture)
                            .useLightmap()
                            .createRenderSetup()));

    private ModRenderTypes() {
        // NO-OP
    }

    public static RenderType textSeeThrough(Identifier texture) {
        return TEXT_SEE_THROUGH.apply(texture);
    }
}
