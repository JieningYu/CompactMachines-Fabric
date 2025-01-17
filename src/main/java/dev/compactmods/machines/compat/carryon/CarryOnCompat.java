package dev.compactmods.machines.compat.carryon;

import dev.compactmods.machines.machine.CompactMachineBlock;
import dev.compactmods.machines.api.room.RoomSize;
import net.minecraft.core.Registry;

public class CarryOnCompat {

    private static final String MOD_ID = "carryon";

    public static void sendIMC() {
        // Denies the machine blocks from being picked up by CarryOn users.
        // Prevents duplication of data on block movement.
        for(var size : RoomSize.values()) {
            final var regName = Registry.BLOCK.getKey(CompactMachineBlock.getBySize(size));
//            if(regName != null)
//                InterModComms.sendTo(MOD_ID, "blacklistBlock", regName::toString);
        }
    }
}
