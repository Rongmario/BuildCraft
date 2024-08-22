package buildcraft.core.compat.module.ic2.cl;

import buildcraft.api.statements.ITriggerExternal;
import buildcraft.core.statements.TriggerPower;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nonnull;
import java.util.Collection;

public class ClIC2Statements {
    public static final ClTriggerEU TRIGGER_EU_HIGH = new ClTriggerEU(true);
    public static final ClTriggerEU TRIGGER_EU_LOW = new ClTriggerEU(false);
    public static final ClTriggerEU[] TRIGGER_EU = { TRIGGER_EU_HIGH, TRIGGER_EU_LOW };

    public static final ClTriggerPowerItem CHARGE_FULL = new ClTriggerPowerItem(true, 0);
    public static final ClTriggerPowerItem CHARGE_LOW = new ClTriggerPowerItem(true, 1);
    public static final ClTriggerPowerItem CHARGE_HIGH = new ClTriggerPowerItem(true, 2);

    public static final ClTriggerPowerItem DISCHARGE_EMPTY = new ClTriggerPowerItem(false, 0);
    public static final ClTriggerPowerItem DISCHARGE_LOW = new ClTriggerPowerItem(false, 1);
    public static final ClTriggerPowerItem DISCHARGE_HIGH = new ClTriggerPowerItem(false, 2);

    public static final ClTriggerPowerItem[] CHARGE_ITEM = {CHARGE_LOW, CHARGE_HIGH, CHARGE_FULL};
    public static final ClTriggerPowerItem[] DISCHARGE_ITEM = {DISCHARGE_EMPTY, DISCHARGE_LOW, DISCHARGE_HIGH};


    public static void addTriggers(Collection<ITriggerExternal> res, @Nonnull EnumFacing side, TileEntity tile) {
        if (!TriggerPower.isTriggeringTile(tile) && ClTriggerEU.isTriggeringTile(tile)) {
            res.add(TRIGGER_EU_HIGH);
            res.add(TRIGGER_EU_LOW);
        }

        if (ClTriggerPowerItem.isTriggeringTile(tile, true)) {
            res.add(CHARGE_LOW);
            res.add(CHARGE_HIGH);
            res.add(CHARGE_FULL);
        }
        if (ClTriggerPowerItem.isTriggeringTile(tile, false)) {
            res.add(DISCHARGE_EMPTY);
            res.add(DISCHARGE_LOW);
            res.add(DISCHARGE_HIGH);
        }
    }

}
