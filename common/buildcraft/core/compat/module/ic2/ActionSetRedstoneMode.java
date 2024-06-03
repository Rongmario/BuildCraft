package buildcraft.core.compat.module.ic2;

import buildcraft.api.statements.IActionExternal;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.core.BCCoreSprites;
import buildcraft.core.statements.BCStatement;
import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import ic2.core.block.wiring.TileEntityElectricBlock;
import ic2.core.init.Localization;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ActionSetRedstoneMode extends BCStatement implements IActionExternal {
    private final byte mode;

    public ActionSetRedstoneMode(byte mode) {
        super("rs."+mode);
        this.mode = mode;
    }

    @Override
    public String getDescription() {
        return "Set "+ Localization.translate("ic2.EUStorage.gui.mod.redstone" + mode);
    }

    @Override
    public void actionActivate(TileEntity target, EnumFacing side, IStatementContainer source, IStatementParameter[] parameters) {
        if (target instanceof TileEntityElectricBlock) {
            TileEntityElectricBlock block = (TileEntityElectricBlock) target;
            if (block.redstoneMode != mode) {
                block.redstoneMode = mode;
                block.markDirty();
                World w = block.getWorld();
                if (w != null) {
                    BlockPos pos = block.getPos();
                    IBlockState state = w.getBlockState(pos);
                    block.getWorld().notifyBlockUpdate(pos, state, state, 3);
                }
            }
        }
    }

    public static boolean canActivate(TileEntity tile) {
        return tile instanceof TileEntityElectricBlock;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public SpriteHolderRegistry.SpriteHolder getSprite() {
        return BCCoreSprites.REDSTONE;
    }

    @Override
    public IStatement[] getPossible() {
        return IC2Statements.REDSTONE_MODE;
    }
}
