package redstonedubstep.mods.vanishmod;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
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
	public static final MutableComponent VANISHMOD_PREFIX = (new TextComponent("[")).append(new TextComponent("Vanishmod").withStyle(s -> s.applyFormat(ChatFormatting.GRAY).withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.curseforge.com/minecraft/mc-mods/vanishmod")))).append("] ");
	private static final Set<ServerPlayer> vanishedPlayers = new HashSet<>();

	public static List<? extends Entity> formatEntityList(List<? extends Entity> rawList, Entity forPlayer) {
		return rawList.stream().filter(entity -> !(entity instanceof Player player) || !isVanished(player, forPlayer)).collect(Collectors.toList());
	}

	public static List<ServerPlayer> formatPlayerList(List<ServerPlayer> rawList, Entity forPlayer) {
		return rawList.stream().filter(player -> !isVanished(player, forPlayer)).collect(Collectors.toList());
	}

	public static void sendPacketsOnVanish(ServerPlayer currentPlayer, ServerLevel world, boolean vanished) {
		List<ServerPlayer> list = world.getServer().getPlayerList().getPlayers();
		ServerChunkCache chunkProvider = currentPlayer.getLevel().getChunkSource();

		currentPlayer.refreshTabListName();

		for (ServerPlayer player : list) {
			if (!player.equals(currentPlayer)) { //prevent packet from being sent to the executor of the command
				if (!canSeeVanishedPlayers(player))
					player.connection.send(new ClientboundPlayerInfoPacket(vanished ? Action.REMOVE_PLAYER : Action.ADD_PLAYER, currentPlayer));
				if (isVanished(player))
					currentPlayer.connection.send(new ClientboundPlayerInfoPacket(canSeeVanishedPlayers(currentPlayer) ? Action.ADD_PLAYER : Action.REMOVE_PLAYER, player)); //update the vanishing player's tab list in case the vanishing player can (not) see other vanished players now

				if (VanishConfig.CONFIG.hidePlayersFromWorld.get()) {
					if (vanished && !canSeeVanishedPlayers(player))
						player.connection.send(new ClientboundRemoveEntitiesPacket(currentPlayer.getId())); //remove the vanishing player for the other players that cannot see vanished players
					else if (isVanished(player) && !canSeeVanishedPlayers(currentPlayer))
						currentPlayer.connection.send(new ClientboundRemoveEntitiesPacket(player.getId())); //if the vanishing players cannot see vanished players now, remove them for the vanishing player
				}
			}
		}

		//We can safely send the tracking update for the vanishing or unvanishing player to everyone, the more strict and player-aware filter gets applied in MixinChunkMapTrackedEntity
		chunkProvider.chunkMap.entityMap.remove(currentPlayer.getId()); //we don't want an error in our log because the entity to be tracked is already on that list
		chunkProvider.addEntity(currentPlayer);

		currentPlayer.connection.send(new ClientboundSetActionBarTextPacket(VanishUtil.getVanishedStatusText(currentPlayer)));
	}

	public static void sendJoinOrLeaveMessageToPlayers(List<ServerPlayer> playerList, ServerPlayer sender, boolean leaveMessage) {
		if (VanishConfig.CONFIG.sendFakeJoinLeaveMessages.get()) {
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
	}

	public static void updateVanishedPlayerList(ServerPlayer player, boolean vanished) {
		if (vanished)
			vanishedPlayers.add(player);
		else
			vanishedPlayers.remove(player);

		SoundSuppressionHelper.updateVanishedPlayerMap(player, vanished);
	}

	public static TranslatableComponent getVanishedStatusText(ServerPlayer player) {
		return new TranslatableComponent(VanishUtil.isVanished(player) ? VanishConfig.CONFIG.onVanishQuery.get() : VanishConfig.CONFIG.onUnvanishQuery.get(), player.getDisplayName());
	}

	public static boolean canSeeVanishedPlayers(Entity entity) {
		if (entity instanceof Player player)
			return (VanishConfig.CONFIG.vanishedPlayersSeeEachOther.get() && VanishUtil.isVanished(player)) || (VanishConfig.CONFIG.seeVanishedPermissionLevel.get() >= 0 && player.hasPermissions(VanishConfig.CONFIG.seeVanishedPermissionLevel.get()));

		return false;
	}

	public static boolean isVanished(Player player) {
		return isVanished(player, null);
	}

	public static boolean isVanished(Player player, Entity forPlayer) {
		if (player != null && !player.level.isClientSide) {
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
