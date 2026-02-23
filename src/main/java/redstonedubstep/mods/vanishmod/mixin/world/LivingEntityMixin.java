package redstonedubstep.mods.vanishmod.mixin.world;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.core.Holder;
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
		if (VanishUtil.isVanished(this, p -> VanishConfig.CONFIG.hidePlayersFromWorld.get()))
			callbackInfo.setReturnValue(false);
	}

	//Makes a vanished player pretend that they have the invisibility effect serverside, so e.g. minimap mods hide those players
	@Inject(method = "hasEffect", at = @At("HEAD"), cancellable = true)
	private void vanishmod$hasInvisibility(Holder<MobEffect> effect, CallbackInfoReturnable<Boolean> callbackInfo) {
		if (effect == MobEffects.INVISIBILITY && VanishUtil.isVanished(this, p -> VanishConfig.CONFIG.spoofVanishedPlayerInvisibility.get()))
			callbackInfo.setReturnValue(true);
	}

	//Makes a vanished player pretend that they have the invisibility effect serverside, so e.g. minimap mods hide those players
	@Inject(method = "getEffect", at = @At("HEAD"), cancellable = true)
	private void vanishmod$getInvisibilityEffect(Holder<MobEffect> effect, CallbackInfoReturnable<MobEffectInstance> callbackInfo) {
		if (effect == MobEffects.INVISIBILITY && VanishUtil.isVanished(this, p -> VanishConfig.CONFIG.spoofVanishedPlayerInvisibility.get()))
			callbackInfo.setReturnValue(new MobEffectInstance(MobEffects.INVISIBILITY, 10));
	}

	//This mixin ensures that the serverside invisibility induced by this mod is not synced to the client side, by directly checking with the active effects map (which is not modified by this mod)
	@WrapOperation(method = "updateInvisibilityStatus", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hasEffect(Lnet/minecraft/core/Holder;)Z"))
	private boolean vanishmod$correctInvisibilityStatus(LivingEntity entity, Holder<MobEffect> effect, Operation<Boolean> original) {
		if (effect == MobEffects.INVISIBILITY && VanishUtil.isVanished(this, p -> VanishConfig.CONFIG.spoofVanishedPlayerInvisibility.get()))
			return entity.getActiveEffectsMap().containsKey(MobEffects.INVISIBILITY);

		return original.call(entity, effect);
	}
}
