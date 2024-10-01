package fuzs.immersivedamageindicators.config;

import fuzs.puzzleslib.api.config.v3.Config;
import fuzs.puzzleslib.api.config.v3.ConfigCore;

public class ClientConfig implements ConfigCore {
    @Config
    public Hud hud = new Hud();
    @Config
    public Bar bar = new Bar();
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
        @Config
        public boolean onlyWhenHurt = false;
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

    public static class Bar implements ConfigCore {
        @Config
        public NumberType damageNumberType = NumberType.LAST;
        @Config(name = "friend_color")
        String rawFriendColor = "#00FF00";
        @Config(name = "friend_color_secondary")
        String rawFriendColorSecondary = "#008000";
        @Config(name = "foe_color")
        String rawFoeColor = "#FF0000";
        @Config(name = "foe_color_secondary")
        String rawFoeColorSecondary = "#800000";

        public int friendColor;
        public int friendColorSecondary;
        public int foeColor;
        public int foeColorSecondary;

        @Override
        public void afterConfigReload() {
            this.friendColor = parseRGBColor(this.rawFriendColor);
            this.friendColorSecondary = parseRGBColor(this.rawFriendColorSecondary);
            this.foeColor = parseRGBColor(this.rawFoeColor);
            this.foeColorSecondary = parseRGBColor(this.rawFoeColorSecondary);
        }
    }

    public static class InWorld implements ConfigCore {
        @Config
        public Mode mode = Mode.NONE;
        @Config
        public int distance = 60;
        @Config
        public boolean onlyWhenLookingAt = false;
        @Config
        public boolean onlyWhenHurt = false;
    }

    public enum Mode {
        NONE,
        WHEN_HOLDING_WEAPON,
        ALWAYS
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
