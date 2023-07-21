package buildcraft.transport.net;

import buildcraft.lib.BCLibProxy;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.transport.pipe.flow.PipeFlowPower;
import buildcraft.transport.tile.TilePipeHolder;
import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
public class PacketPowerUpdate implements IMessage {


    public PacketPowerUpdate() {
        super();
    }

    public PacketPowerUpdate(BlockPos pos) {
        super();
        this.pos = pos;
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        PacketBufferBC buf = PacketBufferBC.asPacketBufferBc(buffer);
        displayPower = new int[6];
        displayFlow = new short[6];
        overload = buf.readBoolean();
        for (int i = 0; i < displayPower.length; i++) {
            displayPower[i] = buf.readUnsignedByte();
            displayFlow[i] = buf.readByte();
        }
        pos = BlockPos.fromLong(buf.readLong());
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        PacketBufferBC buf = PacketBufferBC.asPacketBufferBc(buffer);
        buf.writeBoolean(overload);
        for (int i = 0; i < displayPower.length; i++) {
            buf.writeByte(displayPower[i]);
            buf.writeByte(displayFlow[i]);
        }
        buf.writeLong(pos.toLong());
    }


    public static final IMessageHandler<PacketPowerUpdate, IMessage> HANDLER =
            (message, ctx) -> {
                World world = BCLibProxy.getProxy().getClientWorld();
                if (world == null) {
                    return null;
                }
                if (!world.isBlockLoaded(message.pos)) {
                    return null;
                }

                TileEntity entity = world.getTileEntity(message.pos);
                if (!(entity instanceof TilePipeHolder)) {
                    return null;
                }

                TilePipeHolder pipe = (TilePipeHolder) entity;
                if (pipe.getPipe() == null) {
                    return null;
                }

                if (!(pipe.getPipe().flow instanceof PipeFlowPower)) {
                    return null;
                }

                ((PipeFlowPower) pipe.getPipe().flow).handlePowerPacket(message);

                return null;
            };

    public boolean overload;
    public int[] displayPower;
    public short[] displayFlow;
    public BlockPos pos;


}
