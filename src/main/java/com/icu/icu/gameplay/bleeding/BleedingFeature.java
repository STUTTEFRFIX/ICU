package com.icu.icu.gameplay.bleeding;

import com.icu.icu.IcuAttachments;
import com.icu.icu.IcuMod;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Bleeding module - massive bleeding (haemorrhage).
 *
 * <p>This is the complete gameplay of the module. All rule constants are public
 * and sit at the top so the numbers can be tuned without reading the logic.</p>
 *
 * <h2>Rules</h2>
 * <ol>
 *   <li><b>Trigger</b> - a player is hit by a sword- or axe-class weapon and the
 *       final damage after armour/enchantment mitigation is greater than
 *       {@link #TRIGGER_DAMAGE}. The threshold is low because armour makes
 *       larger numbers unreachable; see the constant for the calibration.</li>
 *   <li><b>Stacking</b> - every qualifying hit adds one layer, uncapped.</li>
 *   <li><b>Blood loss</b> - once per second, each layer deals
 *       {@link #DAMAGE_PER_LAYER} points.</li>
 *   <li><b>No time limit</b> - only death and respawn end the effect.</li>
 *   <li><b>Forced prone</b> - swimming pose, self-propelled movement locked,
 *       jumping cancelled, knockback suppressed.</li>
 *   <li><b>Status effects</b> - Nausea and Darkness, refreshed continuously and
 *       removed together with the bleeding.</li>
 * </ol>
 */
@EventBusSubscriber(modid = IcuMod.MODID)
public final class BleedingFeature {
    private BleedingFeature() {}

    // ------------------------------------------------------------------
    // Tunable rules
    // ------------------------------------------------------------------

    /**
     * Final (post-mitigation) damage that must be exceeded to open a wound.
     *
     * <p>Calibrated against the 1.21.1 armour formula. Reaching higher numbers is
     * mathematically impossible once armour is worn, so the threshold is
     * deliberately low:</p>
     * <ul>
     *   <li>no armour - almost any sword or axe hit opens a wound</li>
     *   <li>leather - needs a heavy hit</li>
     *   <li>chainmail / iron - needs a sword or axe hit of iron tier or better</li>
     *   <li>diamond / netherite - practically protected (the raw damage a player
     *       can reach, about 19.5 on a critical hit with a Sharpness V netherite
     *       axe, cannot push 3 points through full diamond armour)</li>
     * </ul>
     */
    public static final float TRIGGER_DAMAGE = 3.0F;

    /** Health lost per layer, applied once per second. */
    public static final float DAMAGE_PER_LAYER = 2.0F;

    /** Refresh window for the two status effects, in ticks. */
    private static final int EFFECT_REFRESH_TICKS = 20;

    /** Blood loss and particles run once every this many ticks. */
    private static final int TICK_INTERVAL = 20;

    // ------------------------------------------------------------------
    // Trigger
    // ------------------------------------------------------------------

    /**
     * {@code LivingDamageEvent.Post} fires after armour, enchantments and
     * resistance have been applied - exactly the damage the player really took.
     */
    @SubscribeEvent
    public static void onLivingDamagePost(LivingDamageEvent.Post event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (player.level().isClientSide() || !player.isAlive() || player.isCreative() || player.isSpectator()) {
            return;
        }
        if (event.getNewDamage() <= TRIGGER_DAMAGE) {
            return;
        }
        if (!isBladedWeaponAttack(event.getSource())) {
            return;
        }

        player.getData(IcuAttachments.BLEEDING).addLayer();
    }

    /** Sword-class or axe-class weapons, resolved through the vanilla item tags. */
    private static boolean isBladedWeaponAttack(DamageSource source) {
        if (!(source.getEntity() instanceof LivingEntity attacker)) {
            return false;
        }
        ItemStack weapon = attacker.getMainHandItem();
        if (weapon.isEmpty()) {
            return false;
        }
        return weapon.is(ItemTags.SWORDS) || weapon.is(ItemTags.AXES);
    }

    // ------------------------------------------------------------------
    // Per-tick upkeep: blood loss, prone lock, status effects, particles
    // ------------------------------------------------------------------

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        BleedingData data = player.getData(IcuAttachments.BLEEDING);
        if (!data.isBleeding() || player.level().isClientSide()) {
            return;
        }

        applyProneLock(player);

        if (player.tickCount % TICK_INTERVAL == 0) {
            player.hurt(BleedingDamage.source(player), DAMAGE_PER_LAYER * data.getLayers());
            emitBloodParticles(player, data.getLayers());
        }

        applyStatusEffects(player);
    }

    /**
     * Forced prone. The swimming pose makes the player visually crawl, speed is
     * zeroed so no self-propelled movement is possible, the jump impulse is
     * cancelled and knockback is suppressed.
     */
    private static void applyProneLock(Player player) {
        player.setPose(Pose.SWIMMING);

        // Keep gravity (so the player still falls) but remove all input-driven
        // horizontal motion, and cancel any upward impulse such as a jump.
        double vertical = player.getDeltaMovement().y;
        player.setDeltaMovement(0.0D, vertical > 0.0D ? 0.0D : vertical, 0.0D);
        player.hurtMarked = true;
        player.push(0.0D, 0.0D, 0.0D);
    }

    private static void applyStatusEffects(Player player) {
        // In 1.21.1 the nausea effect is still exposed as MobEffects.CONFUSION.
        player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, EFFECT_REFRESH_TICKS, 0, false, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, EFFECT_REFRESH_TICKS, 0, false, false, false));
    }

    private static void emitBloodParticles(Player player, int layers) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        int count = Math.min(3 + layers * 2, 30);
        serverLevel.sendParticles(
                ParticleTypes.DAMAGE_INDICATOR,
                player.getX(),
                player.getY() + 0.9D,
                player.getZ(),
                count,
                0.35D, 0.5D, 0.35D,
                0.02D);
    }

    // ------------------------------------------------------------------
    // End condition: death / respawn
    // ------------------------------------------------------------------

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        player.getData(IcuAttachments.BLEEDING).clear();
        player.removeEffect(MobEffects.CONFUSION);
        player.removeEffect(MobEffects.DARKNESS);
    }
}
