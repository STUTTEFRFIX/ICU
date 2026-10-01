package com.icu.icu;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.minecraft.world.item.CreativeModeTabs;
import org.slf4j.Logger;

/**
 * ICU - realistic body trauma and medical system for Minecraft.
 *
 * <h2>What this mod is</h2>
 * <p>ICU rebuilds wounding and medical treatment in Minecraft. Gameplay is added
 * as self-contained modules under {@code com.icu.icu.gameplay}, one package per
 * feature.</p>
 *
 * <h2>Modules in this build</h2>
 * <ul>
 *   <li>{@link com.icu.icu.gameplay.bleeding} - massive bleeding, blood volume,
 *       bandage treatment and the recovery window</li>
 *   <li>{@link com.icu.icu.gameplay.pain} - the shared pain meter</li>
 *   <li>{@link com.icu.icu.gameplay.sprain} - sprained ankles from falls</li>
 * </ul>
 *
 * <h2>Where to start reading</h2>
 * <ol>
 *   <li>{@link com.icu.icu.gameplay.bleeding.BleedingFeature} - the bleeding rules</li>
 *   <li>{@link IcuAttachments} - all persistent player state</li>
 *   <li>{@link IcuItems} - items this mod adds</li>
 * </ol>
 *
 * <p>See {@code README.md} and {@code docs/} in the project root for the full map.</p>
 */
@Mod(IcuMod.MODID)
public class IcuMod {
    /** Mod id; matches {@code mod_id} in gradle.properties and the resource namespace. */
    public static final String MODID = "icu";

    public static final Logger LOGGER = LogUtils.getLogger();

    public IcuMod(IEventBus modEventBus) {
        // Registration only. Gameplay logic lives in the gameplay packages.
        IcuAttachments.register(modEventBus);
        IcuItems.register(modEventBus);
        modEventBus.addListener(this::addCreativeTabItems);
        LOGGER.info("[ICU] loaded - bleeding, pain and sprain modules active");
    }

    /** Puts the bandage in the vanilla Combat tab so it is easy to find. */
    private void addCreativeTabItems(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(IcuItems.BANDAGE);
        }
    }
}
