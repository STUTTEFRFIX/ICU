package com.nofo.nofo.bleed;

import com.nofo.nofo.ModAttachments;
import com.nofo.nofo.ModAttachments.BleedingData;
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
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Massive bleeding (haemorrhage) - the single feature of this ICU build.
 *
 * <h2>Rules implemented</h2>
 * <ol>
 *   <li>Trigger: a player is hit by a sword/axe-class weapon, and the
 *       <b>final</b> damage after armour/enchantment mitigation is
 *       {@code > 10} points (more than 5 hearts).</li>
 *   <li>Each qualifying hit adds one bleeding layer, uncapped.</li>
 *   <li>Every second, each layer deals 2 points (1 heart) of bleed damage.</li>
 *   <li>There is no time limit: only death and respawn clear the effect.</li>
 *   <li>While bleeding the player is forced prone: swimming pose, no
 *       self-propelled movement, no jumping and no knockback.</li>
 *   <li>Nausea and Darkness are applied continuously and cleared together with
 *       the bleeding.</li>
 * </ol>
 */
@EventBusSubscriber(modid = com.nofo.nofo.NofoMod.MODID)
public final class BleedHandler {
    private BleedHandler() {}

    /** Final damage (post-mitigation) that must be exceeded to open a wound. */
    public static final float BLEED_TRIGGER_DAMAGE = 10.0F;

    /** Damage per layer, applied once per second. */
    public static final float DAMAGE_PER_LAYER = 2.0F;

    /** Refresh window for the two status effects (ticks). */
    private static final int EFFECT_REFRESH_TICKS = 20;

    // ------------------------------------------------------------------
    // Trigger
    // ------------------------------------------------------------------

    /**
     * {@code LivingDamageEvent.Post} fires after armour, enchantments and
     * resistance have been applied, which is exactly the "real" damage the
     * player received.
     */
    @SubscribeEvent
    public static void onLivingDamagePost(LivingDamageEvent.Post event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (player.level().isClientSide() || !player.isAlive() || player.isCreative() || player.isSpectator()) {
            return;
        }
        if (event.getNewDamage() <= BLEED_TRIGGER_DAMAGE) {
            return;
        }
        if (!isBladedWeaponAttack(event.getSource())) {
            return;
        }

        player.getData(ModAttachments.BLEEDING).addLayer();
    }

    private static boolean isBladedWeaponAttack(DamageSource source) {
        if (!(source.getEntity() instanceof LivingEntity attacker)) {
            return false;
        }
        ItemStack weapon = attacker.getMainHandItem();
        if (weapon.isEmpty()) {
            return false;
        }
        // "刀斧类" == sword-class or axe-class weapons.
        return weapon.is(ItemTags.SWORDS) || weapon.is(ItemTags.AXES);
    }

    // ------------------------------------------------------------------
    // Per-tick upkeep: blood loss, prone lock, status effects, particles
    // ------------------------------------------------------------------

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        BleedingData data = player.getData(ModAttachments.BLEEDING);
        if (!data.isBleeding()) {
            return;
        }
        if (player.level().isClientSide()) {
            return;
        }

        applyProneLock(player);

        if (player.tickCount % 20 == 0) {
            player.hurt(BleedDamage.source(player), DAMAGE_PER_LAYER * data.getLayers());
            emitBloodParticles(player, data.getLayers());
        }

        applyStatusEffects(player);
    }

    /**
     * Forced prone: the swimming pose makes the player visually crawl on the
     * ground, speed is zeroed so no self-propelled movement is possible, the
     * jump impulse is cancelled, and knockback is suppressed as requested.
     */
    private static void applyProneLock(Player player) {
        player.setPose(Pose.SWIMMING);

        if (player.getDeltaMovement().y > 0.0D) {
            player.setDeltaMovement(0.0D, 0.0D, 0.0D);
        } else {
            player.setDeltaMovement(0.0D, player.getDeltaMovement().y, 0.0D);
        }
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
        player.getData(ModAttachments.BLEEDING).clear();
        player.removeEffect(MobEffects.CONFUSION);
        player.removeEffect(MobEffects.DARKNESS);
    }
}
