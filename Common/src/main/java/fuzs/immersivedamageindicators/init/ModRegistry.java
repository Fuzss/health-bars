package fuzs.immersivedamageindicators.init;

import fuzs.immersivedamageindicators.ImmersiveDamageIndicators;
import fuzs.puzzleslib.api.attachment.v4.DataAttachmentRegistry;
import fuzs.puzzleslib.api.attachment.v4.DataAttachmentType;
import net.minecraft.world.entity.Entity;
import net.torocraft.torohealth.bars.HealthTracker;

public class ModRegistry {
    public static final DataAttachmentType<Entity, HealthTracker> HEALTH_TRACKER_ATTACHMENT_TYPE = DataAttachmentRegistry.<HealthTracker>entityBuilder()
            .build(ImmersiveDamageIndicators.id("health_tracker"));

    public static void bootstrap() {
        // NO-OP
    }
}
