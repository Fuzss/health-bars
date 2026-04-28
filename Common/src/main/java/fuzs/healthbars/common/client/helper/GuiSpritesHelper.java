package fuzs.healthbars.common.client.helper;

import fuzs.healthbars.common.HealthBars;
import net.minecraft.client.gui.Gui;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

public class GuiSpritesHelper {
    public static final Identifier ARMOR_FULL_SPRITE = Identifier.withDefaultNamespace("hud/armor_full");
    public static final Identifier TOUGHNESS_FULL_SPRITE = HealthBars.id("hud/toughness_full");
    private static final Identifier HEART_VEHICLE_FULL_SPRITE = HealthBars.id("hud/heart/vehicle_full");

    public static Identifier getSprite(LivingEntity livingEntity) {
        return isMount(livingEntity) ? HEART_VEHICLE_FULL_SPRITE : getSprite(forEntity(livingEntity), livingEntity);
    }

    public static Identifier getSprite(Gui.HeartType heartType) {
        return getSprite(heartType, null);
    }

    private static Identifier getSprite(Gui.HeartType heartType, @Nullable LivingEntity livingEntity) {
        Identifier identifier = heartType.getSprite(isHardcore(livingEntity), false, false);
        return HealthBars.id(identifier.getPath());
    }

    private static boolean isHardcore(@Nullable LivingEntity livingEntity) {
        return livingEntity instanceof Player player && player.level().getLevelData().isHardcore();
    }

    private static boolean isMount(LivingEntity livingEntity) {
        return livingEntity instanceof Mob mob && mob.isSaddled();
    }

    /**
     * Most of these do not even work, as mobs effects are only synced to the client for the local player.
     *
     * @see net.minecraft.client.gui.Gui.HeartType#forPlayer(Player)
     */
    private static Gui.HeartType forEntity(LivingEntity livingEntity) {
        if (livingEntity.hasEffect(MobEffects.POISON)) {
            return Gui.HeartType.POISIONED;
        } else if (livingEntity.hasEffect(MobEffects.WITHER)) {
            return Gui.HeartType.WITHERED;
        } else if (livingEntity.isFullyFrozen()) {
            return Gui.HeartType.FROZEN;
        } else if (livingEntity.getAbsorptionAmount() > 0.0F) {
            return Gui.HeartType.ABSORBING;
        } else {
            return Gui.HeartType.NORMAL;
        }
    }
}
