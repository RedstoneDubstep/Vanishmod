package redstonedubstep.mods.vanishmod.mixin;

import java.util.Arrays;
import java.util.UUID;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPlayerListItemPacket;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import redstonedubstep.mods.vanishmod.VanishUtil;

@Mixin(PlayerList.class)
public abstract class MixinPlayerList {
	//remove vanished players from packet that is sent on connect
	@Redirect(method="initializeConnectionToPlayer", at=@At(value="NEW", target="net/minecraft/network/play/server/SPlayerListItemPacket", ordinal=0))
	public SPlayerListItemPacket constructPacketOnJoin(SPlayerListItemPacket.Action actionIn, ServerPlayerEntity... playersIn) {
		return new SPlayerListItemPacket(actionIn, VanishUtil.formatPlayerList(Arrays.asList(playersIn)));
	}

	//remove vanished players from packet that is sent on disconnect
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

	//Part 2 of a way to prevent sounds from vanished players being sent, other part is in MixinServerWorld#redirectSendToAlNearExcept
	@Inject(method="sendToAllNearExcept", at=@At(value="INVOKE_ASSIGN", target="Ljava/util/List;get(I)Ljava/lang/Object;"), cancellable=true, locals=LocalCapture.CAPTURE_FAILSOFT)
	private void onSendToAllNearExcept(PlayerEntity except, double x, double y, double z, double radius, RegistryKey<World> dimension, IPacket<?> packet, CallbackInfo info) {
		if (VanishUtil.isVanished((ServerPlayerEntity)except) && VanishUtil.isMixinInvolved) {
			VanishUtil.isMixinInvolved = false;
			info.cancel();
		}
	}
}
