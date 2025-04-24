package fuzs.healthbars.init;

import fuzs.healthbars.HealthBars;
import fuzs.healthbars.client.helper.HealthTracker;
import fuzs.puzzleslib.api.attachment.v4.DataAttachmentRegistry;
import fuzs.puzzleslib.api.attachment.v4.DataAttachmentType;
import fuzs.puzzleslib.api.init.v3.registry.RegistryManager;
import fuzs.puzzleslib.api.init.v3.tags.TagFactory;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public class ModRegistry {
    static final RegistryManager REGISTRIES = RegistryManager.from(HealthBars.MOD_ID);
    public static final Holder.Reference<SimpleParticleType> DAMAGE_VALUE_PARTICLE_TYPE = REGISTRIES.registerParticleType(
            "damage_value");
    public static final DataAttachmentType<Entity, HealthTracker> HEALTH_TRACKER_ATTACHMENT_TYPE = DataAttachmentRegistry.<HealthTracker>entityBuilder()
            .build(HealthBars.id("health_tracker"));

    public static final TagKey<EntityType<?>> BOSSES_ENTITY_TYPE_TAG = TagFactory.COMMON.registerEntityTypeTag("bosses");

    public static void bootstrap() {
        // NO-OP
    }
}
