package redstonedubstep.mods.vanishmod.mixin;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPlayerListItemPacket;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import redstonedubstep.mods.vanishmod.VanishUtil;

@Mixin(PlayerList.class)
public abstract class MixinPlayerList {
	@Unique
	private ServerPlayerEntity joiningPlayer;

	//Prevent join, leave, death and advancement messages of vanished players from being broadcast
	@Inject(method = "broadcastMessage", at = @At(value = "HEAD"), cancellable = true)
	public void redirectBroadcastMessage(ITextComponent text, ChatType chatType, UUID uuid, CallbackInfo callbackInfo) {
		if (text instanceof TranslationTextComponent) {
			TranslationTextComponent component = ((TranslationTextComponent)text);

			if (component.getKey().startsWith("multiplayer.player.joined") && VanishUtil.isVanished(joiningPlayer)) {
				joiningPlayer = null;
				callbackInfo.cancel();
			}
			else if (component.getKey().startsWith("multiplayer.player.left") || component.getKey().startsWith("death.") || component.getKey().startsWith("chat.type.advancement")) {
				if (component.getArgs()[0] instanceof StringTextComponent) {
					for (ServerPlayerEntity player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
						if (player.getDisplayName().getString().equals(((StringTextComponent)component.getArgs()[0]).getString()) && VanishUtil.isVanished(player))
							callbackInfo.cancel();
					}
				}
			}
		}
	}

	//Notify vanished players that they are still vanished when they join the server
	@Inject(method = "placeNewPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/management/PlayerList;broadcastMessage(Lnet/minecraft/util/text/ITextComponent;Lnet/minecraft/util/text/ChatType;Ljava/util/UUID;)V"))
	public void onSendJoinMessage(NetworkManager networkManager, ServerPlayerEntity player, CallbackInfo ci) {
		if (VanishUtil.isVanished(player)) {
			player.sendMessage(VanishUtil.VANISHMOD_PREFIX.copy().append("Note: You are still vanished"), player.getUUID());
		}

		joiningPlayer = player;
	}
}
