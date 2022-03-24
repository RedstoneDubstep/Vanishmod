package redstonedubstep.mods.vanishmod.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.authlib.GameProfile;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.network.play.server.SPlayerListItemPacket;
import net.minecraft.network.play.server.SPlayerListItemPacket.Action;
import net.minecraft.server.MinecraftServer;
import redstonedubstep.mods.vanishmod.VanishUtil;

@Mixin(ServerPlayNetHandler.class)
public class MixinServerPlayNetHandler {
	@Shadow
	public ServerPlayerEntity player;
	@Shadow @Final
	private MinecraftServer server;

	//Prevent tab list information packet from being sent to other players if the player in the packet is vanished for the receiver.
	//We don't filter latency packets, because they would break this method as there are multiple players in the "entries" argument of a latency update packet
	//We also don't filter player removal packets, because this mod uses them to remove vanished players after their status has changed to be vanished,
	//and it can be done safely because not suppressing these packets does not break this mod (in: a player removal packet sent too much wouldn't break this mod as much as a player addition packet)
	@Inject(method = "send(Lnet/minecraft/network/IPacket;)V", at = @At("HEAD"), cancellable = true)
	private void onSendPacket(IPacket<?> packet, CallbackInfo callbackInfo) {
		if (packet instanceof SPlayerListItemPacket) {
			SPlayerListItemPacket infoPacket = (SPlayerListItemPacket)packet;

			if (infoPacket.action != Action.UPDATE_LATENCY && infoPacket.action != Action.REMOVE_PLAYER) {
				GameProfile sentPlayer = !infoPacket.entries.isEmpty() ? infoPacket.entries.get(0).getProfile() : null;

				if (sentPlayer != null) {
					if (VanishUtil.isVanished(server.getPlayerList().getPlayer(sentPlayer.getId()), player))
						callbackInfo.cancel();
				}
			}
		}
	}
}
