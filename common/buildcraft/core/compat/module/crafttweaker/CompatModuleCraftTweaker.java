package buildcraft.core.compat.module.crafttweaker;

import buildcraft.core.compat.CompatModuleBase;

import crafttweaker.CraftTweakerAPI;

public class CompatModuleCraftTweaker extends CompatModuleBase {
    @Override
    public String compatModId() {
        return "crafttweaker";
    }

    @Override
    public void preInit() {
        CraftTweakerAPI.registerClass(AssemblyTable.class);
        CraftTweakerAPI.registerClass(CombustionEngine.class);
        CraftTweakerAPI.registerClass(IntegrationTable.class);
    }
}
