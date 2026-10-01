package com.icu.icu.gameplay.bleeding;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * The recovery window that opens when a bleed is successfully treated with a
 * bandage.
 *
 * <h2>Rules</h2>
 * <ul>
 *   <li>Lasts {@value #DURATION_TICKS} ticks (5 minutes).</li>
 *   <li>Running continuously for more than {@value #SPRINT_LIMIT_TICKS} ticks,
 *       or jumping more than {@value #JUMP_LIMIT} times, tears the wound open
 *       again.</li>
 *   <li>Any jump or sprint resets the 5 minute countdown (without reopening the
 *       wound).</li>
 * </ul>
 */
public final class BleedingRecoveryData {
    /** 5 minutes at 20 ticks per second. */
    public static final int DURATION_TICKS = 20 * 60 * 5;

    /** 15 seconds of continuous sprinting reopens the wound. */
    public static final int SPRINT_LIMIT_TICKS = 20 * 15;

    /** The 4th jump reopens the wound, so more than 3 jumps is the trigger. */
    public static final int JUMP_LIMIT = 3;

    public static final Codec<BleedingRecoveryData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("remaining").forGetter(BleedingRecoveryData::getRemainingTicks),
            Codec.INT.fieldOf("sprint").forGetter(BleedingRecoveryData::getSprintTicks),
            Codec.INT.fieldOf("jumps").forGetter(BleedingRecoveryData::getJumpCount)
    ).apply(instance, BleedingRecoveryData::new));

    private int remainingTicks;
    private int sprintTicks;
    private int jumpCount;

    public BleedingRecoveryData() {
        this(0, 0, 0);
    }

    public BleedingRecoveryData(int remainingTicks, int sprintTicks, int jumpCount) {
        this.remainingTicks = Math.max(0, remainingTicks);
        this.sprintTicks = Math.max(0, sprintTicks);
        this.jumpCount = Math.max(0, jumpCount);
    }

    public int getRemainingTicks() {
        return remainingTicks;
    }

    public int getSprintTicks() {
        return sprintTicks;
    }

    public int getJumpCount() {
        return jumpCount;
    }

    public boolean isActive() {
        return remainingTicks > 0;
    }

    /** Called when a bandage successfully stops a bleed. */
    public void start() {
        this.remainingTicks = DURATION_TICKS;
        this.sprintTicks = 0;
        this.jumpCount = 0;
    }

    /** Any jump or sprint restarts the countdown. */
    public void resetTimer() {
        this.remainingTicks = DURATION_TICKS;
    }

    /** Records one second of sprinting; returns true when the wound reopens. */
    public boolean tickSprint() {
        sprintTicks++;
        resetTimer();
        return sprintTicks > SPRINT_LIMIT_TICKS;
    }

    /** Records one jump; returns true when the wound reopens. */
    public boolean recordJump() {
        jumpCount++;
        resetTimer();
        return jumpCount > JUMP_LIMIT;
    }

    public void tickDown() {
        if (remainingTicks > 0) {
            remainingTicks--;
        }
    }

    public void stop() {
        this.remainingTicks = 0;
        this.sprintTicks = 0;
        this.jumpCount = 0;
    }

    /** Keeps sprint counting honest while the player is not sprinting. */
    public void clearSprintStreak() {
        this.sprintTicks = 0;
    }
}
