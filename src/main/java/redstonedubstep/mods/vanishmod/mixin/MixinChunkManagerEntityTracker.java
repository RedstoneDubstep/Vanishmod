package redstonedubstep.mods.vanishmod.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.server.ChunkManager.EntityTracker;
import redstonedubstep.mods.vanishmod.VanishUtil;

@Mixin(EntityTracker.class)
public abstract class MixinChunkManagerEntityTracker {
	@Shadow
	@Final
	private Entity entity;

	//Don't track vanished players for other players.
	@Inject(method = "updateTrackingState(Lnet/minecraft/entity/player/ServerPlayerEntity;)V", at = @At("HEAD"), cancellable = true)
	private void onUpdateTrackingState(ServerPlayerEntity player, CallbackInfo info) {
		if (entity instanceof ServerPlayerEntity && VanishUtil.isVanished((ServerPlayerEntity)entity)) {
			info.cancel();
		}
	}
}