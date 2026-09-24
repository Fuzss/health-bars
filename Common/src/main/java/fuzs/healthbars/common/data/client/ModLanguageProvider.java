package fuzs.healthbars.common.data.client;

import fuzs.healthbars.common.HealthBars;
import fuzs.healthbars.common.client.handler.KeyBindingHandler;
import fuzs.puzzleslib.common.api.client.data.v3.language.AbstractLanguageProvider;
import fuzs.puzzleslib.common.api.data.v3.core.DataProviderContext;

public class ModLanguageProvider extends AbstractLanguageProvider {

    public ModLanguageProvider(DataProviderContext context) {
        super(context);
    }

    @Override
    public void addTranslations() {
        this.addKeyCategory(HealthBars.MOD_ID, HealthBars.MOD_NAME);
        this.add(KeyBindingHandler.TOGGLE_HEALTH_BARS_KEY_MAPPING, "Toggle Health Bars");
        this.add(KeyBindingHandler.KEY_STATUS_MESSAGE, "Render Health Bars: %s");
    }
}
