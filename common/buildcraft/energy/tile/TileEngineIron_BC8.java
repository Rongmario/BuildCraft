/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy.tile;

import java.io.IOException;

import javax.annotation.Nonnull;

import buildcraft.energy.BCEnergyConfig;
import buildcraft.energy.BCEnergyFluids;
import buildcraft.lib.fluid.FluidManager;
import buildcraft.lib.tile.item.ItemHandlerManager;
import buildcraft.lib.tile.item.ItemHandlerSimple;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;

import net.minecraftforge.fluids.*;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.core.IFluidFilter;
import buildcraft.api.core.IFluidHandlerAdv;
import buildcraft.api.fuels.BuildcraftFuelRegistry;
import buildcraft.api.fuels.IFuel;
import buildcraft.api.fuels.IFuelManager.IDirtyFuel;
import buildcraft.api.fuels.ISolidCoolant;
import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.transport.pipe.IItemPipe;

import buildcraft.lib.engine.EngineConnector;
import buildcraft.lib.engine.TileEngineBase_BC8;
import buildcraft.lib.fluid.Tank;
import buildcraft.lib.fluid.TankProperties;
import buildcraft.lib.gui.help.ElementHelpInfo;
import buildcraft.lib.misc.CapUtil;
import buildcraft.lib.misc.EntityUtil;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.net.PacketBufferBC;

import buildcraft.energy.BCEnergyGuis;

public class TileEngineIron_BC8 extends TileEngineBase_BC8 {
    public static final int MAX_FLUID = 10_000;

    public static final double COOLDOWN_RATE = 0.05;
    public static final int MAX_COOLANT_PER_TICK = 40;

    public final Tank tankFuel = new Tank("fuel", MAX_FLUID, this, this::isValidFuel);
    public final Tank tankCoolant = new Tank("coolant", MAX_FLUID, this, this::isValidCoolant) {
        @Override
        protected FluidGetResult map(ItemStack stack, int space) {
            ISolidCoolant coolant = BuildcraftFuelRegistry.coolant.getSolidCoolant(stack);
            if (coolant == null) {
                return super.map(stack, space);
            }
            FluidStack fluidCoolant = coolant.getFluidFromSolidCoolant(stack);
            if (fluidCoolant == null || fluidCoolant.amount <= 0 || fluidCoolant.amount > space) {
                return super.map(stack, space);
            }
            return new FluidGetResult(StackUtil.EMPTY, fluidCoolant);
        }
    };
    private final IFluidHandlerAdv fluidHandler = new InternalFluidHandler();

    private int penaltyCooling = 0;
    private boolean lastPowered = false;
    private double burnTime;
    private IFuel currentFuel;

    public final ItemHandlerSimple invFuel;


    public TileEngineIron_BC8() {

        invFuel = itemManager.addInvHandler("fuel", 1, this::isValidFuel, ItemHandlerManager.EnumAccess.BOTH, EnumPipePart.VALUES);

        tankManager.addAll(tankFuel, tankCoolant);

        caps.addCapabilityInstance(CapUtil.CAP_FLUIDS, fluidHandler, EnumPipePart.VALUES);
    }

    private boolean isValidFuel(int slot, ItemStack stack) {
        if (stack.getItem() == ItemBlock.getItemFromBlock(Blocks.ICE) || stack.getItem() == ItemBlock.getItemFromBlock(Blocks.PACKED_ICE)) return true;
        if (stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
            IFluidHandlerItem cap = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
            FluidStack toDrain = cap.drain(1000, false);
            return isValidFuel(toDrain) || isValidCoolant(toDrain);
        }
        return false;
    }

