package redstonedubstep.mods.vanishmod.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.ServerStatusResponse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerStatusResponse.Players.class)
public abstract class MixinServerStatusResponsePlayers {
	@Shadow
	private int onlinePlayerCount;

	//update the onlinePlayerCount when setting the players; also makes use of an AT to un-final onlinePlayerCount
	@Inject(method="setPlayers", at=@At("HEAD"))
	private void onSetPlayers(GameProfile[] players, CallbackInfo info) {
		this.onlinePlayerCount = players.length;
	}
}
