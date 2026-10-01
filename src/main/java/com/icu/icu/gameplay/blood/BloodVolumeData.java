package com.icu.icu.gameplay.blood;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Blood volume, the hidden resource behind the bleeding module.
 *
 * <h2>Rules</h2>
 * <ul>
 *   <li>Starts at {@value #MAX} and is capped there.</li>
 *   <li>While bleeding it drops {@value #LOSS_PER_SECOND} points every second.</li>
 *   <li>Reaching zero kills the player immediately.</li>
 *   <li>While <b>not</b> bleeding it regenerates {@value #REGEN_AMOUNT} points
 *       every {@link #REGEN_INTERVAL_TICKS} ticks (10 minutes).</li>
 *   <li>Stopping a bleed never refills it - the current value is kept.</li>
 * </ul>
 *
 * <p>The regeneration timer is stored alongside the value so it survives a
 * world reload.</p>
 */
public final class BloodVolumeData {
    public static final float MAX = 100.0F;

    /** Points lost per second while bleeding. */
    public static final float LOSS_PER_SECOND = 5.0F;

    /** Points regenerated per interval while not bleeding. */
    public static final float REGEN_AMOUNT = 5.0F;

    /** 10 minutes at 20 ticks per second. */
    public static final int REGEN_INTERVAL_TICKS = 20 * 60 * 10;

    public static final Codec<BloodVolumeData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("volume").forGetter(BloodVolumeData::getVolume),
            Codec.INT.fieldOf("regen_timer").forGetter(BloodVolumeData::getRegenTicks)
    ).apply(instance, BloodVolumeData::new));

    private float volume;
    private int regenTicks;

    public BloodVolumeData() {
        this(MAX, 0);
    }

    public BloodVolumeData(float volume, int regenTicks) {
        this.volume = clamp(volume);
        this.regenTicks = Math.max(0, regenTicks);
    }

    public float getVolume() {
        return volume;
    }

    public int getRegenTicks() {
        return regenTicks;
    }

    public boolean isEmpty() {
        return volume <= 0.0F;
    }

    public boolean isFull() {
        return volume >= MAX;
    }

    /** Called once per second while bleeding. Returns true when it hits zero. */
    public boolean drain(float amount) {
        volume = clamp(volume - amount);
        return volume <= 0.0F;
    }

    /** Called once per tick while not bleeding. */
    public void tickRegen() {
        if (isFull()) {
            regenTicks = 0;
            return;
        }
        regenTicks++;
        if (regenTicks >= REGEN_INTERVAL_TICKS) {
            regenTicks -= REGEN_INTERVAL_TICKS;
            volume = clamp(volume + REGEN_AMOUNT);
        }
    }

    /** Never refills; used on respawn only. */
    public void reset() {
        volume = MAX;
        regenTicks = 0;
    }

    private static float clamp(float v) {
        return Math.max(0.0F, Math.min(MAX, v));
    }
}
