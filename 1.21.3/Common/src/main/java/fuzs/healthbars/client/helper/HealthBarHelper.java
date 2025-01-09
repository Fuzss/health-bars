package fuzs.healthbars.client.helper;

import fuzs.healthbars.HealthBars;
import fuzs.healthbars.config.ClientConfig;
import fuzs.puzzleslib.api.core.v1.CommonAbstractions;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;

public class HealthBarHelper {

    public static ResourceLocation getBarSprite(BossEvent.BossBarColor barColor, boolean background) {
        return HealthBars.id((background ? BossHealthOverlay.BAR_BACKGROUND_SPRITES :
                BossHealthOverlay.BAR_PROGRESS_SPRITES)[barColor.ordinal()].getPath());
    }

    public static ResourceLocation getOverlaySprite(BossEvent.BossBarOverlay barOverlay, boolean background) {
        return HealthBars.id((background ? BossHealthOverlay.OVERLAY_BACKGROUND_SPRITES :
                BossHealthOverlay.OVERLAY_PROGRESS_SPRITES)[barOverlay.ordinal() - 1].getPath());
    }

    public static int getBarWidth(ClientConfig.BarConfig config, HealthTracker healthTracker) {
        int barScale = config.scaleBarWidthByHealth ? getBarScaleFromHealth(healthTracker.getData().maxHealth()) - 2 :
                0;
        barScale = Math.max(barScale, 0) + config.healthBarWidth;
        return getBarWidthByScale(Mth.clamp(barScale, 1, 4));
    }

    public static int getBarWidthByScale(int scale) {
        // the vanilla texture width is 182 which we split into 45*4+2 for scaling
        return 45 * scale + 2;
    }

    public static int getBarScaleFromHealth(float health) {
        return Mth.ceil(health / 40.0F);
    }

    public static BossEvent.BossBarColor getBarColor(LivingEntity livingEntity, ClientConfig.BarColors barColors) {
        EntityType<?> entityType = livingEntity.getType();
        if (entityType == EntityType.ENDER_DRAGON) {
            return BossEvent.BossBarColor.PINK;
        } else if (CommonAbstractions.INSTANCE.isBossMob(entityType)) {
            return BossEvent.BossBarColor.PURPLE;
        } else {
            if (livingEntity instanceof Enemy) {
                return barColors.monsterColor;
            } else {
                return switch (entityType.getCategory()) {
                    case MONSTER -> barColors.monsterColor;
                    case AMBIENT, MISC -> barColors.ambientColor;
                    case WATER_AMBIENT, WATER_CREATURE, UNDERGROUND_WATER_CREATURE, AXOLOTLS -> barColors.aquaticColor;
                    case CREATURE -> barColors.friendlyColor;
                    // this can be extended on NeoForge, so we need a default branch (hoping the compiler does not remove it)
                    default -> barColors.miscColor;
                };
            }
        }
    }
}
