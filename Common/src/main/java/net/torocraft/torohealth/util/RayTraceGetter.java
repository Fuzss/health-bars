package net.torocraft.torohealth.util;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Predicate;

public class RayTraceGetter implements BlockGetter {
    private static final Predicate<Entity> IS_VISIBLE = entity -> !entity.isSpectator() && entity.isPickable();

    @Override
    public BlockEntity getBlockEntity(BlockPos pos) {
        return Minecraft.getInstance().level.getBlockEntity(pos);
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return Minecraft.getInstance().level.getBlockState(pos);
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return Minecraft.getInstance().level.getFluidState(pos);
    }

    public LivingEntity getEntityInCrosshair(float partialTicks, double reachDistance) {
        Minecraft client = Minecraft.getInstance();
        Entity viewer = client.getCameraEntity();

        if (viewer == null) {
            return null;
        }

        Vec3 position = viewer.getEyePosition(partialTicks);
        Vec3 look = viewer.getViewVector(1.0F);
        Vec3 max = position.add(look.x * reachDistance, look.y * reachDistance, look.z * reachDistance);
        AABB searchBox = viewer.getBoundingBox().expandTowards(look.scale(reachDistance)).inflate(1.0D, 1.0D, 1.0D);

        EntityHitResult result = ProjectileUtil.getEntityHitResult(viewer, position, max, searchBox, IS_VISIBLE,
                reachDistance * reachDistance
        );

        if (result == null || result.getEntity() == null) {
            return null;
        }

        if (result.getEntity() instanceof LivingEntity) {
            LivingEntity target = (LivingEntity) result.getEntity();

            HitResult blockHit = clip(setupRayTraceContext(client.player, reachDistance, Fluid.NONE));

            if (!blockHit.getType().equals(Type.MISS)) {
                double blockDistance = blockHit.getLocation().distanceTo(position);
                if (blockDistance > target.distanceTo(client.player)) {
                    return target;
                }
            } else {
                return target;
            }
        }

        return null;
    }

    private ClipContext setupRayTraceContext(Player player, double distance, Fluid fluidHandling) {
        float pitch = player.getXRot();
        float yaw = player.getYRot();
        Vec3 fromPos = player.getEyePosition(1.0F);
        float float_3 = Mth.cos(-yaw * 0.017453292F - 3.1415927F);
        float float_4 = Mth.sin(-yaw * 0.017453292F - 3.1415927F);
        float float_5 = -Mth.cos(-pitch * 0.017453292F);
        float xComponent = float_4 * float_5;
        float yComponent = Mth.sin(-pitch * 0.017453292F);
        float zComponent = float_3 * float_5;
        Vec3 toPos = fromPos.add((double) xComponent * distance, (double) yComponent * distance,
                (double) zComponent * distance
        );
        return new ClipContext(fromPos, toPos, ClipContext.Block.OUTLINE, fluidHandling, player);
    }

    @Override
    public BlockHitResult clip(ClipContext context) {
        return BlockGetter.traverseBlocks(context.getFrom(), context.getTo(), context, (c, pos) -> {
            BlockState block = this.getBlockState(pos);
            if (!block.canOcclude()) {
                return null;
            }
            VoxelShape blockShape = c.getBlockShape(block, this, pos);
            return this.clipWithInteractionOverride(c.getFrom(), c.getTo(), pos, blockShape, block);
        }, (c) -> {
            Vec3 v = c.getFrom().subtract(c.getTo());
            return BlockHitResult.miss(c.getTo(), Direction.getNearest(v.x, v.y, v.z), BlockPos.containing(c.getTo()));
        });
    }

    @Override
    public int getMinBuildHeight() {
        return 0;
    }

    @Override
    public int getHeight() {
        return 0;
    }
}
