/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import buildcraft.api.BCItems;
import buildcraft.api.enums.EnumEngineType;
import buildcraft.api.recipes.AssemblyRecipe;
import buildcraft.core.BCCoreBlocks;
import buildcraft.core.BCCoreItems;
import buildcraft.lib.misc.ColourUtil;
import buildcraft.silicon.recipe.FacadeAssemblyRecipes;
import buildcraft.silicon.recipe.FacadeSwapRecipe;
import buildcraft.transport.BCTransportItems;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import buildcraft.api.core.BCLog;
import buildcraft.api.enums.EnumRedstoneChipset;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.recipes.AssemblyRecipeBasic;
import buildcraft.api.recipes.IngredientStack;

import buildcraft.lib.recipe.AssemblyRecipeRegistry;
import buildcraft.lib.recipe.IngredientNBTBC;
import buildcraft.lib.recipe.RecipeBuilderShaped;

import buildcraft.core.BCCoreConfig;
import buildcraft.silicon.gate.EnumGateLogic;
import buildcraft.silicon.gate.EnumGateMaterial;
import buildcraft.silicon.gate.GateVariant;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

@Mod.EventBusSubscriber(modid = BCSilicon.MODID)
public class BCSiliconRecipes {
    private static Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    @SubscribeEvent
    public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        if (BCSiliconItems.plugGate != null) {



            /*
            // You can craft some of the basic gate types in a normal crafting table
            RecipeBuilderShaped builder = new RecipeBuilderShaped();
            builder.add(" m ");
            builder.add("mrm");
            builder.add(" b ");
            builder.map('r', "dustRedstone");
            builder.map('b', BCItems.Transport.PLUG_BLOCKER, Blocks.COBBLESTONE);

            // Base craftable types

            builder.map('m', Items.BRICK);
            makeGateRecipe(builder, EnumGateMaterial.CLAY_BRICK, EnumGateModifier.NO_MODIFIER);

            builder.map('m', "ingotIron");
            makeGateRecipe(builder, EnumGateMaterial.IRON, EnumGateModifier.NO_MODIFIER);

            builder.map('m', Items.NETHERBRICK);
            makeGateRecipe(builder, EnumGateMaterial.NETHER_BRICK, EnumGateModifier.NO_MODIFIER);

            // Iron modifier addition
            GateVariant variant =
                new GateVariant(EnumGateLogic.AND, EnumGateMaterial.IRON, EnumGateModifier.NO_MODIFIER);
            ItemStack ironGateBase = BCSiliconItems.plugGate.getStack(variant);
            builder = new RecipeBuilderShaped();
            builder.add(" m ");
            builder.add("mgm");
            builder.add(" m ");
            builder.map('g', ironGateBase);

            builder.map('m', new ItemStack(Items.DYE, 1, EnumDyeColor.BLUE.getDyeDamage()));
            makeGateRecipe(builder, EnumGateMaterial.IRON, EnumGateModifier.LAPIS);

            builder.map('m', Items.QUARTZ);
            makeGateRecipe(builder, EnumGateMaterial.IRON, EnumGateModifier.QUARTZ);

            // And Gate <-> Or Gate (shapeless)
            // TODO: Create a recipe class for this instead!
            for (EnumGateMaterial material : EnumGateMaterial.VALUES) {
                if (material == EnumGateMaterial.CLAY_BRICK) {
                    continue;
                }
                for (EnumGateModifier modifier : EnumGateModifier.VALUES) {
                    GateVariant varAnd = new GateVariant(EnumGateLogic.AND, material, modifier);
                    ItemStack resultAnd = BCSiliconItems.plugGate.getStack(varAnd);

                    GateVariant varOr = new GateVariant(EnumGateLogic.OR, material, modifier);
                    ItemStack resultOr = BCSiliconItems.plugGate.getStack(varOr);

                    String regNamePrefix = resultOr.getItem().getRegistryName() + "_" + modifier + "_" + material;
                    ForgeRegistries.RECIPES.register(new ShapedOreRecipe(resultOr.getItem().getRegistryName(),
                        resultAnd, "i", 'i', new IngredientNBTBC(resultOr)).setRegistryName(regNamePrefix + "_or"));
                    ForgeRegistries.RECIPES.register(new ShapedOreRecipe(resultAnd.getItem().getRegistryName(),
                        resultOr, "i", 'i', new IngredientNBTBC(resultAnd)).setRegistryName(regNamePrefix + "_and"));
                }
            }
        }
*/
            if (BCSiliconItems.plugPulsar != null) {
                ItemStack output = new ItemStack(BCSiliconItems.plugPulsar);

                ItemStack redstoneEngine;
                if (BCCoreBlocks.engine != null) {
                    redstoneEngine = BCCoreBlocks.engine.getStack(EnumEngineType.WOOD);
                } else {
                    redstoneEngine = new ItemStack(Blocks.REDSTONE_BLOCK);
                }

                Set<IngredientStack> input = new HashSet<>();
                input.add(new IngredientStack(Ingredient.fromStacks(redstoneEngine)));
                input.add(new IngredientStack(CraftingHelper.getIngredient("ingotIron"), 2));
                AssemblyRecipe recipe = new AssemblyRecipeBasic("plug_pulsar", 1000 * MjAPI.MJ, input, output);
                AssemblyRecipeRegistry.register(recipe);
            }
            if (BCSiliconItems.plugGate != null) {
                ItemStack output = new ItemStack(BCSiliconItems.plugGate);
                ImmutableSet<IngredientStack> input = ImmutableSet.of(IngredientStack.of(Ingredient.fromStacks(new ItemStack(BCSiliconItems.redstoneChipset))));
                AssemblyRecipeRegistry.register(new AssemblyRecipeBasic("gate_basic", 1000 * MjAPI.MJ, input, output));

                input = ImmutableSet.of(IngredientStack.of(new ItemStack(BCSiliconItems.redstoneChipset, 1, 1)),
                        IngredientStack.of(new ItemStack(BCTransportItems.wire, 1, 14)));
                output = BCSiliconItems.plugGate.getStack(new GateVariant(EnumGateLogic.OR, EnumGateMaterial.IRON));
                AssemblyRecipeRegistry.register(new AssemblyRecipeBasic("gate_iron_or", 2000 * MjAPI.MJ, input, output));

                output = BCSiliconItems.plugGate.getStack(new GateVariant(EnumGateLogic.AND, EnumGateMaterial.IRON));
                AssemblyRecipeRegistry.register(new AssemblyRecipeBasic("gate_iron_and", 2000 * MjAPI.MJ, input, output));



                input = ImmutableSet.of(IngredientStack.of(new ItemStack(BCSiliconItems.redstoneChipset, 1, 2)),
                        IngredientStack.of(new ItemStack(BCTransportItems.wire, 1, 14)),
                        IngredientStack.of(new ItemStack(BCTransportItems.wire, 1, 13)));
                output = BCSiliconItems.plugGate.getStack(new GateVariant(EnumGateLogic.OR, EnumGateMaterial.GOLD));
                AssemblyRecipeRegistry.register(new AssemblyRecipeBasic("gate_gold_or", 4000 * MjAPI.MJ, input, output));

                output = BCSiliconItems.plugGate.getStack(new GateVariant(EnumGateLogic.AND, EnumGateMaterial.GOLD));
                AssemblyRecipeRegistry.register(new AssemblyRecipeBasic("gate_gold_and", 4000 * MjAPI.MJ, input, output));



                input = ImmutableSet.of(IngredientStack.of(new ItemStack(BCSiliconItems.redstoneChipset, 1, 3)),
                        IngredientStack.of(new ItemStack(BCTransportItems.wire, 1, 14)),
                        IngredientStack.of(new ItemStack(BCTransportItems.wire, 1, 13)),
                        IngredientStack.of(new ItemStack(BCTransportItems.wire, 1, 11)));
                output = BCSiliconItems.plugGate.getStack(new GateVariant(EnumGateLogic.OR, EnumGateMaterial.QUARTZ));
                AssemblyRecipeRegistry.register(new AssemblyRecipeBasic("gate_quartz_or", 8000 * MjAPI.MJ, input, output));

                output = BCSiliconItems.plugGate.getStack(new GateVariant(EnumGateLogic.AND, EnumGateMaterial.QUARTZ));
                AssemblyRecipeRegistry.register(new AssemblyRecipeBasic("gate_quartz_and", 8000 * MjAPI.MJ, input, output));



                input = ImmutableSet.of(IngredientStack.of(new ItemStack(BCSiliconItems.redstoneChipset, 1, 4)),
                        IngredientStack.of(new ItemStack(BCTransportItems.wire, 1, 14)),
                        IngredientStack.of(new ItemStack(BCTransportItems.wire, 1, 13)),
                        IngredientStack.of(new ItemStack(BCTransportItems.wire, 1, 11)),
                        IngredientStack.of(new ItemStack(BCTransportItems.wire, 1, 0)));
                output = BCSiliconItems.plugGate.getStack(new GateVariant(EnumGateLogic.OR, EnumGateMaterial.DIAMOND));
                AssemblyRecipeRegistry.register(new AssemblyRecipeBasic("gate_diamond_or", 16000 * MjAPI.MJ, input, output));

                output = BCSiliconItems.plugGate.getStack(new GateVariant(EnumGateLogic.AND, EnumGateMaterial.DIAMOND));
                AssemblyRecipeRegistry.register(new AssemblyRecipeBasic("gate_diamond_and", 16000 * MjAPI.MJ, input, output));



                input = ImmutableSet.of(IngredientStack.of(new ItemStack(BCSiliconItems.redstoneChipset, 1, 5)),
                        IngredientStack.of(new ItemStack(BCTransportItems.wire, 1, 14)),
                        IngredientStack.of(new ItemStack(BCTransportItems.wire, 1, 13)),
                        IngredientStack.of(new ItemStack(BCTransportItems.wire, 1, 11)),
                        IngredientStack.of(new ItemStack(BCTransportItems.wire, 1, 0)),
                        IngredientStack.of(new ItemStack(BCTransportItems.wire, 1, 4)));
                output = BCSiliconItems.plugGate.getStack(new GateVariant(EnumGateLogic.OR, EnumGateMaterial.EMERALD));
                AssemblyRecipeRegistry.register(new AssemblyRecipeBasic("gate_emerald_or", 32000 * MjAPI.MJ, input, output));

                output = BCSiliconItems.plugGate.getStack(new GateVariant(EnumGateLogic.AND, EnumGateMaterial.EMERALD));
                AssemblyRecipeRegistry.register(new AssemblyRecipeBasic("gate_emerald_and", 32000 * MjAPI.MJ, input, output));
            }

            if (BCSiliconItems.plugLightSensor != null) {
                AssemblyRecipeRegistry.register(new AssemblyRecipeBasic("light-sensor", 500 * MjAPI.MJ,
                        ImmutableSet.of(IngredientStack.of(Blocks.DAYLIGHT_DETECTOR)),
                        new ItemStack(BCSiliconItems.plugLightSensor)));
            }

            if (BCSiliconItems.plugFacade != null) {
                AssemblyRecipeRegistry.register(FacadeAssemblyRecipes.INSTANCE);
                ForgeRegistries.RECIPES.register(FacadeSwapRecipe.INSTANCE);
            }

            if (BCSiliconItems.plugLens != null) {
                for (EnumDyeColor colour : ColourUtil.COLOURS) {
                    String name = String.format("lens-regular-%s", colour.getUnlocalizedName());
                    IngredientStack stainedGlass = IngredientStack.of("blockGlass" + ColourUtil.getName(colour));
                    ImmutableSet<IngredientStack> input = ImmutableSet.of(stainedGlass);
                    ItemStack output = BCSiliconItems.plugLens.getStack(colour, false);
                    AssemblyRecipeRegistry.register(new AssemblyRecipeBasic(name, 500 * MjAPI.MJ, input, output));

                    name = String.format("lens-filter-%s", colour.getUnlocalizedName());
                    output = BCSiliconItems.plugLens.getStack(colour, true);
                    input = ImmutableSet.of(stainedGlass, IngredientStack.of(new ItemStack(Blocks.IRON_BARS)));
                    AssemblyRecipeRegistry.register(new AssemblyRecipeBasic(name, 500 * MjAPI.MJ, input, output));
                }

                IngredientStack glass = IngredientStack.of("blockGlass");
                ImmutableSet<IngredientStack> input = ImmutableSet.of(glass);
                ItemStack output = BCSiliconItems.plugLens.getStack(null, false);
                AssemblyRecipeRegistry.register(new AssemblyRecipeBasic("lens-regular", 500 * MjAPI.MJ, input, output));

                output = BCSiliconItems.plugLens.getStack(null, true);
                input = ImmutableSet.of(glass, IngredientStack.of(new ItemStack(Blocks.IRON_BARS)));
                AssemblyRecipeRegistry.register(new AssemblyRecipeBasic("lens-filter", 500 * MjAPI.MJ, input, output));
            }

            if (BCSiliconItems.plugFader != null) {
                AssemblyRecipeRegistry.register(new AssemblyRecipeBasic("fader", 1000 * MjAPI.MJ,
                        ImmutableSet.of(IngredientStack.of(Items.COMPARATOR), new IngredientStack(CraftingHelper.getIngredient(Items.IRON_INGOT), 2)),
                        new ItemStack(BCSiliconItems.plugFader)));
            }

            if (BCSiliconItems.plugTimer != null) {
                AssemblyRecipeRegistry.register(new AssemblyRecipeBasic("timer", 1000 * MjAPI.MJ,
                        ImmutableSet.of(IngredientStack.of(Items.REPEATER), new IngredientStack(CraftingHelper.getIngredient(Items.IRON_INGOT), 2)),
                        new ItemStack(BCSiliconItems.plugTimer)));
            }


            if (BCSiliconItems.gateCopier != null) {
                ImmutableSet.Builder<IngredientStack> input = ImmutableSet.builder();
                if (BCCoreItems.wrench != null) {
                    input.add(IngredientStack.of(BCCoreItems.wrench));
                } else {
                    input.add(IngredientStack.of(Items.STICK));
                    input.add(IngredientStack.of(Items.IRON_INGOT));
                }

                if (BCSiliconItems.redstoneChipset != null) {
                    input.add(IngredientStack.of(EnumRedstoneChipset.IRON.getStack(1)));
                } else {
                    input.add(IngredientStack.of("dustRedstone"));
                    input.add(IngredientStack.of("dustRedstone"));
                    input.add(IngredientStack.of("ingotGold"));
                }

                AssemblyRecipeRegistry.register(
                        new AssemblyRecipeBasic(
                                "gate_copier", 500 * MjAPI.MJ, input.build(), new ItemStack(BCSiliconItems.gateCopier)
                        )
                );
            }
            if (BCSiliconItems.redstoneChipset != null) {
                ImmutableSet<IngredientStack> input = ImmutableSet.of(IngredientStack.of("dustRedstone"));
                ItemStack output = EnumRedstoneChipset.RED.getStack(1);
                AssemblyRecipeRegistry
                        .register(new AssemblyRecipeBasic("redstone_chipset", 10000 * MjAPI.MJ, input, output));

                input = ImmutableSet.of(IngredientStack.of("dustRedstone"), IngredientStack.of("ingotIron"));
                output = EnumRedstoneChipset.IRON.getStack(1);
                AssemblyRecipeRegistry.register(new AssemblyRecipeBasic("iron_chipset", 20000 * MjAPI.MJ, input, output));

                input = ImmutableSet.of(IngredientStack.of("dustRedstone"), IngredientStack.of("ingotGold"));
                output = EnumRedstoneChipset.GOLD.getStack(1);
                AssemblyRecipeRegistry.register(new AssemblyRecipeBasic("gold_chipset", 40000 * MjAPI.MJ, input, output));

                input = ImmutableSet.of(IngredientStack.of("dustRedstone"), IngredientStack.of("gemQuartz"));
                output = EnumRedstoneChipset.QUARTZ.getStack(1);
                AssemblyRecipeRegistry.register(new AssemblyRecipeBasic("quartz_chipset", 60000 * MjAPI.MJ, input, output));

                input = ImmutableSet.of(IngredientStack.of("dustRedstone"), IngredientStack.of("gemDiamond"));
                output = EnumRedstoneChipset.DIAMOND.getStack(1);
                AssemblyRecipeRegistry
                        .register(new AssemblyRecipeBasic("diamond_chipset", 80000 * MjAPI.MJ, input, output));

                input = ImmutableSet.of(IngredientStack.of("dustRedstone"), IngredientStack.of("gemEmerald"));
                output = EnumRedstoneChipset.EMERALD.getStack(1);
                AssemblyRecipeRegistry
                        .register(new AssemblyRecipeBasic("emerald_chipset", 100000 * MjAPI.MJ, input, output));
            }

            scanForJsonRecipes();
        }
    }

    private static void scanForJsonRecipes() {
        final boolean[] failed = { false };
        for (ModContainer mod : Loader.instance().getActiveModList()) {
            JsonContext ctx = new JsonContext(mod.getModId());
            CraftingHelper.findFiles(mod, "assets/" + mod.getModId() + "/assembly_recipes_pre_mj", null, (root, file) -> {
                try {
                    readAndAddJsonRecipe(ctx, root, file);
                    return true;
                } catch (IOException io) {
                    BCLog.logger.error("Couldn't read recipe " + root.relativize(file) + " from " + file, io);
                    failed[0] = true;
                    return true;
                }
            }, false, false);
        }

        Path configRoot = BCCoreConfig.configFolder.toPath().resolve("assembly_recipes_pre_mj");
        if (!Files.isDirectory(configRoot)) {
            try {
                Files.createDirectory(configRoot);
            } catch (IOException e) {
                BCLog.logger.warn("[silicon.assembly] Unable to create the folder " + configRoot);
                failed[0] = true;
                return;
            }
        }

        try {
            JsonContext ctx = new JsonContext("_config");
            Files.walkFileTree(configRoot, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    try {
                        readAndAddJsonRecipe(ctx, configRoot, file);
                    } catch (JsonParseException e) {
                        e.printStackTrace();
                        failed[0] = true;
                    } catch (IOException io) {
                        io.printStackTrace();
                        failed[0] = true;
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            BCLog.logger.warn("[silicon.assembly] Failed to walk the config folder " + configRoot, e);
            failed[0] = false;
        }

        if (failed[0]) {
            throw new IllegalStateException("Failed to read some assembly recipe files! Check the log for details");
        }
    }

    private static void readAndAddJsonRecipe(JsonContext ctx, Path root, Path file)
        throws JsonParseException, IOException {
        if (!file.toString().endsWith(".json")) {
            return;
        }

        String name = root.relativize(file).toString().replace("\\", "/");
        ResourceLocation key = new ResourceLocation(ctx.getModId(), name);
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            JsonObject json = JsonUtils.fromJson(GSON, reader, JsonObject.class);
            if (json == null || json.isJsonNull()) throw new JsonSyntaxException("Json is null (empty file?)");

            ItemStack output = CraftingHelper.getItemStack(json.getAsJsonObject("result"), ctx);
            long powercost = json.get("MJ").getAsLong() * MjAPI.MJ;

            ArrayList<IngredientStack> ingredients = new ArrayList<>();

            json.getAsJsonArray("components").forEach(element -> {
                JsonObject object = element.getAsJsonObject();
                ingredients.add(new IngredientStack(CraftingHelper.getIngredient(object.get("ingredient"), ctx),
                    JsonUtils.getInt(object, "amount", 1)));
            });

            AssemblyRecipeRegistry.REGISTRY.put(key,
                new AssemblyRecipeBasic(key, powercost, ImmutableSet.copyOf(ingredients), output));
        }
    }
}
