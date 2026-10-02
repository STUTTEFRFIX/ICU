package com.icu.icu.gameplay.bleeding;

import com.icu.icu.IcuAttachments;
import com.icu.icu.IcuMod;
import com.icu.icu.gameplay.blood.BloodVolumeData;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
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
 * <p>All rule constants are public and sit at the top, so the numbers can be
 * tuned without reading the logic.</p>
 *
 * <h2>Rules</h2>
 * <ol>
 *   <li><b>Trigger</b> - hit by a sword- or axe-class weapon and the final damage
 *       after armour and enchantments is greater than {@link #TRIGGER_DAMAGE}.</li>
 *   <li><b>Stacking</b> - every qualifying hit adds one layer, uncapped. Layers
 *       are recorded for diagnostics and for the recovery rules.</li>
 *   <li><b>Blood loss</b> - while bleeding, blood volume drops
 *       {@link BloodVolumeData#LOSS_PER_SECOND} points every second. At zero the
 *       player dies immediately, whatever their health was. This is the
 *       <b>only</b> lethal mechanism: there is deliberately no health drain, so
 *       the player always gets the full 20 seconds to treat the wound.</li>
 *   <li><b>Forced prone</b> - handled by
 *       {@link com.icu.icu.gameplay.IcuPose}, the single owner of forced poses.</li>
 *   <li><b>Status effects</b> - Nausea and Darkness, refreshed continuously.</li>
 *   <li><b>Treatment</b> - a bandage held for 3 seconds stops the bleed and opens
 *       the recovery window. Blood volume is never refilled.</li>
 * </ol>
 *
 * <p>The heart-beat sound is not implemented yet; the design is fixed and awaits
 * audio assets.</p>
 */
@EventBusSubscriber(modid = IcuMod.MODID)
public final class BleedingFeature {
    private BleedingFeature() {}

    // ------------------------------------------------------------------
    // Tunable rules
    // ------------------------------------------------------------------

    /** Final (post-mitigation) damage that must be exceeded to open a wound. */
    public static final float TRIGGER_DAMAGE = 7.0F;

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
        if (player.level().isClientSide()) {
            return;
        }

        BloodVolumeData blood = player.getData(IcuAttachments.BLOOD_VOLUME);

        if (!player.getData(IcuAttachments.BLEEDING).isBleeding()) {
            // Out of danger: the body slowly makes new blood.
            blood.tickRegen();
            return;
        }

        // The pose and movement lock are owned by IcuPose, which runs every tick.
        applyStatusEffects(player);

        if (player.tickCount % TICK_INTERVAL != 0) {
            return;
        }

        emitBloodParticles(player, player.getData(IcuAttachments.BLEEDING).getLayers());

        // The one and only lethal mechanism. There is deliberately no extra health
        // damage: blood volume running out is what kills, so the player always has
        // the full 20 seconds to find a bandage.
        if (blood.drain(BloodVolumeData.LOSS_PER_SECOND) && player.isAlive()) {
            killFromBloodLoss(player);
        }
    }

    /**
     * Blood volume reached zero. The player dies immediately, regardless of how
     * much health was left.
     */
    private static void killFromBloodLoss(Player player) {
        // TODO: play the flat-line sound ("滴——") here once the audio asset exists.
        player.hurt(BleedingDamage.source(player), Float.MAX_VALUE);
        if (player.isAlive()) {
            player.setHealth(0.0F);
        }
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
        player.getData(IcuAttachments.BLEEDING_RECOVERY).stop();
        player.getData(IcuAttachments.BLOOD_VOLUME).reset();
        player.getData(IcuAttachments.PAIN).reset();
        player.getData(IcuAttachments.SPRAIN).clear();
        player.removeEffect(MobEffects.CONFUSION);
        player.removeEffect(MobEffects.DARKNESS);
    }
}
