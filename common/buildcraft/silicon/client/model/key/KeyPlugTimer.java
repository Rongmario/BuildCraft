package buildcraft.silicon.client.model.key;

import buildcraft.api.transport.pluggable.PluggableModelKey;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;

public class KeyPlugTimer extends PluggableModelKey {
    public KeyPlugTimer(EnumFacing side) {
        super(BlockRenderLayer.CUTOUT, side);
    }
}
