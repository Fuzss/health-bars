package fuzs.healthbars.data.client;

import fuzs.healthbars.HealthBars;
import fuzs.healthbars.client.handler.KeyBindingHandler;
import fuzs.puzzleslib.api.client.data.v2.AbstractLanguageProvider;
import fuzs.puzzleslib.api.data.v2.core.DataProviderContext;

public class ModLanguageProvider extends AbstractLanguageProvider {

    public ModLanguageProvider(DataProviderContext context) {
        super(context);
    }

    @Override
    public void addTranslations(TranslationBuilder builder) {
        builder.addKeyCategory(HealthBars.MOD_ID, HealthBars.MOD_NAME);
        builder.add(KeyBindingHandler.TOGGLE_KEY_MAPPING, "Toggle Health Bars");
        builder.add(KeyBindingHandler.KEY_STATUS_MESSAGE, "Render Health Bars: %s");
    }
}
