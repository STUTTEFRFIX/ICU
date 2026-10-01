package com.icu.icu.gameplay.sprain;

import com.icu.icu.IcuAttachments;
import com.icu.icu.IcuMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;

/**
 * Sprained ankle on a qualifying fall.
 *
 * <p>Everything is derived from vanilla {@code fallDistance}; no height is
 * computed by hand. The event only fires for genuine falls, so slow falling,
 * elytra gliding and chorus-fruit teleports are already excluded by the game and
 * water landings are checked explicitly.</p>
 *
 * <h2>Chance by fall distance</h2>
 * <table>
 *   <tr><th>Fall distance</th><th>Chance</th></tr>
 *   <tr><td>5 or less</td><td>nothing</td></tr>
 *   <tr><td>more than 5, less than 10</td><td>{@value #CHANCE_TIER_1}%</td></tr>
 *   <tr><td>10 to less than 15</td><td>{@value #CHANCE_TIER_2}%</td></tr>
 *   <tr><td>15 to less than 20</td><td>{@value #CHANCE_TIER_3}%</td></tr>
 *   <tr><td>20 or more</td><td>{@value #CHANCE_TIER_4}%</td></tr>
 * </table>
 *
 * <p>Landing on hay reduces the chance by
 * {@value #HAY_CHANCE_REDUCTION} percentage points. Protection or Feather
 * Falling reduces it by {@value #CHANCE_PER_ENCHANT_LEVEL} points per level,
 * counted from whichever of the two is higher; level
 * {@value #FULL_IMMUNITY_LEVEL} or above removes the risk entirely.</p>
 */
@EventBusSubscriber(modid = IcuMod.MODID)
public final class SprainFeature {
    private SprainFeature() {}

    public static final double MIN_FALL_DISTANCE = 5.0D;

    public static final int CHANCE_TIER_1 = 35;
    public static final int CHANCE_TIER_2 = 50;
    public static final int CHANCE_TIER_3 = 90;
    public static final int CHANCE_TIER_4 = 100;

    /** Hay landing is a reduction, not an exemption. */
    public static final int HAY_CHANCE_REDUCTION = 20;

    /** Points removed per enchantment level. */
    public static final int CHANCE_PER_ENCHANT_LEVEL = 8;

    /** At this level, protection or feather falling removes the risk entirely. */
    public static final int FULL_IMMUNITY_LEVEL = 5;

    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (player.level().isClientSide() || player.isCreative() || player.isSpectator()) {
            return;
        }

        double distance = event.getDistance();
        if (distance <= MIN_FALL_DISTANCE) {
            return;
        }
        if (landedInWater(player)) {
            return;
        }
        if (player.fireImmune()) {
            return;
        }
        // Slow falling still fires the fall event, it merely cancels the damage,
        // so the exemption has to be explicit here.
        if (player.hasEffect(MobEffects.SLOW_FALLING)) {
            return;
        }

        int chance = baseChance(distance);
        chance = applyProtection(player, chance);
        chance = applyLandingSurface(player, chance);

        if (chance <= 0) {
            return;
        }
        if (player.getRandom().nextInt(100) >= chance) {
            return;
        }

        player.getData(IcuAttachments.SPRAIN).setSprained(true);
        if (player instanceof ServerPlayer serverPlayer) {
            // Visible only to the player who fell.
            serverPlayer.displayClientMessage(
                    Component.translatable("message.icu.sprained"), false);
        }
    }

    /** Base chance for a fall of the given distance. */
    public static int baseChance(double distance) {
        if (distance < 10.0D) {
            return CHANCE_TIER_1;
        }
        if (distance < 15.0D) {
            return CHANCE_TIER_2;
        }
        if (distance < 20.0D) {
            return CHANCE_TIER_3;
        }
        return CHANCE_TIER_4;
    }

    /** Water of any depth, even a single block, protects the ankles. */
    private static boolean landedInWater(Player player) {
        BlockPos pos = player.blockPosition();
        for (int dy = 0; dy <= 1; dy++) {
            BlockState state = player.level().getBlockState(pos.below(dy));
            if (!state.getFluidState().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /** Hay is a reduction; it never makes things worse. */
    private static int applyLandingSurface(Player player, int chance) {
        BlockPos landing = BlockPos.containing(player.getX(), player.getY() - 0.1D, player.getZ());
        BlockState below = player.level().getBlockState(landing);
        if (below.is(Blocks.HAY_BLOCK)) {
            chance -= HAY_CHANCE_REDUCTION;
        }
        return Math.max(0, chance);
    }

    /** Protection and Feather Falling, counted from whichever is higher. */
    private static int applyProtection(Player player, int chance) {
        var lookup = player.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        Holder<Enchantment> featherFalling = lookup.getOrThrow(Enchantments.FEATHER_FALLING);
        Holder<Enchantment> protection = lookup.getOrThrow(Enchantments.PROTECTION);

        // EnchantmentHelper totals the level across every worn piece; the design
        // says to count from whichever of the two enchantments is higher.
        int level = Math.max(
                EnchantmentHelper.getEnchantmentLevel(featherFalling, player),
                EnchantmentHelper.getEnchantmentLevel(protection, player));

        if (level <= 0) {
            return chance;
        }
        if (level >= FULL_IMMUNITY_LEVEL) {
            return 0;
        }
        return Math.max(0, chance - level * CHANCE_PER_ENCHANT_LEVEL);
    }
}
