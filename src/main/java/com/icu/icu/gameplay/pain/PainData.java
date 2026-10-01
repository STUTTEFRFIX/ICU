package com.icu.icu.gameplay.pain;

import com.mojang.serialization.Codec;

/**
 * The shared pain meter used by every injury module.
 *
 * <p>Bleeding and a sprained ankle both feed this single value, exactly as the
 * design requires. It runs from 0 to {@value #MAX}.</p>
 *
 * <ul>
 *   <li>At {@value #MAX} the player is forced to collapse and cannot act.</li>
 *   <li>While collapsed the value falls {@value #COLLAPSE_RECOVERY_PER_SECOND}
 *       per second; action returns at {@value #RECOVER_AT}.</li>
 * </ul>
 */
public final class PainData {
    public static final float MAX = 100.0F;

    /** Pain is considered recovered (movement returns) below this value. */
    public static final float RECOVER_AT = 80.0F;

    /** Pain lost per second while lying down. */
    public static final float COLLAPSE_RECOVERY_PER_SECOND = 5.0F;

    public static final Codec<PainData> CODEC =
            Codec.FLOAT.xmap(PainData::new, PainData::getPain);

    private float pain;

    public PainData() {
        this(0.0F);
    }

    public PainData(float pain) {
        this.pain = clamp(pain);
    }

    public float getPain() {
        return pain;
    }

    public boolean isCollapsed() {
        return pain >= MAX;
    }

    public void set(float value) {
        this.pain = clamp(value);
    }

    public void add(float amount) {
        this.pain = clamp(this.pain + amount);
    }

    public void subtract(float amount) {
        this.pain = clamp(this.pain - amount);
    }

    public void reset() {
        this.pain = 0.0F;
    }

    private static float clamp(float v) {
        return Math.max(0.0F, Math.min(MAX, v));
    }
}
