package com.icu.icu.gameplay.pain;

import com.icu.icu.IcuAttachments;
import com.icu.icu.IcuMod;
import com.icu.icu.gameplay.IcuPose;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * The shared pain meter.
 *
 * <p>Bleeding and a sprained ankle both feed this one value, exactly as the
 * design requires. It is settled once per second, never per distance
 * travelled.</p>
 *
 * <h2>Sources</h2>
 * <table>
 *   <tr><th>Source</th><th>Change per second</th></tr>
 *   <tr><td>Bleeding</td><td>{@value #BLEEDING_PER_SECOND} (always, even while lying still)</td></tr>
 *   <tr><td>Sprained, walking</td><td>{@value #SPRAIN_WALK_PER_SECOND}</td></tr>
 *   <tr><td>Sprained, sprinting</td><td>{@value #SPRAIN_SPRINT_PER_SECOND}</td></tr>
 *   <tr><td>Lying down (collapsed)</td><td>{@value #COLLAPSE_RELIEF_PER_SECOND}</td></tr>
 * </table>
 *
 * <p>A sprained jump adds {@value #SPRAIN_PER_JUMP} immediately.</p>
 *
 * <p>At {@value PainData#MAX} the player collapses and is shown
 * "我好疼 我好疼" three times, visible only to themselves. Movement returns once
 * pain falls to {@link PainData#RECOVER_AT}.</p>
 *
 * <p>The pose and the movement lock are <b>not</b> applied here: they are handled
 * every tick by {@link IcuPose}, which is the single owner of forced poses. This
 * class only decides what the pain value becomes, and announces the collapse
 * once.</p>
 */
@EventBusSubscriber(modid = IcuMod.MODID)
public final class PainFeature {
    private PainFeature() {}

    public static final float BLEEDING_PER_SECOND = 10.0F;
    public static final float SPRAIN_WALK_PER_SECOND = 5.0F;
    public static final float SPRAIN_SPRINT_PER_SECOND = 10.0F;
    public static final float SPRAIN_PER_JUMP = 15.0F;
    public static final float COLLAPSE_RELIEF_PER_SECOND = 5.0F;

    /** "I'm in so much pain" is shown this many times when collapsing. */
    private static final int COLLAPSE_MESSAGE_COUNT = 3;

    private static final int TICK_INTERVAL = 20;

    @SubscribeEvent
    public static void onLivingJump(net.neoforged.neoforge.event.entity.living.LivingEvent.LivingJumpEvent event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide()) {
            return;
        }
        if (!player.getData(IcuAttachments.SPRAIN).isSprained()) {
            return;
        }
        player.getData(IcuAttachments.PAIN).add(SPRAIN_PER_JUMP);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) {
            return;
        }
        if (player.tickCount % TICK_INTERVAL != 0) {
            return;
        }

        PainData pain = player.getData(IcuAttachments.PAIN);
        boolean bleeding = player.getData(IcuAttachments.BLEEDING).isBleeding();
        boolean sprained = player.getData(IcuAttachments.SPRAIN).isSprained();

        // Read this before changing the value: it tells us whether this update is
        // the one that pushes the player over the edge.
        boolean wasCollapsed = pain.isCollapsed();

        float delta = 0.0F;

        // Bleeding hurts regardless of what the player is doing.
        if (bleeding) {
            delta += BLEEDING_PER_SECOND;
        }

        // A sprained ankle hurts while it is used, never once the player is down.
        if (sprained && !wasCollapsed) {
            if (player.isSprinting()) {
                delta += SPRAIN_SPRINT_PER_SECOND;
            } else if (isWalking(player)) {
                delta += SPRAIN_WALK_PER_SECOND;
            }
        }

        // Lying down eases the pain.
        if (wasCollapsed) {
            delta -= COLLAPSE_RELIEF_PER_SECOND;
        }

        if (delta > 0.0F) {
            pain.add(delta);
        } else if (delta < 0.0F) {
            pain.subtract(-delta);
        }

        // Announce exactly once, on the update that reaches the limit. The pose
        // and the movement lock are applied every tick by IcuPose.
        if (!wasCollapsed && pain.isCollapsed()) {
            announceCollapse(player);
        }
    }

    /** True when the player has actually moved this tick. */
    private static boolean isWalking(Player player) {
        double dx = player.getX() - player.xOld;
        double dz = player.getZ() - player.zOld;
        return (dx * dx + dz * dz) > 1.0E-4D;
    }

    /** Shown only to the player who is in pain. */
    private static void announceCollapse(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            for (int i = 0; i < COLLAPSE_MESSAGE_COUNT; i++) {
                serverPlayer.displayClientMessage(
                        Component.translatable("message.icu.pain_max"), false);
            }
        }
    }
}
