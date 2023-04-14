package redstonedubstep.mods.vanishmod;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket.Action;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;
import redstonedubstep.mods.vanishmod.api.PlayerVanishEvent;
import redstonedubstep.mods.vanishmod.compat.Mc2DiscordCompat;
import redstonedubstep.mods.vanishmod.misc.SoundSuppressionHelper;

public class VanishUtil {
	public static final MutableComponent VANISHMOD_PREFIX = new TextComponent("").append(new TextComponent("[").withStyle(ChatFormatting.WHITE)).append(new TextComponent("Vanishmod").withStyle(s -> s.applyFormat(ChatFormatting.GRAY).withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.curseforge.com/minecraft/mc-mods/vanishmod")))).append(new TextComponent("] ").withStyle(ChatFormatting.WHITE));
	private static final Set<ServerPlayer> vanishedPlayers = new HashSet<>();
	private static final Set<String> vanishingQueue = new HashSet<>();

	public static List<? extends Entity> formatEntityList(List<? extends Entity> rawList, Entity forPlayer) {
		return rawList.stream().filter(entity -> !(entity instanceof Player player) || !isVanished(player, forPlayer)).collect(Collectors.toList());
	}

	public static List<ServerPlayer> formatPlayerList(List<ServerPlayer> rawList, Entity forPlayer) {
		return rawList.stream().filter(player -> !isVanished(player, forPlayer)).collect(Collectors.toList());
	}

	public static void toggleVanish(ServerPlayer player) {
		boolean vanishes = !VanishUtil.isVanished(player);
		String note = "Note: You can still see yourself in the tab list for technical reasons, but you are vanished for other players. \nNote: Be careful when producing noise near other players, because while most sounds will get suppressed, some won't due to technical limitations. \nNote: While vanished, only players that are able to see you will receive your chat messages. If you want to chat with everyone, use the /say command.";

		if (vanishes)
			player.sendMessage(VanishUtil.VANISHMOD_PREFIX.copy().append("Note: ").append(new TextComponent("(...)").withStyle(s -> s.applyFormat(ChatFormatting.GRAY).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent(note))))), Util.NIL_UUID);

		VanishUtil.sendJoinOrLeaveMessageToPlayers(player.getLevel().getServer().getPlayerList().getPlayers(), player, vanishes, false);
		VanishUtil.updateVanishedStatus(player, vanishes);
		VanishUtil.sendJoinOrLeaveMessageToPlayers(player.getLevel().getServer().getPlayerList().getPlayers(), player, vanishes, true); //We always need to send fake join/leave messages when the player is in an unvanished state, thus we try twice and return early (within that method) if the player is vanished

