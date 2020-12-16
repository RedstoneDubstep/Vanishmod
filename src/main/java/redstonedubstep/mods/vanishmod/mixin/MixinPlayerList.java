package redstonedubstep.mods.vanishmod.mixin;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPlayerListItemPacket;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import redstonedubstep.mods.vanishmod.VanishUtil;

@Mixin(PlayerList.class)
public abstract class MixinPlayerList {
	//Remove vanished players from packet that is sent to all players on connect
	@Redirect(method="initializeConnectionToPlayer", at=@At(value="NEW", target="net/minecraft/network/play/server/SPlayerListItemPacket", ordinal=0))
	public SPlayerListItemPacket constructPacketToAll(SPlayerListItemPacket.Action actionIn, ServerPlayerEntity[] playersIn) {
		return new SPlayerListItemPacket(actionIn, VanishUtil.formatPlayerList(Arrays.asList(playersIn)));
	}

	//Remove vanished players from packets that are sent to the joining player on connect. Includes an extra check to ensure that the vanished player gets information about itself
	@Redirect(method="initializeConnectionToPlayer", at=@At(value="NEW", target="net/minecraft/network/play/server/SPlayerListItemPacket", ordinal=1))
	public SPlayerListItemPacket constructPacketToJoinedPlayer(SPlayerListItemPacket.Action actionIn, ServerPlayerEntity[] playersIn, NetworkManager netManager, ServerPlayerEntity receiver) {
		List<ServerPlayerEntity> list;

		if (VanishUtil.isVanished(receiver) && receiver.equals(playersIn[0])) {
			list=Arrays.asList(playersIn);
		} else {
			list=VanishUtil.formatPlayerList(Arrays.asList(playersIn));
		}

		return new SPlayerListItemPacket(actionIn, list);
	}

	//Remove vanished players from packet that is sent on disconnect
	@Redirect(method="playerLoggedOut", at=@At(value="NEW", target="net/minecraft/network/play/server/SPlayerListItemPacket"))
	public SPlayerListItemPacket constructPacketOnLeave(SPlayerListItemPacket.Action actionIn, ServerPlayerEntity... playersIn) {
		return new SPlayerListItemPacket(actionIn, VanishUtil.formatPlayerList(Arrays.asList(playersIn)));
	}

	//Block join message when player is vanished
	@Redirect(method="initializeConnectionToPlayer", at=@At(value="INVOKE", target="Lnet/minecraft/server/management/PlayerList;func_232641_a_(Lnet/minecraft/util/text/ITextComponent;Lnet/minecraft/util/text/ChatType;Ljava/util/UUID;)V"))
	public void redirectFunc_232641_a_(PlayerList playerList, ITextComponent content, ChatType chatType, UUID uuid, NetworkManager netManager, ServerPlayerEntity player) {
		if (!VanishUtil.isVanished(player))
			playerList.func_232641_a_(content, chatType, uuid);
		else
			player.sendMessage(new StringTextComponent("Note: You are still vanished"), player.getUniqueID());
	}
}
