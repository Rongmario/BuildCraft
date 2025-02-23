/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport;

import buildcraft.transport.net.PacketPowerUpdate;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.BCModules;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pipe.PipeApiClient;
import buildcraft.api.transport.pipe.PipeBehaviour;

import buildcraft.lib.net.MessageManager;

import buildcraft.transport.client.PipeRegistryClient;
import buildcraft.transport.client.render.PipeWireRenderer;
import buildcraft.transport.container.ContainerDiamondPipe;
import buildcraft.transport.container.ContainerEmeraldPipe;
import buildcraft.transport.container.ContainerEmzuliPipe_BC8;
import buildcraft.transport.container.ContainerFilteredBuffer_BC8;
import buildcraft.transport.gui.GuiDiamondPipe;
import buildcraft.transport.gui.GuiEmeraldPipe;
import buildcraft.transport.gui.GuiEmzuliPipe_BC8;
import buildcraft.transport.gui.GuiFilteredBuffer;
import buildcraft.transport.net.MessageMultiPipeItem;
import buildcraft.transport.pipe.behaviour.PipeBehaviourDiamond;
import buildcraft.transport.pipe.behaviour.PipeBehaviourEmzuli;
import buildcraft.transport.pipe.behaviour.PipeBehaviourEmerald;
import buildcraft.transport.tile.TileFilteredBuffer;
import buildcraft.transport.wire.MessageWireSystems;
import buildcraft.transport.wire.MessageWireSystemsPowered;

public abstract class BCTransportProxy implements IGuiHandler {
    @SidedProxy(modId = BCTransport.MODID)
    private static BCTransportProxy proxy;

