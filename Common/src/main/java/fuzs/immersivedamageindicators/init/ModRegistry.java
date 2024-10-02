package fuzs.immersivedamageindicators.init;

import fuzs.immersivedamageindicators.ImmersiveDamageIndicators;
import fuzs.puzzleslib.api.attachment.v4.DataAttachmentRegistry;
import fuzs.puzzleslib.api.attachment.v4.DataAttachmentType;
import fuzs.puzzleslib.api.core.v1.utility.ResourceLocationHelper;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import fuzs.immersivedamageindicators.client.helper.HealthTracker;

public class ModRegistry {
    public static final TagKey<EntityType<?>> BOSSES_ENTITY_TYPE_TAG = TagKey.create(Registries.ENTITY_TYPE,
            ResourceLocationHelper.parse("c:bosses")
    );
    public static final DataAttachmentType<Entity, HealthTracker> HEALTH_TRACKER_ATTACHMENT_TYPE = DataAttachmentRegistry.<HealthTracker>entityBuilder()
            .build(ImmersiveDamageIndicators.id("health_tracker"));

    public static void bootstrap() {
        // NO-OP
    }
}
