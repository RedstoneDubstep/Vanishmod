package redstonedubstep.mods.vanishmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import redstonedubstep.mods.vanishmod.VanishConfig;
import redstonedubstep.mods.vanishmod.VanishUtil;
import redstonedubstep.mods.vanishmod.misc.FieldHolder;
import redstonedubstep.mods.vanishmod.misc.SoundSuppressionHelper;

@Mixin(value = PlayerList.class)
public abstract class MixinPlayerList {
	//Vanishes any unvanished players that are on the vanishing queue. Also acts as a helper for accessing the player that is currently joining the server
	@Inject(method = "placeNewPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"))
	public void onSendJoinMessage(Connection networkManager, ServerPlayer player, CallbackInfo ci) {
		if (VanishUtil.removeFromQueue(player.getGameProfile().getName()) && !VanishUtil.isVanished(player))
			VanishUtil.toggleVanish(player);

		FieldHolder.joiningPlayer = player;
	}

	//Conceals the vanished sender of a message sent by the /say or /me command by putting a system chat sender with the name "vanished" as the sender
	@ModifyVariable(method = "broadcastChatMessage(Lnet/minecraft/network/chat/PlayerChatMessage;Lnet/minecraft/commands/CommandSourceStack;Lnet/minecraft/network/chat/ChatType$Bound;)V", at = @At(value = "HEAD"), argsOnly = true)
	public ChatType.Bound redirectAsChatSender(ChatType.Bound original, PlayerChatMessage chatMessage, CommandSourceStack commandSource) {
		if (VanishConfig.CONFIG.hidePlayerNameInChat.get() && commandSource.getEntity() instanceof ServerPlayer player && VanishUtil.isVanished(player))
			return ChatType.bind(VanishUtil.getChatTypeRegistryKey(original, player), player.level.registryAccess(), Component.literal("vanished").withStyle(ChatFormatting.GRAY));

		return original;
	}

	//Fixes clients that are not allowed to see vanished players disconnecting when receiving a message from vanished players due to the sender UUID not being present on these clients
	@ModifyVariable(method = "broadcastChatMessage(Lnet/minecraft/network/chat/PlayerChatMessage;Lnet/minecraft/commands/CommandSourceStack;Lnet/minecraft/network/chat/ChatType$Bound;)V", at = @At(value = "HEAD"), argsOnly = true)
	public PlayerChatMessage modifyChatSender(PlayerChatMessage original, PlayerChatMessage chatMessage, CommandSourceStack commandSource) {
		if (commandSource.getEntity() instanceof ServerPlayer player && VanishUtil.isVanished(player))
			return PlayerChatMessage.system(original.signedContent());

		return original;
	}

	//Stores the player that is exempted from broadcasting a given sound packet, which most likely is the one causing the packet to be sent, so the information can be used later for sound suppression
	@Inject(method = "broadcast", at = @At("HEAD"))
	public void onBroadcast(Player except, double x, double y, double z, double radius, ResourceKey<Level> dimension, Packet<?> packet, CallbackInfo callbackInfo) {
		if (VanishConfig.CONFIG.hidePlayersFromWorld.get() && except != null && (packet instanceof ClientboundSoundPacket || packet instanceof ClientboundSoundEntityPacket || packet instanceof ClientboundLevelEventPacket))
			SoundSuppressionHelper.putSoundPacket(packet, except);
	}
}
