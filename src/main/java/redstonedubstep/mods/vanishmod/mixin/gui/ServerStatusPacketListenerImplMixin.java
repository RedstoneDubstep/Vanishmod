package redstonedubstep.mods.vanishmod.mixin.gui;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.network.protocol.status.ClientboundStatusResponsePacket;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.server.network.ServerStatusPacketListenerImpl;
import redstonedubstep.mods.vanishmod.VanishConfig;
import redstonedubstep.mods.vanishmod.misc.FieldHolder;

@Mixin(ServerStatusPacketListenerImpl.class)
public class ServerStatusPacketListenerImplMixin {
	//Updates the player list sent to clients on the Multiplayer screen to only display and count unvanished players
	@WrapOperation(method = "handleStatusRequest", at = @At(value = "NEW", target = "(Lnet/minecraft/network/protocol/status/ServerStatus;Ljava/lang/String;)Lnet/minecraft/network/protocol/status/ClientboundStatusResponsePacket;"))
	public ClientboundStatusResponsePacket vanishmod$constructSServerInfoPacket(ServerStatus status, String cachedStatus, Operation<ClientboundStatusResponsePacket> original) {
		if (VanishConfig.CONFIG.hidePlayersFromPlayerLists.get() && FieldHolder.vanishedServerStatus != null) {
			status = FieldHolder.vanishedServerStatus;
			cachedStatus = null;
		}

		return original.call(status, cachedStatus);
	}
}
