package redstonedubstep.mods.vanishmod.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.authlib.GameProfile;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket.Action;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import redstonedubstep.mods.vanishmod.VanishUtil;

@Mixin(ServerGamePacketListenerImpl.class)
public class MixinServerGamePacketListenerImpl {
	@Shadow
	public ServerPlayer player;
	@Shadow @Final
	private MinecraftServer server;

	//Prevent tab list information packet from being sent to other players if the player in the packet is vanished for the receiver.
	//We don't filter latency packets, because they would break this method as there are multiple players in the "entries" argument of a latency update packet
	//We also don't filter player removal packets, because this mod uses them to remove vanished players after their status has changed to be vanished,
	//and it can be done safely because not suppressing these packets does not break this mod (in: a player removal packet sent too much wouldn't break this mod as much as a player addition packet)
	@Inject(method = "send(Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"), cancellable = true)
	private void onSendPacket(Packet<?> packet, CallbackInfo callbackInfo) {
		if (packet instanceof ClientboundPlayerInfoPacket infoPacket && infoPacket.getAction() != Action.UPDATE_LATENCY && infoPacket.getAction() != Action.REMOVE_PLAYER) {
			GameProfile sentPlayer = !infoPacket.getEntries().isEmpty() ? infoPacket.getEntries().get(0).getProfile() : null;

			if (sentPlayer != null) {
				if (VanishUtil.isVanished(server.getPlayerList().getPlayer(sentPlayer.getId()), player))
					callbackInfo.cancel();
			}
		}
	}
}
