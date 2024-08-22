package buildcraft.core.compat.module.ic2;

import ic2.api.classic.energy.ClassicEnergyNet;
import ic2.api.classic.energy.IPacketEnergyNet;
import ic2.core.platform.lang.ILocaleBlock;

public class ClassicImport {
    public static IPacketEnergyNet get() {
        ILocaleBlock l = () -> null;
        return ClassicEnergyNet.instance;
    }
}
