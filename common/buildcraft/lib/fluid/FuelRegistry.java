/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.fluid;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import net.minecraftforge.fluids.FluidStack;

import buildcraft.api.fuels.IFuel;
import buildcraft.api.fuels.IFuelManager;

public enum FuelRegistry implements IFuelManager {
    INSTANCE;

    private final List<IFuel> fuels = new LinkedList<>();

    @Override
    public <F extends IFuel> F addFuel(F fuel) {
        fuels.add(fuel);
        return fuel;
    }

    @Override
    public IFuel addFuel(FluidStack fluid, long powerPerCycle, int totalBurningTime) {
        IFuel f = null;
        for (IFuel fuel : fuels) {
            if (fuel.getFluid().isFluidEqual(fluid)) f = fuel;
        }
        if (f == null) {
            return addFuel(new Fuel(fluid, powerPerCycle, totalBurningTime));
        } else {
            if (f.getPowerPerCycle() > powerPerCycle) return f;
            else {
                fuels.remove(f);
                return addFuel(new Fuel(fluid, powerPerCycle, totalBurningTime));
            }
        }
    }

    @Override
    public IDirtyFuel addDirtyFuel(FluidStack fuel, long powerPerCycle, int totalBurningTime, FluidStack residue) {
        return new DirtyFuel(addFuel(fuel, powerPerCycle, totalBurningTime));
    }

    @Override
    public Collection<IFuel> getFuels() {
        return fuels;
    }

    @Override
    public IFuel getFuel(FluidStack fluid) {
        if (fluid == null) {
            return null;
        }
        for (IFuel fuel : fuels) {
            if (fuel.getFluid().isFluidEqual(fluid)) {
                return fuel;
            }
        }
        return null;
    }

    public static class Fuel implements IFuel {
        private final FluidStack fluid;
        private final long powerPerCycle;
        private final int totalBurningTime;

        public Fuel(FluidStack fluid, long powerPerCycle, int totalBurningTime) {
            this.fluid = fluid;
            this.powerPerCycle = powerPerCycle;
            this.totalBurningTime = totalBurningTime;
        }

        @Override
        public FluidStack getFluid() {
            return fluid;
        }

        @Override
        public long getPowerPerCycle() {
            return powerPerCycle;
        }

        @Override
        public int getTotalBurningTime() {
            return totalBurningTime;
        }
    }

    public static class DirtyFuel extends Fuel implements IDirtyFuel {

        private final FluidStack residue;

        public DirtyFuel(FluidStack fluid, long powerPerCycle, int totalBurningTime, FluidStack residue) {
            super(fluid, powerPerCycle, totalBurningTime);
            this.residue = residue;
        }

        public DirtyFuel(IFuel f) {
            this(f.getFluid(), f.getPowerPerCycle(), f.getTotalBurningTime(), new FluidStack(f.getFluid(), 0));
        }

        @Override
        public FluidStack getResidue() {
            return residue;
        }
    }
}
