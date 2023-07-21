package buildcraft.transport.pipe.behaviour;

import buildcraft.api.mj.IMjRedstoneReceiver;
import buildcraft.api.mj.MjAPI;
import net.minecraftforge.energy.IEnergyStorage;

public class PipeRFWrapper implements IEnergyStorage {

    private IMjRedstoneReceiver pipeReceiver;
    public PipeRFWrapper(IMjRedstoneReceiver pipeReceiver) {
        this.pipeReceiver = pipeReceiver;
    }
    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        long mj = maxReceive * MjAPI.MJ / MjAPI.rfPerMj;
        long excess = pipeReceiver.receivePower(mj, true);
        long difference = mj - excess;
        int ret = mjToRf(difference);
        if (ret > 0) {
            pipeReceiver.receivePower(mj, simulate);
            return ret;
        }
        return 0;
    }

    private int mjToRf(long mj) {
        double rf = (double) (mj * MjAPI.rfPerMj) / (double) MjAPI.MJ;
        return (int) Math.ceil(rf);
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return 0;
    }

    @Override
    public int getEnergyStored() {
        return 0;
    }

    @Override
    public int getMaxEnergyStored() {
        return 512;
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public boolean canReceive() {
        return pipeReceiver.canReceive();
    }
}
