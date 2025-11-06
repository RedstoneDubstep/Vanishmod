package redstonedubstep.mods.vanishmod.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.authlib.GameProfile;

import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.ChatType.Bound;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import redstonedubstep.mods.vanishmod.VanishConfig;
import redstonedubstep.mods.vanishmod.VanishUtil;
import redstonedubstep.mods.vanishmod.misc.TraceHandler;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {
	@Shadow
	@Final
	public MinecraftServer server;

	@Shadow
	public abstract void sendSystemMessage(Component component);

	//player entity needs a constructor, so here we go
	public ServerPlayerMixin(Level world, GameProfile gameProfile) {
		super(world, gameProfile);
	}

	//1. Suppresses chat and /teammsg messages from vanished to unvanished players
	//2. Changes other chat messages to system messages so unvanished clients don't disconnect when receiving these messages (due to the sender's UUID not being present there)
	//3. Conceals the vanished sender of a /say, /me or /msg message by replacing its name with "vanished"
	@Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
	public void vanishmod$onSendChatMessage(OutgoingChatMessage message, boolean filter, Bound chatType, CallbackInfo callback) {
		if (message instanceof OutgoingChatMessage.Player playerChatMessage) {
			Player sender = server.getPlayerList().getPlayer(playerChatMessage.message().link().sender());
			ResourceKey<ChatType> chatTypeKey = VanishUtil.getChatTypeRegistryKey(chatType, this);

			if (VanishUtil.isVanished(sender, this)) {
				if (!VanishConfig.CONFIG.hideChatMessages.get() || (chatTypeKey != ChatType.CHAT && chatTypeKey != ChatType.TEAM_MSG_COMMAND_INCOMING)) {
					if (VanishConfig.CONFIG.hidePlayerNameInChat.get()) {
						Component replacement = Component.literal(VanishConfig.CONFIG.vanishedPlayerNameReplacement.get());

						TraceHandler.trace(sender, "Chat Message Sender (now \"" + replacement.getString() + "\")", message.content().getString());
						chatType = ChatType.bind(chatTypeKey, level().registryAccess(), replacement);
					}

					sendSystemMessage(chatType.decorate(playerChatMessage.content()));
					callback.cancel();
					return;
				}

				TraceHandler.trace(sender, "Chat Message", message.content().getString());
				callback.cancel();
			}
		}
	}
}
