package redstonedubstep.mods.vanishmod.mixin.chat;

import java.util.UUID;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import redstonedubstep.mods.vanishmod.VanishUtil;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class MixinServerGamePacketListenerImpl implements ServerGamePacketListener {
	@Shadow
	public ServerPlayer player;

	//Block leave message when player is vanished
	@Redirect(method = "onDisconnect", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/ChatType;Ljava/util/UUID;)V"))
	public void redirectBroadcastMessage(PlayerList playerList, Component content, ChatType chatType, UUID uuid) {
		if (!VanishUtil.isVanished(player)) {
			playerList.broadcastMessage(content, chatType, uuid);
		}
	}
}
