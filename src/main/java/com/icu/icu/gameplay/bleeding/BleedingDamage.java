package com.icu.icu.gameplay.bleeding;

import com.icu.icu.IcuMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.player.Player;

/**
 * The custom damage type used by the bleeding module.
 *
 * <p>The type is declared by the data file
 * {@code data/icu/damage_type/bleed.json}. This class turns it into a usable
 * {@link DamageSource} and, through that file's {@code message_id}, gives the
 * death message its key ({@code death.attack.bleed}).</p>
 *
 * <p>It is used for the flat-line death when blood volume reaches zero, which is
 * the only way a haemorrhage kills in this build.</p>
 */
public final class BleedingDamage {
    private BleedingDamage() {}

    public static final ResourceKey<DamageType> BLEED =
            ResourceKey.create(Registries.DAMAGE_TYPE,
                    ResourceLocation.fromNamespaceAndPath(IcuMod.MODID, "bleed"));

    /**
     * Builds the blood-loss damage source.
     *
     * <p>Invulnerability is bypassed on purpose: vanilla grants 10 ticks of
     * invulnerability after every hit, which would otherwise swallow the
     * killing blow.</p>
     */
    public static DamageSource source(Player player) {
        return new DamageSource(
                player.level().registryAccess()
                        .registryOrThrow(Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(BLEED),
                null,
                null);
    }
}
