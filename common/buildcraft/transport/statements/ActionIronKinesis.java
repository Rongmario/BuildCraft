package buildcraft.transport.statements;

import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.core.statements.BCStatement;
import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.transport.BCTransportSprites;
import buildcraft.transport.BCTransportStatements;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ActionIronKinesis extends BCStatement implements IActionInternal {
    public final int index;

    public ActionIronKinesis(int index) {
        super("buildcraft:pipe.ironpower." + index, "buildcraft.pipe.ironpower." + index);
        this.index = index;
    }

    @Override
    public String getDescription() {
        return LocaleUtil.localize("gate.action.pipe.ironpower."+index);
    }

    @Override
    public void actionActivate(IStatementContainer source, IStatementParameter[] parameters) {}

    @Override
    public String toString() {
        return "ActionIronKinesis[" + index + "]";
    }

    @Override
    @SideOnly(Side.CLIENT)
    public SpriteHolderRegistry.SpriteHolder getSprite() {
        return BCTransportSprites.getIronKinesis(index);
    }

    @Override
    public IStatement[] getPossible() {
        return BCTransportStatements.ACTION_IRON_KINESES;
    }
}
