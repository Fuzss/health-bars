package fuzs.healthbars.client.renderer.entity.state;

import fuzs.healthbars.HealthBars;
import fuzs.healthbars.client.helper.HealthBarHelper;
import fuzs.healthbars.world.entity.HealthTracker;
import fuzs.healthbars.client.helper.GuiSpritesHelper;
import fuzs.healthbars.config.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.jspecify.annotations.Nullable;

public class HealthTrackerRenderState {
    public Component displayName = CommonComponents.EMPTY;
    public int backgroundColor;
    public int health;
    public int maxHealth;
    public int armorValue;
    public int toughnessValue;
    public double renderOffset;
    @Nullable public Identifier healthSprite;
    @Nullable public Identifier armorSprite;
    @Nullable public Identifier toughnessSprite;
    public int healthData;
    public float healthProgress;
    public int barWidth;
    public float barProgress;
    public float backgroundBarProgress;
    public BossEvent.BossBarColor barColor = BossEvent.BossBarColor.WHITE;
    public ClientConfig.NotchedStyle notchedStyle = ClientConfig.NotchedStyle.COLORED;

    private HealthTrackerRenderState() {
        // NO-OP
    }

    public static HealthTrackerRenderState extractRenderState(HealthTracker healthTracker, LivingEntity livingEntity, float partialTick, ClientConfig.BarConfig config) {
        HealthTrackerRenderState renderState = new HealthTrackerRenderState();
        renderState.displayName = livingEntity.getDisplayName();
        renderState.backgroundColor =
                config.textBackground ? Minecraft.getInstance().options.getBackgroundColor(0.25F) : 0;
        renderState.health = Mth.ceil(livingEntity.getHealth());
        renderState.maxHealth = Mth.ceil(livingEntity.getMaxHealth());
        renderState.armorValue = livingEntity.getArmorValue();
        renderState.toughnessValue = Mth.floor(livingEntity.getAttributeValue(Attributes.ARMOR_TOUGHNESS));
        renderState.renderOffset = HealthBars.CONFIG.get(ClientConfig.class).gui.mobRenderOffsets.<Double>getOptional(
                livingEntity.getType(),
                0).orElse(0.0);
        if (config.renderComponentSprites) {
            renderState.healthSprite = GuiSpritesHelper.getSprite(livingEntity);
            renderState.armorSprite = GuiSpritesHelper.ARMOR_FULL_SPRITE;
            renderState.toughnessSprite = GuiSpritesHelper.TOUGHNESS_FULL_SPRITE;
        } else {
            renderState.healthSprite = renderState.armorSprite = renderState.toughnessSprite = null;
        }

        renderState.healthData = healthTracker.getHealthDelta();
        renderState.healthProgress = healthTracker.getHealthProgress();
        renderState.barWidth = HealthBarHelper.getBarWidth(config, renderState.maxHealth);
        renderState.barProgress = healthTracker.getBarProgress(partialTick);
        renderState.backgroundBarProgress = healthTracker.getBackgroundBarProgress(partialTick);
        renderState.barColor = HealthBarHelper.getBarColor(livingEntity, config.barColors);
        renderState.notchedStyle = config.barColors.notchedStyle;
        return renderState;
    }

    public Component getHealthComponent() {
        return Component.literal(this.health + "/" + this.maxHealth);
    }

    public boolean drawShadow() {
        return this.backgroundColor == 0;
    }
}
