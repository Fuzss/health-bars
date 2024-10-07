package fuzs.healthbars.config;

import fuzs.puzzleslib.api.config.v3.Config;
import fuzs.puzzleslib.api.config.v3.ConfigCore;
import fuzs.puzzleslib.api.config.v3.serialization.ConfigDataSet;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.EntityType;

import java.util.List;

public class ClientConfig implements ConfigCore {
    @Config(description = "Options controlling health bars rendered as part of the gui.")
    public final Gui gui = new Gui();
    @Config(description = "Options controlling health bars rendered above entities in the level.")
    public final Level level = new Level();
    @Config(
            description = {
                    "The raytrace range for finding a picked entity.",
                    "Setting this to -1 will make it use the player entity interaction range, which is 3 in survival."
            }
    )
    @Config.IntRange(min = -1, max = 128)
    public int pickedEntityInteractionRange = -1;
    @Config(
            description = {
                    "Coyote time in seconds after which a no longer picked entity will still show the plaques.",
                    "Set to -1 to keep the old entity until a new one is picked by the crosshair."
            }
    )
    @Config.IntRange(min = -1)
    public int pickedEntityDelay = 2;

    public static abstract class BarConfig implements ConfigCore {
        @Config(description = "Options controlling how bars render for different kinds of mobs.")
        public final BarColors barColors = new BarColors();
        @Config(description = "Options controlling how recently received damage / gained health is shown.")
        public final DamageValues damageValues = new DamageValues();
        @Config(description = "The default width multiplier for the health bar display.")
        @Config.IntRange(min = 1, max = 4)
        public int healthBarWidth = 3;
        @Config(description = "Increase the health bar width for mobs with a lot of health, like bosses.")
        public boolean scaleBarWidthByHealth = true;
    }

    public static class Gui extends BarConfig {
        @Config(description = "Offset in pixels on the horizontal axis from the screen border.")
        @Config.IntRange(min = 0)
        public int offsetWidth = 9;
        @Config(description = "Offset in pixels on the vertical axis from the screen border.")
        @Config.IntRange(min = 0)
        public int offsetHeight = 9;
        @Config(description = "Choose a position on the screen to show the interface at.")
        public AnchorPoint anchorPoint = AnchorPoint.TOP_LEFT;
        @Config(description = "Allow rendering the visual mob display as part of the health bar overlay.")
        public boolean renderEntityDisplay = true;
        @Config(description = "Allow rendering the health and possibly armor text.")
        public boolean renderAttributeComponents = true;
        @Config(
                name = "mob_render_offsets",
                description = "Custom vertical offsets for rendered entities. Offsets scale with entity dimensions, meaning larger entities require larger values."
        )
        List<String> mobRenderOffsetsRaw = List.of("minecraft:allay,-0.05", "minecraft:armadillo,-0.1",
                "minecraft:axolotl,0.1", "minecraft:bat,-0.3", "minecraft:bee,-0.1", "minecraft:camel,0.3",
                "minecraft:cat,-0.25", "minecraft:chicken,0.15", "minecraft:cod,0.25", "minecraft:endermite,0.3",
                "minecraft:fox,-0.15", "minecraft:frog,-0.05", "minecraft:ghast,-1.0", "minecraft:glow_squid,-0.2",
                "minecraft:horse,0.1", "minecraft:llama,0.3", "minecraft:ocelot,-0.15", "minecraft:parrot,-0.3",
                "minecraft:polar_bear,-0.3", "minecraft:salmon,0.15", "minecraft:silverfish,0.35",
                "minecraft:squid,-0.2", "minecraft:tadpole,0.3", "minecraft:trader_llama,0.3",
                "minecraft:tropical_fish,0.15", "minecraft:vex,-0.2", "minecraft:warden,0.4"
        );

        public Gui() {
            this.healthBarWidth = 3;
        }

        public ConfigDataSet<EntityType<?>> mobRenderOffsets;

        @Override
        public void afterConfigReload() {
            this.mobRenderOffsets = ConfigDataSet.from(Registries.ENTITY_TYPE, this.mobRenderOffsetsRaw, double.class);
        }
    }

    public static class Level extends BarConfig {
        @Config(description = "Show plaques for the entity picked by the crosshair only.")
        public boolean pickedEntity = false;
        @Config(description = "Custom scale for rendering plaques.")
        @Config.DoubleRange(min = 0.05, max = 2.0)
        public double plaqueScale = 0.5;
        @Config(
                description = "Dynamically increase plaque size the further away the camera is to simplify readability."
        )
        public boolean scaleWithDistance = true;
        @Config(
                description = "Distance to the mob at which plaques will still be visible. The distance is halved when the mob is crouching."
        )
        @Config.IntRange(min = 0)
        public int maxRenderDistance = 96;
        @Config(description = "Allow rendering the mob display name above the health bar. This will replace the vanilla name plate rendering.")
        public boolean renderTitleComponent = true;
        @Config(description = "Allow rendering the health text.")
        public boolean renderHealthComponent = true;
        @Config(description = "Allow rendering the heart sprite as part of the health text.")
        public boolean renderSpriteComponent = true;
        @Config(description = "Show a black background box behind plaques. Disabled by default as it doesn't work with shaders.")
        public boolean renderBackground = false;
        @Config(description = "Always render plaques with full brightness to be most visible, ignoring local lighting conditions.")
        public FullBrightRendering fullBrightness = FullBrightRendering.UNOBSTRUCTED;
        @Config(description = "Offset in pixels on the vertical axis from default position.")
        public int offsetHeight = 0;
        @Config(description = "Show plaques from mobs obstructed by walls the player cannot see through, similar to the nameplates of other players.")
        public boolean behindWalls = true;

        public Level() {
            this.healthBarWidth = 2;
        }
    }

    public static class DamageValues implements ConfigCore {
        @Config(description = "Allow values from receiving damage or healing to show.")
        public boolean renderDamageValues = true;
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

    public enum FullBrightRendering {
        ALWAYS,
        UNOBSTRUCTED,
        NEVER
    }
}
