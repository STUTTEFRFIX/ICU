package com.icu.icu;

import com.icu.icu.gameplay.bleeding.BleedingData;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Every piece of persistent per-entity data the ICU mod stores.
 *
 * <p>Each gameplay module contributes one attachment here. Attachments are
 * serialized so state survives a world reload, and they deliberately do
 * <b>not</b> use {@code copyOnDeath()}: a wound must end when the player dies.</p>
 *
 * <p>Current attachments:</p>
 * <ul>
 *   <li>{@link #BLEEDING} - stacked haemorrhage layers, see
 *       {@link com.icu.icu.gameplay.bleeding}.</li>
 * </ul>
 */
public final class IcuAttachments {
    private IcuAttachments() {}

    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, IcuMod.MODID);

    /** Stacked massive-bleeding layers. Zero means the player is not bleeding. */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<BleedingData>> BLEEDING =
            ATTACHMENT_TYPES.register("bleeding",
                    () -> AttachmentType.builder(() -> new BleedingData())
                            .serialize(BleedingData.CODEC)
                            .build());

    /** Called once from {@link IcuMod}; not meant to be called anywhere else. */
    public static void register(IEventBus modEventBus) {
        ATTACHMENT_TYPES.register(modEventBus);
    }
}
