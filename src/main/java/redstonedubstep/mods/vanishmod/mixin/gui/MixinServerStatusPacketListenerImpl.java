package redstonedubstep.mods.vanishmod.mixin.gui;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.mojang.authlib.GameProfile;

import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.server.network.ServerStatusPacketListenerImpl;
import net.minecraft.network.protocol.status.ClientboundStatusResponsePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.PlayerList;
import redstonedubstep.mods.vanishmod.VanishConfig;
import redstonedubstep.mods.vanishmod.VanishUtil;

@Mixin(ServerStatusPacketListenerImpl.class)
public abstract class MixinServerStatusPacketListenerImpl {
	@Shadow
	@Final
	private MinecraftServer server;

	//stop server from sending the names of vanished players to the Multiplayer screen
	@Redirect(method = "handleStatusRequest", at = @At(value = "NEW", target = "net/minecraft/network/protocol/status/ClientboundStatusResponsePacket"))
	public ClientboundStatusResponsePacket constructSServerInfoPacket(ServerStatus response) {
		if (VanishConfig.CONFIG.hidePlayersFromPlayerLists.get()) {
			PlayerList list = server.getPlayerList();
			GameProfile[] players = response.getPlayers().getSample();
			GameProfile[] newPlayersHelper = new GameProfile[players.length]; //this helper is needed to evaluate the right size for the actual array
			GameProfile[] newPlayers;
			int visiblePlayersCount = 0;

			for (GameProfile profile : players) {
				if (!VanishUtil.isVanished(list.getPlayer(profile.getId()))) {
					newPlayersHelper[visiblePlayersCount] = profile;
					visiblePlayersCount++;
				}
			}

			newPlayers = new GameProfile[visiblePlayersCount];

			for (int i = 0; i < newPlayersHelper.length; i++) {
				if (newPlayersHelper[i] != null)
					newPlayers[i] = newPlayersHelper[i];
			}

			response.getPlayers().setSample(newPlayers);
		}

		return new ClientboundStatusResponsePacket(response);
	}
}
