package redstonedubstep.mods.vanishmod.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.mojang.authlib.GameProfile;

import net.minecraft.network.ServerStatusResponse;
import net.minecraft.network.status.ServerStatusNetHandler;
import net.minecraft.network.status.server.SServerInfoPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import redstonedubstep.mods.vanishmod.VanishUtil;

@Mixin(ServerStatusNetHandler.class)
public abstract class MixinServerStatusNetHandler {
	@Shadow
	@Final
	private MinecraftServer server;

	//stop server from sending the names of vanished players to the Multiplayer screen
	@Redirect(method = "processServerQuery", at = @At(value = "NEW", target = "net/minecraft/network/status/server/SServerInfoPacket"))
	public SServerInfoPacket constructSServerInfoPacket(ServerStatusResponse response) {
		PlayerList list = server.getPlayerList();
		GameProfile[] players = response.getPlayers().getPlayers();
		GameProfile[] newPlayersHelper = new GameProfile[players.length]; //this helper is needed to evaluate the right size for the actual array
		GameProfile[] newPlayers;
		int visiblePlayersCount = 0;

		for (GameProfile profile : players) {
			if (!VanishUtil.isVanished(list.getPlayerByUUID(profile.getId()))) {
				newPlayersHelper[visiblePlayersCount] = profile;
				visiblePlayersCount++;
			}
		}

		newPlayers = new GameProfile[visiblePlayersCount];

		for (int i = 0; i < newPlayersHelper.length; i++) {
			if (newPlayersHelper[i] != null)
				newPlayers[i] = newPlayersHelper[i];
		}

		response.getPlayers().setPlayers(newPlayers);
		return new SServerInfoPacket(response);
	}
}
