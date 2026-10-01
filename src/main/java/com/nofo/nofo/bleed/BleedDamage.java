package com.nofo.nofo.bleed;

import com.nofo.nofo.NofoMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.player.Player;

/**
 * The custom damage type behind the haemorrhage feature.
 *
 * <p>The type itself is defined by the data file
 * {@code data/nofo/damage_type/bleed.json}; this class only turns it into a
 * usable {@link DamageSource} and gives the death message its key
 * ({@code death.attack.bleed}).</p>
 */
public final class BleedDamage {
    private BleedDamage() {}

    public static final ResourceKey<DamageType> BLEED_TYPE =
            ResourceKey.create(Registries.DAMAGE_TYPE,
                    ResourceLocation.fromNamespaceAndPath(NofoMod.MODID, "bleed"));

    /**
     * Builds the per-tick blood-loss damage source.
     *
     * <p>Invulnerability is bypassed on purpose: vanilla grants 10 ticks of
     * invulnerability after every hit, which would otherwise swallow most of the
     * "2 points per second per layer" damage.</p>
     */
    public static DamageSource source(Player player) {
        return new DamageSource(
                player.level().registryAccess()
                        .registryOrThrow(Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(BLEED_TYPE),
                null,
                null);
    }
}
