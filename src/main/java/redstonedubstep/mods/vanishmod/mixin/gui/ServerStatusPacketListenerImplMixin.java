package redstonedubstep.mods.vanishmod.mixin.gui;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.mojang.authlib.GameProfile;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.network.protocol.status.ClientboundStatusResponsePacket;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerStatusPacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import net.minecraftforge.server.ServerLifecycleHooks;
import redstonedubstep.mods.vanishmod.VanishConfig;
import redstonedubstep.mods.vanishmod.VanishUtil;

@Mixin(ServerStatusPacketListenerImpl.class)
public class ServerStatusPacketListenerImplMixin {
	//Updates the player list sent to clients on the Multiplayer screen to only display and count unvanished players
	@Redirect(method = "handleStatusRequest", at = @At(value = "NEW", target = "(Lnet/minecraft/network/protocol/status/ServerStatus;Ljava/lang/String;)Lnet/minecraft/network/protocol/status/ClientboundStatusResponsePacket;"))
	public ClientboundStatusResponsePacket vanishmod$constructSServerInfoPacket(ServerStatus status, String cachedStatus) {
		if (VanishConfig.CONFIG.hidePlayersFromPlayerLists.get()) {
			MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
			PlayerList list = server.getPlayerList();
			List<ServerPlayer> unvanishedPlayers = VanishUtil.formatPlayerList(list.getPlayers(), null);
			int unvanishedPlayerCount = unvanishedPlayers.size();
			int maxPlayers = list.getMaxPlayers();
			ServerStatus.Players playerStatus;

			if (server.hidesOnlinePlayers())
				playerStatus = new ServerStatus.Players(maxPlayers, unvanishedPlayerCount, List.of());
			else {
				int playerSampleSize = Math.min(unvanishedPlayers.size(), 12);
				ObjectArrayList<GameProfile> newPlayers = new ObjectArrayList<>(playerSampleSize);
				int offset = ThreadLocalRandom.current().nextInt(0, unvanishedPlayerCount - playerSampleSize);

				for(int l = 0; l < playerSampleSize; ++l) {
					ServerPlayer player = unvanishedPlayers.get(offset + l);
					newPlayers.add(player.allowsListing() ? player.getGameProfile() : MinecraftServer.ANONYMOUS_PLAYER_PROFILE);
				}

				Collections.shuffle(newPlayers, ThreadLocalRandom.current());
				playerStatus = new ServerStatus.Players(maxPlayers, unvanishedPlayerCount, newPlayers);
			}

			status = new ServerStatus(status.description(), Optional.of(playerStatus), status.version(), status.favicon(), status.enforcesSecureChat(), status.forgeData());
			cachedStatus = null;
		}

		return new ClientboundStatusResponsePacket(status, cachedStatus);
	}
}
