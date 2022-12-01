package redstonedubstep.mods.vanishmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.Connection;
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
	@Inject(method = "placeNewPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/ChatType;Ljava/util/UUID;)V"))
	public void onSendJoinMessage(Connection networkManager, ServerPlayer player, CallbackInfo ci) {
		if (VanishUtil.removeFromQueue(player.getGameProfile().getName()) && !VanishUtil.isVanished(player))
			VanishUtil.toggleVanish(player);

		FieldHolder.joiningPlayer = player;
	}

	//Stores the player that is exempted from broadcasting a given sound packet, which most likely is the one causing the packet to be sent, so the information can be used later for sound suppression
	@Inject(method = "broadcast", at = @At("HEAD"))
	public void onBroadcast(Player except, double x, double y, double z, double radius, ResourceKey<Level> dimension, Packet<?> packet, CallbackInfo callbackInfo) {
		if (VanishConfig.CONFIG.hidePlayersFromWorld.get() && except != null && (packet instanceof ClientboundSoundPacket || packet instanceof ClientboundSoundEntityPacket || packet instanceof ClientboundLevelEventPacket))
			SoundSuppressionHelper.putSoundPacket(packet, except);
	}
}
