package com.nofo.nofo;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Persistent per-entity data used by the ICU mod.
 *
 * <p>Currently one attachment: the number of stacked massive-bleeding layers a
 * player carries. It is serialized so the state survives a world reload, and it
 * deliberately does <b>not</b> use {@code copyOnDeath()}: the effect must end
 * when the player dies, and a fresh respawn starts with zero layers.</p>
 */
public final class ModAttachments {
    private ModAttachments() {}

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, NofoMod.MODID);

    /** Number of stacked haemorrhage layers. 0 = not bleeding. */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<BleedingData>> BLEEDING =
            ATTACHMENT_TYPES.register("bleeding",
                    () -> AttachmentType.builder(() -> new BleedingData())
                            .serialize(BleedingData.CODEC)
                            .build());

    public static void register(IEventBus modEventBus) {
        ATTACHMENT_TYPES.register(modEventBus);
    }

    /** Mutable holder for the bleeding layer counter. */
    public static final class BleedingData {
        public static final Codec<BleedingData> CODEC =
                Codec.INT.xmap(BleedingData::new, BleedingData::getLayers);

        private int layers;

        public BleedingData() {
            this(0);
        }

        public BleedingData(int layers) {
            this.layers = Math.max(0, layers);
        }

        public int getLayers() {
            return layers;
        }

        /** Adds one haemorrhage layer; layers are intentionally uncapped. */
        public void addLayer() {
            this.layers++;
        }

        public void clear() {
            this.layers = 0;
        }

        public boolean isBleeding() {
            return this.layers > 0;
        }
    }
}
