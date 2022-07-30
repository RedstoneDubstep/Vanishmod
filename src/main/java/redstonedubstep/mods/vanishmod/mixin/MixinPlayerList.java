package redstonedubstep.mods.vanishmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraftforge.server.ServerLifecycleHooks;
import redstonedubstep.mods.vanishmod.VanishConfig;
import redstonedubstep.mods.vanishmod.VanishUtil;

@Mixin(value = PlayerList.class)
public abstract class MixinPlayerList {
	@Unique
	private ServerPlayer joiningPlayer;

	//Prevent join, leave, death and advancement messages of vanished players from being broadcast
	@Inject(method = "broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V", at = @At(value = "HEAD"), cancellable = true)
	public void onBroadcastSystemMessage(Component text, boolean overlay, CallbackInfo callbackInfo) {
		if (text instanceof MutableComponent component && component.getContents() instanceof TranslatableContents content) {
			if (content.getKey().startsWith("multiplayer.player.joined") && VanishUtil.isVanished(joiningPlayer)) {
				joiningPlayer = null;
				callbackInfo.cancel();
			}
			else if (content.getKey().startsWith("multiplayer.player.left") || content.getKey().startsWith("death.") || content.getKey().startsWith("chat.type.advancement")) {
				if (content.getArgs()[0] instanceof Component playerName) {
					for (ServerPlayer player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
						if (player.getDisplayName().getString().equals(playerName.getString()) && VanishUtil.isVanished(player))
							callbackInfo.cancel();
					}
				}
			}
		}
	}

	//Vanishes any unvanished players that are on the vanishing queue. Also acts as a helper for accessing the player in question in the method above, as you cannot get it from PlayerList#broadcastSystemMessage
	@Inject(method = "placeNewPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"))
	public void onSendJoinMessage(Connection networkManager, ServerPlayer player, CallbackInfo ci) {
		if (VanishUtil.removeFromQueue(player.getGameProfile().getName()) && !VanishUtil.isVanished(player))
			VanishUtil.toggleVanish(player);

		joiningPlayer = player;
	}

	//Conceals the vanished sender of a message sent by the /say or /me command by putting a system chat sender with the name "vanished" as the sender
	@ModifyVariable(method = "broadcastChatMessage(Lnet/minecraft/network/chat/PlayerChatMessage;Lnet/minecraft/commands/CommandSourceStack;Lnet/minecraft/network/chat/ChatType$Bound;)V", at = @At(value = "HEAD"), argsOnly = true)
	public ChatType.Bound redirectAsChatSender(ChatType.Bound original, PlayerChatMessage chatMessage, CommandSourceStack commandSource) {
		if (VanishConfig.CONFIG.hidePlayerNameInChat.get() && commandSource.getEntity() instanceof ServerPlayer player && VanishUtil.isVanished(player))
			return ChatType.bind(VanishUtil.getChatTypeRegistryKey(original, player), player.level.registryAccess(), Component.literal("vanished").withStyle(ChatFormatting.GRAY));

		return original;
	}
}
