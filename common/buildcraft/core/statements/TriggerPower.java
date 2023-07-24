/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.statements;

import buildcraft.transport.BCTransport;
import buildcraft.transport.TriggerPowerHandler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.ITriggerExternal;
import buildcraft.api.statements.ITriggerInternal;

import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;
import buildcraft.lib.misc.LocaleUtil;

import buildcraft.core.BCCoreSprites;
import buildcraft.core.BCCoreStatements;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.common.Loader;

public class TriggerPower extends BCStatement implements ITriggerInternal, ITriggerExternal {
    private final boolean high;

    public TriggerPower(boolean high) {
        super("buildcraft:energyStored" + (high ? "high" : "low"));
        this.high = high;
    }

    @Override
    public SpriteHolder getSprite() {
        return high ? BCCoreSprites.TRIGGER_POWER_HIGH : BCCoreSprites.TRIGGER_POWER_LOW;
    }

    @Override
    public String getDescription() {
        return LocaleUtil.localize("gate.trigger.machine.energyStored." + (high ? "high" : "low"));
    }

    public boolean isTriggeredMjConnector(IEnergyStorage readable) {
        if (readable == null) {
            return false;
        }
        long stored = readable.getEnergyStored();
        long max = readable.getMaxEnergyStored();

        if (max > 0) {
            double level = stored / (double) max;
            if (high) {
                return level > 0.95;
            } else {
                return level < 0.05;
            }
        }
        return false;
    }

    public static boolean isTriggeringTile(TileEntity tile) {
        return isTriggeringTile(tile, null);
    }

    public static boolean isTriggeringTile(TileEntity tile, EnumFacing face) {
        if (Loader.isModLoaded(BCTransport.MODID)) {
            return TriggerPowerHandler.isTriggeringTile(tile, face);
        }
        return tile.hasCapability(CapabilityEnergy.ENERGY, face);
    }

    protected boolean isActive(TileEntity tile, EnumPipePart side) {
        return isTriggeredMjConnector(tile.getCapability(CapabilityEnergy.ENERGY, side.face));
    }

    @Override
    public boolean isTriggerActive(IStatementContainer source, IStatementParameter[] parameters) {
        return isActive(source.getTile(), EnumPipePart.CENTER);
    }

    @Override
    public boolean isTriggerActive(TileEntity target, EnumFacing side, IStatementContainer source, IStatementParameter[] parameters) {
        return isActive(target, EnumPipePart.fromFacing(side.getOpposite()));
    }

    @Override
    public IStatement[] getPossible() {
        return BCCoreStatements.TRIGGER_POWER;
    }
}
