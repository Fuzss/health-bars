package net.torocraft.torohealth.util;

import fuzs.puzzleslib.api.item.v2.ToolTypeHelper;
import net.minecraft.client.Minecraft;

public class PlayerWeaponHelper {

    public static boolean isHoldingWeapon() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.player != null && (ToolTypeHelper.INSTANCE.isWeapon(minecraft.player.getMainHandItem()) ||
                ToolTypeHelper.INSTANCE.isWeapon(minecraft.player.getOffhandItem()));
    }
}
