package fuzs.immersivedamageindicators.config;

import fuzs.puzzleslib.api.config.v3.Config;
import fuzs.puzzleslib.api.config.v3.ConfigCore;

public class ClientConfig implements ConfigCore {
    private static final String KEY_GENERAL_CATEGORY = "general";

    @Config(category = KEY_GENERAL_CATEGORY, description = "Show plaques for the entity picked by the crosshair only.")
    public boolean pickedEntity = false;
    @Config(category = KEY_GENERAL_CATEGORY, description = {"The raytrace range for finding a picked entity.", "Setting this to -1 will make it use the player entity interaction range, which is 3 in survival."})
    @Config.IntRange(min = -1, max = 64)
    public int pickedEntityInteractionRange = -1;
    @Config(category = KEY_GENERAL_CATEGORY, description = {
            "Coyote time in seconds after which a no longer picked entity will still show the plaques.", "Set to -1 to keep the old entity until a new one is picked by the crosshair."
    })
    @Config.IntRange(min = -1)
    public int pickedEntityDelay = 2;
    @Config(category = KEY_GENERAL_CATEGORY, description = "Custom scale for rendering plaques.")
    @Config.DoubleRange(min = 0.05, max = 2.0)
    public double plaqueScale = 0.5;
    @Config(category = KEY_GENERAL_CATEGORY, description = "Dynamically increase plaque size the further away the camera is to simplify readability.")
    public boolean scaleWithDistance = true;
    @Config(category = KEY_GENERAL_CATEGORY, description = "Distance to the mob at which plaques will still be visible. The distance is halved when the mob is crouching.")
    @Config.IntRange(min = 0)
    public int maxRenderDistance = 48;

    @Config
    public Hud hud = new Hud();
    @Config
    public BarColors barColors = new BarColors();
    @Config
    public InWorld inWorld = new InWorld();
    @Config
    public Particle particle = new Particle();

    static int parseRGBColor(String s) {
        try {
            return Integer.parseInt(s.startsWith("#") ? s.substring(1) : s, 16);
        } catch (NumberFormatException exception) {
            return -1;
        }
    }

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

    public static class Particle implements ConfigCore {
        @Config
        public boolean show = true;
        @Config(name = "damage_color")
        String rawDamageColor = "#FF0000";
        @Config(name = "heal_color")
        String rawHealColor = "#00FF00";
        @Config
        public int distance = 60;

        public int damageColor;
        public int healColor;

        @Override
        public void afterConfigReload() {
            this.damageColor = parseRGBColor(this.rawDamageColor);
            this.healColor = parseRGBColor(this.rawHealColor);
        }
    }

    public static class BarColors implements ConfigCore {
        @Config(name = "background_color")
        String rawBackgroundColor = "#8C8C8C";
        @Config(name = "friendly_color")
        String rawFriendlyColor = "#1DEC00";
        @Config(name = "aquatic_color")
        String rawAquaticColor = "#00B7EC";
        @Config(name = "arthropod_color")
        String rawArthropodColor = "#E9EC00";
        @Config(name = "monster_color")
        String rawMonsterColor = "#EC3500";
        @Config(name = "illager_color")
        String rawIllagerColor = "#ECECEC";
        @Config(name = "dragon_color")
        String rawDragonColor = "#EC00B8";
        @Config(name = "boss_color")
        String rawBossColor = "#7B00EC";
        @Config(description = "Dim background color amount.")
        @Config.DoubleRange(min = 0.0, max = 1.0)
        public double backgroundDim = 0.5;

        public int backgroundColor;
        public int friendColor;
        public int aquaticColor;
        public int arthropodColor;
        public int monsterColor;
        public int illagerColor;
        public int dragonColor;
        public int bossColor;

        @Override
        public void afterConfigReload() {
            this.backgroundColor = parseRGBColor(this.rawBackgroundColor);
            this.friendColor = parseRGBColor(this.rawFriendlyColor);
            this.aquaticColor = parseRGBColor(this.rawAquaticColor);
            this.arthropodColor = parseRGBColor(this.rawArthropodColor);
            this.monsterColor = parseRGBColor(this.rawMonsterColor);
            this.illagerColor = parseRGBColor(this.rawIllagerColor);
            this.dragonColor = parseRGBColor(this.rawDragonColor);
            this.bossColor = parseRGBColor(this.rawBossColor);
        }
    }

    public static class InWorld implements ConfigCore {
        @Config
        public boolean mode = true;
        @Config
        public int distance = 60;
        @Config
        public boolean onlyWhenLookingAt = false;
        @Config
        public boolean onlyWhenHurt = false;
    }

    public enum NumberType {
        NONE,
        LAST,
        CUMULATIVE
    }

    public enum AnchorPoint {
        TOP_LEFT,
        TOP_CENTER,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_CENTER,
        BOTTOM_RIGHT
    }
}
