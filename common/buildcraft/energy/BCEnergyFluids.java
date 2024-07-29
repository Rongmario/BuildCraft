/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.material.MapColor;
import net.minecraft.util.ResourceLocation;

import buildcraft.api.BCModules;

import buildcraft.lib.fluid.BCFluid;
import buildcraft.lib.fluid.BCFluidBlock;
import buildcraft.lib.fluid.FluidManager;

public class BCEnergyFluids {
    public static BCFluid oil;
    public static BCFluid fuel;


    public static BCFluid[] crudeOil = new BCFluid[1]; // these arrays are for mod (TR) compat
    public static BCFluid[] oilDense = {};
    public static BCFluid[] oilDistilled = {};
    public static BCFluid[] oilHeavy = {};
    public static BCFluid[] fuelDense = {};
    public static BCFluid[] fuelLight = new BCFluid[1];
    public static BCFluid[] fuelMixedHeavy = {};
    public static BCFluid[] fuelMixedLight = {};
    public static BCFluid[] fuelGaseous = {};
    public static BCFluid[] oilResidue = {};

    public static final List<BCFluid> allFluids = new ArrayList<>();

    public static void preInit() {
        int[][] data = { //@formatter:off
            // Tabular form of all the fluid values
            // density, viscosity, boil, spread,  tex_light,   tex_dark, sticky, flammable
            {      900,      2000,    3,      6, 0x50_50_50, 0x05_05_05,      1,         1 },// Oil
            {      400,       600,    1,      8, 0xFF_FF_30, 0xE4_CF_00,      0,         1 },// Fuel

        };//@formatter:on
        if (BCModules.FACTORY.isLoaded()) {
            oil = defineFluid(data[0], "oil");
            crudeOil[0] = oil;
            fuel = defineFluid(data[1], "fuel");
            fuelLight[0] = fuel;
        }
    }

    private static BCFluid defineFluid(int[] data, String name) {
        final int density = data[0];
        final int baseViscosity = data[1];
        final int baseQuanta = data[3];
        final int texLight = data[4];
        final int texDark = data[5];
        final boolean sticky = BCEnergyConfig.oilIsSticky && data[6] == 1;
        final boolean flammable = BCEnergyConfig.enableOilBurn && data[7] == 1;

        String fluidTexture = "buildcraftenergy:blocks/fluids/" + name;
        BCFluid def = new BCFluid(name, new ResourceLocation(fluidTexture + "_still"),
            new ResourceLocation(fluidTexture + "_flow"));
        def.setBlockName(name);
        def.setMapColour(getMapColor(texDark));
        def.setFlammable(flammable);
        def.setUnlocalizedName(name);
        def.setTemperature(300);
        def.setViscosity(baseViscosity);
        def.setDensity(density);
        def.setGaseous(def.getDensity() < 0);
        def.setColour(texLight, texDark);
        FluidManager.register(def);

        BCFluidBlock block = (BCFluidBlock) def.getBlock();
        block.setLightOpacity(3);
        block.setSticky(sticky);
        // Distance that the fluid will travel: 1->16
        // Higher heat values travel a little further
        block.setQuantaPerBlock(baseQuanta);

        allFluids.add(def);
        return def;
    }

    private static MapColor getMapColor(int color) {
        MapColor bestMapColor = MapColor.BLACK;
        int currentDifference = Integer.MAX_VALUE;

        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        for (MapColor mapColor : MapColor.COLORS) {
            if (mapColor == null || mapColor.colorValue == 0) {
                continue;
            }
            int mr = (mapColor.colorValue >> 16) & 0xFF;
            int mg = (mapColor.colorValue >> 8) & 0xFF;
            int mb = mapColor.colorValue & 0xFF;

            int dr = mr - r;
            int dg = mg - g;
            int db = mb - b;

            int difference = dr * dr + dg * dg + db * db;

            if (difference < currentDifference) {
                currentDifference = difference;
                bestMapColor = mapColor;
            }
        }
        return bestMapColor;
    }
}
