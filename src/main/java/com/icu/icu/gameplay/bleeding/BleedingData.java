package com.icu.icu.gameplay.bleeding;

import com.mojang.serialization.Codec;

/**
 * Per-player state of the bleeding module: how many haemorrhage layers the
 * player currently carries.
 *
 * <p>Layers are intentionally uncapped. They are recorded for diagnostics and
 * for the recovery rules; the bleeding module deals no health damage, so the
 * layer count does not itself decide how fast the player dies. Blood volume
 * running out is what kills, at a fixed
 * {@link com.icu.icu.gameplay.blood.BloodVolumeData#LOSS_PER_SECOND} per second
 * regardless of how many layers are stacked.</p>
 */
public final class BleedingData {
    /** Serialized as a single integer so the wound survives a world reload. */
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

    /** Opens one more wound; layers are intentionally uncapped. */
    public void addLayer() {
        this.layers++;
    }

    /** Used when the player dies and respawns. */
    public void clear() {
        this.layers = 0;
    }

    public boolean isBleeding() {
        return this.layers > 0;
    }
}
