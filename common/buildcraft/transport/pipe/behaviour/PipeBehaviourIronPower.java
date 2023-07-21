package buildcraft.transport.pipe.behaviour;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.transport.pipe.*;
import buildcraft.transport.pipe.flow.IVariableFlowHook;
import buildcraft.transport.pipe.flow.PipeFlowPower;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import java.io.IOException;

public class PipeBehaviourIronPower extends PipeBehaviour implements IVariableFlowHook {

    public static final int[] LIMITER = {20, 40, 80, 160, 320, 640, 1280};

    private int limitedIndex = 6;

    public PipeBehaviourIronPower(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourIronPower(IPipe pipe, NBTTagCompound nbt) {
        super(pipe, nbt);
        if( nbt.hasKey("index")) {
            limitedIndex = nbt.getInteger("index");
        }
    }

    public void iterateLimiter() {
        limitedIndex++;
        limitedIndex = limitedIndex % LIMITER.length;
    }

    @Override
    public NBTTagCompound writeToNbt() {
        NBTTagCompound nbt = super.writeToNbt();
        nbt.setInteger("index", limitedIndex);
        return nbt;
    }

    @Override
    public void writePayload(PacketBuffer buffer, Side side) {
        super.writePayload(buffer, side);
        if (side == Side.SERVER) {
            buffer.writeInt(limitedIndex);
        }
    }

    @Override
    public void readPayload(PacketBuffer buffer, Side side, MessageContext ctx) throws IOException {
        super.readPayload(buffer, side, ctx);
        if (side == Side.CLIENT) {
            limitedIndex = buffer.readInt();
        }
    }

    @Override
    public int getTextureIndex(EnumFacing face) {
        return limitedIndex;
    }

    @Override
    public boolean onPipeActivate(EntityPlayer player, RayTraceResult trace, float hitX, float hitY, float hitZ, EnumPipePart part) {
        if (player.world.isRemote) {
            return true;
        }
        iterateLimiter();
        pipe.getHolder().scheduleNetworkUpdate(IPipeHolder.PipeMessageReceiver.BEHAVIOUR);
        if (pipe.getFlow() instanceof PipeFlowPower) {
            ((PipeFlowPower) pipe.getFlow()).reconfigure();
        }
        player.sendMessage(new TextComponentString("Switched to "+LIMITER[limitedIndex]+" RF/t limit"));

        return true;
    }

    @Override
    public int getMaxFlow() {
        return LIMITER[limitedIndex];
    }
}
