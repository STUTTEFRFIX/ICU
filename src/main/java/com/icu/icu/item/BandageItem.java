package com.icu.icu.item;

import com.icu.icu.IcuAttachments;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

/**
 * The bandage: the only way to stop a haemorrhage short of dying.
 *
 * <h2>Rules</h2>
 * <ul>
 *   <li>Must be held for {@value #USE_TICKS} ticks (3 seconds).</li>
 *   <li>The bleed keeps draining blood volume during those 3 seconds, so a
 *       player who waits too long simply dies first.</li>
 *   <li>Finishing the hold clears the bleed and opens the recovery window.</li>
 *   <li>It does <b>not</b> restore blood volume - whatever was lost stays lost.</li>
 * </ul>
 *
 * <p>The heart-beat sound stage is intentionally not implemented yet; only
 * timing and state live here.</p>
 */
public class BandageItem extends Item {
    /** 3 seconds at 20 ticks per second. */
    public static final int USE_TICKS = 20 * 3;

    /** Short cooldown so the animation cannot be spammed. */
    private static final int COOLDOWN_TICKS = 20;

    public BandageItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return USE_TICKS;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity instanceof Player player && !level.isClientSide()) {
            boolean wasBleeding = player.getData(IcuAttachments.BLEEDING).isBleeding();
            if (wasBleeding) {
                // Stop the wound. Blood volume is deliberately left untouched.
                player.getData(IcuAttachments.BLEEDING).clear();
                player.getData(IcuAttachments.BLEEDING_RECOVERY).start();
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.WOOL_PLACE, SoundSource.PLAYERS, 1.0F, 1.2F);
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                    player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
                }
            }
        }
        return stack;
    }
}
