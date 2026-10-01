package com.icu.icu;

import com.icu.icu.item.BandageItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Every item the ICU mod adds.
 *
 * <table>
 *   <tr><th>Item</th><th>Purpose</th></tr>
 *   <tr><td>{@link #BANDAGE}</td><td>stops a haemorrhage, must be held for
 *       {@link BandageItem#USE_TICKS} ticks</td></tr>
 * </table>
 */
public final class IcuItems {
    private IcuItems() {}

    private static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(IcuMod.MODID);

    /** The bandage. Holding it for 3 seconds stops an active bleed. */
    public static final DeferredItem<Item> BANDAGE =
            ITEMS.register("bandage", () -> new BandageItem(new Item.Properties().stacksTo(16)));

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