    @Override
    public void update() {
        super.update();

        if (!world.isRemote) {
            ItemStack stack = invFuel.getStackInSlot(0);
            boolean update = false;
            if (stack.getItem() == Items.WATER_BUCKET) {
                FluidStack water = new FluidStack(FluidRegistry.WATER, 1000);
                if (tankCoolant.fill(water, false) == water.amount) {
                    tankCoolant.fill(water, true);
                    invFuel.setStackInSlot(0, new ItemStack(Items.BUCKET));
                    update = true;
                }
            }
            if (isValidFuel(FluidUtil.getFluidContained(stack))) {
                FluidStack fuel = FluidUtil.getFluidContained(stack);
                if (FluidUtil.getFilledBucket(new FluidStack(fuel.getFluid(), Fluid.BUCKET_VOLUME)).isItemEqual(stack)) {
                    if (tankFuel.fill(fuel, false) == fuel.amount) {
                        tankFuel.fill(fuel, true);
                        invFuel.setStackInSlot(0, new ItemStack(Items.BUCKET));
                        update = true;
                    }
                }
            }
            if (stack.getCount() > 0) {
                FluidStack water = new FluidStack(FluidRegistry.WATER, 0);
                if (stack.getItem() == ItemBlock.getItemFromBlock(Blocks.ICE)) {
                    water.amount = 1000;
                } else if (stack.getItem() == ItemBlock.getItemFromBlock(Blocks.PACKED_ICE)) {
                    water.amount = 3000;
                }
                if (water.amount > 0) {
                    if (tankCoolant.fill(water, false) == water.amount) {
                        tankCoolant.fill(water, true);
                        invFuel.extractItem(0, 1, false);
                        update = true;
                    }
                }
            }
            if (update) {
                sendNetworkGuiUpdate(NET_GUI_DATA);
                sendNetworkGuiUpdate(NET_GUI_TICK);
            }
        }
    }

