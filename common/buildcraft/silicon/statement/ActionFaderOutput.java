package buildcraft.silicon.statement;

import buildcraft.api.statements.*;
import buildcraft.api.statements.containers.IRedstoneStatementContainer;
import buildcraft.api.statements.containers.ISidedStatementContainer;
import buildcraft.core.BCCoreSprites;
import buildcraft.core.statements.BCStatement;
import buildcraft.core.statements.StatementParamGateSideOnly;
import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.silicon.BCSiliconStatements;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ActionFaderOutput extends BCStatement implements IActionInternal {
    private final int strength;

    public ActionFaderOutput(int strength) {
        super("buildcraft:redstone.output."+strength);
        this.strength = strength;
    }

    @Override
    public String getDescription() {
        return LocaleUtil.localize("gate.action.redstone.output."+strength);
    }

    @Override
    public void actionActivate(IStatementContainer source, IStatementParameter[] parameters) {
        if (source instanceof IRedstoneStatementContainer) {
            EnumFacing side = null;
            if (source instanceof ISidedStatementContainer && isSideOnly(parameters)) {
                side = ((ISidedStatementContainer) source).getSide();
            }
            ((IRedstoneStatementContainer) source).setRedstoneOutput(side, strength);
        }
    }

    protected boolean isSideOnly(IStatementParameter[] parameters) {
        if (parameters != null && parameters.length >= (getRGSOSlot() + 1)
                && parameters[getRGSOSlot()] instanceof StatementParamGateSideOnly) {
            return ((StatementParamGateSideOnly) parameters[getRGSOSlot()]).isSpecific;
        }

        return false;
    }

    @Override
    public IStatementParameter createParameter(int index) {
        if (index == 0) {
            return StatementParamGateSideOnly.ANY;
        }
        return null;
    }

    protected int getRGSOSlot() {
        return 0;
    }

    @Override
    public int maxParameters() {
        return 1;
    }


    @Override
    public IStatement[] getPossible() {
        return BCSiliconStatements.REDSTONE_OUTPUTS;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public SpriteHolderRegistry.SpriteHolder getSprite() {
        return BCCoreSprites.REDSTONE_STRENGTH[strength];
    }

    @Override
    public <T> T convertTo(Class<T> clazz) {
        T obj = super.convertTo(clazz);
        if (obj != null) {
            return obj;
        }
        if (clazz.isInstance(BCSiliconStatements.REDSTONE_INPUTS[strength])) {
            return clazz.cast(BCSiliconStatements.REDSTONE_INPUTS[strength]);
        }
        return null;
    }
}
