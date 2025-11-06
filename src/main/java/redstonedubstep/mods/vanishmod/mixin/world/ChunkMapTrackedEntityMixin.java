package redstonedubstep.mods.vanishmod.mixin.world;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.level.ChunkMap.TrackedEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import redstonedubstep.mods.vanishmod.VanishUtil;

@Mixin(TrackedEntity.class)
public class ChunkMapTrackedEntityMixin {
	@Shadow
	@Final
	Entity entity;

	//Prevent tracking of vanished players for other players, which prevents vanished players from being rendered for anyone but themselves and permitted players.
	//This tracking prevention needs to apply unconditionally, because clients will log an error if they receive an entity add packet for an unknown player
	@Inject(method = "updatePlayer", at = @At("HEAD"), cancellable = true)
	private void vanishmod$onUpdatePlayer(ServerPlayer otherPlayer, CallbackInfo info) {
		if (entity instanceof Player player && VanishUtil.isVanished(player, otherPlayer))
			info.cancel();
	}
}