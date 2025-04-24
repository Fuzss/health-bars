package fuzs.healthbars.client.helper;

import fuzs.healthbars.config.ClientConfig;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.LivingEntity;

public class HealthTrackerRenderState {
    public int maxHealth;
    public Component healthComponent = CommonComponents.EMPTY;
    public Component displayName = CommonComponents.EMPTY;
    public int healthData;
    public float healthProgress;
    public double renderOffset;
    public int armorValue;
    public float barProgress;
    public float backgroundBarProgress;
    public BossEvent.BossBarColor barColor = BossEvent.BossBarColor.WHITE;
    public ClientConfig.NotchedStyle notchedStyle = ClientConfig.NotchedStyle.COLORED;

    private HealthTrackerRenderState() {
        // NO-OP
    }

    public static HealthTrackerRenderState extractRenderState(HealthTracker healthTracker, LivingEntity livingEntity, float partialTick, ClientConfig.BarColors barColors) {
        HealthTrackerRenderState renderState = new HealthTrackerRenderState();
        renderState.maxHealth = healthTracker.getData().maxHealth();
        renderState.healthComponent = healthTracker.getData().getHealthComponent();
        renderState.displayName = healthTracker.getData().displayName();
        renderState.healthData = healthTracker.getHealthDelta();
        renderState.healthProgress = healthTracker.getHealthProgress();
        renderState.renderOffset = healthTracker.getData().renderOffset();
        renderState.armorValue = healthTracker.getData().armorValue();
        renderState.barProgress = healthTracker.getBarProgress(partialTick);
        renderState.backgroundBarProgress = healthTracker.getBackgroundBarProgress(partialTick);
        renderState.barColor = HealthBarHelper.getBarColor(livingEntity, barColors);
        renderState.notchedStyle = barColors.notchedStyle;
        return renderState;
    }
}
