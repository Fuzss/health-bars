package fuzs.immersivedamageindicators.client.helper;

import fuzs.immersivedamageindicators.ImmersiveDamageIndicators;
import fuzs.immersivedamageindicators.config.ClientConfig;
import fuzs.immersivedamageindicators.init.ModRegistry;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;

public class HealthBarHelper {

    public static ResourceLocation getBarSprite(BossEvent.BossBarColor barColor, boolean background) {
        return ImmersiveDamageIndicators.id((background ? BossHealthOverlay.BAR_BACKGROUND_SPRITES :
                BossHealthOverlay.BAR_PROGRESS_SPRITES)[barColor.ordinal()].getPath());
    }

    public static ResourceLocation getOverlaySprite(BossEvent.BossBarOverlay barOverlay, boolean background) {
        return ImmersiveDamageIndicators.id((background ? BossHealthOverlay.OVERLAY_BACKGROUND_SPRITES :
                BossHealthOverlay.OVERLAY_PROGRESS_SPRITES)[barOverlay.ordinal() - 1].getPath());
    }

    public static int getBarWidth(ClientConfig.BarConfig config, HealthTracker healthTracker) {
        int barScale = config.scaleBarWidthByHealth ? getBarScaleFromHealth(healthTracker.getHealth()) - 2 : 0;
        barScale = Math.max(barScale, 0) + config.healthBarWidth;
        return getBarWidthByScale(Mth.clamp(barScale, 1, 4));
    }

    public static int getBarWidthByScale(int scale) {
        // the texture width is 182 which we split into 45*4+2
        return 45 * scale + 2;
    }

    public static int getBarScaleFromHealth(float health) {
        return Mth.ceil(health / 40.0F);
    }

    public static BossEvent.BossBarColor getBarColor(LivingEntity livingEntity, ClientConfig.BarColors barColors) {
        EntityType<?> entityType = livingEntity.getType();
        if (entityType == EntityType.ENDER_DRAGON) {
            return BossEvent.BossBarColor.PINK;
        } else if (entityType.is(ModRegistry.BOSSES_ENTITY_TYPE_TAG)) {
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
                    default -> barColors.miscColor;
                };
            }
        }
    }
}
