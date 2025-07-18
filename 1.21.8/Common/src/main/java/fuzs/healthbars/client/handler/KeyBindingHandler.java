package fuzs.healthbars.client.handler;

import fuzs.healthbars.HealthBars;
import fuzs.healthbars.config.ClientConfig;
import fuzs.puzzleslib.api.client.core.v1.context.KeyMappingsContext;
import fuzs.puzzleslib.api.client.key.v1.KeyActivationHandler;
import fuzs.puzzleslib.api.client.key.v1.KeyMappingHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.ModConfigSpec;

public class KeyBindingHandler {
    public static final KeyMapping TOGGLE_KEY_MAPPING = KeyMappingHelper.registerUnboundKeyMapping(
            HealthBars.id("toggle_health_bars"));
    private static final Component ON_COMPONENT = Component.empty().append(CommonComponents.OPTION_ON).withStyle(
            ChatFormatting.GREEN);
    private static final Component OFF_COMPONENT = Component.empty().append(CommonComponents.OPTION_OFF).withStyle(
            ChatFormatting.RED);
    public static final String KEY_STATUS_MESSAGE = "key.toggle_health_bars.message";

    public static void onRegisterKeyMappings(KeyMappingsContext context) {
        context.registerKeyMapping(KeyBindingHandler.TOGGLE_KEY_MAPPING,
                KeyActivationHandler.forGame((Minecraft minecraft) -> {
                    ModConfigSpec.ConfigValue<Boolean> enableRendering = HealthBars.CONFIG.get(
                            ClientConfig.class).anyRendering;
                    enableRendering.set(!enableRendering.get());
                    Component component = Component.translatable(KEY_STATUS_MESSAGE,
                            enableRendering.get() ? ON_COMPONENT : OFF_COMPONENT
                    );
                    minecraft.gui.setOverlayMessage(component, false);
                })
        );
    }
}
