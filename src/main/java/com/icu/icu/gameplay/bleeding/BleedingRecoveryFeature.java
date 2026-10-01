package com.icu.icu.gameplay.bleeding;

import com.icu.icu.IcuAttachments;
import com.icu.icu.IcuMod;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * The recovery window that follows a successful bandage.
 *
 * <h2>Rules</h2>
 * <ul>
 *   <li>Runs for {@link BleedingRecoveryData#DURATION_TICKS} ticks (5 minutes).</li>
 *   <li>Sprinting for more than
 *       {@link BleedingRecoveryData#SPRINT_LIMIT_TICKS} ticks, or a 4th jump,
 *       tears the wound open again - the player starts bleeding with the blood
 *       volume they have left.</li>
 *   <li>Any jump or sprint restarts the 5 minute countdown.</li>
 * </ul>
 */
@EventBusSubscriber(modid = IcuMod.MODID)
public final class BleedingRecoveryFeature {
    private BleedingRecoveryFeature() {}

    /** Fires when the player leaves the ground, which is how a jump is counted. */
    @SubscribeEvent
    public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (player.level().isClientSide()) {
            return;
        }
        BleedingRecoveryData recovery = player.getData(IcuAttachments.BLEEDING_RECOVERY);
        if (!recovery.isActive()) {
            return;
        }
        if (recovery.recordJump()) {
            reopenWound(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) {
            return;
        }
        BleedingRecoveryData recovery = player.getData(IcuAttachments.BLEEDING_RECOVERY);
        if (!recovery.isActive()) {
            return;
        }

        // A bleed that came back ends the recovery window naturally.
        if (player.getData(IcuAttachments.BLEEDING).isBleeding()) {
            recovery.stop();
            return;
        }

        if (player.isSprinting()) {
            if (recovery.tickSprint()) {
                reopenWound(player);
                return;
            }
        } else {
            recovery.clearSprintStreak();
        }

        recovery.tickDown();
    }

    /** Tears the wound open again, keeping the current blood volume. */
    private static void reopenWound(Player player) {
        player.getData(IcuAttachments.BLEEDING).addLayer();
        player.getData(IcuAttachments.BLEEDING_RECOVERY).stop();
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.displayClientMessage(
                    Component.translatable("message.icu.wound_reopened"), true);
        }
    }
}
