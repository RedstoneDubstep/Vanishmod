package redstonedubstep.mods.vanishmod.mixin.gui;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.network.protocol.status.ClientboundStatusResponsePacket;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.server.network.ServerStatusPacketListenerImpl;
import redstonedubstep.mods.vanishmod.VanishConfig;
import redstonedubstep.mods.vanishmod.misc.FieldHolder;

@Mixin(ServerStatusPacketListenerImpl.class)
public class ServerStatusPacketListenerImplMixin {
	//Updates the player list sent to clients on the Multiplayer screen to only display/count unvanished players
	@Redirect(method = "handleStatusRequest", at = @At(value = "NEW", target = "(Lnet/minecraft/network/protocol/status/ServerStatus;)Lnet/minecraft/network/protocol/status/ClientboundStatusResponsePacket;"))
	public ClientboundStatusResponsePacket vanishmod$constructSServerInfoPacket(ServerStatus status) {
		if (VanishConfig.CONFIG.hidePlayersFromPlayerLists.get() && FieldHolder.VANISHED_SERVER_STATUS.getPlayers() != null)
			status = FieldHolder.VANISHED_SERVER_STATUS;

		return new ClientboundStatusResponsePacket(status);
	}
}
