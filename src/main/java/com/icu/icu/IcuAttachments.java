package com.icu.icu;

import com.icu.icu.gameplay.bleeding.BleedingData;
import com.icu.icu.gameplay.bleeding.BleedingRecoveryData;
import com.icu.icu.gameplay.blood.BloodVolumeData;
import com.icu.icu.gameplay.pain.PainData;
import com.icu.icu.gameplay.sprain.SprainData;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Every piece of persistent per-player data the ICU mod stores.
 *
 * <p>Each gameplay module contributes its own attachments here, so this is the
 * single place to look when asking "what state does ICU keep on a player?".</p>
 *
 * <p>None of them use {@code copyOnDeath()}: death wipes the whole injury state,
 * so a respawn always starts clean.</p>
 *
 * <table>
 *   <tr><th>Attachment</th><th>Module</th><th>Meaning</th></tr>
 *   <tr><td>{@link #BLEEDING}</td><td>bleeding</td><td>stacked haemorrhage layers</td></tr>
 *   <tr><td>{@link #BLOOD_VOLUME}</td><td>bleeding</td><td>hidden blood volume, 0-100</td></tr>
 *   <tr><td>{@link #BLEEDING_RECOVERY}</td><td>bleeding</td><td>post-treatment recovery window</td></tr>
 *   <tr><td>{@link #PAIN}</td><td>pain</td><td>shared pain meter, 0-100</td></tr>
 *   <tr><td>{@link #SPRAIN}</td><td>sprain</td><td>sprained ankle state</td></tr>
 * </table>
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

    /** Hidden blood volume. Drains while bleeding, regenerates very slowly. */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<BloodVolumeData>> BLOOD_VOLUME =
            ATTACHMENT_TYPES.register("blood_volume",
                    () -> AttachmentType.builder(() -> new BloodVolumeData())
                            .serialize(BloodVolumeData.CODEC)
                            .build());

    /** Recovery window that starts when a bleed is successfully treated. */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<BleedingRecoveryData>> BLEEDING_RECOVERY =
            ATTACHMENT_TYPES.register("bleeding_recovery",
                    () -> AttachmentType.builder(() -> new BleedingRecoveryData())
                            .serialize(BleedingRecoveryData.CODEC)
                            .build());

    /** Pain meter shared by bleeding and sprains. */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PainData>> PAIN =
            ATTACHMENT_TYPES.register("pain",
                    () -> AttachmentType.builder(() -> new PainData())
                            .serialize(PainData.CODEC)
                            .build());

    /** Sprained ankle state. */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<SprainData>> SPRAIN =
            ATTACHMENT_TYPES.register("sprain",
                    () -> AttachmentType.builder(() -> new SprainData())
                            .serialize(SprainData.CODEC)
                            .build());

    /** Called once from {@link IcuMod}; not meant to be called anywhere else. */
    public static void register(IEventBus modEventBus) {
        ATTACHMENT_TYPES.register(modEventBus);
    }
}
