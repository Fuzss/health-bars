package fuzs.healthbars.client.helper;

import fuzs.healthbars.HealthBars;
import fuzs.healthbars.client.handler.PickEntityHandler;
import fuzs.healthbars.config.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.EquipmentSlot;
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

    public static boolean isEntityVisible(LivingEntity livingEntity, float partialTick, boolean mustBePicked) {
        Minecraft minecraft = Minecraft.getInstance();
        return isEntityVisible(minecraft.level,
                livingEntity,
                minecraft.player,
                partialTick,
                minecraft.getEntityRenderDispatcher(),
                mustBePicked);
    }

    public static boolean isEntityVisible(Level level, LivingEntity livingEntity, Player player, float partialTick, EntityRenderDispatcher entityRenderDispatcher, boolean mustBePicked) {
        if (mustBePicked && livingEntity != PickEntityHandler.getCrosshairPickEntity()) {
            return false;
        } else if (!shouldShowName(livingEntity)) {
            // run this earlier than vanilla to avoid raytracing if not necessary
            return false;
        } else {
            return entityRenderDispatcher.distanceToSqr(livingEntity) < getMaxRenderDistanceSqr(level,
                    livingEntity,
                    player,
                    partialTick);
        }
    }

    /**
     * @see net.minecraft.client.renderer.entity.LivingEntityRenderer#shouldShowName(LivingEntity, double)
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
                    case HIDE_FOR_OTHER_TEAMS -> playerTeam == null ? isVisible :
                            entityTeam.isAlliedTo(playerTeam) && (entityTeam.canSeeFriendlyInvisibles() || isVisible);
                    case HIDE_FOR_OWN_TEAM ->
                            playerTeam == null ? isVisible : !entityTeam.isAlliedTo(playerTeam) && isVisible;
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
                for (EquipmentSlot equipmentSlot : EquipmentSlot.VALUES) {
                    ItemStack itemStack = entity.getItemBySlot(equipmentSlot);
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

    private static int getMaxRenderDistanceSqr(Level level, LivingEntity livingEntity, Player player, float partialTick) {
        int maxRenderDistance = HealthBars.CONFIG.get(ClientConfig.class).level.maxRenderDistance;
        if (livingEntity.isDiscrete()) maxRenderDistance /= 2;
        // use this instead of LivingEntity::hasLineOfSight, so we can look through transparent blocks like glass
        if (pickVisual(level, livingEntity, player, partialTick).getType() != HitResult.Type.MISS) {
            maxRenderDistance /= 4;
        }

        return maxRenderDistance * maxRenderDistance;
    }

    private static HitResult pickVisual(Level level, LivingEntity livingEntity, Player player, float partialTick) {
        Vec3 playerEyePosition = player.getEyePosition(partialTick);
        Vec3 entityEyePosition = livingEntity.getEyePosition(partialTick);
        return level.clip(new ClipContext(playerEyePosition,
                entityEyePosition,
                ClipContext.Block.VISUAL,
                ClipContext.Fluid.NONE,
                player));
    }
}
