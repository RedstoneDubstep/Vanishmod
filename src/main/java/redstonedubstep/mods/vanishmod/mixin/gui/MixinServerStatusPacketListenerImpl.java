package redstonedubstep.mods.vanishmod.mixin.gui;

import java.util.Arrays;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.mojang.authlib.GameProfile;

import net.minecraft.network.protocol.status.ClientboundStatusResponsePacket;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerStatusPacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import redstonedubstep.mods.vanishmod.VanishConfig;
import redstonedubstep.mods.vanishmod.VanishUtil;

@Mixin(ServerStatusPacketListenerImpl.class)
public abstract class MixinServerStatusPacketListenerImpl {
	@Shadow
	@Final
	private MinecraftServer server;

	//Stop server from sending the names of vanished players to the Multiplayer screen
	@Redirect(method = "handleStatusRequest", at = @At(value = "NEW", target = "net/minecraft/network/protocol/status/ClientboundStatusResponsePacket"))
	public ClientboundStatusResponsePacket constructSServerInfoPacket(ServerStatus response) {
		if (VanishConfig.CONFIG.hidePlayersFromPlayerLists.get()) {
			PlayerList list = server.getPlayerList();
			GameProfile[] players = response.getPlayers().getSample();

			if (players != null) {
				GameProfile[] newPlayers = Arrays.stream(players)
						.filter(p -> !VanishUtil.isVanished(list.getPlayer(p.getId())))
						.toArray(GameProfile[]::new);

				response.getPlayers().setSample(newPlayers);
			}
		}

		return new ClientboundStatusResponsePacket(response);
	}
}
