package net.torocraft.torohealth.mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.torocraft.torohealth.ToroHealth;
import net.torocraft.torohealth.bars.BarStates;
import net.torocraft.torohealth.util.HoldingWeaponUpdater;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerEntityMixin extends LivingEntity {

  protected PlayerEntityMixin(EntityType<? extends LivingEntity> type, Level world) {
    super(type, world);
  }

  @Inject(method = "tick()V", at = @At("HEAD"))
  private void tick(CallbackInfo info) {
    if (!this.level.isClientSide) {
      return;
    }
    ToroHealth.HUD.setEntity(ToroHealth.RAYTRACE.getEntityInCrosshair(0, ToroHealth.CONFIG.hud.distance));
    BarStates.tick();
    HoldingWeaponUpdater.update();
    ToroHealth.HUD.tick();
  }

}
