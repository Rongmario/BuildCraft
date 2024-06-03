package buildcraft.core.compat.module.ic2;

import buildcraft.api.statements.*;
import buildcraft.core.BCCoreSprites;
import buildcraft.core.statements.BCStatement;
import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import buildcraft.lib.misc.LocaleUtil;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.comp.Energy;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

public class TriggerEU extends BCStatement implements ITriggerExternal {
    private final boolean high;

    public TriggerEU(boolean high) {
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

    private static Energy getEnergy(TileEntity tile) {
        if (tile instanceof TileEntityBlock) {
            if (((TileEntityBlock) tile).hasComponent(Energy.class)) {
                return ((TileEntityBlock) tile).getComponent(Energy.class);
            }
        }
        return null;
    }

    public static boolean isTriggeringTile(TileEntity tile) {
        Energy energy = getEnergy(tile);
        if (energy != null) {
            return energy.getCapacity() > 0;
        }
        return false;
    }

    @Override
    public boolean isTriggerActive(TileEntity target, EnumFacing side, IStatementContainer source, IStatementParameter[] parameters) {
        Energy energy = getEnergy(target);
        if (energy != null) {
            double stored = energy.getEnergy();
            double max = energy.getCapacity();
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
        return IC2Statements.TRIGGER_EU;
    }
}
