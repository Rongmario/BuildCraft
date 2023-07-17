package buildcraft.energy;

import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjBattery;
import net.minecraftforge.energy.IEnergyStorage;

public class BCRfWrapper implements IEnergyStorage {

    private MjBattery battery;

    public BCRfWrapper(MjBattery battery) {
        this.battery = battery;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        long mjToAdd = (maxReceive * MjAPI.MJ) / MjAPI.rfPerMj;
        long added = mjToAdd - battery.addPowerChecking(mjToAdd, simulate);
        return (int) (added * MjAPI.rfPerMj / MjAPI.MJ);
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return 0;
    }

    @Override
    public int getEnergyStored() {
        return (int) (MjAPI.rfPerMj * battery.getStored() / MjAPI.MJ);
    }

    @Override
    public int getMaxEnergyStored() {
        return (int) (MjAPI.rfPerMj * battery.getCapacity() / MjAPI.MJ);
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public boolean canReceive() {
        return !battery.isFull();
    }
}
