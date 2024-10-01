package net.torocraft.torohealth.bars;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import fuzs.immersivedamageindicators.ImmersiveDamageIndicators;
import fuzs.immersivedamageindicators.client.ImmersiveDamageIndicatorsClient;
import fuzs.immersivedamageindicators.config.ClientConfig;
import fuzs.immersivedamageindicators.config.ClientConfig.Mode;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.torocraft.torohealth.util.EntityRelationHelper;
import net.torocraft.torohealth.util.EntityRelationHelper.Relation;
import net.torocraft.torohealth.util.PlayerWeaponHelper;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class HealthBarRenderer {
    private static final ResourceLocation GUI_BARS_TEXTURES = ImmersiveDamageIndicators.id("textures/gui/bars.png");
    private static final int DARK_GRAY = 0x808080;
    private static final int FULL_SIZE = 92;

    private static ClientConfig.InWorld inWorldConfig() {
        return ImmersiveDamageIndicators.CONFIG.get(ClientConfig.class).inWorld;
    }

    public static ClientConfig.Bar barConfig() {
        return ImmersiveDamageIndicators.CONFIG.get(ClientConfig.class).bar;
    }

    private static ClientConfig.Particle particleConfig() {
        return ImmersiveDamageIndicators.CONFIG.get(ClientConfig.class).particle;
    }

    private static final List<LivingEntity> renderedEntities = new ArrayList<>();

    public static void prepareRenderInWorld(LivingEntity entity) {
        Minecraft client = Minecraft.getInstance();

        if (!EntityRelationHelper.showHealthBar(entity, client)) {
            return;
        }

        if (entity.distanceTo(client.getCameraEntity()) > inWorldConfig().distance) {
            return;
        }

        BarStates.getState(entity);

        if (Mode.NONE == inWorldConfig().mode) {
            return;
        }

        if (Mode.WHEN_HOLDING_WEAPON == inWorldConfig().mode && !PlayerWeaponHelper.isHoldingWeapon()) {
            return;
        }

        if (inWorldConfig().onlyWhenLookingAt && ImmersiveDamageIndicatorsClient.HUD_RENDERER.getEntity() != entity) {
            return;
        }

        if (inWorldConfig().onlyWhenHurt && entity.getHealth() >= entity.getMaxHealth()) {
            return;
        }

        renderedEntities.add(entity);

    }

    public static void renderInWorld(PoseStack poseStack, Camera camera) {

        Minecraft client = Minecraft.getInstance();

        if (camera == null) {
            camera = client.getEntityRenderDispatcher().camera;
        }

        if (camera == null) {
            renderedEntities.clear();
            return;
        }

        if (renderedEntities.isEmpty()) {
            return;
        }

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

        for (LivingEntity entity : renderedEntities) {
            float scaleToGui = 0.025f;
            boolean sneaking = entity.isCrouching();
            float height = entity.getBbHeight() + 0.6F - (sneaking ? 0.25F : 0.0F);

            float tickDelta = client.getTimer().getGameTimeDeltaPartialTick(false);
            double x = Mth.lerp(tickDelta, entity.xo, entity.getX());
            double y = Mth.lerp(tickDelta, entity.yo, entity.getY());
            double z = Mth.lerp(tickDelta, entity.zo, entity.getZ());

            Vec3 camPos = camera.getPosition();
            double camX = camPos.x;
            double camY = camPos.y;
            double camZ = camPos.z;

            poseStack.pushPose();
            poseStack.translate(x - camX, (y + height) - camY, z - camZ);
            poseStack.mulPose(Axis.YP.rotationDegrees(-camera.getYRot()));
            poseStack.mulPose(Axis.XP.rotationDegrees(camera.getXRot()));
            poseStack.scale(-scaleToGui, -scaleToGui, scaleToGui);

            render(poseStack, entity, 0, 0, FULL_SIZE, true);

            poseStack.popPose();
        }

        RenderSystem.disableBlend();

        renderedEntities.clear();
    }

    public static HealthTracker render(PoseStack poseStack, LivingEntity entity, int x, int y, int width, boolean inWorld) {

        Relation relation = EntityRelationHelper.determineRelation(entity);

        int color = relation.equals(Relation.FRIEND) ? barConfig().friendColor : barConfig().foeColor;
        int deltaColor = relation.equals(Relation.FRIEND) ? barConfig().friendColorSecondary :
                barConfig().foeColorSecondary;

        HealthTracker state = HealthTracker.getHealthTracker(entity);

//        float percent = Math.min(1.0F, Math.min(state.health, entity.getMaxHealth()) / entity.getMaxHealth());
//        float percent2 = Math.min(state.previousHealthDisplay, entity.getMaxHealth()) / entity.getMaxHealth();
        int zOffset = 0;

        float barProgress = state.getBarProgress(Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false));
        float backgroundBarProgress = state.getBackgroundBarProgress(Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false));

        Matrix4f matrix4f = poseStack.last().pose();
        drawBar(ResourceLocation.withDefaultNamespace("boss_bar/white_background"), matrix4f, x, y, width, 1.0F, DARK_GRAY, zOffset++, inWorld);
        drawBar(ResourceLocation.withDefaultNamespace("boss_bar/white_progress"), matrix4f, x, y, width, backgroundBarProgress, deltaColor, zOffset++, inWorld);
        drawBar(ResourceLocation.withDefaultNamespace("boss_bar/notched_12_progress"), matrix4f, x, y, width, backgroundBarProgress, deltaColor, zOffset++, inWorld);
        drawBar(ResourceLocation.withDefaultNamespace("boss_bar/white_progress"), matrix4f, x, y, width, barProgress, color, zOffset, inWorld);
        drawBar(ResourceLocation.withDefaultNamespace("boss_bar/notched_12_progress"), matrix4f, x, y, width, barProgress, color, zOffset, inWorld);

        return state;
    }

    public static void drawDamageNumber(GuiGraphics guiGraphics, Font font, int damageAmount, int posX, int posY, int width) {
        if (damageAmount != 0) {
            String s = Integer.toString(Math.abs(damageAmount));
            int stringWidth = font.width(s);
            int color = damageAmount < 0 ? particleConfig().healColor : particleConfig().damageColor;
            guiGraphics.drawString(font, s, posX + (width / 2) - stringWidth, posY + 5, color, true);
        }
    }

    public static void drawDamageNumber(PoseStack poseStack, Font font, int damageAmount, int posX, int posY, int width) {
        if (damageAmount != 0) {
            String s = Integer.toString(Math.abs(damageAmount));
            int stringWidth = font.width(s);
            int color = damageAmount < 0 ? particleConfig().healColor : particleConfig().damageColor;
            drawString(poseStack, font, s, posX + (width / 2) - stringWidth, posY + 5, color, true);
        }
    }

    public static void drawString(PoseStack poseStack, Font font, String text, int x, int y, int color, boolean dropShadow) {
        MultiBufferSource.BufferSource multiBufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        font.drawInBatch(text, x, y, color, dropShadow, poseStack.last().pose(), multiBufferSource,
                Font.DisplayMode.POLYGON_OFFSET, 0, 15728880, font.isBidirectional()
        );
    }

    static GuiGraphics createGuiGraphics(PoseStack poseStack) {
        return createGuiGraphics(poseStack.last().pose());
    }

    static GuiGraphics createGuiGraphics(Matrix4f matrix4f) {
        Minecraft minecraft = Minecraft.getInstance();
        GuiGraphics guiGraphics = new GuiGraphics(minecraft, minecraft.renderBuffers().bufferSource());
        guiGraphics.pose().mulPose(matrix4f);
        return guiGraphics;
    }

    private static void drawBar(ResourceLocation resourceLocation, Matrix4f matrix4f, int posX, int posY, int width, float percentage, int color, int zOffset, boolean inWorld) {
        float c = 0.00390625F;
        int u = 0;
        int v = 6 * 5 * 2 + 5;
        int uw = Mth.ceil(92 * percentage);
        int vh = 5;

        double size = percentage * width;
        int h = inWorld ? 4 : 6;

        if (inWorld) zOffset *= -1;

        float r = FastColor.ARGB32.red(color) / 255.0F;
        float g = FastColor.ARGB32.green(color) / 255.0F;
        float b = FastColor.ARGB32.blue(color) / 255.0F;

        RenderSystem.setShaderColor(r, g, b, 1.0F);
//        RenderSystem.setShader(GameRenderer::getPositionTexShader);
//        RenderSystem.setShaderTexture(0, GUI_BARS_TEXTURES);
//        RenderSystem.enableBlend();

        int half = width / 2;

        float zOffsetAmount = inWorld ? -0.1F : 0.1F;

//        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS,
//                DefaultVertexFormat.POSITION_TEX
//        );
//        bufferBuilder.addVertex(matrix4f, (float) (-half + x), (float) y, zOffset * zOffsetAmount).setUv(u * c, v * c);
//        bufferBuilder.addVertex(matrix4f, (float) (-half + x), (float) (h + y), zOffset * zOffsetAmount).setUv(u * c,
//                (v + vh) * c
//        );
//        bufferBuilder.addVertex(matrix4f, (float) (-half + size + x), (float) (h + y), zOffset * zOffsetAmount).setUv(
//                (u + uw) * c, (v + vh) * c);
//        bufferBuilder.addVertex(matrix4f, (float) (-half + size + x), (float) y, zOffset * zOffsetAmount).setUv(
//                ((u + uw) * c), v * c);
//        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());


        posX -= (width / 2);
        GuiGraphics guiGraphics = createGuiGraphics(matrix4f);
        TextureAtlasSprite sprite = Minecraft.getInstance().getGuiSprites().getSprite(resourceLocation);
        GuiSpriteScaling.NineSlice.Border border = new GuiSpriteScaling.NineSlice.Border(2, 2, 2, 2);
        GuiSpriteScaling.NineSlice nineSlice = new GuiSpriteScaling.NineSlice(
                sprite.contents().width(),
                sprite.contents().height(), border
        );
        guiGraphics.blitNineSlicedSprite(sprite, nineSlice, posX, posY, 0,
                (int) (width * percentage), sprite.contents().height());
        matrix4f.translate(0.0F, 0.0F, 0.03F * (inWorld ? -1 : 1));
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

    }
}
