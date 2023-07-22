package buildcraft.lib.fluid;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nullable;

public class TankIO implements IFluidHandler {

    private final Tank in;
    private final Tank out;

    public TankIO(Tank in, Tank out) {
        super();
        this.in = in;
        this.out = out;
    }

    @Override
    public IFluidTankProperties[] getTankProperties() {
        return new IFluidTankProperties[]{in.getTankProperties()[0], out.getTankProperties()[0]};
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        return in.fill(resource, doFill);
    }

    @Nullable
    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain) {
        return out.drain(resource, doDrain);
    }

    @Nullable
    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        return out.drain(maxDrain, doDrain);
    }
}
