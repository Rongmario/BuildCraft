package buildcraft.silicon.statement;

import buildcraft.api.statements.*;
import buildcraft.core.statements.BCStatement;
import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.silicon.BCSilicon;
import buildcraft.silicon.BCSiliconSprites;
import buildcraft.silicon.BCSiliconStatements;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TriggerTimer extends BCStatement implements ITriggerInternal {
    private final int setting;
    public static final int[] TIMERS = {5, 10, 15};

    public TriggerTimer(int setting) {
        super("buildcraft:timer_"+setting);
        this.setting = setting;
    }

    @Override
    public String getDescription() {
        return LocaleUtil.localize("gate.trigger.timer." +setting);
    }

    @Override
    public boolean isTriggerActive(IStatementContainer source, IStatementParameter[] parameters) {
        return BCSilicon.counter % (40 * TIMERS[setting]) < 6;
    }

    @Override
    public IStatement[] getPossible() {
        return BCSiliconStatements.TRIGGER_TIMER;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public SpriteHolderRegistry.SpriteHolder getSprite() {
        return BCSiliconSprites.TRIGGER_TIMER[setting];
    }
}
