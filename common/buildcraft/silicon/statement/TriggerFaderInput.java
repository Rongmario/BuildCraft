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

public class TriggerFaderInput extends BCStatement implements ITriggerInternal {
    public final int strength;

    public TriggerFaderInput(int strength) {
        super("buildcraft:redstone.input." + strength);
        this.strength = strength;
    }

    @Override
    public SpriteHolderRegistry.SpriteHolder getSprite() {
        return BCCoreSprites.REDSTONE_STRENGTH[strength];
    }

    @Override
    public String getDescription() {
        return LocaleUtil.localize("gate.trigger.redstone.input." +strength);
    }

    @Override
    public boolean isTriggerActive(IStatementContainer container, IStatementParameter[] parameters) {
        if (container instanceof IRedstoneStatementContainer) {
            int level = ((IRedstoneStatementContainer) container).getRedstoneInput(null);
            if (parameters.length > 0 && parameters[0] instanceof StatementParamGateSideOnly
                    && ((StatementParamGateSideOnly) parameters[0]).isSpecific
                    && container instanceof ISidedStatementContainer) {
                level = ((IRedstoneStatementContainer) container)
                        .getRedstoneInput(((ISidedStatementContainer) container).getSide());
            }

            return level == strength;
        } else {
            return false;
        }
    }

    @Override
    public IStatement[] getPossible() {
        return BCSiliconStatements.REDSTONE_INPUTS;
    }

    @Override
    public <T> T convertTo(Class<T> clazz) {
        T obj = super.convertTo(clazz);
        if (obj != null) {
            return obj;
        }

        if (clazz.isInstance(BCSiliconStatements.REDSTONE_OUTPUTS[strength])) {
            return clazz.cast(BCSiliconStatements.REDSTONE_OUTPUTS[strength]);
        }

        return null;
    }
}
