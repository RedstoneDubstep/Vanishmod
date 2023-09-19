package redstonedubstep.mods.vanishmod.mixin;

import java.util.UUID;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.authlib.GameProfile;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
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

	//player entity needs a constructor, so here we go
	public ServerPlayerMixin(Level world, BlockPos pos, float f, GameProfile gameProfile) {
		super(world, pos, f, gameProfile);
	}

	//Suppresses chat and /teammsg messages by vanished players from being sent to players that can't see them
	@Inject(method = "sendMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/ChatType;Ljava/util/UUID;)V", at = @At("HEAD"), cancellable = true)
	public void vanishmod$onPlayerChatMessage(Component message, ChatType chatType, UUID sender, CallbackInfo callbackInfo) {
		if (message instanceof TranslatableComponent component && VanishUtil.isVanished(server.getPlayerList().getPlayer(sender), this)) {
			if (chatType == ChatType.CHAT && component.getKey().contains("chat.type.text"))
				callbackInfo.cancel();
			else if (chatType == ChatType.SYSTEM && component.getKey().contains("chat.type.team.text"))
				callbackInfo.cancel();
		}
	}

	//Conceals the vanished sender of a /say, /me or /msg message by replacing its name with "vanished"
	@ModifyVariable(method = "sendMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/ChatType;Ljava/util/UUID;)V", at = @At("HEAD"), argsOnly = true)
	public Component vanishmod$modifyPlayerChatMessage(Component original, Component original2, ChatType chatType, UUID sender) {
		if (VanishConfig.CONFIG.hidePlayerNameInChat.get() && original instanceof TranslatableComponent component && VanishUtil.isVanished(server.getPlayerList().getPlayer(sender), this)) {
			Component vanished = new TextComponent("vanished").withStyle(ChatFormatting.GRAY);

			if (component.getKey().contains("chat.type.announcement"))
				original = new TranslatableComponent("chat.type.announcement", vanished, component.getArgs()[1]).withStyle(original.getStyle());
			else if (component.getKey().contains("chat.type.emote"))
				original = new TranslatableComponent("chat.type.emote", vanished, component.getArgs()[1]).withStyle(original.getStyle());
			else if (component.getKey().contains("commands.message.display.incoming"))
				original = new TranslatableComponent("commands.message.display.incoming", vanished, component.getArgs()[1]).withStyle(original.getStyle());
		}

		return original;
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
