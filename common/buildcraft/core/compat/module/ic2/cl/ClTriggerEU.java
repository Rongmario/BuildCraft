package buildcraft.core.compat.module.ic2.cl;

import buildcraft.api.statements.*;
import buildcraft.core.BCCoreSprites;
import buildcraft.core.statements.BCStatement;
import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import buildcraft.lib.misc.LocaleUtil;
import ic2.api.classic.tile.machine.IEUStorage;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

public class ClTriggerEU extends BCStatement implements ITriggerExternal {
    private final boolean high;

    public ClTriggerEU(boolean high) {
        super("buildcraft:energyStoredEU" + (high ? "high" : "low"));
        this.high = high;
    }

    @Override
    public SpriteHolderRegistry.SpriteHolder getSprite() {
        return high ? BCCoreSprites.TRIGGER_POWER_HIGH : BCCoreSprites.TRIGGER_POWER_LOW;
    }

    @Override
    public String getDescription() {
        return LocaleUtil.localize("gate.trigger.machine.energyStored." + (high ? "high" : "low"));
    }

    private static IEUStorage getEnergy(TileEntity tile) {
        if (tile instanceof IEUStorage) {
            return (IEUStorage) tile;
        }
        return null;
    }

    public static boolean isTriggeringTile(TileEntity tile) {
        IEUStorage energy = getEnergy(tile);
        if (energy != null) {
            return energy.getMaxEU() > 0;
        }
        return false;
    }

    @Override
    public boolean isTriggerActive(TileEntity target, EnumFacing side, IStatementContainer source, IStatementParameter[] parameters) {
        IEUStorage energy = getEnergy(target);
        if (energy != null) {
            double stored = energy.getStoredEU();
            double max = energy.getMaxEU();
            if (max > 0) {
                double level = stored / max;
                if (high) {
                    return level > 0.95;
                } else {
                    return level < 0.05;
                }
            }
        }
        return false;
    }

    @Override
    public IStatement[] getPossible() {
        return ClIC2Statements.TRIGGER_EU;
    }
}
