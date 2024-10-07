package fuzs.immersivedamageindicators.client.renderer;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import fuzs.immersivedamageindicators.ImmersiveDamageIndicators;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public final class ModRenderType extends RenderType {
    /**
     * Copied from {@link #TEXT}.
     */
    public static final Function<ResourceLocation, RenderType> ICON = Util.memoize((resourceLocation) -> {
        return RenderType.create(ImmersiveDamageIndicators.id("icon").toString(),
                DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS, 1536, false, true,
                RenderType.CompositeState.builder().setShaderState(RENDERTYPE_TEXT_SHADER).setTextureState(
                        new RenderStateShard.TextureStateShard(resourceLocation, false, false)).setTransparencyState(
                        TRANSLUCENT_TRANSPARENCY).setLightmapState(LIGHTMAP).createCompositeState(false)
        );
    });
    /**
     * Copied from {@link #TEXT_SEE_THROUGH}, although {@link #RENDERTYPE_SOLID_SHADER} is used, as
     * {@link #RENDERTYPE_TEXT_SEE_THROUGH_SHADER} does not seem to support depth. Unfortunately
     * {@link #RENDERTYPE_SOLID_SHADER} messes up the colors when viewing textures from inside water.
     */
    public static final Function<ResourceLocation, RenderType> ICON_SEE_THROUGH = Util.memoize((resourceLocation) -> {
        return RenderType.create(ImmersiveDamageIndicators.id("icon_see_through").toString(),
                DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS, 1536, false, true,
                RenderType.CompositeState.builder()
                        .setShaderState(RENDERTYPE_SOLID_SHADER)
                        .setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false))
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setLightmapState(LIGHTMAP)
                        .setDepthTestState(NO_DEPTH_TEST)
                        .setWriteMaskState(COLOR_WRITE)
                        .createCompositeState(false)
        );
    });

    private ModRenderType(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }
}
