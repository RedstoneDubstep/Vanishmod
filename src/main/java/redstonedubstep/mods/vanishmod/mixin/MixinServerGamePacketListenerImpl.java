package redstonedubstep.mods.vanishmod.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket.Action;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket.PlayerUpdate;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.phys.Vec3;
import redstonedubstep.mods.vanishmod.VanishConfig;
import redstonedubstep.mods.vanishmod.VanishUtil;
import redstonedubstep.mods.vanishmod.misc.SoundSuppressionHelper;

@Mixin(ServerGamePacketListenerImpl.class)
public class MixinServerGamePacketListenerImpl {
	@Shadow
	public ServerPlayer player;
	@Shadow @Final
	private MinecraftServer server;

	//Filter any packets that we wish to not send to certain clients, mainly consisting of player info and sound packets.
	//We don't filter player info removal packets, because this mod uses them to remove players after their status has changed to be vanished,
	//and it can be done safely because not suppressing these packets does not break this mod (in: a player removal packet sent too much wouldn't break this mod as much as a player addition packet)
	@Inject(method = "send(Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"), cancellable = true)
	private void onSendPacket(Packet<?> packet, CallbackInfo callbackInfo) {
		if (packet instanceof ClientboundPlayerInfoPacket infoPacket && infoPacket.getAction() != Action.REMOVE_PLAYER) {
			List<PlayerUpdate> vanishedPacketEntries = infoPacket.getEntries().stream().filter(p -> VanishUtil.isVanished(server.getPlayerList().getPlayer(p.getProfile().getId()), player)).toList();

			if (vanishedPacketEntries.equals(infoPacket.getEntries()))
				callbackInfo.cancel();
			else if (!vanishedPacketEntries.isEmpty())
				infoPacket.getEntries().removeAll(vanishedPacketEntries);
		}
		else if (VanishConfig.CONFIG.hidePlayersFromWorld.get()) {
			if (packet instanceof ClientboundSoundPacket soundPacket && SoundSuppressionHelper.shouldSuppressSoundEventFor(SoundSuppressionHelper.getPlayerForPacket(soundPacket), player.level, soundPacket.getX(), soundPacket.getY(), soundPacket.getZ(), player))
				callbackInfo.cancel();
			else if (packet instanceof ClientboundSoundEntityPacket soundPacket && SoundSuppressionHelper.shouldSuppressSoundEventFor(SoundSuppressionHelper.getPlayerForPacket(soundPacket), player.level, player.level.getEntity(soundPacket.getId()), player))
				callbackInfo.cancel();
			else if (packet instanceof ClientboundLevelEventPacket soundPacket && SoundSuppressionHelper.shouldSuppressSoundEventFor(SoundSuppressionHelper.getPlayerForPacket(soundPacket), player.level, Vec3.atCenterOf(soundPacket.getPos()), player))
				callbackInfo.cancel();
		}
	}
}
