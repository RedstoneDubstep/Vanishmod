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

	//Prevent tab list information packet from being sent to other players if the player in the packet is vanished. Contains an extra check to ensure the joining/leaving player gets information about itself
	@Inject(method = "send(Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"), cancellable = true)
	private void onSendPacket(Packet<?> packet, CallbackInfo callbackInfo) {
		if (packet instanceof ClientboundPlayerInfoPacket infoPacket && (infoPacket.getAction() == Action.ADD_PLAYER || infoPacket.getAction() == Action.REMOVE_PLAYER)) {
			GameProfile sentPlayer = !infoPacket.getEntries().isEmpty() ? infoPacket.getEntries().get(0).getProfile() : null;

			if (sentPlayer != null && VanishUtil.isVanished(server.getPlayerList().getPlayer(sentPlayer.getId()), player)) {
				if (infoPacket.getAction() == Action.ADD_PLAYER && !player.getUUID().equals(sentPlayer.getId()))
					callbackInfo.cancel();
			}
		}
	}
}
