package buildcraft.core.compat.module.theoneprobe;

import net.minecraftforge.fml.common.event.FMLInterModComms;

import buildcraft.core.compat.CompatModuleBase;

public class CompatModuleTheOneProbe extends CompatModuleBase {

    @Override
    public String compatModId() {
        return "theoneprobe";
    }

    @Override
    public void preInit() {
        FMLInterModComms.sendFunctionMessage(compatModId(), "getTheOneProbe",
            "buildcraft.core.compat.module.theoneprobe.BCPluginTOP");
    }
}
