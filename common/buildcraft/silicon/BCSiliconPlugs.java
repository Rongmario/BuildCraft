package buildcraft.silicon;

import buildcraft.silicon.plug.*;
import net.minecraft.util.ResourceLocation;

import buildcraft.api.BCModules;
import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pluggable.PluggableDefinition;
import buildcraft.api.transport.pluggable.PluggableDefinition.IPluggableCreator;
import buildcraft.api.transport.pluggable.PluggableDefinition.IPluggableNbtReader;
import buildcraft.api.transport.pluggable.PluggableDefinition.IPluggableNetLoader;

public class BCSiliconPlugs {

    public static PluggableDefinition gate;
    public static PluggableDefinition lens;
    public static PluggableDefinition pulsar;
    public static PluggableDefinition lightSensor;
    public static PluggableDefinition facade;
    public static PluggableDefinition timer;
    public static PluggableDefinition fader;

    public static void preInit() {
        gate = register("gate", PluggableGate::new, PluggableGate::new);
        lens = register("lens", PluggableLens::new, PluggableLens::new);
        pulsar = register("pulsar", PluggablePulsar::new, PluggablePulsar::new);
        lightSensor = register("daylight_sensor", PluggableLightSensor::new);
        facade = register("facade", PluggableFacade::new, PluggableFacade::new);
        timer = register("timer", PluggableTimer::new);
        fader = register("fader", PluggableFader::new);
    }

    private static PluggableDefinition register(String name, IPluggableCreator creator) {
        return register(new PluggableDefinition(idFor(name), creator));
    }

    private static PluggableDefinition register(String name, IPluggableNbtReader reader, IPluggableNetLoader loader) {
        return register(new PluggableDefinition(idFor(name), reader, loader));
    }

    private static PluggableDefinition register(PluggableDefinition def) {
        // TODO: Add config for enabling/disabling
        PipeApi.pluggableRegistry.register(def);

        // TODO: remove this in 7.99.19!
        // This handles the migration of most of the transport pluggables into silicon
        String modId = BCModules.TRANSPORT.getModId();
        PipeApi.pluggableRegistry.register(new ResourceLocation(modId, def.identifier.getResourcePath()), def);
        return def;
    }

    private static ResourceLocation idFor(String name) {
        return new ResourceLocation("buildcraftsilicon", name);
    }
}
