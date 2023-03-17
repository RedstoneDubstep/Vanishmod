package redstonedubstep.mods.vanishmod.mixin.gui;

import java.util.List;
import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.mojang.authlib.GameProfile;

import net.minecraft.network.protocol.status.ClientboundStatusResponsePacket;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.network.protocol.status.ServerStatus.Players;
import net.minecraft.server.network.ServerStatusPacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import net.minecraftforge.server.ServerLifecycleHooks;
import redstonedubstep.mods.vanishmod.VanishConfig;
import redstonedubstep.mods.vanishmod.VanishUtil;

@Mixin(ServerStatusPacketListenerImpl.class)
public abstract class MixinServerStatusPacketListenerImpl {
	//Stop server from sending the names of vanished players to the Multiplayer screen
	@Redirect(method = "handleStatusRequest", at = @At(value = "NEW", target = "net/minecraft/network/protocol/status/ClientboundStatusResponsePacket"))
	public ClientboundStatusResponsePacket constructSServerInfoPacket(ServerStatus status, String cachedStatus) {
		if (VanishConfig.CONFIG.hidePlayersFromPlayerLists.get()) {
			PlayerList list = ServerLifecycleHooks.getCurrentServer().getPlayerList();
			Optional<Players> players = status.players();

			if (players.isPresent()) {
				List<GameProfile> gameProfiles = players.get().sample();

				List<GameProfile> newGameProfiles = gameProfiles.stream()
						.filter(p -> !VanishUtil.isVanished(list.getPlayer(p.getId())))
						.toList();

				status = new ServerStatus(status.description(), Optional.of(new Players(players.get().max(), newGameProfiles.size() , newGameProfiles)), status.version(), status.favicon(), status.enforcesSecureChat(), status.forgeData());
				cachedStatus = null;
			}
		}

		return new ClientboundStatusResponsePacket(status, cachedStatus);
	}
}
