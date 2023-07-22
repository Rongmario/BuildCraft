/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory;

import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.lib.client.model.ModelHolderVariable;

import buildcraft.factory.client.render.RenderDistiller;
import buildcraft.factory.client.render.RenderMiningWell;
import buildcraft.factory.client.render.RenderPump;
import buildcraft.factory.client.render.RenderTank;
import buildcraft.factory.tile.TileDistiller_BC8;
import buildcraft.factory.tile.TileMiningWell;
import buildcraft.factory.tile.TilePump;
import buildcraft.factory.tile.TileTank;

public class BCFactoryModels {
    public static final ModelHolderVariable DISTILLER = new ModelHolderVariable(
        "buildcraftfactory:models/tiles/distiller.json",
        TileDistiller_BC8.MODEL_FUNC_CTX
    );


    public static void fmlPreInit() {
        MinecraftForge.EVENT_BUS.register(BCFactoryModels.class);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void onModelRegistry(ModelRegistryEvent event) {

    }

    public static void fmlInit() {
        ClientRegistry.bindTileEntitySpecialRenderer(TileMiningWell.class, new RenderMiningWell());
        ClientRegistry.bindTileEntitySpecialRenderer(TilePump.class, new RenderPump());
        ClientRegistry.bindTileEntitySpecialRenderer(TileTank.class, new RenderTank());
        ClientRegistry.bindTileEntitySpecialRenderer(TileDistiller_BC8.class, new RenderDistiller());
    }

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {

    }
}
