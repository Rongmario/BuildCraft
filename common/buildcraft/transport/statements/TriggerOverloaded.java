package buildcraft.transport.statements;

import buildcraft.api.core.render.ISprite;
import buildcraft.api.gates.IGate;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.api.transport.pipe.PipeFlow;
import buildcraft.core.statements.BCStatement;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.transport.BCTransportSprites;
import buildcraft.transport.pipe.flow.PipeFlowPower;

import javax.annotation.Nullable;

public class TriggerOverloaded extends BCStatement implements ITriggerInternal {

    public TriggerOverloaded() {
        super("buildcraft:powerOverloaded");
    }

    @Override
    public boolean isTriggerActive(IStatementContainer source, IStatementParameter[] parameters) {
        if (!(source instanceof IGate)) {
            return false;
        }
        PipeFlow f = ((IGate) source).getPipeHolder().getPipe().getFlow();
        if (!(f instanceof PipeFlowPower)) {
            return false;
        }
        final PipeFlowPower flow = (PipeFlowPower) f;

        return flow.isOverloaded2();
    }

    @Override
    public String getDescription() {
        return LocaleUtil.localize("gate.trigger.pipe.tooMuchEnergy");
    }

    @Nullable
    @Override
    public ISprite getSprite() {
        return BCTransportSprites.TRIGGER_OVERLOADED;
    }

}
