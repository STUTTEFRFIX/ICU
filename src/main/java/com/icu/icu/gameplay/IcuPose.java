package com.icu.icu.gameplay;

import com.icu.icu.IcuAttachments;
import com.icu.icu.IcuMod;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * The single place that decides a player's forced pose and movement lock.
 *
 * <h2>Why this class exists</h2>
 * <p>Two modules can force a player down: bleeding (the wound makes them
 * collapse) and the pain meter (pain at its maximum). When each module wrote the
 * pose itself they fought over it, which showed up in third person as the player
 * flickering between standing and prone: one module set the pose every tick, the
 * other only once per second, and whichever ran last won.</p>
 *
 * <p>So the pose is computed here, once per tick, from the two conditions. Every
 * other module must ask this class rather than touching {@code setPose}.</p>
 *
 * <h2>The rules</h2>
 * <ul>
 *   <li>Bleeding always forces prone - the wound is active every tick.</li>
 *   <li>Pain at its maximum forces prone as well.</li>
 *   <li>Both locks also stop self-propelled movement, jumping and knockback.</li>
 *   <li>Anything else gets {@link Pose#STANDING} back.</li>
 * </ul>
 */
@EventBusSubscriber(modid = IcuMod.MODID)
public final class IcuPose {
    private IcuPose() {}

    /** True when either condition is currently forcing the player down. */
    public static boolean isForcedProne(Player player) {
        boolean bleeding = player.getData(IcuAttachments.BLEEDING).isBleeding();
        boolean maxPain = player.getData(IcuAttachments.PAIN).isCollapsed();
        return bleeding || maxPain;
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) {
            return;
        }
        if (isForcedProne(player)) {
            applyProneLock(player);
        } else if (player.getPose() == Pose.SWIMMING && !player.isSwimming() && !player.isInWater()) {
            // Only undo a pose we forced; a genuinely swimming player is left alone.
            player.setPose(Pose.STANDING);
        }
    }

    /**
     * Forces the prone pose and removes every way of moving: the swimming pose
     * makes the player visually crawl, horizontal speed is zeroed, the jump
     * impulse is cancelled and knockback is suppressed.
     *
     * <p>Called every tick, because movement input is applied every tick too:
     * doing this once per second let a player holding a movement key push
     * themselves back out of the pose.</p>
     */
    public static void applyProneLock(Player player) {
        player.setPose(Pose.SWIMMING);

        double vertical = player.getDeltaMovement().y;
        player.setDeltaMovement(0.0D, vertical > 0.0D ? 0.0D : vertical, 0.0D);
        player.hurtMarked = true;
        player.push(0.0D, 0.0D, 0.0D);
    }
}
