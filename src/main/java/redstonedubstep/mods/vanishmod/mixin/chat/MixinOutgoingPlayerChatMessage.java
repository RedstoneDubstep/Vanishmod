package redstonedubstep.mods.vanishmod.mixin.chat;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;
import redstonedubstep.mods.vanishmod.VanishUtil;

@Mixin(OutgoingChatMessage.Player.class)
public class MixinOutgoingPlayerChatMessage {
	@Shadow @Final private PlayerChatMessage message;

	//Suppresses chat messages by vanished players from being sent to players that can't see them
	@Inject(method = "sendToPlayer", at = @At(value = "HEAD"), cancellable = true)
	public void onSendToPlayer(ServerPlayer receiver, boolean overlay, ChatType.Bound chatType, CallbackInfo callbackInfo) {
		if (VanishUtil.getChatTypeRegistryKey(chatType, receiver) == ChatType.CHAT && VanishUtil.isVanished(receiver.server.getPlayerList().getPlayer(message.link().sender()), receiver))
			callbackInfo.cancel();
	}
}