package redstonedubstep.mods.vanishmod.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.authlib.GameProfile;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
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

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {
	@Shadow
	@Final
	public MinecraftServer server;

	@Shadow
	public abstract void sendSystemMessage(Component component);

	//player entity needs a constructor, so here we go
	public ServerPlayerMixin(Level world, BlockPos pos, float angle, GameProfile gameProfile) {
		super(world, pos, angle, gameProfile);
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
				if (VanishConfig.CONFIG.hidePlayerNameInChat.get())
					chatType = ChatType.bind(chatTypeKey, level.registryAccess(), Component.literal("vanished").withStyle(ChatFormatting.GRAY));

				if (chatTypeKey != ChatType.CHAT && chatTypeKey != ChatType.TEAM_MSG_COMMAND_INCOMING)
					sendSystemMessage(chatType.decorate(playerChatMessage.content()));

				callback.cancel();
			}
		}
	}

	//Hacky mixin that should improve mod compat: mods should always respect spectator mode when targeting players, and this mixin lets isSpectator also check if the player is vanished (and thus should also not be targeted); but don't interfere with Vanilla's isSpectator() calls, else weird glitches can happen
	@Inject(method = "isSpectator", at = @At("HEAD"), cancellable = true)
	public void vanishmod$onIsSpectator(CallbackInfoReturnable<Boolean> callback) {
		if (VanishConfig.CONFIG.fixPlayerDetectionModCompatibility.get() && VanishUtil.isVanished(this)) {
			String callerClassName = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(frames -> frames.skip(2).findFirst().map(f -> f.getDeclaringClass().getPackageName()).orElse("")); //0 is this mixin, 1 is isSpectator(), 2 is the caller of isSpectator()

			if (!callerClassName.isEmpty() && !callerClassName.startsWith("net.minecraft.")) //if a mod calls this on a vanished player, then it is a spectator and should not be targeted
				callback.setReturnValue(true);
		}
	}
}
