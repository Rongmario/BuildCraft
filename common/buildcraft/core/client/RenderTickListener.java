/* Copyright (c) 2016 AlexIIL and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core.client;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.profiler.Profiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;

import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import buildcraft.api.core.IBox;
import buildcraft.api.items.IMapLocation.MapLocationType;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.core.BCCoreConfig;
import buildcraft.core.BCCoreItems;
import buildcraft.core.EntityLaser;
import buildcraft.core.LaserData;
import buildcraft.core.item.ItemMapLocation;
import buildcraft.core.item.ItemMarkerConnector;
import buildcraft.core.lib.utils.Utils;
import buildcraft.core.render.RenderLaser;
import buildcraft.lib.BCLibProxy;
import buildcraft.lib.client.render.DetatchedRenderer;
import buildcraft.lib.client.render.laser.LaserData_BC8;
import buildcraft.lib.client.render.laser.LaserData_BC8.LaserType;
import buildcraft.lib.client.render.laser.LaserRenderer_BC8;
import buildcraft.lib.marker.MarkerCache;
import buildcraft.lib.marker.MarkerSubCache;
import buildcraft.lib.misc.MatrixUtil;
import buildcraft.lib.misc.VecUtil;

public enum RenderTickListener {
    INSTANCE;

    private static final Vec3d[][][] MAP_LOCATION_POINT = new Vec3d[6][][];
    private static final String DIFF_START, DIFF_HEADER_FORMATTING;

    static {
        double[][][] upFace = {// Comments for formatting :)
            { { 0.5, 0.9, 0.5 }, { 0.5, 1.6, 0.5 } },// Main line
            { { 0.5, 0.9, 0.5 }, { 0.8, 1.2, 0.5 } }, // First arrow part (+X)
            { { 0.5, 0.9, 0.5 }, { 0.2, 1.2, 0.5 } }, // Second arrow part (-X)
            { { 0.5, 0.9, 0.5 }, { 0.5, 1.2, 0.8 } }, // Third arrow part (+Z)
            { { 0.5, 0.9, 0.5 }, { 0.5, 1.2, 0.2 } }, // Forth arrow part (-Z)
        };

        for (EnumFacing face : EnumFacing.values()) {
            Matrix4f matrix = MatrixUtil.rotateTowardsFace(EnumFacing.UP, face);
            Vec3d[][] arr = new Vec3d[5][2];
            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 2; j++) {
                    double[] from = upFace[i][j];
                    Point3f point = new Point3f(new Point3d(from));
                    matrix.transform(point);
                    Vec3d to = new Vec3d(point.x, point.y, point.z);
                    arr[i][j] = to;
                }
            }

            MAP_LOCATION_POINT[face.ordinal()] = arr;
        }
        DIFF_START = TextFormatting.RED + "" + TextFormatting.BOLD + "!" + TextFormatting.RESET;
        DIFF_HEADER_FORMATTING = TextFormatting.AQUA + "" + TextFormatting.BOLD;
    }

    @SubscribeEvent
    public void renderOverlay(RenderGameOverlayEvent.Text event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (!mc.gameSettings.showDebugInfo) return;
        if (mc.thePlayer.hasReducedDebug() || mc.gameSettings.reducedDebugInfo || !mc.thePlayer.capabilities.isCreativeMode) {
            return;
        }
        List<String> left = event.getLeft();
        List<String> right = event.getRight();

        RayTraceResult mouseOver = mc.objectMouseOver;
        if (mouseOver == null) {
            return;
        }
        Type type = mouseOver.typeOfHit;

        boolean both = BCCoreConfig.useLocalServerOnClient;

        IDebuggable client = getDebuggableObject(mouseOver);
        IDebuggable server = both ? getServer(client) : null;

        if (client == null) return;
        EnumFacing side = mouseOver.sideHit;
        if (server == null) {
            client.getDebugInfo(left, right, side);
        } else {
            List<String> serverLeft = new ArrayList<>();
            List<String> serverRight = new ArrayList<>();

            List<String> clientLeft = new ArrayList<>();
            List<String> clientRight = new ArrayList<>();

            server.getDebugInfo(serverLeft, serverRight, side);
            client.getDebugInfo(clientLeft, clientRight, side);

            final String headerFirst = DIFF_HEADER_FORMATTING + "SERVER:";
            final String headerSecond = DIFF_HEADER_FORMATTING + "CLIENT:";
            appendDiff(left, serverLeft, clientLeft, headerFirst, headerSecond);
            appendDiff(right, serverRight, clientRight, headerFirst, headerSecond);

        }
    }

    private static IDebuggable getDebuggableObject(RayTraceResult mouseOver) {
        Type type = mouseOver.typeOfHit;
        WorldClient world = Minecraft.getMinecraft().theWorld;
        if (type == Type.BLOCK) {
            BlockPos pos = mouseOver.getBlockPos();
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof IDebuggable) {
                return (IDebuggable) tile;
            }
        }
        return null;
    }

    private static IDebuggable getServer(IDebuggable client) {
        if (client == null) return null;
        if (client instanceof TileEntity) {
            TileEntity tile = (TileEntity) client;
            tile = BCLibProxy.getProxy().getServerTile(tile);
            if (tile != client && tile instanceof IDebuggable) {
                return (IDebuggable) tile;
            }
        }
        return null;
    }

    private static void appendDiff(List<String> dest, List<String> first, List<String> second, String headerFirst, String headerSecond) {
        if (first.isEmpty()) return;
        dest.add("");
        dest.add(headerFirst);
        dest.addAll(first);
        dest.add("");
        dest.add(headerSecond);
        if (first.size() != second.size()) {
            // no diffing
            dest.addAll(second);
        } else {
            for (int l = 0; l < first.size(); l++) {
                String shownLine = first.get(l);
                String diffLine = second.get(l);
                if (shownLine.equals(diffLine)) {
                    dest.add(diffLine);
                } else {
                    if (diffLine.startsWith(" ")) {
                        dest.add(DIFF_START + diffLine.substring(1));
                    } else {
                        dest.add(DIFF_START + diffLine);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void tick(RenderWorldLastEvent event) {
        float partialTicks = event.getPartialTicks();
        renderHeldItemInWorld(partialTicks);
    }

    private static void renderHeldItemInWorld(float partialTicks) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (player == null) return;
        ItemStack mainHand = player.getHeldItemMainhand();
        ItemStack offHand = player.getHeldItemOffhand();
        WorldClient world = mc.theWorld;

        mc.mcProfiler.startSection("bc");
        mc.mcProfiler.startSection("renderWorld");

        DetatchedRenderer.fromWorldOriginPre(player, partialTicks);

        Item mainHandItem = mainHand == null ? null : mainHand.getItem();
        Item offHandItem = offHand == null ? null : offHand.getItem();

        if (mainHandItem == BCCoreItems.mapLocation) {
            renderMapLocation(world, mainHand);
        } else if (mainHandItem == BCCoreItems.markerConnector || offHandItem == BCCoreItems.markerConnector) {
            renderMarkerConnector(world, player, partialTicks);
        }

        DetatchedRenderer.fromWorldOriginPost();

        mc.mcProfiler.endSection();
        mc.mcProfiler.endSection();
    }

    private static void renderMapLocation(WorldClient world, ItemStack stack) {
        MapLocationType type = MapLocationType.getFromStack(stack);
        if (type == MapLocationType.SPOT) {
            EnumFacing face = ItemMapLocation.getPointFace(stack);
            IBox box = ItemMapLocation.getPointBox(stack);
            Vec3d[][] vectors = MAP_LOCATION_POINT[face.ordinal()];
            GL11.glTranslated(box.min().getX(), box.min().getY(), box.min().getZ());
            for (Vec3d[] vec : vectors) {
                LaserData laser = new LaserData(vec[0], vec[1]);
                RenderLaser.doRenderLaser(world, Minecraft.getMinecraft().getTextureManager(), laser, EntityLaser.LASER_STRIPES_YELLOW);
            }
        } else if (type == MapLocationType.AREA) {
            IBox box = ItemMapLocation.getAreaBox(stack);
            LaserData[] laserBox = Utils.createLaserDataBox(new Vec3d(box.min()), new Vec3d(box.max().add(1, 1, 1)));

            for (LaserData laser : laserBox) {
                RenderLaser.doRenderLaser(world, Minecraft.getMinecraft().getTextureManager(), laser, EntityLaser.LASER_STRIPES_YELLOW);
            }
        } else if (type == MapLocationType.PATH) {
            // TODO!
        } else if (type == MapLocationType.ZONE) {
            // TODO!
        }
    }

    private static void renderMarkerConnector(WorldClient world, EntityPlayer player, float partialTicks) {
        Profiler profiler = Minecraft.getMinecraft().mcProfiler;
        profiler.startSection("marker");
        for (MarkerCache<?> cache : MarkerCache.CACHES) {
            profiler.startSection(cache.name);
            renderMarkerCache(player, cache.getSubCache(world));
            profiler.endSection();
        }
        profiler.endSection();
    }

    private static void renderMarkerCache(EntityPlayer player, MarkerSubCache<?> cache) {
        Profiler profiler = Minecraft.getMinecraft().mcProfiler;
        profiler.startSection("compute");
        Set<LaserData_BC8> toRender = new HashSet<>();
        for (final BlockPos a : cache.getAllMarkers()) {
            for (final BlockPos b : cache.getValidConnections(a)) {
                if (a.toLong() > b.toLong()) {
                    // Only render each pair once
                    continue;
                }

                Vec3d start = new Vec3d(a).add(Utils.VEC_HALF);
                Vec3d end = new Vec3d(b).add(Utils.VEC_HALF);

                Vec3d startToEnd = end.subtract(start).normalize();
                Vec3d endToStart = start.subtract(end).normalize();
                start = start.add(VecUtil.scale(startToEnd, 0.125));
                end = end.add(VecUtil.scale(endToStart, 0.125));

                LaserType laserType = cache.getPossibleLaserType();
                if (laserType == null || isLookingAt(a, b, player)) {
                    laserType = BuildCraftLaserManager.MARKER_DEFAULT_POSSIBLE;
                }

                LaserData_BC8 data = new LaserData_BC8(laserType, start, end, 1 / 16.0);
                toRender.add(data);
            }
        }
        profiler.endStartSection("render");
        for (LaserData_BC8 laser : toRender) {
            LaserRenderer_BC8.renderLaserGlList(laser);
        }
        profiler.endSection();
    }

    private static boolean isLookingAt(BlockPos from, BlockPos to, EntityPlayer player) {
        return ItemMarkerConnector.doesInteract(from, to, player);
    }
}
