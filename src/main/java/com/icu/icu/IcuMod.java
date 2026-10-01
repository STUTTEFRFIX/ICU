package com.icu.icu;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

/**
 * ICU - realistic body trauma and medical system for Minecraft.
 *
 * <h2>What this mod is</h2>
 * <p>ICU rebuilds wounding and medical treatment in Minecraft. Gameplay is added
 * as self-contained modules under {@code com.icu.icu.gameplay}, one package per
 * feature. This build ships exactly one module: {@code gameplay.bleeding}
 * (massive bleeding / haemorrhage).</p>
 *
 * <h2>Where to start reading</h2>
 * <ol>
 *   <li>{@link com.icu.icu.gameplay.bleeding.BleedingFeature} - the gameplay rules</li>
 *   <li>{@link com.icu.icu.gameplay.bleeding.BleedingData} - the per-player state</li>
 *   <li>{@link com.icu.icu.IcuAttachments} - how that state is stored</li>
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
        LOGGER.info("[ICU] loaded - bleeding module active");
    }
}
