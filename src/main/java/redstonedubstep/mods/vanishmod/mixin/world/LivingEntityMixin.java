package redstonedubstep.mods.vanishmod.mixin.world;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import redstonedubstep.mods.vanishmod.VanishConfig;
import redstonedubstep.mods.vanishmod.VanishUtil;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
	//Entity needs a constructor, so here we go
	private LivingEntityMixin(EntityType<?> entityType, Level world) {
		super(entityType, world);
	}

	//Prevent entities like passive mobs or pufferfish from detecting vanished players
	@Inject(method = "canBeSeenByAnyone", at = @At("HEAD"), cancellable = true)
	public void vanishmod$onCanBeSeen(CallbackInfoReturnable<Boolean> callbackInfo) {
		if (VanishConfig.CONFIG.hidePlayersFromWorld.get() && VanishUtil.isVanished(this))
			callbackInfo.setReturnValue(false);
	}

	//Makes a vanished player pretend that they have the invisibility effect serverside, so e.g. minimap mods hide those players
	@Inject(method = "hasEffect", at = @At("HEAD"), cancellable = true)
	private void vanishmod$hasInvisibility(MobEffect effect, CallbackInfoReturnable<Boolean> callbackInfo) {
		if (effect == MobEffects.INVISIBILITY && VanishConfig.CONFIG.spoofVanishedPlayerInvisibility.get() && VanishUtil.isVanished(this))
			callbackInfo.setReturnValue(true);
	}

	//Makes a vanished player pretend that they have the invisibility effect serverside, so e.g. minimap mods hide those players
	@Inject(method = "getEffect", at = @At("HEAD"), cancellable = true)
	private void vanishmod$getInvisibilityEffect(MobEffect effect, CallbackInfoReturnable<MobEffectInstance> callbackInfo) {
		if (effect == MobEffects.INVISIBILITY && VanishConfig.CONFIG.spoofVanishedPlayerInvisibility.get() && VanishUtil.isVanished(this))
			callbackInfo.setReturnValue(new MobEffectInstance(MobEffects.INVISIBILITY, 10));
	}

	//This mixin ensures that the serverside invisibility induced by this mod is not synced to the client side, by directly checking with the active effects map (which is not modified by this mod)
	@Redirect(method = "updateInvisibilityStatus", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hasEffect(Lnet/minecraft/world/effect/MobEffect;)Z"))
	private boolean vanishmod$correctInvisibilityStatus(LivingEntity entity, MobEffect effect) {
		if (effect == MobEffects.INVISIBILITY && VanishConfig.CONFIG.spoofVanishedPlayerInvisibility.get() && VanishUtil.isVanished(entity))
			return entity.getActiveEffectsMap().containsKey(MobEffects.INVISIBILITY);

		return entity.hasEffect(effect);
	}
}
