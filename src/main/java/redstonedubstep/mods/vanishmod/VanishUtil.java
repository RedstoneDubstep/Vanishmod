package redstonedubstep.mods.vanishmod;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.play.server.SDestroyEntitiesPacket;
import net.minecraft.network.play.server.SPlayerListItemPacket;
import net.minecraft.network.play.server.SPlayerListItemPacket.Action;
import net.minecraft.network.play.server.STitlePacket;
import net.minecraft.network.play.server.STitlePacket.Type;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;
import redstonedubstep.mods.vanishmod.api.PlayerVanishEvent;
import redstonedubstep.mods.vanishmod.compat.Mc2DiscordCompat;

public class VanishUtil {
	public static List<ServerPlayerEntity> formatPlayerList(List<ServerPlayerEntity> rawList) {
		return rawList.stream().filter(player -> !isVanished(player)).collect(Collectors.toList());
	}

	public static void sendPacketsOnVanish(ServerPlayerEntity currentPlayer, ServerWorld world, boolean vanished) {
		List<ServerPlayerEntity> list = world.players();

		for (ServerPlayerEntity player : list) {
			ServerChunkProvider chunkProvider = player.getLevel().getChunkSource();

			if (!player.equals(currentPlayer)) { //prevent packet from being sent to the executor of the command
				player.connection.send(new SPlayerListItemPacket(vanished ? Action.REMOVE_PLAYER : Action.ADD_PLAYER, currentPlayer));
				if (!vanished) {
					chunkProvider.chunkMap.entityMap.remove(currentPlayer.getId()); //we don't want an error in our log because the entity to be tracked is already on that list
					chunkProvider.addEntity(currentPlayer);
				}
				else if (VanishConfig.CONFIG.hidePlayersFromWorld.get()) {
					player.connection.send(new SDestroyEntitiesPacket(currentPlayer.getId()));
				}
			}
		}

		currentPlayer.connection.send(new STitlePacket(Type.ACTIONBAR, VanishUtil.getVanishedStatusText(currentPlayer)));
	}

	public static void sendJoinOrLeaveMessageToPlayers(List<ServerPlayerEntity> playerList, ServerPlayerEntity sender, boolean leaveMessage) {
		if (VanishConfig.CONFIG.sendJoinLeaveMessages.get()) {
			IFormattableTextComponent message = new TranslationTextComponent(leaveMessage ? "multiplayer.player.left" : "multiplayer.player.joined", sender.getDisplayName()).withStyle(TextFormatting.YELLOW);

			for (ServerPlayerEntity receiver : playerList) {
				receiver.sendMessage(message, sender.getUUID());
			}

			if (ModList.get().isLoaded("mc2discord"))
				Mc2DiscordCompat.sendPlayerStatusMessage(sender, leaveMessage);
		}
	}

	public static void updateVanishedStatus(ServerPlayerEntity player, boolean vanished) {
		CompoundNBT persistentData = player.getPersistentData();
		CompoundNBT deathPersistentData = persistentData.getCompound(PlayerEntity.PERSISTED_NBT_TAG);

		deathPersistentData.putBoolean("Vanished", vanished);
		persistentData.put(PlayerEntity.PERSISTED_NBT_TAG, deathPersistentData); //Because the deathPersistentData could have been created newly by getCompound if it didn't exist before

		if (ModList.get().isLoaded("mc2discord")) {
			Mc2DiscordCompat.hidePlayer(player, vanished);
		}

		MinecraftForge.EVENT_BUS.post(new PlayerVanishEvent(player, vanished));
	}

	public static TranslationTextComponent getVanishedStatusText(ServerPlayerEntity player) {
		return new TranslationTextComponent(VanishUtil.isVanished(player) ? VanishConfig.CONFIG.onVanishQuery.get() : VanishConfig.CONFIG.onUnvanishQuery.get(), player.getDisplayName());
	}

	public static boolean isVanished(UUID uuid, ServerWorld world) {
		Entity entity = world.getEntity(uuid);

		if (entity instanceof PlayerEntity)
			return isVanished((PlayerEntity)entity);

		return false;
	}

	public static boolean isVanished(PlayerEntity player) {
		if (player != null && !player.level.isClientSide) {
			CompoundNBT deathPersistedData = player.getPersistentData().getCompound(PlayerEntity.PERSISTED_NBT_TAG);

			return deathPersistedData.getBoolean("Vanished");
		}

		return false;
	}
}
