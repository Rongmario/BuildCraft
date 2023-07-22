package buildcraft.core.compat.module.jei.factory;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import buildcraft.api.BCModules;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.BlankRecipeCategory;

public class CategoryDistiller extends BlankRecipeCategory<WrapperDistiller> {
    public static final String UID = "buildcraft:category_distiller";
    public static final ResourceLocation distillerBackground = new ResourceLocation("buildcraftfactory:textures/gui/distiller.png");

    private final IDrawable background, slot, fakeBackground;

    public CategoryDistiller(IGuiHelper helper) {
        this.fakeBackground = helper.createBlankDrawable(76, 65);
        this.background = helper.createDrawable(distillerBackground, 61, 12, 36, 57);
        this.slot = helper.createDrawable(distillerBackground, 7, 34, 18, 18);
    }

    @Override
    public String getUid() {
        return UID;
    }

    @Override
    public String getTitle() {
        return "Refinery";
    }

    @Override
    public String getModName() {
        return BCModules.FACTORY.name();
    }

    @Override
    public IDrawable getBackground() {
        return fakeBackground;
    }

    @Override
    public void drawExtras(Minecraft minecraft) {
        this.background.draw(minecraft, 18, 4);
        this.slot.draw(minecraft, 0, 25); // -20, 21);
        this.slot.draw(minecraft, 54, 25); // 36, -4);
    }

//    @Override
//    public void drawAnimations(Minecraft minecraft) {}

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, WrapperDistiller recipeWrapper, IIngredients ingredients) {
        IGuiFluidStackGroup guiFluidStacks = recipeLayout.getFluidStacks();

        guiFluidStacks.init(0, true, /*-19, 22*/ 1, 26, 16, 16, 10, false, null);
        guiFluidStacks.set(0, ingredients.getInputs(FluidStack.class).get(0));

        guiFluidStacks.init(1, false, /*37, -3*/ 55, 26, 16, 16, 10, false, null);
        guiFluidStacks.set(1, ingredients.getOutputs(FluidStack.class).get(0));
    }
}
