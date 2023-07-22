package buildcraft.core.compat.module.jei.silicon;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import buildcraft.api.recipes.IngredientStack;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import mezz.jei.api.ingredients.VanillaTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.recipes.IntegrationRecipe;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawableAnimated;
import mezz.jei.api.gui.IDrawableStatic;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;

public class WrapperIntegrationTable implements IRecipeWrapper {
    public final IntegrationRecipe recipe;
    public final List<List<ItemStack>> inputs;
    public final ItemStack output;
    public final IDrawableAnimated progressBar;


    public WrapperIntegrationTable(IGuiHelper guiHelper, IntegrationRecipe recipe) {
        this.recipe = recipe;

        List<List<ItemStack>> _inputs = Lists.newArrayList();

        List<ItemStack> inner1 = new ArrayList<>();
        for (ItemStack matching : recipe.getCenterStack().ingredient.getMatchingStacks()) {
            matching = matching.copy();
            matching.setCount(recipe.getCenterStack().count);
            inner1.add(matching);
        }
        _inputs.add(inner1);


        for (IngredientStack in : recipe.getSurrounding()) {
            List<ItemStack> inner = new ArrayList<>();
            for (ItemStack matching : in.ingredient.getMatchingStacks()) {
                matching = matching.copy();
                matching.setCount(in.count);
                inner.add(matching);
            }
            _inputs.add(inner);
        }

        this.inputs = ImmutableList.copyOf(_inputs);
        this.output = recipe.getOutput();

        ResourceLocation backgroundLocation = new ResourceLocation("buildcraftsilicon", "textures/gui/integration_table.png");
        IDrawableStatic progressDrawable = guiHelper.createDrawable(backgroundLocation, 176, 3, 4, 70, 0, 0, 0, 0);
        this.progressBar = guiHelper.createAnimatedDrawable(progressDrawable, (int) Math.max(10, recipe.getRequiredMicroJoules() / MjAPI.MJ / 50), IDrawableAnimated.StartDirection.BOTTOM, false);
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.ITEM, inputs);
        ingredients.setOutput(VanillaTypes.ITEM, output);

    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        this.progressBar.draw(minecraft, 156, 5);
        minecraft.fontRenderer.drawString(MjAPI.formatRFFromMj(this.recipe.getRequiredMicroJoules()) + " RF", 81, 5, Color.gray.getRGB());
    }

    @Override
    public List<String> getTooltipStrings(int mouseX, int mouseY) {
        return Lists.newArrayList();
    }

    @Override
    public boolean handleClick(Minecraft minecraft, int mouseX, int mouseY, int mouseButton) {
        return false;
    }
}
