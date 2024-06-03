package buildcraft.silicon.client.model.key;

import buildcraft.api.transport.pluggable.PluggableModelKey;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;

public class KeyPlugFader extends PluggableModelKey {


    public KeyPlugFader(EnumFacing side) {
        super(BlockRenderLayer.CUTOUT, side);
    }
}
