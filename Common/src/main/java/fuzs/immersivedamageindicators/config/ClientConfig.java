package fuzs.immersivedamageindicators.config;

import fuzs.puzzleslib.api.config.v3.Config;
import fuzs.puzzleslib.api.config.v3.ConfigCore;
import fuzs.puzzleslib.api.config.v3.serialization.ConfigDataSet;
import fuzs.puzzleslib.api.config.v3.serialization.KeyedValueProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.EntityType;

import java.util.List;

public class ClientConfig implements ConfigCore {
    private static final String KEY_GENERAL_CATEGORY = "general";

    @Config(category = KEY_GENERAL_CATEGORY, description = "Show plaques for the entity picked by the crosshair only.")
    public boolean pickedEntity = false;
    @Config(
            category = KEY_GENERAL_CATEGORY, description = {
            "The raytrace range for finding a picked entity.",
            "Setting this to -1 will make it use the player entity interaction range, which is 3 in survival."
    }
    )
    @Config.IntRange(min = -1, max = 128)
    public int pickedEntityInteractionRange = -1;
    @Config(
            category = KEY_GENERAL_CATEGORY, description = {
            "Coyote time in seconds after which a no longer picked entity will still show the plaques.",
            "Set to -1 to keep the old entity until a new one is picked by the crosshair."
    }
    )
    @Config.IntRange(min = -1)
    public int pickedEntityDelay = 2;
    @Config(category = KEY_GENERAL_CATEGORY, description = "Custom scale for rendering plaques.")
    @Config.DoubleRange(min = 0.05, max = 2.0)
    public double plaqueScale = 0.5;
    @Config(
            category = KEY_GENERAL_CATEGORY,
            description = "Dynamically increase plaque size the further away the camera is to simplify readability."
    )
    public boolean scaleWithDistance = true;
    @Config(
            category = KEY_GENERAL_CATEGORY,
            description = "Distance to the mob at which plaques will still be visible. The distance is halved when the mob is crouching."
    )
    @Config.IntRange(min = 0)
    public int maxRenderDistance = 48;

    @Config
    public final Hud hud = new Hud();
    @Config
    public final World world = new World();

    public static abstract class BarConfig implements ConfigCore {
        @Config
        public final BarColors barColors = new BarColors();
        @Config
        public final DamageValues damageValues = new DamageValues();
        @Config(description = "The default width multiplier for the health bar display.")
        @Config.IntRange(min = 1, max = 4)
        public int healthBarWidth = 3;
        @Config(description = "Increase the health bar width for mobs with a lot of health, like bosses.")
        public boolean scaleBarWidthByHealth = true;
    }

    public static class Hud extends BarConfig {
        @Config
        @Config.IntRange(min = 0)
        public int offsetX = 9;
        @Config
        @Config.IntRange(min = 0)
        public int offsetY = 9;
        @Config
        public int scale = 1;
        @Config
        public AnchorPoint anchorPoint = AnchorPoint.TOP_LEFT;
        @Config(name = "mob_offset_overrides")
        List<String> mobOffsetOverridesRaw = List.of("minecraft:allay,-0.05", "minecraft:armadillo,-0.2",
                "minecraft:axolotl,0.1", "minecraft:bat,-0.3", "minecraft:bee,-0.1", "minecraft:camel,0.3",
                "minecraft:cat,-0.25", "minecraft:chicken,0.15", "minecraft:cod,0.25", "minecraft:endermite,0.3",
                "minecraft:fox,-0.15", "minecraft:frog,-0.05", "minecraft:ghast,-1.0", "minecraft:glow_squid,-0.2",
                "minecraft:horse,0.1", "minecraft:llama,0.3", "minecraft:ocelot,-0.15", "minecraft:parrot,-0.3",
                "minecraft:polar_bear,-0.3", "minecraft:salmon,0.15", "minecraft:silverfish,0.35",
                "minecraft:squid,-0.2", "minecraft:tadpole,0.3", "minecraft:trader_llama,0.3",
                "minecraft:tropical_fish,0.15", "minecraft:vex,-0.2", "minecraft:warden,0.4"
        );

        public Hud() {
            this.healthBarWidth = 3;
        }

        public ConfigDataSet<EntityType<?>> mobOffsetOverrides;

        @Override
        public void afterConfigReload() {
            this.mobOffsetOverrides = ConfigDataSet.from(Registries.ENTITY_TYPE, this.mobOffsetOverridesRaw,
                    double.class
            );
        }
    }

    public static class World extends BarConfig {

        public World() {
            this.healthBarWidth = 2;
        }
    }

    public static class DamageValues implements ConfigCore {
        @Config(description = "The text color when displaying negative values, when the entity has received damage.")
        public ChatFormatting damageColor = ChatFormatting.RED;
        @Config(description = "The text color when displaying positive values, when the entity has healed.")
        public ChatFormatting healColor = ChatFormatting.GREEN;
        @Config(description = "Draw a bold black outline around damage values, just like the experience level in the player hud.")
        public boolean strongTextOutline = true;

        public int getTextColor(int damageAmount) {
            ChatFormatting chatFormatting = damageAmount > 0 ? this.healColor : this.damageColor;
            return chatFormatting.isColor() ? chatFormatting.getColor() : -1;
        }
    }

    public static class BarColors implements ConfigCore {
        @Config(description = "The health bar color for monsters, such as zombies, blazes and vindicators.")
        public BossEvent.BossBarColor monsterColor = BossEvent.BossBarColor.RED;
        @Config(description = "The health bar color for ambient mobs, such as bats, snow golems and villagers.")
        public BossEvent.BossBarColor ambientColor = BossEvent.BossBarColor.YELLOW;
        @Config(description = "The health bar color for animals, such as pigs, armadillos and striders.")
        public BossEvent.BossBarColor friendlyColor = BossEvent.BossBarColor.GREEN;
        @Config(description = "The health bar color for passive sea life, such as cod, squid and axolotl.")
        public BossEvent.BossBarColor aquaticColor = BossEvent.BossBarColor.BLUE;
        @Config(description = "The health bar color for all unclassified mobs.")
        public BossEvent.BossBarColor miscColor = BossEvent.BossBarColor.WHITE;
        @Config(description = "Choose a style for applying the notch texture on top of health bars.")
        public NotchedStyle notchedStyle = NotchedStyle.COLORED;
    }

    public enum NotchedStyle {
        ALL,
        COLORED,
        NONE
    }
}
