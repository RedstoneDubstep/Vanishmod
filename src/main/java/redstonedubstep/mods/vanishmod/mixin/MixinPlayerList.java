package redstonedubstep.mods.vanishmod.mixin;

import java.util.UUID;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.Connection;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraftforge.server.ServerLifecycleHooks;
import redstonedubstep.mods.vanishmod.VanishUtil;

@Mixin(value = PlayerList.class, priority = 900) //these mixin methods (especially the upper ones) are pretty important for this mod to work, so we set the priority of these mixins to be higher so no mixins clash because of same priorities
public abstract class MixinPlayerList {
	@Shadow
	public abstract ServerPlayer getPlayer(UUID p_11260_);

	@Unique
	private ServerPlayer joiningPlayer;

	//Prevent join, leave, death and advancement messages of vanished players from being broadcast
	@Inject(method = "broadcastMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/ChatType;Ljava/util/UUID;)V", at = @At(value = "HEAD"), cancellable = true)
	public void redirectBroadcastMessage(Component text, ChatType chatType, UUID uuid, CallbackInfo callbackInfo) {
		if (text instanceof TranslatableComponent component) {
			if (component.getKey().startsWith("multiplayer.player.joined") && VanishUtil.isVanished(joiningPlayer)) {
				joiningPlayer = null;
				callbackInfo.cancel();
			}
			else if (component.getKey().startsWith("multiplayer.player.left") || component.getKey().startsWith("death.") || component.getKey().startsWith("chat.type.advancement")) {
				if (component.getArgs()[0] instanceof Component playerName) {
					for (ServerPlayer player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
						if (player.getDisplayName().getString().equals(playerName.getString()) && VanishUtil.isVanished(player))
							callbackInfo.cancel();
					}
				}
			}
		}
	}

	//Notify vanished players that they are still vanished when they join the server
	@Inject(method = "placeNewPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/ChatType;Ljava/util/UUID;)V"))
	public void onSendJoinMessage(Connection networkManager, ServerPlayer player, CallbackInfo ci) {
		if (VanishUtil.isVanished(player)) {
			player.sendMessage(VanishUtil.VANISHMOD_PREFIX.copy().append("Note: You are still vanished"), player.getUUID());
		}

		joiningPlayer = player;
	}
}
