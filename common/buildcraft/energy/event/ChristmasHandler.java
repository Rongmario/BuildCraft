package buildcraft.energy.event;

import java.lang.reflect.Field;
import java.time.Month;
import java.time.MonthDay;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.util.text.translation.LanguageMap;

import buildcraft.api.core.BCLog;

import buildcraft.lib.fluid.BCFluid;

import buildcraft.energy.BCEnergyConfig;
import buildcraft.energy.BCEnergyFluids;

/** Used for automatically changing lang entries, fluid colours, and a few other things around christmas time. This is
 * in energy rather than lib because no other module does anything at christmas. */
public class ChristmasHandler {

    private static Boolean enabled;

    public static boolean isEnabled() {
        if (enabled == null) {
            throw new IllegalStateException("Unknown until init!");
        }
        return enabled;
    }

    private static void fmlPreInit() {
        enabled = BCEnergyConfig.christmasEventStatus.isEnabled(MonthDay.of(Month.DECEMBER, 25));
        if (isEnabled()) {
            setColours(0x50_50_50, 0x05_05_05, BCEnergyFluids.oil);
            setColours(0xFF_FF_30, 0xE4_CF_00, BCEnergyFluids.fuel);
        }
    }

    public static void fmlPreInitDedicatedServer() {
        fmlPreInit();
        if (isEnabled()) {
            replaceLangEntries();
        }
    }

    public static void fmlPreInitClient() {
        fmlPreInit();
        if (isEnabled()) {
            ((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager())
                .registerReloadListener(m -> replaceLangEntries());
        }
    }

    private static void setColours(int lightColour, int darkColour, BCFluid fluid) {
        if (fluid != null) {
            fluid.setColour(lightColour, darkColour);
            if (fluid.isGaseous()) {
                fluid.setGaseous(false);
            }
            if (fluid.getDensity() < 0) {
                fluid.setDensity(-fluid.getDensity());
            }
        }
    }

    private static void replaceLangEntries() {
        try {
            replaceLangEntries0();
        } catch (ReflectiveOperationException e) {
            BCLog.logger.warn("[energy.christmas] Unable to replace language entries! Did something change?", e);
        }
    }

    private static void replaceLangEntries0() throws ReflectiveOperationException {
        Class<?> cls = LanguageMap.class;
        Field fldInstance = null, fldLangMap = null;
        for (Field fld : cls.getDeclaredFields()) {
            if (fld.getType() == cls) {
                if (fldInstance == null) {
                    fldInstance = fld;
                } else {
                    throw new ReflectiveOperationException(
                        "Found duplicate fields for instance! (" + fldInstance + " and " + fld + ")");
                }
            } else if (fld.getType() == Map.class) {
                if (fldLangMap == null) {
                    fldLangMap = fld;
                } else {
                    throw new ReflectiveOperationException(
                        "Found duplicate fields for langMap! (" + fldLangMap + " and " + fld + ")");
                }
            }
        }
        if (fldInstance == null) {
            throw new ReflectiveOperationException("Couln't find the instance field!");
        }
        if (fldLangMap == null) {
            throw new ReflectiveOperationException("Couln't find the map field!");
        }
        fldInstance.setAccessible(true);
        fldLangMap.setAccessible(true);

        LanguageMap instance = (LanguageMap) fldInstance.get(null);
        // never cast to a Map<String, String> as a mod
        // might change it with bytecode manipulation
        // Fortunately we can just replace the entry ourselves,
        // As Map.get() takes an object, not a generic value.
        checkAndReplaceEntries((Map<?, ?>) fldLangMap.get(instance));

        fldInstance.setAccessible(false);
        fldLangMap.setAccessible(false);
    }

    private static <K, V> void checkAndReplaceEntries(Map<K, V> map) {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            K key = entry.getKey();
            V altValue = map.get("buildcraft.christmas." + key);
            if (altValue != null) {
                entry.setValue(altValue);
            }
        }
    }
}
