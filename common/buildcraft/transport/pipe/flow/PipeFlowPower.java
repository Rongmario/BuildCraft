/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.pipe.flow;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import buildcraft.api.mj.MjAPI;
import buildcraft.lib.misc.data.AverageDouble;
import buildcraft.transport.BCTransportPlugs;
import buildcraft.transport.net.PacketPowerUpdate;
import buildcraft.transport.plug.PluggablePowerAdaptor;
import buildcraft.transport.tile.TilePipeHolder;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.relauncher.Side;

import buildcraft.api.core.SafeTimeTracker;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.api.transport.pipe.IFlowPower;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.IPipe.ConnectedType;
import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pipe.PipeApi.PowerTransferInfo;
import buildcraft.api.transport.pipe.PipeEventPower;
import buildcraft.api.transport.pipe.PipeFlow;

import buildcraft.lib.misc.VecUtil;
import buildcraft.lib.misc.data.AverageInt;

public class PipeFlowPower extends PipeFlow implements IDebuggable, IFlowPower, IEnergyStorage {
    private static final int DEFAULT_MAX_POWER = 100;
    public static final int NET_POWER_AMOUNTS = 2;

    public Vec3d clientDisplayFlowCentre = Vec3d.ZERO;
    public Vec3d clientDisplayFlowCentreLast = Vec3d.ZERO;

    public int maxPower = -1;
    public boolean isReceiver = false;

    public PipeFlowPower(IPipe pipe) {
        super(pipe);
        for (int i = 0; i < 6; i++) {
            powerAverage[i] = new AverageInt(8);
        }
    }

    public boolean isPowerSource(TileEntity t, EnumFacing o) {
        if (t == null) {
            for (EnumFacing side : EnumFacing.VALUES) updateTile(side);
            return false;
        }
        IEnergyStorage storage = t.getCapability(CapabilityEnergy.ENERGY, o.getOpposite());
        return storage != null && storage.getEnergyStored() > 0;
    }


    @Override
    public boolean canConnect(EnumFacing face, PipeFlow other) {
        return other instanceof PipeFlowPower;
    }

    @Override
    public boolean canConnect(EnumFacing face, TileEntity oTile) {
        return oTile.hasCapability(CapabilityEnergy.ENERGY, face.getOpposite());
    }

