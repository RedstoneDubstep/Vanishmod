package redstonedubstep.mods.vanishmod;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket.Action;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;
import redstonedubstep.mods.vanishmod.api.PlayerVanishEvent;
import redstonedubstep.mods.vanishmod.compat.Mc2DiscordCompat;

public class VanishUtil {
	public static List<ServerPlayer> formatPlayerList(List<ServerPlayer> rawList) {
		return rawList.stream().filter(player -> !isVanished(player)).collect(Collectors.toList());
	}

	public static void sendPacketsOnVanish(ServerPlayer currentPlayer, ServerLevel world, boolean vanished) {
		List<ServerPlayer> list = world.players();

		for (ServerPlayer player : list) {
			ServerChunkCache chunkProvider = player.getLevel().getChunkSource();

			if (!player.equals(currentPlayer)) { //prevent packet from being sent to the executor of the command
				player.connection.send(new ClientboundPlayerInfoPacket(vanished ? Action.REMOVE_PLAYER : Action.ADD_PLAYER, currentPlayer));
				if (!vanished) {
					chunkProvider.chunkMap.entityMap.remove(currentPlayer.getId()); //we don't want an error in our log because the entity to be tracked is already on that list
					chunkProvider.addEntity(currentPlayer);
				}
				else if (VanishConfig.CONFIG.hidePlayersFromWorld.get()) {
					player.connection.send(new ClientboundRemoveEntitiesPacket(currentPlayer.getId()));
				}
			}
		}
	}

	public static void sendJoinOrLeaveMessageToPlayers(List<ServerPlayer> playerList, ServerPlayer sender, boolean leaveMessage) {
		Component message = new TranslatableComponent(leaveMessage ? "multiplayer.player.left" : "multiplayer.player.joined", sender.getDisplayName()).withStyle(ChatFormatting.YELLOW);

		for (ServerPlayer receiver : playerList) {
			receiver.sendMessage(message, sender.getUUID());
		}

		if (ModList.get().isLoaded("mc2discord")) {
			Mc2DiscordCompat.sendPlayerStatusMessage(sender, leaveMessage);
		}
	}

	public static void updateVanishedStatus(ServerPlayer player, boolean vanished) {
		CompoundTag persistentData = player.getPersistentData();
		CompoundTag deathPersistentData = persistentData.getCompound(Player.PERSISTED_NBT_TAG);

		deathPersistentData.putBoolean("Vanished", vanished);
		persistentData.put(Player.PERSISTED_NBT_TAG, deathPersistentData); //Because the deathPersistentData could have been created newly by getCompound if it didn't exist before

		if (ModList.get().isLoaded("mc2discord")) {
			Mc2DiscordCompat.hidePlayer(player, vanished);
		}

		MinecraftForge.EVENT_BUS.post(new PlayerVanishEvent(player, vanished));
	}

	public static boolean isVanished(UUID uuid, ServerLevel world) {
		Entity entity = world.getEntity(uuid);

		if (entity instanceof Player) {
			return isVanished((Player)entity);
		}

		return false;
	}

	public static boolean isVanished(Player player) {
		if (player != null && !player.level.isClientSide) {
			CompoundTag deathPersistedData = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);

			return deathPersistedData.getBoolean("Vanished");
		}

		return false;
	}
}
