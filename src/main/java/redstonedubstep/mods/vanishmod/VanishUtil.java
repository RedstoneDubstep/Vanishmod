package redstonedubstep.mods.vanishmod;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.Team;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;
import redstonedubstep.mods.vanishmod.api.PlayerVanishEvent;
import redstonedubstep.mods.vanishmod.compat.JoinLeaveMessagesCompat;
import redstonedubstep.mods.vanishmod.compat.Mc2DiscordCompat;
import redstonedubstep.mods.vanishmod.misc.SoundSuppressionHelper;

public class VanishUtil {
	public static final MutableComponent VANISHMOD_PREFIX = Component.literal("").append(Component.literal("[").withStyle(ChatFormatting.WHITE)).append(Component.literal("Vanishmod").withStyle(s -> s.applyFormat(ChatFormatting.GRAY).withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.curseforge.com/minecraft/mc-mods/vanishmod")))).append(Component.literal("] ").withStyle(ChatFormatting.WHITE));
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
			player.sendSystemMessage(VanishUtil.VANISHMOD_PREFIX.copy().append("Note: ").append(Component.literal("(...)").withStyle(s -> s.applyFormat(ChatFormatting.GRAY).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(note))))));

		VanishUtil.sendJoinOrLeaveMessageToPlayers(player.server.getPlayerList().getPlayers(), player, vanishes, false);
		VanishUtil.updateVanishedStatus(player, vanishes);
		VanishUtil.sendJoinOrLeaveMessageToPlayers(player.server.getPlayerList().getPlayers(), player, vanishes, true); //We always need to send fake join/leave messages when the player is in an unvanished state, thus we try twice and return early (within that method) if the player is vanished

		VanishUtil.sendPacketsOnVanish(player, player.serverLevel(), vanishes);
	}

	public static void sendPacketsOnVanish(ServerPlayer changingPlayer, ServerLevel world, boolean vanishes) {
		List<ServerPlayer> list = world.getServer().getPlayerList().getPlayers();
		ServerChunkCache chunkProvider = changingPlayer.serverLevel().getChunkSource();

		for (ServerPlayer otherPlayer : list) {
			boolean otherPlayerVanished = isVanished(otherPlayer);
			boolean otherAllowedToSeeChanging = allowedToSeePlayer(otherPlayer, changingPlayer, otherPlayerVanished, vanishes);
			boolean changingAllowedToSeeOther = allowedToSeePlayer(changingPlayer, otherPlayer, vanishes, otherPlayerVanished);

			if (!otherPlayer.equals(changingPlayer)) { //prevent packets from being sent to the executor of the command
				//If the other player can or cannot see the changing player now, add or remove the changing player to/from the other player's client side info list
				otherPlayer.connection.send(otherAllowedToSeeChanging ? ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(List.of(changingPlayer)) : new ClientboundPlayerInfoRemovePacket(List.of(changingPlayer.getUUID())));
				//If the changing player can or cannot see the other player now, add or remove the other player to/from the changing player's client side info list
				if (otherPlayerVanished)
					changingPlayer.connection.send(changingAllowedToSeeOther ? ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(List.of(otherPlayer)) : new ClientboundPlayerInfoRemovePacket(List.of(otherPlayer.getUUID())));

				if (VanishConfig.CONFIG.hidePlayersFromWorld.get()) {
					//If the other player cannot see the vanishing player, destroy the changing player entity for the other player
					if (vanishes && !otherAllowedToSeeChanging)
						otherPlayer.connection.send(new ClientboundRemoveEntitiesPacket(changingPlayer.getId()));
					//If the unvanishing player cannot see other vanished players now, remove their entities for the unvanishing player
					else if (!vanishes && !changingAllowedToSeeOther)
						changingPlayer.connection.send(new ClientboundRemoveEntitiesPacket(otherPlayer.getId()));
				}
			}
		}

		//We can safely send the tracking update for the changing player to everyone, the more strict and player-aware filter gets applied in ChunkMapTrackedEntityMixin. But we don't need to do that ourselves if the player has not been added yet (for example before it has fully joined the server)
		if (chunkProvider.chunkMap.entityMap.containsKey(changingPlayer.getId())) {
			chunkProvider.chunkMap.entityMap.remove(changingPlayer.getId()); //we don't want an error in our log because the entity to be tracked is already on that list
			chunkProvider.addEntity(changingPlayer);
		}

		changingPlayer.connection.send(new ClientboundSetActionBarTextPacket(VanishUtil.getVanishedStatusText(changingPlayer, vanishes)));
		changingPlayer.refreshTabListName();
	}

	public static void sendJoinOrLeaveMessageToPlayers(List<ServerPlayer> playerList, ServerPlayer sender, boolean leaveMessage, boolean beforeStatusChange) {
		if (VanishConfig.CONFIG.sendFakeJoinLeaveMessages.get() && leaveMessage != beforeStatusChange && sender.server.getPlayerList().getPlayers().contains(sender)) { //Only send fake messages if the player has actually fully joined the server before this method is invoked
			Component message = getFakeJoinLeaveMessage(sender, leaveMessage);

			if (message != null) {
				for (ServerPlayer receiver : playerList) {
					receiver.sendSystemMessage(message);
				}
			}

			if (ModList.get().isLoaded("mc2discord"))
				Mc2DiscordCompat.sendFakeJoinLeaveMessage(sender, leaveMessage);
		}
	}

	public static Component getFakeJoinLeaveMessage(ServerPlayer player, boolean leaveMessage) {
		if (ModList.get().isLoaded("joinleavemessages"))
			return JoinLeaveMessagesCompat.getFakeJoinLeaveMessage(player, leaveMessage);

		return Component.translatable(leaveMessage ? "multiplayer.player.left" : "multiplayer.player.joined", player.getDisplayName()).withStyle(ChatFormatting.YELLOW);
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
	}

	public static void updateVanishedPlayerList(ServerPlayer player, boolean vanished) {
		if (vanished)
			vanishedPlayers.add(player);
		else
			vanishedPlayers.remove(player);

		SoundSuppressionHelper.updateVanishedPlayerMap(player, vanished);
	}

	public static MutableComponent getVanishedStatusText(ServerPlayer player, boolean isVanished) {
		return Component.translatable(isVanished ? VanishConfig.CONFIG.onVanishQuery.get() : VanishConfig.CONFIG.onUnvanishQuery.get(), player.getDisplayName());
	}

	public static boolean addToQueue(String playerName) {
		return vanishingQueue.add(playerName);
	}

	public static boolean removeFromQueue(String playerName) {
		return vanishingQueue.remove(playerName);
	}

	public static boolean allowedToSeePlayer(Entity player, Entity otherPlayer, boolean isVanished, boolean isOtherVanished) {
		if (player.equals(otherPlayer)) //All players should be able to see each other
			return true;

		return !isOtherVanished || canSeeAllVanishedPlayers(player, isVanished) || checkTeamVisibility(player, otherPlayer);
	}

	public static boolean canSeeAllVanishedPlayers(Entity entity, boolean isVanished) {
		if (entity instanceof Player player)
			return (VanishConfig.CONFIG.vanishedPlayersSeeEachOther.get() && isVanished) || (VanishConfig.CONFIG.seeVanishedPermissionLevel.get() >= 0 && player.hasPermissions(VanishConfig.CONFIG.seeVanishedPermissionLevel.get()));

		return false;
	}

	public static boolean checkTeamVisibility(Entity player, Entity otherPlayer) {
		if (!VanishConfig.CONFIG.seeVanishedTeamPlayers.get())
			return false;

		Team team = player.getTeam();

		return team != null && team.canSeeFriendlyInvisibles() && team.getPlayers().contains(otherPlayer.getScoreboardName());
	}

	public static ResourceKey<ChatType> getChatTypeRegistryKey(ChatType.Bound chatType, Player player) {
		return player.level().registryAccess().registryOrThrow(Registries.CHAT_TYPE).getResourceKey(chatType.chatType()).orElse(ChatType.CHAT);
	}

	public static boolean isVanished(Entity player) {
		return isVanished(player, null);
	}

	public static boolean isVanished(Entity player, Entity forPlayer) {
		if (player instanceof Player && !player.level().isClientSide) {
			boolean isVanished = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getBoolean("Vanished");

			if (forPlayer != null)
				return !allowedToSeePlayer(forPlayer, player, isVanished(forPlayer), isVanished);

			return isVanished;
		}

		return false;
	}
}
