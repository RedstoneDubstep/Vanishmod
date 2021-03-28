package redstonedubstep.mods.vanishmod;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SDestroyEntitiesPacket;
import net.minecraft.network.play.server.SPlayerListItemPacket;
import net.minecraft.network.play.server.SPlayerListItemPacket.Action;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import redstonedubstep.mods.vanishmod.compat.Mc2DiscordCompat;

public class VanishUtil {
	public static List<ServerPlayerEntity> formatPlayerList(List<ServerPlayerEntity> rawList) {
		return rawList.stream().filter(player -> !isVanished(player)).collect(Collectors.toList());
	}

	public static void sendPacketsOnVanish(ServerPlayerEntity currentPlayer, ServerWorld world, boolean vanished) {
		List<ServerPlayerEntity> list = world.getPlayers();

		for (ServerPlayerEntity player : list) {
			ServerChunkProvider chunkProvider = player.getServerWorld().getChunkProvider();

			if (!player.equals(currentPlayer)) { //prevent packet from being sent to the executor of the command
				player.connection.sendPacket(new SPlayerListItemPacket(vanished ? Action.REMOVE_PLAYER : Action.ADD_PLAYER, currentPlayer));
				if (!vanished) {
					chunkProvider.chunkManager.entities.remove(currentPlayer.getEntityId()); //we don't want an error in our log because the entity to be tracked is already on that list
					chunkProvider.track(currentPlayer);
				}
				else {
					player.connection.sendPacket(new SDestroyEntitiesPacket(currentPlayer.getEntityId()));
				}
			}
		}
	}

	public static void sendJoinOrLeaveMessageToPlayers(List<ServerPlayerEntity> playerList, ServerPlayerEntity sender, boolean joinMessage) {
		IFormattableTextComponent message = new TranslationTextComponent(joinMessage ? "multiplayer.player.left" : "multiplayer.player.joined", sender.getDisplayName()).mergeStyle(TextFormatting.YELLOW);

		for (ServerPlayerEntity receiver : playerList) {
			receiver.sendMessage(message, sender.getUniqueID());
		}
	}

	public static void updateVanishedStatus(ServerPlayerEntity player, boolean vanished) {
		player.getPersistentData().putBoolean("vanished", vanished);
		player.setInvisible(vanished);
		Mc2DiscordCompat.hidePlayer(player, vanished);
		MinecraftForge.EVENT_BUS.post(new PlayerVanishEvent(player, vanished));
	}

	public static boolean isVanished(UUID uuid, ServerWorld world) {
		Entity entity = world.getEntityByUuid(uuid);

		if (entity instanceof ServerPlayerEntity) {
			return isVanished((ServerPlayerEntity)entity);
		}

		return false;
	}

	public static boolean isVanished(ServerPlayerEntity player) {
		if (player != null) {
			return player.getPersistentData().getBoolean("vanished");
		}

		return false;
	}
}
