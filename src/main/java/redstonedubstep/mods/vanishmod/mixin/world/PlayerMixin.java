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

	//Hacky mixin that should improve mod compat: mods should always respect spectator mode when targeting players, and this mixin lets isSpectator also check if the player is vanished (and thus should also not be targeted); but don't interfere with Vanilla's isSpectator() calls, else weird glitches can happen
	@Inject(method = "isSpectator", at = @At("HEAD"), cancellable = true)
	public void vanishmod$onIsSpectator(CallbackInfoReturnable<Boolean> callback) {
		if (VanishConfig.CONFIG.fixPlayerDetectionModCompatibility.get() && VanishUtil.isVanished(this)) {
			String callerClassName = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(frames -> frames.skip(2).findFirst().map(f -> f.getDeclaringClass().getPackageName()).orElse("")); //0 is this mixin, 1 is isSpectator(), 2 is the caller of isSpectator()

			if (!callerClassName.isEmpty() && !callerClassName.startsWith("net.minecraft.")) //if a mod calls this on a vanished player, then it is a spectator and should not be targeted
				callback.setReturnValue(true);
		}
	}
}
