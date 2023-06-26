package redstonedubstep.mods.vanishmod.mixin.gui;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.mojang.authlib.GameProfile;

import net.minecraft.network.protocol.status.ClientboundStatusResponsePacket;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerStatusPacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.Mth;
import redstonedubstep.mods.vanishmod.VanishConfig;
import redstonedubstep.mods.vanishmod.VanishUtil;

@Mixin(ServerStatusPacketListenerImpl.class)
public class ServerStatusPacketListenerImplMixin {
	@Shadow
	@Final
	private MinecraftServer server;

	//Updates the player list sent to clients on the Multiplayer screen to only display/count unvanished players
	@Redirect(method = "handleStatusRequest", at = @At(value = "NEW", target = "(Lnet/minecraft/network/protocol/status/ServerStatus;)Lnet/minecraft/network/protocol/status/ClientboundStatusResponsePacket;"))
	public ClientboundStatusResponsePacket vanishmod$constructSServerInfoPacket(ServerStatus status) {
		if (VanishConfig.CONFIG.hidePlayersFromPlayerLists.get()) {
			PlayerList list = server.getPlayerList();
			List<ServerPlayer> unvanishedPlayers = VanishUtil.formatPlayerList(list.getPlayers(), null);
			int unvanishedPlayerCount = unvanishedPlayers.size();
			ServerStatus.Players playerStatus = new ServerStatus.Players(list.getMaxPlayers(), unvanishedPlayerCount);

			if (!server.hidesOnlinePlayers()) {
				GameProfile[] newPlayers = new GameProfile[Math.min(unvanishedPlayerCount, 12)];
				int offset = Mth.nextInt(server.overworld().random, 0, unvanishedPlayerCount - newPlayers.length);

				for(int i = 0; i < newPlayers.length; ++i) {
					ServerPlayer player = unvanishedPlayers.get(offset + i);

					if (player.allowsListing())
						newPlayers[i] = player.getGameProfile();
					else
						newPlayers[i] = MinecraftServer.ANONYMOUS_PLAYER_PROFILE;
				}

				Collections.shuffle(Arrays.asList(newPlayers));
				playerStatus.setSample(newPlayers);
				status.setPlayers(playerStatus);
			}
		}

		return new ClientboundStatusResponsePacket(status);
	}
}
