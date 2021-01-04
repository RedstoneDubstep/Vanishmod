package redstonedubstep.mods.vanishmod;

import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.CommandSource;
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

public class VanishUtil {
	public static List<ServerPlayerEntity> formatPlayerList(List<ServerPlayerEntity> rawList) {
		List<ServerPlayerEntity> formattedList = Lists.newArrayList();

		for (ServerPlayerEntity player : rawList) {
			if (!isVanished(player))
				formattedList.add(player);
		}

		return formattedList;
	}

	public static void sendPacketsOnVanish(CommandContext<CommandSource> ctx, boolean vanished) throws CommandSyntaxException {
		ServerPlayerEntity currentPlayer = ctx.getSource().asPlayer();

		List<ServerPlayerEntity> list = ctx.getSource().getWorld().getPlayers();

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

	public static void sendMessageToAllPlayers(List<ServerPlayerEntity> playerList, ServerPlayerEntity sender, boolean vanished) {
		IFormattableTextComponent content = new TranslationTextComponent(vanished ? "multiplayer.player.left" : "multiplayer.player.joined", sender.getDisplayName()).mergeStyle(TextFormatting.YELLOW);

		for (ServerPlayerEntity receiver : playerList) {
			receiver.sendMessage(content, sender.getUniqueID());
		}
	}

	public static void updateVanishedStatus(ServerPlayerEntity player, boolean vanished) {
		player.getPersistentData().putBoolean("vanished", vanished);
		player.setInvisible(vanished);
	}

	public static boolean isVanished(UUID uuid, ServerWorld world) {
		Entity entity = world.getEntityByUuid(uuid);
		if (!(entity instanceof ServerPlayerEntity))
			return false;

		return isVanished((ServerPlayerEntity)entity);
	}

	public static boolean isVanished(ServerPlayerEntity player) {
		if (player == null)
			return false;

		return player.getPersistentData().getBoolean("vanished");
	}

}
