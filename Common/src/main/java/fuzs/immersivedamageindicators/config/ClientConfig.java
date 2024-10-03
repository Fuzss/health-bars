package fuzs.immersivedamageindicators.config;

import fuzs.puzzleslib.api.config.v3.Config;
import fuzs.puzzleslib.api.config.v3.ConfigCore;
import net.minecraft.ChatFormatting;
import net.minecraft.world.BossEvent;

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
    @Config.IntRange(min = -1, max = 64)
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
    public BarColors barColors = new BarColors();
    @Config
    public ParticleColors particleColors = new ParticleColors();

    public static class Hud implements ConfigCore {
        @Config
        public int distance = 60;
        @Config
        public int x = 4;
        @Config
        public int y = 4;
        @Config
        public int scale = 1;
        @Config
        public int hideDelay = 20;
        @Config
        public AnchorPoint anchorPoint = AnchorPoint.TOP_LEFT;
        @Config
        public boolean showEntity = true;
        @Config
        public boolean showBar = true;
        @Config
        public boolean showSkin = true;
    }

    public static class ParticleColors implements ConfigCore {
        @Config
        public ChatFormatting damageColor = ChatFormatting.RED;
        @Config
        public ChatFormatting healColor = ChatFormatting.GREEN;
    }

    public static class BarColors implements ConfigCore {
        @Config
        public BossEvent.BossBarColor monsterColor = BossEvent.BossBarColor.RED;
        @Config
        public BossEvent.BossBarColor ambientColor = BossEvent.BossBarColor.YELLOW;
        @Config
        public BossEvent.BossBarColor friendlyColor = BossEvent.BossBarColor.GREEN;
        @Config
        public BossEvent.BossBarColor aquaticColor = BossEvent.BossBarColor.BLUE;
        @Config
        public BossEvent.BossBarColor miscColor = BossEvent.BossBarColor.WHITE;
        @Config
        public NotchedStyle notchedStyle = NotchedStyle.COLORED;
        @Config
        @Config.IntRange(min = 1, max = 6)
        public int minBarWidth = 3;
    }

    public enum AnchorPoint {
        TOP_LEFT,
        TOP_CENTER,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_CENTER,
        BOTTOM_RIGHT
    }

    public enum NotchedStyle {
        ALL,
        COLORED,
        NONE
    }
}
