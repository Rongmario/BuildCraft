package buildcraft.silicon;

import buildcraft.silicon.statement.*;

public class BCSiliconStatements {

    public static final TriggerLightSensor TRIGGER_LIGHT_LOW;
    public static final TriggerLightSensor TRIGGER_LIGHT_HIGH;
    public static final TriggerLightSensor[] TRIGGER_LIGHT;
    public static final TriggerTimer[] TRIGGER_TIMER = new TriggerTimer[TriggerTimer.TIMERS.length];

    public static final ActionPowerPulsar ACTION_PULSAR_CONSTANT;
    public static final ActionPowerPulsar ACTION_PULSAR_SINGLE;
    public static final ActionPowerPulsar[] ACTION_PULSAR;

    public static final TriggerFaderInput[] REDSTONE_INPUTS = new TriggerFaderInput[16];
    public static final ActionFaderOutput[] REDSTONE_OUTPUTS = new ActionFaderOutput[16];

    static {
        TRIGGER_LIGHT_LOW = new TriggerLightSensor(false);
        TRIGGER_LIGHT_HIGH = new TriggerLightSensor(true);
        TRIGGER_LIGHT = new TriggerLightSensor[] { TRIGGER_LIGHT_LOW, TRIGGER_LIGHT_HIGH };

        ACTION_PULSAR_CONSTANT = new ActionPowerPulsar(true);
        ACTION_PULSAR_SINGLE = new ActionPowerPulsar(false);
        ACTION_PULSAR = new ActionPowerPulsar[] { ACTION_PULSAR_CONSTANT, ACTION_PULSAR_SINGLE };

        for (int i = 0; i < TriggerTimer.TIMERS.length; i++) {
            TRIGGER_TIMER[i] = new TriggerTimer(i);
        }

        for (int i = 0; i < 16; i++) {
            REDSTONE_INPUTS[i] = new TriggerFaderInput(i);
            REDSTONE_OUTPUTS[i] = new ActionFaderOutput(i);
        }
    }

    public static void preInit() {
        // NO-OP: just to call the above static block
    }
}
