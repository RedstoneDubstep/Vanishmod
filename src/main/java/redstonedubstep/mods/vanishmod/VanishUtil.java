package redstonedubstep.mods.vanishmod;

import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SPlayerListItemPacket;
import net.minecraft.network.play.server.SPlayerListItemPacket.Action;
import net.minecraft.network.play.server.SSpawnPlayerPacket;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class VanishUtil {
	private static final List<UUID> vanishedPlayers = Lists.newArrayList();

	public static List<ServerPlayerEntity> formatPlayerList(List<ServerPlayerEntity> rawList) {
		List<ServerPlayerEntity> formattedList = Lists.newArrayList();

		for (ServerPlayerEntity player : rawList) {
			if (!vanishedPlayers.contains(player.getUniqueID()))
				formattedList.add(player);
		}

		System.out.println("The formatted list:"+formattedList.toString());

		return formattedList;
	}

	public static void sendPacketOnVanish(CommandContext<CommandSource> ctx, boolean vanished) throws CommandSyntaxException {
		System.out.println("SendPacketOnVanish has been called! vanished="+vanished);
		List<ServerPlayerEntity> list = ctx.getSource().getWorld().getPlayers();

		for (ServerPlayerEntity player : list) {
			if (!player.equals(ctx.getSource().asPlayer())) { //prevent packet from being sent to the executor of the command
				player.connection.sendPacket(new SPlayerListItemPacket(vanished ? Action.REMOVE_PLAYER : Action.ADD_PLAYER, ctx.getSource().asPlayer()));
				if (!vanished) {
					player.connection.sendPacket(new SSpawnPlayerPacket(ctx.getSource().asPlayer()));
					//player.connection.sendPacket(new SPlayerListItemPacket(Action.UPDATE_GAME_MODE, ctx.getSource().asPlayer()));
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

	public static void updateVanishedList(ServerPlayerEntity player, boolean vanished) {
		if (vanished)
			vanishedPlayers.add(player.getUniqueID());
		else
			vanishedPlayers.remove(player.getUniqueID());

		player.setInvisible(vanished);
	}

	public static boolean isVanished(ServerPlayerEntity player) {
		return isVanished(player.getUniqueID());
	}

	public static boolean isVanished(UUID uuid) {
		return vanishedPlayers.contains(uuid);
	}
}
