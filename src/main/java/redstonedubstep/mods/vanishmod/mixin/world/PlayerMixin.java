package redstonedubstep.mods.vanishmod.mixin.world;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import redstonedubstep.mods.vanishmod.VanishConfig;
import redstonedubstep.mods.vanishmod.VanishUtil;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity {
	//LivingEntity needs a constructor, so here we go
	public PlayerMixin(Level world) {
		super(EntityType.PLAYER, world);
	}

	//Fixes that the night can be skipped in some instances when a vanished player is sleeping
	@Inject(method = "isSleepingLongEnough", at = @At("HEAD"), cancellable = true)
	private void vanishmod$onIsSleepingLongEnough(CallbackInfoReturnable<Boolean> callbackInfo) {
		if (VanishConfig.CONFIG.hidePlayersFromWorld.get() && VanishUtil.isVanished(this))
			callbackInfo.setReturnValue(false);
	}
}