    public void reconfigure() {
        PipeEventPower.Configure configure = new PipeEventPower.Configure(pipe.getHolder(), this);
        PowerTransferInfo pti = PipeApi.getPowerTransferInfo(pipe.getDefinition());
        configure.setReceiver(pti.isReceiver);
        configure.setMaxPower(pti.transferPerTick);
        if (pipe.getBehaviour() instanceof IVariableFlowHook) {
            int maxRF = ((IVariableFlowHook) pipe.getBehaviour()).getMaxFlow();
            configure.setMaxPower(maxRF);
        }
        pipe.getHolder().fireEvent(configure);
        isReceiver = configure.isReceiver();
        maxPower = configure.getMaxPower();
        if (maxPower < 0) {
            maxPower = DEFAULT_MAX_POWER;
        }
    }


    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY) {
            return CapabilityEnergy.ENERGY.cast(this);
        }
        return null;
    }

    @Override
    public void onTick() {
        if (maxPower == -1) {
            reconfigure();
        }

        for (int i = 0; i < 6; i++) {
            if (internalPower[i] < 1.0) internalPower[i] = 0;
            if (internalNextPower[i] < 1.0) internalNextPower[i] = 0;
        }

        if (pipe.getHolder().getPipeWorld().isRemote) {
            clientDisplayFlowCentreLast = clientDisplayFlowCentre;
            for (EnumFacing face : EnumFacing.VALUES) {
                clientDisplayFlowLast[face.ordinal()] = clientDisplayFlow[face.ordinal()];
                double diff = displayFlow[face.ordinal()] * 2.4 * face.getAxisDirection().getOffset();
                clientDisplayFlow[face.ordinal()] += 16 + diff;
                clientDisplayFlow[face.ordinal()] %= 16;

                double cVal = VecUtil.getValue(clientDisplayFlowCentre, face.getAxis());
                cVal += 16 + diff / 2;
                cVal %= 16;
                if (displayFlow[face.ordinal()] == 0) cVal = 0;
                clientDisplayFlowCentre = VecUtil.replaceValue(clientDisplayFlowCentre, face.getAxis(), cVal);
            }
            //return;
        }

        updateEntity();
    }
    public double[] clientDisplayFlow = new double[6];
    public double[] clientDisplayFlowLast = new double[6];

    private void step2() {
        if (pipe != null && pipe.getHolder().getPipeWorld() != null && currentDate != pipe.getHolder().getPipeWorld().getTotalWorldTime()) {
            currentDate = pipe.getHolder().getPipeWorld().getTotalWorldTime();

            Arrays.fill(dbgEnergyInput, 0);
            Arrays.fill(dbgEnergyOffered, 0);
            Arrays.fill(dbgEnergyOutput, 0);

            powerQuery = nextPowerQuery;
            nextPowerQuery = new int[6];

            double[] next = internalPower;
            internalPower = internalNextPower;
            internalNextPower = next;
        }
    }

    private static final int OVERLOAD_TICKS = 60;

    public AverageDouble[] displayPowerAverage = new AverageDouble[6];

    public double[] displayPower = new double[6];
    public short[] displayFlow = new short[6];
    public int[] nextPowerQuery = new int[6];

    public boolean requestingPower() {
        int sum = 0;
        for (int i : nextPowerQuery) {
            sum += i;
        }
        for (int i : powerQuery) {
            sum += i;
        }
        return sum > 0;
    }

    public boolean powerFlowing() {
        double sum = 0;
        for (AverageInt av : powerAverage) {
            sum += av.getAverage();
        }
        return sum > 0;
    }


    public double[] internalNextPower = new double[6];
    public int overload;


    public int[] dbgEnergyInput = new int[6];
    public int[] dbgEnergyOutput = new int[6];
    public int[] dbgEnergyOffered = new int[6];

    private final AverageInt[] powerAverage = new AverageInt[6];
    private final TileEntity[] tiles = new TileEntity[6];
    private final Object[] providers = new Object[6];

    private boolean needsInit = true;

    private int[] powerQuery = new int[6];
    private long currentDate;
    private double[] internalPower = new double[6];




    private void updateTile(EnumFacing side) {
        int o = side.ordinal();
        TileEntity tile = pipe.getConnectedTile(side);
        if (tile != null && pipe.isConnected(side)) {
            tiles[o] = tile;
        } else {
            tiles[o] = null;
            internalPower[o] = 0;
            internalNextPower[o] = 0;
            powerAverage[o].clear();
            displayFlow[o] = 0;
        }
        if (tile instanceof TilePipeHolder && ((TilePipeHolder) tile).getPipe().getFlow() instanceof PipeFlowPower) {
            //boolean f = false;
            //TilePipeHolder holder = (TilePipeHolder) tile;
            //if (holder.getPluggable(side.getOpposite()) != null && holder.getPluggable(side.getOpposite()).definition == BCTransportPlugs.powerAdaptor) {
            //    if (holder.hasCapability(CapabilityEnergy.ENERGY, side.getOpposite()))
            //}
            providers[o] = ((TilePipeHolder) tile).getPipe();
        } else if (tile != null && tile.hasCapability(CapabilityEnergy.ENERGY, side.getOpposite())) {
            providers[o] = tile.getCapability(CapabilityEnergy.ENERGY, side.getOpposite());
        }
    }

    private void init() {
        if (needsInit) {
            needsInit = false;
            for (EnumFacing side : EnumFacing.VALUES) {
                updateTile(side);
            }
        }
    }
    int counter = 0;

    public void updateEntity() {
        if (pipe.getHolder().getPipeWorld().isRemote) {
            for (int i = 0; i < 6; i++) {
                if (displayPowerAverage[i] == null) displayPowerAverage[i] = new AverageDouble(8);
                displayPowerAverage[i].tick(displayPower[i]);
            }
            return;
        }

        short[] lastFlows = new short[6];
        double[] lastDisplayPower = new double[6];

        for (EnumFacing face : EnumFacing.VALUES) {
            int i = face.ordinal();
            lastFlows[i] = displayFlow[i];
            lastDisplayPower[i] = displayPower[i];
        }


        counter++;

        step2();

        init();

        for (EnumFacing side : EnumFacing.VALUES) {
            if ((tiles[side.ordinal()] != null && tiles[side.ordinal()].isInvalid()) || counter % 5 == 0) {
                updateTile(side);
            }
        }

        // FIXME: LEFT OVER FROM MERGE! LOOK AT THIS!
        //Arrays.fill(displayFlow, (short) 0);

        // Send the power to nearby pipes who requested it
        for (int i = 0; i < 6; ++i) {
            if (internalPower[i] > 0) {
                int totalPowerQuery = 0;
                for (int j = 0; j < 6; ++j) {
                    if (j != i && powerQuery[j] > 0) {
                        Object ep = providers[j];
                        if (ep instanceof IPipe || ep instanceof IEnergyStorage) {
                            totalPowerQuery += powerQuery[j];
                        }
                    }
                }
                if (totalPowerQuery > 0) {
                    int unusedPowerQuery = totalPowerQuery;
                    for (int j = 0; j < 6; ++j) {
                        if (j != i && powerQuery[j] > 0) {
                            Object ep = providers[j];
                            double watts = Math.min(internalPower[i] * powerQuery[j] / unusedPowerQuery, internalPower[i]);
                            unusedPowerQuery -= powerQuery[j];

                            if (ep instanceof IPipe && ((IPipe) ep).getFlow() instanceof PipeFlowPower) {
                                PipeFlowPower nearbyTransport = (PipeFlowPower) ((IPipe) ep).getFlow();
                                watts = nearbyTransport.receiveEnergy(EnumFacing.VALUES[j].getOpposite(), watts);
                                internalPower[i] -= watts;
                                dbgEnergyOutput[j] += watts;

                                powerAverage[j].push((int) Math.ceil(watts));
                                powerAverage[i].push((int) Math.ceil(watts));

                                displayFlow[i] = 1;
                                displayFlow[j] = -1;
                            } else {
                                int iWatts = (int) watts;
                                if (ep instanceof IEnergyStorage) {
                                    IEnergyStorage handler = (IEnergyStorage) ep;
                                    if (handler.canReceive()) {
                                        iWatts = handler.receiveEnergy(iWatts, false);
                                    }
                                }

                                internalPower[i] -= iWatts;
                                dbgEnergyOutput[j] += iWatts;

                                powerAverage[j].push(iWatts);
                                powerAverage[i].push(iWatts);

                                displayFlow[i] = 1;
                                displayFlow[j] = -1;
                            }

                        }
                    }
                }
            }
        }

        double highestPower = 0;
        for (int i = 0; i < 6; i++) {
            powerAverage[i].tick();
            double value = powerAverage[i].getAverage() / (double) maxPower;
            value = Math.sqrt(value);
            displayPower[i] = (value);
            if (displayPower[i] > highestPower) {
                highestPower = displayPower[i];
            }
        }


        overload += highestPower > (maxPower * 0.95F) ? 1 : -1;
        if (overload < 0) {
            overload = 0;
        }
        if (overload > OVERLOAD_TICKS) {
            overload = OVERLOAD_TICKS;
        }

        // Compute the tiles requesting energy that are not power pipes
        for (EnumFacing dir : EnumFacing.VALUES) {
            if (!pipe.isConnected(dir)) {
                continue;
            }

            Object tile = providers[dir.ordinal()];

            if (tile instanceof IPipe && ((IPipe) tile).getFlow() instanceof PipeFlowPower) {
                continue;
            }
            if (tile instanceof TilePipeHolder && ((TilePipeHolder) tile).getPipe().getFlow() instanceof PipeFlowPower) {
                continue;
            }
            if (tile instanceof IEnergyStorage && !isReceiver) {
                IEnergyStorage handler = (IEnergyStorage) tile;
                int request = handler.receiveEnergy(this.maxPower, true);
                if (request > 0) {
                    requestEnergy(dir, request);
                }
            }
        }

        // Sum the amount of energy requested on each side
        int[] transferQuery = new int[6];
        for (int i = 0; i < 6; ++i) {
            transferQuery[i] = 0;
            if (!pipe.isConnected(EnumFacing.VALUES[i])) {
                continue;
            }
            for (int j = 0; j < 6; ++j) {
                if (j != i) {
                    transferQuery[i] += powerQuery[j];
                }
            }
            transferQuery[i] = Math.min(transferQuery[i], maxPower);
        }

        // Transfer the requested energy to nearby pipes
        for (int i = 0; i < 6; ++i) {
            if (transferQuery[i] != 0 && tiles[i] != null) {
                TileEntity entity = tiles[i];
                if (entity instanceof TilePipeHolder) {
                    IPipe pipe = ((TilePipeHolder) entity).getPipe();
                    if (pipe.getFlow() instanceof PipeFlowPower) {
                        ((PipeFlowPower) pipe.getFlow()).requestEnergy(EnumFacing.VALUES[i].getOpposite(), transferQuery[i]);
                    }
                }
            }
        }

        // Networking
        boolean didChange = false;
        for (EnumFacing face : EnumFacing.VALUES) {
            int i = face.ordinal();
            if (lastFlows[i] != displayFlow[i] || lastDisplayPower[i] != displayPower[i]) {
                didChange = true;
            }
            if (powerQuery[i] == 0 && powerAverage[i].getAverage() != 0 && internalNextPower[i] == 0 && internalPower[i] == 0) {
                didChange = true;
                //displayPower[i] = 0;


                //powerAverage[i].tick();


                //powerAverage[i].clear();
            }
        }

        // if (tracker.markTimeIfDelay(pipe.getHolder().getPipeWorld())) {
        if (didChange || tracker.markTimeIfDelay(pipe.getHolder().getPipeWorld())) {
            sendPayload(NET_POWER_AMOUNTS);
        }



        if (isReceiver) {

            sources = 0;

            for (EnumFacing o : EnumFacing.VALUES) {
                boolean oldPowerSource = powerSources[o.ordinal()];

                if (pipe.getConnectedType(o) != ConnectedType.TILE) {
                    powerSources[o.ordinal()] = false;
                } else {
                    TileEntity tile = pipe.getConnectedTile(o);

                    powerSources[o.ordinal()] = isPowerSource(tile, o);
                    if (isPowerSource(tile, o)) {
                        sources++;
                    }
                }

                if (oldPowerSource != powerSources[o.ordinal()]) {
                    pipe.markForUpdate();
                }
            }

            if (pipe.getHolder().getPipeWorld().isRemote) {
                // We only do the isRemote check now to get a list
                // of power sources for client-side rendering.
                return;
            }

            if (sources <= 0) {
                extractEnergy(50, false);
                requestedEnergy = 0;
                return;
            }


            int energyToRemove = Math.min(getEnergyStored(), requestedEnergy);

            energyToRemove /= sources;

            if (getEnergyStored() > 0) {
                for (EnumFacing o : EnumFacing.VALUES) {
                    if (!powerSources[o.ordinal()]) {
                        continue;
                    }
                    // PipePowerWood's resistance is 0, so this is fine.
                    energy -= (int) receiveEnergy(o, energyToRemove);
                }
            }

            requestedEnergy = 0;
        }
    }

    private SafeTimeTracker tracker = new SafeTimeTracker(20);


    @Override
    public void writePayload(int id, PacketBuffer buffer, Side side) {
        super.writePayload(id, buffer, side);
        if (side == Side.SERVER) {
            if (id == NET_POWER_AMOUNTS || id == NET_ID_FULL_STATE) {
                buffer.writeBoolean(isOverloaded());
                for (int i = 0; i < displayPower.length; i++) {
                    buffer.writeDouble(displayPower[i]);
                    buffer.writeByte(displayFlow[i]);
                }
            }
        }
        buffer.writeInt(energy);
    }

    @Override
    public void readPayload(int id, PacketBuffer buffer, Side side) throws IOException {
        super.readPayload(id, buffer, side);
        if (side == Side.CLIENT) {
            if (id == NET_POWER_AMOUNTS || id == NET_ID_FULL_STATE) {
                displayPower = new double[6];
                displayFlow = new short[6];
                overload = buffer.readBoolean() ? OVERLOAD_TICKS : 0;
                for (int i = 0; i < displayPower.length; i++) {
                    displayPower[i] = buffer.readDouble();
                    displayFlow[i] = buffer.readByte();
                }
            }
        }
        energy = buffer.readInt();
    }




    public void handlePowerPacket(PacketPowerUpdate packetPower) {
        //displayPower = packetPower.displayPower;
        //displayFlow = packetPower.displayFlow;
        //overload = packetPower.overload ? OVERLOAD_TICKS : 0;
    }

    public boolean isOverloaded() {
        return overload >= OVERLOAD_TICKS;
    }

    public boolean isOverloaded2() {
        for (AverageInt i : powerAverage) {
            if (Math.abs(maxPower-i.getAverage()) < 0.5) return true;
        }
        return false;
    }


    /** Do NOT ever call this from outside Buildcraft. It is NOT part of the API. All power input MUST go through
     * designated input pipes, such as Wooden Power Pipes or a subclass thereof.
     *
     * Otherwise you will make us very sad :( */
    public double receiveEnergy(EnumFacing from, double tVal) {
        int side = from.ordinal();
        double val = tVal;
        step2();

        dbgEnergyOffered[side] += val;

        if (this.pipe instanceof IPipeTransportPowerHook) {
            double ret = ((IPipeTransportPowerHook) this.pipe).receivePower(from, (int) val);
            if (ret >= 0) {
                return ret;
            }
        }

        if (internalNextPower[side] > maxPower) {
            return 0;
        }

        internalNextPower[side] += val;


        if (internalNextPower[side] > maxPower) {
            val -= internalNextPower[side] - maxPower;
            internalNextPower[side] = maxPower;
            if (val < 0) {
                val = 0;
            }
        }

        dbgEnergyInput[side] += val;
        return val;
    }

    public void requestEnergy(EnumFacing from, int amount) {
        if (isReceiver) {
            TileEntity te = pipe.getConnectedTile(from);
            if (te != null && te.hasCapability(CapabilityEnergy.ENERGY, from.getOpposite())) {
                requestedEnergy += amount;
            }
        }
        step2();

        if (this.pipe instanceof IPipeTransportPowerHook) {
            nextPowerQuery[from.ordinal()] += ((IPipeTransportPowerHook) this.pipe).requestPower(from, amount);
        } else {
            nextPowerQuery[from.ordinal()] += amount;
        }
    }


    public PipeFlowPower(IPipe pipe, NBTTagCompound nbt) {
        super(pipe, nbt);
        for (int i = 0; i < 6; ++i) {
            powerQuery[i] = nbt.getInteger("powerQuery[" + i + "]");
            nextPowerQuery[i] = nbt.getInteger("nextPowerQuery[" + i + "]");
            internalPower[i] = nbt.getInteger("internalPower[" + i + "]");
            internalNextPower[i] = nbt.getInteger("internalNextPower[" + i + "]");
            powerAverage[i] = new AverageInt(8);
        }
        if (isReceiver) {
            for (int i = 0; i < EnumFacing.VALUES.length; i++) {
                powerSources[i] = nbt.getBoolean("powerSources[" + i + "]");
            }
        }
        energy = nbt.getInteger("rf");
    }
    @Override
    public NBTTagCompound writeToNbt() {
        NBTTagCompound nbttagcompound = super.writeToNbt();

        for (int i = 0; i < 6; ++i) {
            nbttagcompound.setInteger("powerQuery[" + i + "]", powerQuery[i]);
            nbttagcompound.setInteger("nextPowerQuery[" + i + "]", nextPowerQuery[i]);
            nbttagcompound.setDouble("internalPower[" + i + "]", internalPower[i]);
            nbttagcompound.setDouble("internalNextPower[" + i + "]", internalNextPower[i]);
        }
        if (isReceiver) {
            for (int i = 0; i < EnumFacing.VALUES.length; i++) {
                nbttagcompound.setBoolean("powerSources[" + i + "]", powerSources[i]);
            }
        }
        nbttagcompound.setInteger("rf", energy);

        return nbttagcompound;
    }


    @Override
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        StringBuilder s = new StringBuilder();
        StringBuilder s2 = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            if (powerAverage[i] != null) {
                s.append(powerAverage[i].toString()).append(" ");
            }
            if (displayPowerAverage[i] != null) {
                s2.append(displayPowerAverage[i].toString()).append(" ");
            }
        }
        left.add("averagepower"+s);
        right.add("displayPower"+Arrays.toString(displayPower));
        right.add("dpAverage"+s2);
        right.add("flow"+Arrays.toString(displayFlow));
        left.add("query: "+Arrays.toString(powerQuery));
        left.add("internal: "+Arrays.toString(internalPower)+"<-"+Arrays.toString(internalNextPower));
    }
    public boolean[] powerSources = new boolean[6];

    private int requestedEnergy, sources;

    private int energy;
    private final int capacity = 2560;

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (!isReceiver) return 0;
        int energyReceived = Math.min(capacity - energy, Math.min(2560, maxReceive));
        if (!simulate) energy += energyReceived;
        return energyReceived;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return 0;
    }

    @Override
    public int getEnergyStored()
    {
        return isReceiver ? energy : 0;
    }

    @Override
    public int getMaxEnergyStored()
    {
        return isReceiver ? capacity : 0;
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public boolean canReceive() {
        return isReceiver;
    }
}