        // TileEntity overrides

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("penaltyCooling", penaltyCooling);
        nbt.setDouble("burnTime", burnTime);
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        penaltyCooling = nbt.getInteger("penaltyCooling");
        burnTime = nbt.getDouble("burnTime");
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, Side side, MessageContext ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == Side.CLIENT) {
            if (id == NET_GUI_DATA || id == NET_GUI_TICK) {
                tankManager.readData(buffer);
            }
        }
    }

    @Override
    public void writePayload(int id, PacketBufferBC buffer, Side side) {
        super.writePayload(id, buffer, side);
        if (side == Side.SERVER) {
            if (id == NET_GUI_DATA || id == NET_GUI_TICK) {
                tankManager.writeData(buffer);
            }
        }
    }

    // TileEngineBase overrides

    @Override
    public boolean onActivated(EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY,
        float hitZ) {
        ItemStack current = player.getHeldItem(hand).copy();
        if (super.onActivated(player, hand, side, hitX, hitY, hitZ)) {
            return true;
        }
        if (!current.isEmpty()) {
            if (EntityUtil.getWrenchHand(player) != null) {
                return false;
            }
            if (current.getItem() instanceof IItemPipe) {
                return false;
            }
        }
        if (!world.isRemote) {
            BCEnergyGuis.ENGINE_IRON.openGUI(player, getPos());
        }
        return true;
    }

    @Override
    public double getPistonSpeed() {
        switch (getPowerStage()) {
            case BLUE:
                return 0.04;
            case GREEN:
                return 0.05;
            case YELLOW:
                return 0.06;
            case RED:
                return 0.07;
            default:
                return 0;
        }
    }

    @Nonnull
    @Override
    protected IMjConnector createConnector() {
        return new EngineConnector(false);
    }

    @Override
    public boolean isBurning() {
        FluidStack fuel = tankFuel.getFluid();
        return fuel != null && fuel.amount > 0 && penaltyCooling == 0 && isRedstonePowered;
    }

    @Override
    protected void burn() {
        final FluidStack fuel = this.tankFuel.getFluid();
        if (currentFuel == null || !currentFuel.getFluid().isFluidEqual(fuel)) {
            currentFuel = BuildcraftFuelRegistry.fuel.getFuel(fuel);
        }

        if (fuel == null || currentFuel == null) {
            return;
        }

        if (penaltyCooling <= 0) {
            if (isRedstonePowered) {
                lastPowered = true;

                if (burnTime > 0 || fuel.amount > 0) {
                    if (burnTime > 0) {
                        burnTime--;
                    }
                    if (burnTime <= 0) {
                        if (fuel.amount > 0) {
                            fuel.amount--;
                            burnTime += currentFuel.getTotalBurningTime() / 1000.0;
                        } else {
                            tankFuel.setFluid(null);
                            currentFuel = null;
                            currentOutput = 0;
                            return;
                        }
                    }
                    currentOutput = currentFuel.getPowerPerCycle(); // Comment out for constant power
                    addPower(currentFuel.getPowerPerCycle());
                    heat += currentFuel.getPowerPerCycle() * BCEnergyConfig.heatPerMj / MjAPI.MJ;// * getBiomeTempScalar();
                }
            } else if (lastPowered) {
                lastPowered = false;
                penaltyCooling = 10;
                // 10 tick of penalty on top of the cooling
            }
        }

        if (burnTime <= 0 && fuel.amount <= 0) {
            tankFuel.setFluid(null);
        }
    }

    @Override
    public void updateHeatLevel() {
        double target;
        if (heat > MIN_HEAT && (penaltyCooling > 0 || !isRedstonePowered)) {
            heat -= COOLDOWN_RATE;
            target = MIN_HEAT;
        } else if (heat > IDEAL_HEAT) {
            target = IDEAL_HEAT;
        } else {
            target = heat;
        }

        if (target != heat) {
            // coolEngine(target)
            {
                double coolingBuffer = 0;
                double extraHeat = heat - target;

                if (extraHeat > 0) {
                    // fillCoolingBuffer();
                    {
                        if (tankCoolant.getFluidAmount() > 0) {
                            float coolPerMb =
                                BuildcraftFuelRegistry.coolant.getDegreesPerMb(tankCoolant.getFluid(), (float) heat);
                            if (coolPerMb > 0) {
                                int coolantAmount = Math.min(MAX_COOLANT_PER_TICK, tankCoolant.getFluidAmount());
                                float cooling = coolPerMb;
                                // cooling /= getBiomeTempScalar();
                                coolingBuffer += coolantAmount * cooling;
                                tankCoolant.drain(coolantAmount, true);
                            }
                        }
                    }
                    // end
                }

                // if (coolingBuffer >= extraHeat) {
                // coolingBuffer -= extraHeat;
                // heat -= extraHeat;
                // return;
                // }

                heat -= coolingBuffer;
                coolingBuffer = 0.0f;
            }
            // end
            getPowerStage();
        }

        if (heat <= MIN_HEAT && penaltyCooling > 0) {
            penaltyCooling--;
        }

        if (heat <= MIN_HEAT) {
            heat = MIN_HEAT;
        }
    }

    @Override
    public boolean isActive() {
        return penaltyCooling <= 0;
    }

    @Override
    public long getMaxPower() {
        return 10_000 * MjAPI.MJ;
    }

    @Override
    public long maxPowerReceived() {
        return 2_000 * MjAPI.MJ;
    }

    @Override
    public long maxPowerExtracted() {
        return 500 * MjAPI.MJ;
    }

    @Override
    public float explosionRange() {
        return 4;
    }

    @Override
    protected int getMaxChainLength() {
        return 4;
    }

    @Override
    public long getCurrentOutput() {
        if (currentFuel == null) {
            return 0;
        } else {
            return currentFuel.getPowerPerCycle();
        }
    }

    // Fluid related

    private boolean isValidFuel(FluidStack fluid) {
        return BuildcraftFuelRegistry.fuel.getFuel(fluid) != null;
    }

    private boolean isValidCoolant(FluidStack fluid) {
        return BuildcraftFuelRegistry.coolant.getCoolant(fluid) != null;
    }

    private class InternalFluidHandler implements IFluidHandlerAdv {
        private final IFluidTankProperties[] properties = { //
            new TankProperties(tankFuel, true, false), //
            new TankProperties(tankCoolant, true, false), //
        };

        @Override
        public IFluidTankProperties[] getTankProperties() {
            return properties;
        }

        @Override
        public int fill(FluidStack resource, boolean doFill) {
            int filled = tankFuel.fill(resource, doFill);
            if (filled == 0) {
                filled = tankCoolant.fill(resource, doFill);
            }
            return filled;
        }

        @Override
        public FluidStack drain(FluidStack resource, boolean doDrain) {
            return null;
        }

        @Override
        public FluidStack drain(int maxDrain, boolean doDrain) {
            return null;
        }

        @Override
        public FluidStack drain(IFluidFilter filter, int maxDrain, boolean doDrain) {
            return null;
        }
    }
}
