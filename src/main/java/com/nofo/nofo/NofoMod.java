package com.nofo.nofo;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

/**
 * ICU - realistic body trauma and medical system.
 *
 * <p>This build implements exactly one feature: massive bleeding (haemorrhage)
 * caused by sword/axe strikes whose final (post-mitigation) damage exceeds
 * {@link com.nofo.nofo.bleed.BleedHandler#BLEED_TRIGGER_DAMAGE}.</p>
 */
@Mod(NofoMod.MODID)
public class NofoMod {
    public static final String MODID = "nofo";
    public static final Logger LOGGER = LogUtils.getLogger();

    public NofoMod(IEventBus modEventBus) {
        ModAttachments.register(modEventBus);
        LOGGER.info("[ICU] loaded - haemorrhage feature active");
    }
}
