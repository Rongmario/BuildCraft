/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.pipe.behaviour;

import buildcraft.transport.pipe.flow.PipeFlowPower;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import buildcraft.api.mj.IMjReceiver;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.PipeBehaviour;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

public class PipeBehaviourWoodPower extends PipeBehaviourDirectional {

    public PipeBehaviourWoodPower(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourWoodPower(IPipe pipe, NBTTagCompound nbt) {
        super(pipe, nbt);
    }

    @Override
    protected boolean canFaceDirection(EnumFacing dir) {
        return dir != null && pipe.getConnectedType(dir) == IPipe.ConnectedType.TILE;
    }

    @Override
    public boolean canConnect(EnumFacing face, PipeBehaviour other) {
        return !(other instanceof PipeBehaviourWoodPower) && other.pipe.getFlow() instanceof PipeFlowPower;
    }

    @Override
    public boolean canConnect(EnumFacing face, TileEntity other) {
        return other.hasCapability(CapabilityEnergy.ENERGY, face.getOpposite());
    }

    @Override
    public int getTextureIndex(EnumFacing face) {
        if (face == null) {
            return 0;
        }
        if (pipe.getConnectedPipe(face) != null) {
            return 0;
        }
        TileEntity tile = pipe.getConnectedTile(face);
        if (tile == null) {
            return 0;
        }
        IEnergyStorage recv = tile.getCapability(CapabilityEnergy.ENERGY, face.getOpposite());
        return recv == null ? 1 : recv.canExtract() ? 0 : 1;
    }
}