		VanishUtil.sendPacketsOnVanish(player, player.getLevel(), vanishes);
	}

	public static void sendPacketsOnVanish(ServerPlayer currentPlayer, ServerLevel world, boolean vanishes) {
		List<ServerPlayer> list = world.getServer().getPlayerList().getPlayers();
		ServerChunkCache chunkProvider = currentPlayer.getLevel().getChunkSource();

		for (ServerPlayer player : list) {
			if (!player.equals(currentPlayer)) { //prevent packet from being sent to the executor of the command
				if (!canSeeVanishedPlayers(player))
					player.connection.send(new ClientboundPlayerInfoPacket(vanishes ? Action.REMOVE_PLAYER : Action.ADD_PLAYER, currentPlayer));
				if (isVanished(player))
					currentPlayer.connection.send(new ClientboundPlayerInfoPacket(canSeeVanishedPlayers(currentPlayer, vanishes) ? Action.ADD_PLAYER : Action.REMOVE_PLAYER, player)); //update the vanishing player's tab list in case the vanishing player can (not) see other vanished players now

				if (VanishConfig.CONFIG.hidePlayersFromWorld.get()) {
					if (vanishes && !canSeeVanishedPlayers(player))
						player.connection.send(new ClientboundRemoveEntitiesPacket(currentPlayer.getId())); //remove the vanishing player for the other players that cannot see vanished players
					else if (isVanished(player) && !canSeeVanishedPlayers(currentPlayer, vanishes))
						currentPlayer.connection.send(new ClientboundRemoveEntitiesPacket(player.getId())); //if the vanishing players cannot see vanished players now, remove them for the vanishing player
				}
			}
		}

		//We can safely send the tracking update for the vanishing or unvanishing player to everyone, the more strict and player-aware filter gets applied in ChunkMapTrackedEntityMixin. But we don't need to do that ourselves if the player has not been added yet (for example before it has fully joined the server)
		if (chunkProvider.chunkMap.entityMap.containsKey(currentPlayer.getId())) {
			chunkProvider.chunkMap.entityMap.remove(currentPlayer.getId()); //we don't want an error in our log because the entity to be tracked is already on that list
			chunkProvider.addEntity(currentPlayer);
		}

		currentPlayer.connection.send(new ClientboundSetActionBarTextPacket(VanishUtil.getVanishedStatusText(currentPlayer, vanishes)));
	}

	public static void sendJoinOrLeaveMessageToPlayers(List<ServerPlayer> playerList, ServerPlayer sender, boolean leaveMessage, boolean beforeStatusChange) {
		if (VanishConfig.CONFIG.sendFakeJoinLeaveMessages.get() && leaveMessage != beforeStatusChange && sender.server.getPlayerList().getPlayers().contains(sender)) { //Only send fake messages if the player has actually fully joined the server before this method is invoked
			Component message = new TranslatableComponent(leaveMessage ? "multiplayer.player.left" : "multiplayer.player.joined", sender.getDisplayName()).withStyle(ChatFormatting.YELLOW);

			for (ServerPlayer receiver : playerList) {
				receiver.sendMessage(message, sender.getUUID());
			}

			if (ModList.get().isLoaded("mc2discord"))
				Mc2DiscordCompat.sendFakeJoinLeaveMessage(sender, leaveMessage);
		}
	}

	public static void updateVanishedStatus(ServerPlayer player, boolean vanished) {
		CompoundTag persistentData = player.getPersistentData();
		CompoundTag deathPersistentData = persistentData.getCompound(Player.PERSISTED_NBT_TAG);

		deathPersistentData.putBoolean("Vanished", vanished);
		persistentData.put(Player.PERSISTED_NBT_TAG, deathPersistentData); //Because the deathPersistentData could have been created newly by getCompound if it didn't exist before

		if (ModList.get().isLoaded("mc2discord"))
			Mc2DiscordCompat.hidePlayer(player, vanished);

		updateVanishedPlayerList(player, vanished);
		MinecraftForge.EVENT_BUS.post(new PlayerVanishEvent(player, vanished));
		player.refreshTabListName();
	}

	public static void updateVanishedPlayerList(ServerPlayer player, boolean vanished) {
		if (vanished)
			vanishedPlayers.add(player);
		else
			vanishedPlayers.remove(player);

		SoundSuppressionHelper.updateVanishedPlayerMap(player, vanished);
	}

	public static TranslatableComponent getVanishedStatusText(ServerPlayer player, boolean isVanished) {
		return new TranslatableComponent(isVanished ? VanishConfig.CONFIG.onVanishQuery.get() : VanishConfig.CONFIG.onUnvanishQuery.get(), player.getDisplayName());
	}

	public static boolean addToQueue(String playerName) {
		return vanishingQueue.add(playerName);
	}

	public static boolean removeFromQueue(String playerName) {
		return vanishingQueue.remove(playerName);
	}

	public static boolean canSeeVanishedPlayers(Entity entity) {
		return canSeeVanishedPlayers(entity, isVanished(entity));
	}

	public static boolean canSeeVanishedPlayers(Entity entity, boolean isVanished) {
		if (entity instanceof Player player)
			return (VanishConfig.CONFIG.vanishedPlayersSeeEachOther.get() && isVanished) || (VanishConfig.CONFIG.seeVanishedPermissionLevel.get() >= 0 && player.hasPermissions(VanishConfig.CONFIG.seeVanishedPermissionLevel.get()));

		return false;
	}

	public static boolean isVanished(Entity player) {
		return isVanished(player, null);
	}

	public static boolean isVanished(Entity player, Entity forPlayer) {
		if (player instanceof Player && !player.level.isClientSide) {
			boolean isVanished = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getBoolean("Vanished");

			if (forPlayer != null) {
				if (player.equals(forPlayer)) //No player should ever be vanished for themselves
					return false;

				return isVanished && !canSeeVanishedPlayers(forPlayer);
			}

			return isVanished;
		}

		return false;
	}
}
