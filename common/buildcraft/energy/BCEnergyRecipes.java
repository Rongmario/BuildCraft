/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import buildcraft.api.BCModules;
import buildcraft.api.fuels.BuildcraftFuelRegistry;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.recipes.BuildcraftRecipeRegistry;
import buildcraft.api.recipes.IRefineryRecipeManager.IDistillationRecipe;

import buildcraft.lib.fluid.BCFluid;
import buildcraft.lib.misc.MathUtil;

public class BCEnergyRecipes {
    public static void init() {

        BuildcraftFuelRegistry.coolant.addCoolant(FluidRegistry.WATER, 0.0023f);
        BuildcraftFuelRegistry.coolant.addSolidCoolant(new ItemStack(Blocks.ICE),
            new FluidStack(FluidRegistry.WATER, 1000), 1.5f);
        BuildcraftFuelRegistry.coolant.addSolidCoolant(new ItemStack(Blocks.PACKED_ICE),
            new FluidStack(FluidRegistry.WATER, 1000), 2f);

        if (!BCEnergyConfig.disableEngineRecipes) {
            addFuel(BCEnergyFluids.fuel, (int) (6*MjAPI.MJ), 25000);
            addFuel(BCEnergyFluids.oil, (int) (3*MjAPI.MJ), 12500);
        }


        if (BCModules.FACTORY.isLoaded()) {
            addDistillation(new FluidStack(BCEnergyFluids.oil, 1), new FluidStack(BCEnergyFluids.fuel, 1), 12*MjAPI.MJ);
        }
    }

    private static void addFuel(Fluid in, int mmjPerTick, int ticksPerBucket) {
        BuildcraftFuelRegistry.fuel.addFuel(in, mmjPerTick, ticksPerBucket);
    }


    private static void addDistillation(FluidStack _in, FluidStack _outLiquid,
        long mjCost) {
        IDistillationRecipe existing =
            BuildcraftRecipeRegistry.refineryRecipes.getDistillationRegistry().getRecipeForInput(_in);
        if (existing != null) {
            throw new IllegalStateException("Already added distillation recipe for " + _in.getFluid().getName());
        }

        BuildcraftRecipeRegistry.refineryRecipes.addDistillationRecipe(_in, _outLiquid, mjCost);
    }

}
