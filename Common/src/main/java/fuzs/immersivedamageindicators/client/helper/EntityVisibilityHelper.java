package fuzs.immersivedamageindicators.client.helper;

import fuzs.immersivedamageindicators.ImmersiveDamageIndicators;
import fuzs.immersivedamageindicators.client.handler.PickEntityHandler;
import fuzs.immersivedamageindicators.config.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Team;

public class EntityVisibilityHelper {

    public static boolean isEntityVisible(Level level, LivingEntity livingEntity, Player player, float partialTicks, EntityRenderDispatcher entityRenderDispatcher, boolean mustBePicked) {
        if (mustBePicked && livingEntity != PickEntityHandler.getCrosshairPickEntity()) {
            return false;
        } else if (!shouldShowName(livingEntity)) {
            // run this earlier than vanilla to avoid raytracing if not necessary
            return false;
        } else {
            return mustBePicked || entityRenderDispatcher.distanceToSqr(livingEntity) < getMaxRenderDistanceSqr(level, livingEntity,
                    player, partialTicks
            );
        }
    }

    /**
     * Mostly copied from
     * {@link net.minecraft.client.renderer.entity.LivingEntityRenderer#shouldShowName(LivingEntity)}.
     */
    private static boolean shouldShowName(LivingEntity entity) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        boolean isVisible = isVisibleToPlayer(entity, player);
        if (entity != player) {
            Team entityTeam = entity.getTeam();
            if (entityTeam != null) {
                Team playerTeam = player.getTeam();
                return switch (entityTeam.getNameTagVisibility()) {
                    case ALWAYS -> isVisible;
                    case NEVER -> false;
                    case HIDE_FOR_OTHER_TEAMS -> playerTeam == null ? isVisible : entityTeam.isAlliedTo(playerTeam) &&
                            (entityTeam.canSeeFriendlyInvisibles() || isVisible);
                    case HIDE_FOR_OWN_TEAM -> playerTeam == null ? isVisible : !entityTeam.isAlliedTo(playerTeam) &&
                            isVisible;
                };
            }
        }

        return Minecraft.renderNames() && entity != minecraft.getCameraEntity() && isVisible && !entity.isVehicle();
    }

    private static boolean isVisibleToPlayer(LivingEntity entity, Player player) {
        if (entity.isSpectator()) {
            return false;
        } else if (entity.isInvisibleTo(player)) {
            if (entity.isOnFire() || entity.isCurrentlyGlowing()) {
                return true;
            } else if (entity instanceof Creeper creeper && creeper.isPowered()) {
                return true;
            } else {
                for (ItemStack itemStack : entity.getAllSlots()) {
                    if (!itemStack.isEmpty()) {
                        return true;
                    }
                }

                return false;
            }
        } else {
            return true;
        }
    }

    private static int getMaxRenderDistanceSqr(Level level, LivingEntity livingEntity, Player player, float partialTicks) {
        int maxRenderDistance = ImmersiveDamageIndicators.CONFIG.get(ClientConfig.class).level.maxRenderDistance;
        if (livingEntity.isDiscrete()) maxRenderDistance /= 2;
        // use this instead of LivingEntity::hasLineOfSight, so we can look through transparent blocks like glass
        if (pickVisual(level, livingEntity, player, partialTicks).getType() != HitResult.Type.MISS) {
            maxRenderDistance /= 4;
        }

        return maxRenderDistance * maxRenderDistance;
    }

    private static HitResult pickVisual(Level level, LivingEntity livingEntity, Player player, float partialTicks) {
        Vec3 playerEyePosition = player.getEyePosition(partialTicks);
        Vec3 entityEyePosition = livingEntity.getEyePosition(partialTicks);
        return level.clip(
                new ClipContext(playerEyePosition, entityEyePosition, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE,
                        player
                ));
    }
}
