package redstonedubstep.mods.vanishmod.mixin.world;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.server.ChunkManager.EntityTracker;
import redstonedubstep.mods.vanishmod.VanishConfig;
import redstonedubstep.mods.vanishmod.VanishUtil;

@Mixin(EntityTracker.class)
public abstract class MixinChunkManagerEntityTracker {
	@Shadow
	@Final
	private Entity entity;

	//Prevent tracking of vanished players for other players, which prevents vanished players from being rendered for anyone but themselves and permitted players.
	@Inject(method = "updatePlayer", at = @At("HEAD"), cancellable = true)
	private void onUpdatePlayer(ServerPlayerEntity otherPlayer, CallbackInfo info) {
		if (VanishConfig.CONFIG.hidePlayersFromWorld.get()) {
			if (entity instanceof PlayerEntity && VanishUtil.isVanished((PlayerEntity)entity, otherPlayer))
				info.cancel();
		}
	}
}