package com.icu.icu.gameplay.bleeding;

import com.mojang.serialization.Codec;

/**
 * Per-player state of the bleeding module: how many haemorrhage layers the
 * player currently carries.
 *
 * <p>Layers are intentionally uncapped. One layer drains
 * {@link BleedingFeature#DAMAGE_PER_LAYER} health per second, so the layer count
 * is literally "how fast this player is dying".</p>
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
