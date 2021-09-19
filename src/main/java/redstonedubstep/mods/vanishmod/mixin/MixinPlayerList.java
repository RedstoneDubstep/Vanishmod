package redstonedubstep.mods.vanishmod.mixin;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.server.players.PlayerList;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import redstonedubstep.mods.vanishmod.VanishUtil;

@Mixin(PlayerList.class)
public abstract class MixinPlayerList {
	//Remove vanished players from tab list information packet that is sent to all players on connect
	@Redirect(method = "placeNewPlayer", at = @At(value = "NEW", target = "net/minecraft/network/protocol/game/ClientboundPlayerInfoPacket", ordinal = 0))
	public ClientboundPlayerInfoPacket constructPacketToAll(ClientboundPlayerInfoPacket.Action actionIn, ServerPlayer[] playersIn) {
		return new ClientboundPlayerInfoPacket(actionIn, VanishUtil.formatPlayerList(Arrays.asList(playersIn)));
	}

	//Remove vanished players from tab list information packets that are sent to the joining player on connect. Includes an extra check to ensure that the vanished player gets information about itself
	@Redirect(method = "placeNewPlayer", at = @At(value = "NEW", target = "net/minecraft/network/protocol/game/ClientboundPlayerInfoPacket", ordinal = 1))
	public ClientboundPlayerInfoPacket constructPacketToJoinedPlayer(ClientboundPlayerInfoPacket.Action actionIn, ServerPlayer[] playersIn, Connection netManager, ServerPlayer receiver) {
		List<ServerPlayer> list = Arrays.asList(playersIn);

		if (!VanishUtil.isVanished(receiver) || !receiver.equals(playersIn[0])) {
			list = VanishUtil.formatPlayerList(list);
		}

		return new ClientboundPlayerInfoPacket(actionIn, list);
	}

	//Remove vanished players from tab list information packet that is sent on disconnect
	@Redirect(method = "remove", at = @At(value = "NEW", target = "net/minecraft/network/protocol/game/ClientboundPlayerInfoPacket"))
	public ClientboundPlayerInfoPacket constructPacketOnLeave(ClientboundPlayerInfoPacket.Action actionIn, ServerPlayer... playersIn) {
		return new ClientboundPlayerInfoPacket(actionIn, VanishUtil.formatPlayerList(Arrays.asList(playersIn)));
	}

	//Block join message when player is vanished and notifies vanished player that it is still vanished
	@Redirect(method = "placeNewPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/ChatType;Ljava/util/UUID;)V"))
	public void redirectBroadcastMessage(PlayerList playerList, Component content, ChatType chatType, UUID uuid, Connection netManager, ServerPlayer player) {
		if (!VanishUtil.isVanished(player))
			playerList.broadcastMessage(content, chatType, uuid);
		else
			player.sendMessage(new TextComponent("Note: You are still vanished"), player.getUUID());
	}
}
