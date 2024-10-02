package fuzs.immersivedamageindicators.client.helper;

import fuzs.immersivedamageindicators.ImmersiveDamageIndicators;
import fuzs.immersivedamageindicators.config.ClientConfig;
import fuzs.immersivedamageindicators.init.ModRegistry;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;

public class HealthBarHelper {

    public static int getBarWidthByScale(int scale) {
        return 45 * scale + 2;
    }

    public static int getBarScaleFromHealth(float health) {
        return Mth.ceil(health / 50.0F);
    }

    public static int pickBarColor(LivingEntity livingEntity) {
        ClientConfig.BarColors config = ImmersiveDamageIndicators.CONFIG.get(ClientConfig.class).barColors;
        EntityType<?> entityType = livingEntity.getType();
        if (entityType.is(EntityTypeTags.ILLAGER)) {
            return config.illagerColor;
        } else if (entityType.is(EntityTypeTags.ARTHROPOD)) {
            return config.arthropodColor;
        } else if (entityType.is(EntityTypeTags.AQUATIC)) {
            return config.aquaticColor;
        } else if (entityType == EntityType.ENDER_DRAGON) {
            return config.dragonColor;
        } else if (entityType.is(ModRegistry.BOSSES_ENTITY_TYPE_TAG)) {
            return config.bossColor;
        } else if (livingEntity instanceof Enemy) {
            return config.monsterColor;
        } else {
            return config.friendColor;
        }
    }
}
