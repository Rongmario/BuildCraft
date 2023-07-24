/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.gate;

import java.util.Locale;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

public enum EnumGateMaterial {
    BASIC(Blocks.BRICK_BLOCK, 1, 0, 0),
    IRON(Blocks.IRON_BLOCK, 2, 0, 0),
    GOLD(Blocks.GOLD_BLOCK, 4, 1, 0),
    DIAMOND(Blocks.DIAMOND_BLOCK, 8, 1, 0),
    EMERALD(Blocks.EMERALD_BLOCK, 4, 3, 3),
    QUARTZ(Blocks.QUARTZ_BLOCK, 2, 1, 1);

    public static final EnumGateMaterial[] VALUES = values();

    public final Block block;
    public final int numSlots;
    public final int triggerArgs;
    public final int actionArgs;

    public final String tag = name().toLowerCase(Locale.ROOT);

    EnumGateMaterial(Block block, int numSlots, int triggerArgs, int actionArgs) {
        this.block = block;
        this.numSlots = numSlots;
        this.triggerArgs = triggerArgs;
        this.actionArgs = actionArgs;
    }

    public static EnumGateMaterial getByOrdinal(int ord) {
        if (ord < 0 || ord >= VALUES.length) {
            return EnumGateMaterial.BASIC;
        }
        return VALUES[ord];
    }
}
