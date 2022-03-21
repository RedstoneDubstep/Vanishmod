package redstonedubstep.mods.vanishmod.mixin.world;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ChunkMap.TrackedEntity;
import redstonedubstep.mods.vanishmod.VanishConfig;
import redstonedubstep.mods.vanishmod.VanishUtil;

@Mixin(TrackedEntity.class)
public abstract class MixinChunkMapTrackedEntity {
	@Shadow
	@Final
	Entity entity;

	//Prevent tracking of vanished players for other players, which prevents vanished players from being rendered for anyone but themselves and permitted players.
	@Inject(method = "updatePlayer", at = @At("HEAD"), cancellable = true)
	private void onUpdatePlayer(ServerPlayer otherPlayer, CallbackInfo info) {
		if (VanishConfig.CONFIG.hidePlayersFromWorld.get()) {
			if (entity instanceof Player player && VanishUtil.isVanished(player, otherPlayer))
				info.cancel();
		}
	}
}