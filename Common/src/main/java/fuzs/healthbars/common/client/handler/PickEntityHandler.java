package fuzs.healthbars.common.client.handler;

import fuzs.healthbars.common.HealthBars;
import fuzs.healthbars.common.config.ClientConfig;
import fuzs.puzzleslib.common.api.event.v1.data.MutableFloat;
import fuzs.puzzleslib.common.api.util.v1.EntityHelper;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.AttackRange;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
import org.jspecify.annotations.Nullable;

import java.util.UUID;
import java.util.function.Predicate;

public class PickEntityHandler {
    /**
     * @see net.minecraft.world.entity.EntitySelector#CAN_BE_PICKED
     */
    public static final Predicate<Entity> CAN_BE_PICKED = PickEntityHandler::isEntityPickable;

    @Nullable
    private static UUID crosshairPickEntity;
    private static int pickDelay;

    /**
     * Unlike {@link Minecraft#crosshairPickEntity} the pick is retained across ticks. Only the {@link UUID} is stored
     * so that a removed entity (which would otherwise keep its whole {@link net.minecraft.world.level.Level} alive) can
     * never be pinned here.
     */
    @Nullable
    public static UUID getCrosshairPickEntity() {
        return crosshairPickEntity;
    }

    /**
     * Runs exactly once per frame before {@link net.minecraft.client.renderer.GameRenderer#extract}, so the pick is up
     * to date when entity and GUI render states are extracted later in the same frame.
     */
    public static void onComputeFieldOfView(Camera camera, float partialTick, MutableFloat fov) {
        if (!HealthBars.CONFIG.get(ClientConfig.class).anyRendering.get()) {
            return;
        }

        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && camera.entity() != null) {
            HitResult hitResult = raycastHitResult(player, camera.entity(), partialTick);
            if (hitResult.getType() == HitResult.Type.ENTITY) {
                Entity entity = EntityHelper.getPartEntityParent(((EntityHitResult) hitResult).getEntity());
                crosshairPickEntity = entity.getUUID();
                pickDelay = HealthBars.CONFIG.get(ClientConfig.class).pickedEntityDelay * 20;
            } else if (pickDelay == 0) {
                crosshairPickEntity = null;
            }
        }
    }

    /**
     * @see LocalPlayer#raycastHitResult(float, Entity)
     */
    private static HitResult raycastHitResult(LocalPlayer player, Entity cameraEntity, float partialTick) {
        double blockInteractionRange = player.blockInteractionRange();
        double entityInteractionRange = player.entityInteractionRange();
        int interactionRange = HealthBars.CONFIG.get(ClientConfig.class).pickedEntityInteractionRange;
        if (interactionRange != -1) {
            blockInteractionRange = entityInteractionRange = interactionRange;
        }

        HitResult hitResult = null;
        ItemStack itemStack = player.getActiveItem();
        AttackRange itemAttackRange = itemStack.get(DataComponents.ATTACK_RANGE);
        if (itemAttackRange != null) {
            hitResult = itemAttackRange.getClosesetHit(cameraEntity, partialTick, CAN_BE_PICKED);
            if (hitResult instanceof BlockHitResult) {
                hitResult = LocalPlayer.filterHitResult(hitResult,
                        cameraEntity.getEyePosition(partialTick),
                        blockInteractionRange);
            }
        }

        if (hitResult == null || hitResult.getType() == HitResult.Type.MISS) {
            hitResult = pick(cameraEntity, blockInteractionRange, entityInteractionRange, partialTick);
        }

        return hitResult;
    }

    /**
     * @see LocalPlayer#pick(Entity, double, double, float)
     */
    private static HitResult pick(Entity entity, double blockInteractionRange, double entityInteractionRange, float partialTick) {
        double maxDistance = Math.max(blockInteractionRange, entityInteractionRange);
        double maxDistanceSq = Mth.square(maxDistance);
        Vec3 from = entity.getEyePosition(partialTick);
        HitResult blockHitResult = pick(entity, maxDistance, partialTick, false);
        double blockDistanceSq = blockHitResult.getLocation().distanceToSqr(from);
        if (blockHitResult.getType() != HitResult.Type.MISS) {
            maxDistanceSq = blockDistanceSq;
            maxDistance = Math.sqrt(blockDistanceSq);
        }

        Vec3 direction = entity.getViewVector(partialTick);
        Vec3 to = from.add(direction.x * maxDistance, direction.y * maxDistance, direction.z * maxDistance);
        AABB box = entity.getBoundingBox().expandTowards(direction.scale(maxDistance)).inflate(1.0, 1.0, 1.0);
        EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(entity,
                from,
                to,
                box,
                CAN_BE_PICKED,
                maxDistanceSq);
        return entityHitResult != null && entityHitResult.getLocation().distanceToSqr(from) < blockDistanceSq ?
                LocalPlayer.filterHitResult(entityHitResult, from, entityInteractionRange) :
                LocalPlayer.filterHitResult(blockHitResult, from, blockInteractionRange);
    }

    /**
     * {@link ClipContext.Block} has been changed to {@link ClipContext.Block#VISUAL}.
     *
     * @see Entity#pick(double, float, boolean)
     */
    private static HitResult pick(Entity entity, double range, float partialTicks, boolean withLiquids) {
        Vec3 eyePosition = entity.getEyePosition(partialTicks);
        Vec3 viewVector = entity.getViewVector(partialTicks);
        Vec3 vec3 = eyePosition.add(viewVector.x * range, viewVector.y * range, viewVector.z * range);
        return entity.level()
                .clip(new ClipContext(eyePosition,
                        vec3,
                        ClipContext.Block.VISUAL,
                        withLiquids ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE,
                        entity));
    }

    /**
     * @see net.minecraft.world.entity.EntitySelector#CAN_BE_PICKED
     */
    private static boolean isEntityPickable(Entity entity) {
        return EntityHelper.getPartEntityParent(entity) instanceof LivingEntity && !entity.isSpectator()
                && entity.isPickable();
    }

    public static void onStartClientTick(Minecraft minecraft) {
        if (minecraft.level != null && !minecraft.isPaused()) {
            if (pickDelay > 0) {
                pickDelay--;
            }
        }
    }
}
