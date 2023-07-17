package buildcraft.energy;

import buildcraft.api.mj.MjAPI;
import buildcraft.lib.engine.TileEngineBase_BC8;
import net.minecraftforge.energy.IEnergyStorage;

public class EngineStorageWrapper implements IEnergyStorage {

    private TileEngineBase_BC8 engine;

    public EngineStorageWrapper(TileEngineBase_BC8 engine) {
        this.engine = engine;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        return 0;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        long extracted = engine.extractPower(0, maxExtract * MjAPI.MJ / MjAPI.rfPerMj, !simulate);
        return (int) (extracted * MjAPI.rfPerMj / MjAPI.MJ);
    }

    @Override
    public int getEnergyStored() {
        return (int) (MjAPI.rfPerMj * engine.getEnergyStored() / MjAPI.MJ);
    }

    @Override
    public int getMaxEnergyStored() {
        return (int) (MjAPI.rfPerMj * engine.getMaxPower() / MjAPI.MJ);
    }


    @Override
    public boolean canExtract() {
        return true;
    }

    @Override
    public boolean canReceive() {
        return false;
    }
}
