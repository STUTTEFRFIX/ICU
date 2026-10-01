package com.icu.icu.gameplay.sprain;

import com.mojang.serialization.Codec;

/**
 * A sprained ankle caused by a qualifying fall.
 *
 * <p>A sprain has no timer of its own: it stays until the server clears it.
 * What it does is feed the shared {@link com.icu.icu.gameplay.pain.PainData}
 * while the player keeps moving, which is what eventually forces a collapse.</p>
 */
public final class SprainData {
    /** Serialized as a single boolean so the injury survives a world reload. */
    public static final Codec<SprainData> CODEC =
            Codec.BOOL.xmap(SprainData::new, SprainData::isSprained);

    private boolean sprained;

    public SprainData() {
        this(false);
    }

    public SprainData(boolean sprained) {
        this.sprained = sprained;
    }

    public boolean isSprained() {
        return sprained;
    }

    public void setSprained(boolean value) {
        this.sprained = value;
    }

    public void clear() {
        this.sprained = false;
    }
}
