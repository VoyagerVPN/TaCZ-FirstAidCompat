package ru.ranazy.tacz_firstaid_compat.mixin;

import com.tacz.guns.entity.EntityKineticBullet;
import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.api.distribution.IDamageDistributionAlgorithm;
import ichttt.mods.firstaid.common.EventHandler;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.ranazy.tacz_firstaid_compat.compat.firstaid.TacZDamageDistribution;
import ru.ranazy.tacz_firstaid_compat.mixininterface.EntityKineticBulletExtension;

/**
 * Intercepts FirstAid's damage handling to provide precise bodypart targeting for TacZ weapons.
 * This replaces FirstAid's default random/height-based distribution with our 3D hit detection.
 */
@Mixin(value = EventHandler.class, remap = false)
public class FirstAidEventHandlerMixin {

    @Unique
    private static final ThreadLocal<IDamageDistributionAlgorithm> tacZ_firstaid$customDistribution = new ThreadLocal<>();

    /**
     * Intercepts damage events to detect TacZ bullets.
     * Sets up custom distribution algorithms before FirstAid processes the damage.
     */
    @Inject(
        method = "onLivingHurt",
        at = @At(
            value = "INVOKE",
            target = "Lichttt/mods/firstaid/common/util/CommonUtils;getDamageModel(Lnet/minecraft/world/entity/player/Player;)Lichttt/mods/firstaid/api/damagesystem/AbstractPlayerDamageModel;",
            shift = At.Shift.AFTER
        ),
        remap = false
    )
    private static void tacZ_firstaid$detectTacZDamage(LivingHurtEvent event, CallbackInfo ci) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        DamageSource source = event.getSource();
        Entity directEntity = source.getDirectEntity();

        // Check if damage is from a TacZ bullet
        if (directEntity instanceof EntityKineticBullet) {
            Vec3 hitLocation = ((EntityKineticBulletExtension) directEntity).tacZ_firstaid$getLastHitLocation();

            if (hitLocation != null) {
                // Store custom distribution for this bullet
                tacZ_firstaid$customDistribution.set(new TacZDamageDistribution(hitLocation));
                return;
            }
        }

        // Clear any previous custom distribution
        tacZ_firstaid$customDistribution.remove();
    }

    /**
     * Redirects the handleDamageTaken call to use our custom distribution if available.
     */
    @Redirect(
        method = "onLivingHurt",
        at = @At(
            value = "INVOKE",
            target = "Lichttt/mods/firstaid/common/damagesystem/distribution/DamageDistribution;handleDamageTaken(Lichttt/mods/firstaid/api/distribution/IDamageDistributionAlgorithm;Lichttt/mods/firstaid/api/damagesystem/AbstractPlayerDamageModel;FLnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/damagesource/DamageSource;ZZ)F"
        ),
        remap = false
    )
    private static float tacZ_firstaid$replaceDistribution(
            IDamageDistributionAlgorithm original,
            AbstractPlayerDamageModel damageModel,
            float damage,
            Player player,
            DamageSource source,
            boolean addStat,
            boolean redistributeIfLeft
    ) {
        try {
            IDamageDistributionAlgorithm custom = tacZ_firstaid$customDistribution.get();
            if (custom != null) {
                // Call handleDamageTaken with our custom distribution
                return ichttt.mods.firstaid.common.damagesystem.distribution.DamageDistribution.handleDamageTaken(
                    custom, damageModel, damage, player, source, addStat, redistributeIfLeft
                );
            }

            // Use the original distribution
            return ichttt.mods.firstaid.common.damagesystem.distribution.DamageDistribution.handleDamageTaken(
                original, damageModel, damage, player, source, addStat, redistributeIfLeft
            );
        } finally {
            tacZ_firstaid$customDistribution.remove();
        }
    }
}
