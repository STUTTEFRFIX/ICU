package com.icu.icu.compat.jei.recipe;

import net.minecraft.world.item.ItemStack;

/**
 * The data behind one page in the ICU category.
 *
 * <p>Deliberately tiny: JEI categories are driven by the category class, so this
 * only carries the stacks the page draws.</p>
 *
 * @param input  the stack shown on the left (what the player does or uses)
 * @param output the stack shown on the right (what results)
 * @param noteKey translation key of the line drawn under the slots
 */
public record IcuInfoRecipe(ItemStack input, ItemStack output, String noteKey) {

    /** The single page shown in the ICU category. */
    public static IcuInfoRecipe overview(ItemStack input, ItemStack output) {
        return new IcuInfoRecipe(input, output, "jei.icu.overview.note");
    }
}
