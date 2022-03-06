package redstonedubstep.mods.vanishmod.mixin;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.Connection;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraftforge.server.ServerLifecycleHooks;
import redstonedubstep.mods.vanishmod.VanishUtil;

@Mixin(PlayerList.class)
public abstract class MixinPlayerList {
	@Unique
	private ServerPlayer joiningPlayer;

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

	//Prevent join, leave, death and advancement messages of vanished players from being broadcast
	@Inject(method = "broadcastMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/ChatType;Ljava/util/UUID;)V", at = @At(value = "HEAD"), cancellable = true)
	public void redirectBroadcastMessage(Component text, ChatType chatType, UUID uuid, CallbackInfo callbackInfo) {
		if (text instanceof TranslatableComponent component) {
			if (component.getKey().startsWith("multiplayer.player.joined") && VanishUtil.isVanished(joiningPlayer)) {
				joiningPlayer = null;
				callbackInfo.cancel();
			}
			else if (component.getKey().startsWith("multiplayer.player.left") || component.getKey().startsWith("death.") || component.getKey().startsWith("chat.type.advancement")) {
				if (component.getArgs()[0] instanceof Component playerName) {
					for (ServerPlayer player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
						if (player.getDisplayName().getString().equals(playerName.getString()) && VanishUtil.isVanished(player))
							callbackInfo.cancel();
					}
				}
			}
		}
	}

	//Notify vanished players that they are still vanished when they join the server
	@Inject(method = "placeNewPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/ChatType;Ljava/util/UUID;)V"))
	public void onSendJoinMessage(Connection networkManager, ServerPlayer player, CallbackInfo ci) {
		if (VanishUtil.isVanished(player)) {
			player.sendMessage(new TextComponent("Note: You are still vanished"), player.getUUID());
		}

		joiningPlayer = player;
	}
}
