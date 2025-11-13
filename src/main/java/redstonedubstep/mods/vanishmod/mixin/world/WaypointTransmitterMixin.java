package redstonedubstep.mods.vanishmod.mixin.world;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.waypoints.WaypointTransmitter;
import redstonedubstep.mods.vanishmod.VanishConfig;
import redstonedubstep.mods.vanishmod.VanishUtil;

@Mixin(WaypointTransmitter.class)
public interface WaypointTransmitterMixin {
	//Prevents vanished players from showing up on the locator bar of players that aren't allowed to see them
	@Inject(method = "doesSourceIgnoreReceiver", at = @At("HEAD"), cancellable = true)
	private static void vanishmod$ignoreVanishedSources(LivingEntity source, ServerPlayer receiver, CallbackInfoReturnable<Boolean> callbackInfo) {
		if (VanishConfig.CONFIG.hidePlayersFromWorld.get() && VanishUtil.isVanished(source, receiver))
			callbackInfo.setReturnValue(true);
	}
}
