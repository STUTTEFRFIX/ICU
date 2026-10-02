package com.icu.icu.compat.jei;

import com.icu.icu.IcuMod;
import com.icu.icu.compat.jei.recipe.IcuInfoRecipe;
import com.icu.icu.compat.jei.recipe.IcuOverviewCategory;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

/**
 * JEI integration for ICU.
 *
 * <p>This class only exists when JEI is present: it is annotated with
 * {@link JeiPlugin} and the compile classpath is the only place JEI appears, so
 * ICU keeps working without JEI installed.</p>
 *
 * <h2>What it adds</h2>
 * <ol>
 *   <li><b>Information pages</b> - hovering the bandage in JEI shows what it
 *       does, how long it takes and what it cannot fix.</li>
 *   <li><b>An ICU category</b> with a single overview page that lays out the
 *       bleeding loop and the bandage recipe.</li>
 * </ol>
 *
 * <p>The plain crafting recipe needs no code here: JEI reads every vanilla
 * recipe from the recipe manager, so the bandage recipe already appears in the
 * crafting category.</p>
 */
@JeiPlugin
public class IcuJeiPlugin implements IModPlugin {
    /** Matches the mod id, which is also the resource namespace. */
    private static final ResourceLocation UID =
            ResourceLocation.fromNamespaceAndPath(IcuMod.MODID, "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
                new IcuOverviewCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        // 1) Information pages: shown when the player hovers the ingredient.
        registration.addItemStackInfo(
                new ItemStack(com.icu.icu.IcuItems.BANDAGE.get()),
                Component.translatable("jei.icu.bandage.info.1"),
                Component.translatable("jei.icu.bandage.info.2"),
                Component.translatable("jei.icu.bandage.info.3"));

        // 2) A single overview page in the ICU category.
        registration.addRecipes(IcuOverviewCategory.RECIPE_TYPE,
                List.of(IcuInfoRecipe.overview(
                        new ItemStack(Items.PAPER),
                        new ItemStack(com.icu.icu.IcuItems.BANDAGE.get()))));
    }
}
