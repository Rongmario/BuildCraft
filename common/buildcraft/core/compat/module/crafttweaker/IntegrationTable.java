package buildcraft.core.compat.module.crafttweaker;


import buildcraft.api.mj.MjAPI;
import buildcraft.api.recipes.IngredientStack;
import buildcraft.lib.recipe.IntegrationRecipeBasic;
import buildcraft.lib.recipe.IntegrationRecipeRegistry;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import crafttweaker.annotations.ModOnly;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.ArrayList;
import java.util.List;

@ZenClass("mods.buildcraft.IntegrationTable")
@ModOnly("buildcraftsilicon")
public class IntegrationTable {

    private static int ids;

    @ZenMethod
    public static void addRecipe(IItemStack output, int power, IIngredient target, IIngredient[] ingredients) {
        addRecipe0("auto_" + ids++, output, power, target, ingredients);
    }

    @ZenMethod
    public static void addRecipe(String name, IItemStack output, int power, IIngredient target, IIngredient[] ingredients) {
        addRecipe0("custom/" + name, output, power, target, ingredients);
    }

    private static void addRecipe0(String name, IItemStack output, int power, IIngredient target, IIngredient[] ingredients) {
        CraftTweakerAPI.apply(new AddRecipeAction(name, output, power, target, ingredients));
    }

    // ######################
    // ### Action classes ###
    // ######################

    private static class AddRecipeAction implements IAction {

        private final ItemStack output;
        private final ResourceLocation name;
        private final long requiredMj;
        private final List<IngredientStack> requiredStacks = new ArrayList<>();
        private final IngredientStack target;

        public AddRecipeAction(String name, IItemStack output, int power, IIngredient target, IIngredient[] ingredients) {
            this.output = CraftTweakerMC.getItemStack(output);
            this.target = new IngredientStack(CraftTweakerMC.getIngredient(target), Math.max(1, target.getAmount()));

            for (IIngredient ctIng : ingredients) {
                Ingredient ingredient = CraftTweakerMC.getIngredient(ctIng);
                requiredStacks.add(new IngredientStack(ingredient, Math.max(1, ctIng.getAmount())));
            }

            this.requiredMj = (power * MjAPI.MJ) / MjAPI.rfPerMj;
            this.name = new ResourceLocation("crafttweaker", name);
        }

        @Override
        public void apply() {
            IntegrationRecipeRegistry.INSTANCE.recipes.put(name,
                    new IntegrationRecipeBasic(name, requiredMj, target, requiredStacks, output));
        }

        @Override
        public String describe() {
            return "Adding assembly table recipe for " + output;
        }
    }

}