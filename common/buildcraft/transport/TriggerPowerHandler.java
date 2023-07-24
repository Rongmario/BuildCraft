package buildcraft.transport;

import buildcraft.api.transport.pipe.PipeFlow;
import buildcraft.transport.pipe.flow.PipeFlowPower;
import buildcraft.transport.tile.TilePipeHolder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.energy.CapabilityEnergy;

public class TriggerPowerHandler {
    public static boolean isTriggeringTile(TileEntity tile, EnumFacing face) {
        if (tile instanceof TilePipeHolder) {
            PipeFlow flow = ((TilePipeHolder) tile).getPipe().getFlow();
            if (flow instanceof PipeFlowPower) {
                return ((PipeFlowPower) flow).isReceiver;
            } else {
                return false;
            }
        }
        return tile.hasCapability(CapabilityEnergy.ENERGY, face);
    }
}
