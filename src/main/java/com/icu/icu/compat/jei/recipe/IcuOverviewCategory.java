package com.icu.icu.compat.jei.recipe;

import com.icu.icu.IcuMod;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * The ICU category in JEI: a single page that shows the bleeding loop.
 *
 * <p>Layout, left to right:</p>
 * <pre>
 *   [what hurt you]  ->  [bandage]  ->  [survived]
 * </pre>
 *
 * <p>The category exists because the mechanics are not a crafting recipe: there
 * is no way to express "you are bleeding, hold a bandage for three seconds" as a
 * vanilla recipe, so it gets its own page.</p>
 *
 * <p>The plain crafting recipe needs no code here. JEI reads every vanilla recipe
 * from the recipe manager, so the bandage recipe already shows up in the crafting
 * category with its own "uses" view.</p>
 */
public class IcuOverviewCategory extends AbstractRecipeCategory<IcuInfoRecipe> {

    /** JEI id for the ICU page. */
    public static final RecipeType<IcuInfoRecipe> RECIPE_TYPE =
            RecipeType.create(IcuMod.MODID, "overview", IcuInfoRecipe.class);

    private static final int WIDTH = 128;
    private static final int HEIGHT = 46;

    public IcuOverviewCategory(IGuiHelper guiHelper) {
        super(
                RECIPE_TYPE,
                Component.translatable("jei.icu.category.overview"),
                guiHelper.createDrawableItemStack(new ItemStack(com.icu.icu.IcuItems.BANDAGE.get())),
                WIDTH,
                HEIGHT);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, IcuInfoRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 6, 6)
                .addItemStack(recipe.input());
        builder.addSlot(RecipeIngredientRole.CATALYST, 48, 6)
                .addItemStack(recipe.output());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 100, 6)
                .addItemStack(new ItemStack(Items.GOLDEN_APPLE));
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, IcuInfoRecipe recipe, IFocusGroup focuses) {
        builder.addText(Component.translatable(recipe.noteKey()), 6, 30)
                .setLineSpacing(2);
    }
}
