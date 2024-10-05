package fuzs.immersivedamageindicators.init;

import fuzs.immersivedamageindicators.ImmersiveDamageIndicators;
import fuzs.immersivedamageindicators.client.helper.HealthTracker;
import fuzs.puzzleslib.api.attachment.v4.DataAttachmentRegistry;
import fuzs.puzzleslib.api.attachment.v4.DataAttachmentType;
import fuzs.puzzleslib.api.core.v1.utility.ResourceLocationHelper;
import fuzs.puzzleslib.api.init.v3.registry.RegistryManager;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public class ModRegistry {
    static final RegistryManager REGISTRIES = RegistryManager.from(ImmersiveDamageIndicators.MOD_ID);
    public static final Holder.Reference<SimpleParticleType> DAMAGE_VALUE_PARTICLE_TYPE = REGISTRIES.registerParticleType(
            "damage_value");
    public static final TagKey<EntityType<?>> BOSSES_ENTITY_TYPE_TAG = TagKey.create(Registries.ENTITY_TYPE,
            ResourceLocationHelper.parse("c:bosses")
    );
    public static final DataAttachmentType<Entity, HealthTracker> HEALTH_TRACKER_ATTACHMENT_TYPE = DataAttachmentRegistry.<HealthTracker>entityBuilder()
            .build(ImmersiveDamageIndicators.id("health_tracker"));

    public static void bootstrap() {
        // NO-OP
    }
}
