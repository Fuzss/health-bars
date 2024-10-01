package net.torocraft.torohealth.bars;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BarStates {
    private static final Map<Integer, HealthTracker> STATES = new HashMap<>();

    public static List<BarParticle> PARTICLES = new ArrayList<>();
    private static int tickCount = 0;

    public static HealthTracker getState(LivingEntity entity) {
        int id = entity.getId();
        HealthTracker state = STATES.get(id);
        if (state == null) {
            state = new HealthTracker(entity);
            STATES.put(id, state);
        }
        return state;
    }

    public static void tick() {
        for (HealthTracker state : STATES.values()) {
            state.tick();
        }

        if (tickCount % 200 == 0) {
            cleanCache();
        }

        PARTICLES.forEach(p -> p.tick());
        PARTICLES.removeIf(p -> p.age > 50);

        tickCount++;
    }

    private static void cleanCache() {
        STATES.entrySet().removeIf(BarStates::stateExpired);
    }

    private static boolean stateExpired(Map.Entry<Integer, HealthTracker> entry) {
        if (entry.getValue() == null) {
            return true;
        }

        Minecraft minecraft = Minecraft.getInstance();
        Entity entity = minecraft.level.getEntity(entry.getKey());

        if (!(entity instanceof LivingEntity)) {
            return true;
        }

        if (!minecraft.level.hasChunkAt(entity.blockPosition())) {
            return true;
        }

        return !entity.isAlive();
    }
}
