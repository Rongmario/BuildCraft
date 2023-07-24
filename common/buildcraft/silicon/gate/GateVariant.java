/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.gate;

import java.util.Objects;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

import buildcraft.lib.misc.LocaleUtil;

public class GateVariant {
    public final EnumGateLogic logic;
    public final EnumGateMaterial material;
    public final int numSlots;
    public final int numTriggerArgs, numActionArgs;
    private final int hash;

    public GateVariant(EnumGateLogic logic, EnumGateMaterial material) {
        this.logic = logic;
        this.material = material;
        this.numSlots = material.numSlots;
        this.numTriggerArgs = material.triggerArgs;
        this.numActionArgs = material.actionArgs;
        this.hash = Objects.hash(logic, material);
    }

    public GateVariant(NBTTagCompound nbt) {
        this.logic = EnumGateLogic.getByOrdinal(nbt.getByte("logic"));
        this.material = EnumGateMaterial.getByOrdinal(nbt.getByte("material"));
        this.numSlots = material.numSlots;
        this.numTriggerArgs = material.triggerArgs;
        this.numActionArgs = material.actionArgs;
        this.hash = Objects.hash(logic, material);
    }

    public NBTTagCompound writeToNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setByte("logic", (byte) logic.ordinal());
        nbt.setByte("material", (byte) material.ordinal());
        return nbt;
    }

    public GateVariant(PacketBuffer buffer) {
        this.logic = EnumGateLogic.getByOrdinal(buffer.readUnsignedByte());
        this.material = EnumGateMaterial.getByOrdinal(buffer.readUnsignedByte());
        this.numSlots = material.numSlots;
        this.numTriggerArgs = material.triggerArgs;
        this.numActionArgs = material.actionArgs;
        this.hash = Objects.hash(logic, material);
    }

    public void writeToBuffer(PacketBuffer buffer) {
        buffer.writeByte(logic.ordinal());
        buffer.writeByte(material.ordinal());
    }

    public String getVariantName() {
        return material.tag+"_"+logic.tag;
    }

    public String getLocalizedName() {
        if (material == EnumGateMaterial.BASIC) {
            return LocaleUtil.localize("gate.name.basic");
        } else {
            String gateName = LocaleUtil.localize("gate.name");
            String materialName = LocaleUtil.localize("gate.material." + material.tag);
            Object logicName = LocaleUtil.localize("gate.logic." + logic.tag);
            return String.format(gateName, materialName, logicName);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (obj.getClass() != getClass()) return false;
        GateVariant other = (GateVariant) obj;
        return other.logic == logic//
            && other.material == material;
    }

    @Override
    public int hashCode() {
        return hash;
    }
}
