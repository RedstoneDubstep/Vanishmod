package redstonedubstep.mods.vanishmod.mixin.gui;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.authlib.GameProfile;

import net.minecraft.network.ServerStatusResponse;
import redstonedubstep.mods.vanishmod.VanishConfig;

@Mixin(ServerStatusResponse.Players.class)
public abstract class MixinServerStatusResponsePlayers {
	@Shadow
	public int onlinePlayerCount;

	//update the onlinePlayerCount when setting the players; also makes use of an AT to un-final onlinePlayerCount
	@Inject(method = "setPlayers", at = @At("HEAD"))
	private void onSetPlayers(GameProfile[] players, CallbackInfo info) {
		if (VanishConfig.CONFIG.hidePlayersFromPlayerLists.get()) {
			this.onlinePlayerCount = players.length;
		}
	}
}
