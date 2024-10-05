package fuzs.immersivedamageindicators.client.helper;

import fuzs.immersivedamageindicators.ImmersiveDamageIndicators;
import fuzs.immersivedamageindicators.config.ClientConfig;
import fuzs.immersivedamageindicators.init.ModRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;

public class HealthBarHelper {
    private static final ResourceLocation[] BAR_BACKGROUND_SPRITES = new ResourceLocation[]{
            ResourceLocation.withDefaultNamespace("boss_bar/pink_background"),
            ResourceLocation.withDefaultNamespace("boss_bar/blue_background"),
            ResourceLocation.withDefaultNamespace("boss_bar/red_background"),
            ResourceLocation.withDefaultNamespace("boss_bar/green_background"),
            ResourceLocation.withDefaultNamespace("boss_bar/yellow_background"),
            ResourceLocation.withDefaultNamespace("boss_bar/purple_background"),
            ResourceLocation.withDefaultNamespace("boss_bar/white_background")
    };
    private static final ResourceLocation[] BAR_PROGRESS_SPRITES = new ResourceLocation[]{
            ResourceLocation.withDefaultNamespace("boss_bar/pink_progress"),
            ResourceLocation.withDefaultNamespace("boss_bar/blue_progress"),
            ResourceLocation.withDefaultNamespace("boss_bar/red_progress"),
            ResourceLocation.withDefaultNamespace("boss_bar/green_progress"),
            ResourceLocation.withDefaultNamespace("boss_bar/yellow_progress"),
            ResourceLocation.withDefaultNamespace("boss_bar/purple_progress"),
            ResourceLocation.withDefaultNamespace("boss_bar/white_progress")
    };
    private static final ResourceLocation[] OVERLAY_BACKGROUND_SPRITES = new ResourceLocation[]{
            ResourceLocation.withDefaultNamespace("boss_bar/notched_6_background"),
            ResourceLocation.withDefaultNamespace("boss_bar/notched_10_background"),
            ResourceLocation.withDefaultNamespace("boss_bar/notched_12_background"),
            ResourceLocation.withDefaultNamespace("boss_bar/notched_20_background")
    };
    private static final ResourceLocation[] OVERLAY_PROGRESS_SPRITES = new ResourceLocation[]{
            ResourceLocation.withDefaultNamespace("boss_bar/notched_6_progress"),
            ResourceLocation.withDefaultNamespace("boss_bar/notched_10_progress"),
            ResourceLocation.withDefaultNamespace("boss_bar/notched_12_progress"),
            ResourceLocation.withDefaultNamespace("boss_bar/notched_20_progress")
    };

    public static ResourceLocation getBarSprite(BossEvent.BossBarColor barColor, boolean background) {
        return ImmersiveDamageIndicators.id(
                (background ? BAR_BACKGROUND_SPRITES : BAR_PROGRESS_SPRITES)[barColor.ordinal()].getPath());
    }

    public static ResourceLocation getOverlaySprite(BossEvent.BossBarOverlay barOverlay, boolean background) {
        return ImmersiveDamageIndicators.id(
                (background ? OVERLAY_BACKGROUND_SPRITES : OVERLAY_PROGRESS_SPRITES)[barOverlay.ordinal() -
                        1].getPath());
    }

    public static int getBarWidthByScale(int scale) {
        return 45 * scale + 2;
    }

    public static int getBarScaleFromHealth(float health) {
        return Mth.ceil(health / 50.0F);
    }

    public static BossEvent.BossBarColor getBarColor(LivingEntity livingEntity) {
        EntityType<?> entityType = livingEntity.getType();
        if (entityType == EntityType.ENDER_DRAGON) {
            return BossEvent.BossBarColor.PINK;
        } else if (entityType.is(ModRegistry.BOSSES_ENTITY_TYPE_TAG)) {
            return BossEvent.BossBarColor.PURPLE;
        } else {
            ClientConfig.BarColors config = ImmersiveDamageIndicators.CONFIG.get(ClientConfig.class).barColors;
            if (livingEntity instanceof Enemy) {
                return config.monsterColor;
            } else {
                return switch (entityType.getCategory()) {
                    case MONSTER -> config.monsterColor;
                    case AMBIENT, MISC -> config.ambientColor;
                    case WATER_AMBIENT, WATER_CREATURE, UNDERGROUND_WATER_CREATURE, AXOLOTLS -> config.aquaticColor;
                    case CREATURE -> config.friendlyColor;
                    default -> config.miscColor;
                };
            }
        }
    }
}