    public static BCTransportProxy getProxy() {
        return proxy;
    }

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        BCTransportGuis gui = BCTransportGuis.get(id);
        if (gui == null) return null;
        TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));

        switch (gui) {
            case FILTERED_BUFFER: {
                if (tile instanceof TileFilteredBuffer) {
                    TileFilteredBuffer filteredBuffer = (TileFilteredBuffer) tile;
                    return new ContainerFilteredBuffer_BC8(player, filteredBuffer);
                }
                break;
            }
            case PIPE_DIAMOND: {
                if (tile instanceof IPipeHolder) {
                    IPipeHolder holder = (IPipeHolder) tile;
                    IPipe pipe = holder.getPipe();
                    if (pipe == null) return null;
                    PipeBehaviour behaviour = pipe.getBehaviour();
                    if (behaviour instanceof PipeBehaviourDiamond) {
                        PipeBehaviourDiamond diaPipe = (PipeBehaviourDiamond) behaviour;
                        return new ContainerDiamondPipe(player, diaPipe);
                    }
                }
                break;
            }
            case PIPE_EMERALD: {
                if (tile instanceof IPipeHolder) {
                    IPipeHolder holder = (IPipeHolder) tile;
                    IPipe pipe = holder.getPipe();
                    if (pipe == null) return null;
                    PipeBehaviour behaviour = pipe.getBehaviour();
                    if (behaviour instanceof PipeBehaviourEmerald) {
                        PipeBehaviourEmerald diaPipe = (PipeBehaviourEmerald) behaviour;
                        return new ContainerEmeraldPipe(player, diaPipe);
                    }
                }
                break;
            }
            case PIPE_EMZULI: {
                if (tile instanceof IPipeHolder) {
                    IPipeHolder holder = (IPipeHolder) tile;
                    IPipe pipe = holder.getPipe();
                    if (pipe == null) return null;
                    PipeBehaviour behaviour = pipe.getBehaviour();
                    if (behaviour instanceof PipeBehaviourEmzuli) {
                        PipeBehaviourEmzuli emPipe = (PipeBehaviourEmzuli) behaviour;
                        return new ContainerEmzuliPipe_BC8(player, emPipe);
                    }
                }
                break;
            }
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        return null;
    }

    public void fmlPreInit() {
        MessageManager.registerMessageClass(BCModules.TRANSPORT, MessageWireSystems.class, Side.CLIENT);
        MessageManager.registerMessageClass(BCModules.TRANSPORT, MessageWireSystemsPowered.class, Side.CLIENT);
        MessageManager.registerMessageClass(BCModules.TRANSPORT, MessageMultiPipeItem.class, Side.CLIENT);
        MessageManager.registerMessageClass(BCModules.TRANSPORT, PacketPowerUpdate.class, Side.CLIENT);
    }

    public void fmlInit() {}

    public void fmlPostInit() {}

    @SuppressWarnings("unused")
    @SideOnly(Side.SERVER)
    public static class ServerProxy extends BCTransportProxy {}

    @SuppressWarnings("unused")
    @SideOnly(Side.CLIENT)
    public static class ClientProxy extends BCTransportProxy {
        @Override
        public void fmlPreInit() {
            super.fmlPreInit();
            BCTransportSprites.fmlPreInit();
            BCTransportModels.fmlPreInit();
            PipeApiClient.registry = PipeRegistryClient.INSTANCE;
            PipeWireRenderer.init();

            MessageManager.setHandler(MessageWireSystems.class, MessageWireSystems.HANDLER, Side.CLIENT);
            MessageManager.setHandler(MessageWireSystemsPowered.class, MessageWireSystemsPowered.HANDLER, Side.CLIENT);
            MessageManager.setHandler(MessageMultiPipeItem.class, MessageMultiPipeItem.HANDLER, Side.CLIENT);
            MessageManager.setHandler(PacketPowerUpdate.class, PacketPowerUpdate.HANDLER, Side.CLIENT);
        }

        @Override
        public void fmlInit() {
            super.fmlInit();
            BCTransportModels.fmlInit();
        }

        @Override
        public void fmlPostInit() {
            super.fmlPostInit();
            BCTransportModels.fmlPostInit();
        }

        @Override
        public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
            BCTransportGuis gui = BCTransportGuis.get(id);
            if (gui == null) {
                return null;
            }
            TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
            switch (gui) {
                case FILTERED_BUFFER: {
                    if (tile instanceof TileFilteredBuffer) {
                        TileFilteredBuffer filteredBuffer = (TileFilteredBuffer) tile;
                        return new GuiFilteredBuffer(new ContainerFilteredBuffer_BC8(player, filteredBuffer));
                    }
                    break;
                }
                case PIPE_DIAMOND: {
                    if (tile instanceof IPipeHolder) {
                        IPipeHolder holder = (IPipeHolder) tile;
                        IPipe pipe = holder.getPipe();
                        if (pipe == null) return null;
                        PipeBehaviour behaviour = pipe.getBehaviour();
                        if (behaviour instanceof PipeBehaviourDiamond) {
                            PipeBehaviourDiamond diaPipe = (PipeBehaviourDiamond) behaviour;
                            return new GuiDiamondPipe(player, diaPipe);
                        }
                    }
                    break;
                }
                case PIPE_EMERALD: {
                    if (tile instanceof IPipeHolder) {
                        IPipeHolder holder = (IPipeHolder) tile;
                        IPipe pipe = holder.getPipe();
                        if (pipe == null) return null;
                        PipeBehaviour behaviour = pipe.getBehaviour();
                        if (behaviour instanceof PipeBehaviourEmerald) {
                            PipeBehaviourEmerald diaPipe = (PipeBehaviourEmerald) behaviour;
                            return new GuiEmeraldPipe(player, diaPipe);
                        }
                    }
                    break;
                }
                case PIPE_EMZULI: {
                    if (tile instanceof IPipeHolder) {
                        IPipeHolder holder = (IPipeHolder) tile;
                        IPipe pipe = holder.getPipe();
                        if (pipe == null) return null;
                        PipeBehaviour behaviour = pipe.getBehaviour();
                        if (behaviour instanceof PipeBehaviourEmzuli) {
                            PipeBehaviourEmzuli emzPipe = (PipeBehaviourEmzuli) behaviour;
                            return new GuiEmzuliPipe_BC8(player, emzPipe);
                        }
                    }
                    break;
                }
            }
            return null;
        }
    }
}
