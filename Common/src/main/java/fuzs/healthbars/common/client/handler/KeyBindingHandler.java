package fuzs.healthbars.common.client.handler;

import fuzs.healthbars.common.HealthBars;
import fuzs.healthbars.common.config.ClientConfig;
import fuzs.puzzleslib.common.api.client.core.v1.context.KeyMappingsContext;
import fuzs.puzzleslib.common.api.client.key.v1.KeyActivationHandler;
import fuzs.puzzleslib.common.api.client.key.v1.KeyMappingHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.ModConfigSpec;

public class KeyBindingHandler {
    public static final KeyMapping TOGGLE_HEALTH_BARS_KEY_MAPPING = KeyMappingHelper.registerUnboundKeyMapping(
            HealthBars.id("toggle_health_bars"));
    private static final Component ON_COMPONENT = Component.empty()
            .append(CommonComponents.OPTION_ON)
            .withStyle(ChatFormatting.GREEN);
    private static final Component OFF_COMPONENT = Component.empty()
            .append(CommonComponents.OPTION_OFF)
            .withStyle(ChatFormatting.RED);
    public static final String KEY_STATUS_MESSAGE = TOGGLE_HEALTH_BARS_KEY_MAPPING.getName() + ".message";

    public static void onRegisterKeyMappings(KeyMappingsContext context) {
        context.registerKeyMapping(KeyBindingHandler.TOGGLE_HEALTH_BARS_KEY_MAPPING,
                KeyActivationHandler.forGame((Minecraft minecraft) -> {
                    ModConfigSpec.ConfigValue<Boolean> enableRendering = HealthBars.CONFIG.get(ClientConfig.class).anyRendering;
                    enableRendering.set(!enableRendering.get());
                    enableRendering.save();
                    Component component = Component.translatable(KEY_STATUS_MESSAGE,
                            enableRendering.get() ? ON_COMPONENT : OFF_COMPONENT);
                    minecraft.gui.setOverlayMessage(component, false);
                }));
    }
}
