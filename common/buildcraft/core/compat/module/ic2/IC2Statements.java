package buildcraft.core.compat.module.ic2;

import buildcraft.api.statements.IActionExternal;
import buildcraft.api.statements.ITriggerExternal;
import buildcraft.core.statements.TriggerPower;
import ic2.core.block.wiring.TileEntityElectricBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import scala.actors.threadpool.Arrays;

import javax.annotation.Nonnull;
import java.util.Collection;

public class IC2Statements {
    public static final TriggerEU TRIGGER_EU_HIGH = new TriggerEU(true);
    public static final TriggerEU TRIGGER_EU_LOW = new TriggerEU(false);
    public static final TriggerEU[] TRIGGER_EU = { TRIGGER_EU_HIGH, TRIGGER_EU_LOW };

    public static final TriggerPowerItem CHARGE_FULL = new TriggerPowerItem(true, 0);
    public static final TriggerPowerItem CHARGE_LOW = new TriggerPowerItem(true, 1);
    public static final TriggerPowerItem CHARGE_HIGH = new TriggerPowerItem(true, 2);

    public static final TriggerPowerItem DISCHARGE_EMPTY = new TriggerPowerItem(false, 0);
    public static final TriggerPowerItem DISCHARGE_LOW = new TriggerPowerItem(false, 1);
    public static final TriggerPowerItem DISCHARGE_HIGH = new TriggerPowerItem(false, 2);

    public static final TriggerPowerItem[] CHARGE_ITEM = {CHARGE_LOW, CHARGE_HIGH, CHARGE_FULL};
    public static final TriggerPowerItem[] DISCHARGE_ITEM = {DISCHARGE_EMPTY, DISCHARGE_LOW, DISCHARGE_HIGH};

    public static final ActionSetRedstoneMode[] REDSTONE_MODE = new ActionSetRedstoneMode[TileEntityElectricBlock.redstoneModes];

    static {
        for (byte i = 0; i < REDSTONE_MODE.length; i++) {
            REDSTONE_MODE[i] = new ActionSetRedstoneMode(i);
        }
    }

    public static void addTriggers(Collection<ITriggerExternal> res, @Nonnull EnumFacing side, TileEntity tile) {
        if (!TriggerPower.isTriggeringTile(tile) && TriggerEU.isTriggeringTile(tile)) {
            res.add(TRIGGER_EU_HIGH);
            res.add(TRIGGER_EU_LOW);
        }

        if (TriggerPowerItem.isTriggeringTile(tile, true)) {
            res.add(CHARGE_LOW);
            res.add(CHARGE_HIGH);
            res.add(CHARGE_FULL);
        }
        if (TriggerPowerItem.isTriggeringTile(tile, false)) {
            res.add(DISCHARGE_EMPTY);
            res.add(DISCHARGE_LOW);
            res.add(DISCHARGE_HIGH);
        }
    }

    public static void addActions(Collection<IActionExternal> res, @Nonnull EnumFacing side, TileEntity tile) {
        if (ActionSetRedstoneMode.canActivate(tile)) {
            res.addAll(Arrays.asList(REDSTONE_MODE));
        }
    }

}
