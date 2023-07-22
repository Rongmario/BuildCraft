package buildcraft.core.compat.module.jei.silicon;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import buildcraft.api.BCModules;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;

public class CategoryIntegrationTable implements IRecipeCategory<WrapperIntegrationTable> {
    public static final String UID = "buildcraft-compat:silicon.integration";

    protected final ResourceLocation backgroundLocation;
    private final IDrawable background;
    private WrapperIntegrationTable wrapper = null;

    public CategoryIntegrationTable(IGuiHelper guiHelper) {
        backgroundLocation = new ResourceLocation("buildcraftsilicon", "textures/gui/integration_table.png");
        background = guiHelper.createDrawable(backgroundLocation, 17, 20, 153, 73, 0, 0, 9, 0);
    }

    @Override
    public String getUid() {
        return UID;
    }

    @Override
    public String getTitle() {
        return "Integration Table";
    }

    @Override
    public String getModName() {
        return BCModules.SILICON.name();
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, WrapperIntegrationTable recipeWrapper, IIngredients ingredients) {
        IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();

        List<List<ItemStack>> inputs = ingredients.getInputs(ItemStack.class);
        int inventoryIndex = 0;
        for (int y = 0; y < 3; ++y) {
            for (int x = 0; x < 3; ++x) {
                int slotIndex = ((x == 1) && (y == 1)) ? 0 : (x + y * 3 + 1);
                if (inputs.size() > slotIndex) {
                    guiItemStacks.init(inventoryIndex, true, 10 + x * 25, 3 + y * 25);
                    guiItemStacks.set(inventoryIndex, inputs.get(slotIndex));
                    inventoryIndex++;
                }
            }
        }

        guiItemStacks.init(inventoryIndex, false, 129, 28);
        guiItemStacks.set(inventoryIndex, recipeWrapper.output);
    }
}
