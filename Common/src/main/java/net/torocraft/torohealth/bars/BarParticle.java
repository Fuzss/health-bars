package net.torocraft.torohealth.bars;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class BarParticle {
    static final RandomSource RANDOM = RandomSource.create();

    public int damage;

    public double x;
    public double y;
    public double z;
    public double xPrev;
    public double yPrev;
    public double zPrev;

    public int age;

    public double vx;
    public double vy;
    public double vz;

    public BarParticle(Entity entity, int damage) {
        Minecraft client = Minecraft.getInstance();
        Vec3 entityLocation = entity.position().add(0, entity.getBbHeight() / 2, 0);
        Vec3 cameraLocation = client.gameRenderer.getMainCamera().getPosition();
        double offsetBy = entity.getBbWidth();
        Vec3 offset = cameraLocation.subtract(entityLocation).normalize().scale(offsetBy);
        Vec3 pos = entityLocation.add(offset);

        this.age = 0;
        this.damage = damage;

        this.vx = RANDOM.nextGaussian() * 0.04;
        this.vy = 0.10 + (RANDOM.nextGaussian() * 0.05);
        this.vz = RANDOM.nextGaussian() * 0.04;

        this.x = pos.x;
        this.y = pos.y;
        this.z = pos.z;

        this.xPrev = this.x;
        this.yPrev = this.y;
        this.zPrev = this.z;
    }

    public void tick() {
        this.age++;
        this.xPrev = this.x;
        this.yPrev = this.y;
        this.zPrev = this.z;
        this.x += this.vx;
        this.y += this.vy;
        this.z += this.vz;
//        this.vx *= 0.98;
        this.vy -= 0.01;
//        this.vz *= 0.98;
    }
}
