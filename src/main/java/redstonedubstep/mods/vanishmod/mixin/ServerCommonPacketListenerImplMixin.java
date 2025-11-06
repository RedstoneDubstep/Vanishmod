package redstonedubstep.mods.vanishmod.mixin;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.Holder;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockEventPacket;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import redstonedubstep.mods.vanishmod.VanishConfig;
import redstonedubstep.mods.vanishmod.VanishUtil;
import redstonedubstep.mods.vanishmod.misc.FieldHolder;
import redstonedubstep.mods.vanishmod.misc.SoundSuppressionHelper;
import redstonedubstep.mods.vanishmod.misc.TraceHandler;

@Mixin(ServerCommonPacketListenerImpl.class)
public class ServerCommonPacketListenerImplMixin {
	@Shadow
	@Final
	protected MinecraftServer server;

	//Filter any packets that we wish to not send to players that cannot see vanished players, mainly consisting of player info and sound packets.
	//We don't filter player info removal packets, because this mod uses them to remove players after their status has changed to be vanished,
	//and it can be done safely because not suppressing these packets does not break this mod (in: a player removal packet sent too much wouldn't break this mod as much as a player addition packet)
	//We need to filter the item entity packets because otherwise all other clients think that they picked up an item (and thus show a pickup animation for the local player), while in reality a vanished player did
	@Inject(method = "send(Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"), cancellable = true)
	private void vanishmod$onSendPacket(Packet<?> packet, CallbackInfo callbackInfo) {
		if ((Object)this instanceof ServerGamePacketListenerImpl listener) {
			ServerPlayer receivingPlayer = listener.player;
			Level level = receivingPlayer.level();

			if (packet instanceof ClientboundPlayerInfoUpdatePacket infoPacket) {
				List<ClientboundPlayerInfoUpdatePacket.Entry> filteredPacketEntries = infoPacket.entries().stream().filter(e -> !VanishUtil.isVanished(server.getPlayerList().getPlayer(e.profileId()), receivingPlayer)).toList();

				if (filteredPacketEntries.isEmpty())
					callbackInfo.cancel();
				else if (!filteredPacketEntries.equals(infoPacket.entries()))
					infoPacket.entries = filteredPacketEntries;
			}
			else if (packet instanceof ClientboundTakeItemEntityPacket pickupPacket && level.getEntity(pickupPacket.getPlayerId()) instanceof ServerPlayer pickUppingPlayer && VanishUtil.isVanished(pickUppingPlayer, receivingPlayer)) {
				TraceHandler.trace(pickUppingPlayer, "Pickup Animation", pickupPacket.getItemId() + "x" + pickupPacket.getAmount());
				callbackInfo.cancel();
			}
			else if (VanishConfig.CONFIG.hidePlayersFromWorld.get()) {
				PlayerList playerList = receivingPlayer.server.getPlayerList();
				Holder<SoundEvent> suppressedSound = null;
				Player vanishedIndirectCause = null;

				if (packet instanceof ClientboundSoundPacket soundPacket) {
					vanishedIndirectCause = SoundSuppressionHelper.getIndirectVanishedSoundCause(SoundSuppressionHelper.getPlayerForPacket(soundPacket, playerList), level, soundPacket.getX(), soundPacket.getY(), soundPacket.getZ(), receivingPlayer);

					if (vanishedIndirectCause != null)
						suppressedSound = soundPacket.getSound();
				}
				else if (packet instanceof ClientboundSoundEntityPacket soundPacket) {
					vanishedIndirectCause = SoundSuppressionHelper.getIndirectVanishedSoundCause(SoundSuppressionHelper.getPlayerForPacket(soundPacket, playerList), level, level.getEntity(soundPacket.getId()), receivingPlayer);

					if (vanishedIndirectCause != null)
						suppressedSound = soundPacket.getSound();
				}
				else if (packet instanceof ClientboundLevelEventPacket soundPacket) {
					vanishedIndirectCause = SoundSuppressionHelper.getIndirectVanishedSoundCause(SoundSuppressionHelper.getPlayerForPacket(soundPacket, playerList), level, Vec3.atCenterOf(soundPacket.getPos()), receivingPlayer);

					if (vanishedIndirectCause != null) {
						TraceHandler.trace(vanishedIndirectCause, "Level Event", soundPacket.getType() + "/" + soundPacket.getData());
						callbackInfo.cancel();
					}
				}
				else if (packet instanceof ClientboundBlockEventPacket eventPacket) {
					vanishedIndirectCause = SoundSuppressionHelper.getIndirectVanishedSoundCause(null, level, Vec3.atCenterOf(eventPacket.getPos()), receivingPlayer);

					if (vanishedIndirectCause != null) {
						TraceHandler.trace(vanishedIndirectCause, "Block Event", eventPacket.getBlock().getName().getString() + "/" + eventPacket.getB0() + "/" + eventPacket.getB1());
						callbackInfo.cancel();
					}
				}
				else if (packet instanceof ClientboundLevelParticlesPacket particlesPacket){
					vanishedIndirectCause = SoundSuppressionHelper.getIndirectVanishedParticleCause(null, level, particlesPacket.getX(), particlesPacket.getY(), particlesPacket.getZ(), receivingPlayer);

					if (vanishedIndirectCause != null) {
						TraceHandler.trace(vanishedIndirectCause, "Particle", particlesPacket.getParticle().getClass().getSimpleName());
						callbackInfo.cancel();
					}
				}

				if (suppressedSound != null) {
					TraceHandler.trace(vanishedIndirectCause, "Sound", suppressedSound.value().getLocation().toString());
					callbackInfo.cancel();
				}
			}
		}
	}

	//Prevents vanilla join, leave, death, advancement and command feedback messages of vanished players from being broadcast.
	//Also removes all translation component messages (except for chat and /msg messages) with vanished player references when relevant config is enabled
	@Inject(method = "send(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketSendListener;)V", at = @At("HEAD"), cancellable = true)
	private void vanishmod$onSendPacket(Packet<?> packet, PacketSendListener sendListener, CallbackInfo callbackInfo) {
		if ((Object)this instanceof ServerGamePacketListenerImpl listener && packet instanceof ClientboundSystemChatPacket chatPacket && chatPacket.content() instanceof MutableComponent component && component.getContents() instanceof TranslatableContents content) {
			ServerPlayer player = listener.player;
			List<ServerPlayer> vanishedPlayers = new ArrayList<>(ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers().stream().filter(p -> VanishUtil.isVanished(p, player)).toList());
			String key = content.getKey();
			boolean joiningPlayerVanished = VanishUtil.isVanished(FieldHolder.joiningPlayer, player);

			if (joiningPlayerVanished)
				vanishedPlayers.add(FieldHolder.joiningPlayer);

			if (VanishUtil.isVanished(FieldHolder.leavingPlayer, player))
				vanishedPlayers.add(FieldHolder.leavingPlayer);

			if (VanishConfig.CONFIG.hideSystemMessages.get() || VanishConfig.CONFIG.hidePlayerNameInSystemMessages.get()) {
				ServerPlayer vanishedSender = null;
				Object[] args = content.getArgs();

				if (key.startsWith("multiplayer.player.joined") && joiningPlayerVanished)
					vanishedSender = FieldHolder.joiningPlayer;
				else if (key.startsWith("multiplayer.player.left") || key.startsWith("death.") || key.startsWith("chat.type.advancement.") || key.startsWith("chat.type.admin")) {
					if (args[0] instanceof Component playerName) {
						for (ServerPlayer sender : vanishedPlayers) {
							if (sender.getDisplayName().getString().equals(playerName.getString())) {
								vanishedSender = sender;
								break;
							}
						}
					}
				}

				if (vanishedSender != null) {
					if (VanishConfig.CONFIG.hideSystemMessages.get()) {
						TraceHandler.trace(vanishedSender, "Announcement", component.getString());
						callbackInfo.cancel();
					}
					else if (VanishConfig.CONFIG.hidePlayerNameInSystemMessages.get()) {
						Component replacement = Component.literal(VanishConfig.CONFIG.vanishedPlayerNameReplacement.get());

						TraceHandler.trace(vanishedSender, "Player Name (now \"" + replacement.getString() + "\")", component.getString());
						args[0] = replacement;
					}
				}
			}
			else if (VanishConfig.CONFIG.removeModdedSystemMessageReferences.get() && !key.startsWith("commands.message.display.incoming") && !key.startsWith("chat.type.")) {
				for (Object arg : content.getArgs()) {
					if (arg instanceof Component componentArg) {
						String potentialPlayerName = componentArg.getString();

						for (ServerPlayer vanishedPlayer : vanishedPlayers) {
							if (vanishedPlayer.getDisplayName().getString().equals(potentialPlayerName)) {
								TraceHandler.trace(vanishedPlayer, "Mentioning Message", component.getString());
								callbackInfo.cancel();
								return;
							}
						}
					}
				}
			}
		}
	}
}
