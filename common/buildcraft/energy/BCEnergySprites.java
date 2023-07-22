/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy;

import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import buildcraft.lib.BCLibConfig;
import buildcraft.lib.fluid.BCFluid;

import buildcraft.energy.client.sprite.AtlasSpriteFluid;

public class BCEnergySprites {
    public static void fmlPreInit() {
        MinecraftForge.EVENT_BUS.register(BCEnergySprites.class);
    }

    @SubscribeEvent
    public static void onTextureStitchPre(TextureStitchEvent.Pre event) {
        TextureMap map = event.getMap();
        map.setTextureEntry(new AtlasSpriteFluid(BCEnergyFluids.fuel.getStill().toString(), new ResourceLocation("buildcraftenergy:blocks/fluids/fuel_still"), BCEnergyFluids.fuel));
        map.setTextureEntry(new AtlasSpriteFluid(BCEnergyFluids.fuel.getFlowing().toString(), new ResourceLocation("buildcraftenergy:blocks/fluids/fuel_flow"), BCEnergyFluids.fuel));
        map.setTextureEntry(new AtlasSpriteFluid(BCEnergyFluids.oil.getStill().toString(), new ResourceLocation("buildcraftenergy:blocks/fluids/oil_still"), BCEnergyFluids.oil));
        map.setTextureEntry(new AtlasSpriteFluid(BCEnergyFluids.oil.getFlowing().toString(), new ResourceLocation("buildcraftenergy:blocks/fluids/oil_flow"), BCEnergyFluids.oil));

    }
}
