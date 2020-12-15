package redstonedubstep.mods.vanishmod.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.server.ChunkManager.EntityTracker;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import redstonedubstep.mods.vanishmod.VanishUtil;

@Mixin(EntityTracker.class)
public abstract class MixinChunkManagerEntityTracker {
	@Shadow @Final
	public Entity entity;

	//Don't track vanished players for other players.
	@Inject(method="updateTrackingState(Lnet/minecraft/entity/player/ServerPlayerEntity;)V", at=@At("HEAD"), cancellable=true)
	private void onUpdateTrackingState(ServerPlayerEntity player, CallbackInfo info) {
		if (this.entity instanceof ServerPlayerEntity && VanishUtil.isVanished((ServerPlayerEntity)this.entity))
			info.cancel();
	}
}