package redstonedubstep.mods.vanishmod;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
import net.minecraft.util.Util;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;
import redstonedubstep.mods.vanishmod.api.PlayerVanishEvent;
import redstonedubstep.mods.vanishmod.compat.Mc2DiscordCompat;
import redstonedubstep.mods.vanishmod.misc.SoundSuppressionHelper;

public class VanishUtil {
	public static final IFormattableTextComponent VANISHMOD_PREFIX = new StringTextComponent("").append(new StringTextComponent("[").withStyle(TextFormatting.WHITE)).append(new StringTextComponent("Vanishmod").withStyle(s -> s.applyFormat(TextFormatting.GRAY).withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.curseforge.com/minecraft/mc-mods/vanishmod")))).append(new StringTextComponent("] ").withStyle(TextFormatting.WHITE));
	private static final Set<ServerPlayerEntity> vanishedPlayers = new HashSet<>();
	private static final Set<String> vanishingQueue = new HashSet<>();

	public static List<? extends Entity> formatEntityList(List<? extends Entity> rawList, Entity forPlayer) {
		return rawList.stream().filter(entity -> !(entity instanceof PlayerEntity) || !isVanished(((PlayerEntity)entity), forPlayer)).collect(Collectors.toList());
	}

	public static List<ServerPlayerEntity> formatPlayerList(List<ServerPlayerEntity> rawList, Entity forPlayer) {
		return rawList.stream().filter(player -> !isVanished(player, forPlayer)).collect(Collectors.toList());
	}

	public static void toggleVanish(ServerPlayerEntity player) {
		boolean vanishes = !VanishUtil.isVanished(player);
		String note = "Note: You can still see yourself in the tab list for technical reasons, but you are vanished for other players. \nNote: Be careful when producing noise near other players, because while most sounds will get suppressed, some won't due to technical limitations.";

		VanishUtil.updateVanishedStatus(player, vanishes);

		if (vanishes)
			player.sendMessage(VanishUtil.VANISHMOD_PREFIX.copy().append("Note: ").append(new StringTextComponent("(...)").withStyle(s -> s.applyFormat(TextFormatting.GRAY).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent(note))))), Util.NIL_UUID);

		VanishUtil.sendJoinOrLeaveMessageToPlayers(player.getLevel().getServer().getPlayerList().getPlayers(), player, vanishes);
		VanishUtil.sendPacketsOnVanish(player, player.getLevel(), vanishes);
	}

	public static void sendPacketsOnVanish(ServerPlayerEntity currentPlayer, ServerWorld world, boolean vanished) {
		List<ServerPlayerEntity> list = world.getServer().getPlayerList().getPlayers();
		ServerChunkProvider chunkProvider = currentPlayer.getLevel().getChunkSource();

		currentPlayer.refreshTabListName();

		for (ServerPlayerEntity player : list) {
			if (!player.equals(currentPlayer)) { //prevent packet from being sent to the executor of the command
				if (!canSeeVanishedPlayers(player))
					player.connection.send(new SPlayerListItemPacket(vanished ? Action.REMOVE_PLAYER : Action.ADD_PLAYER, currentPlayer));
				if (isVanished(player))
					currentPlayer.connection.send(new SPlayerListItemPacket(canSeeVanishedPlayers(currentPlayer) ? Action.ADD_PLAYER : Action.REMOVE_PLAYER, player)); //update the vanishing player's tab list in case the vanishing player can (not) see other vanished players now

				if (VanishConfig.CONFIG.hidePlayersFromWorld.get()) {
					if (vanished && !canSeeVanishedPlayers(player))
						player.connection.send(new SDestroyEntitiesPacket(currentPlayer.getId())); //remove the vanishing player for the other players that cannot see vanished players
					else if (isVanished(player) && !canSeeVanishedPlayers(currentPlayer))
						currentPlayer.connection.send(new SDestroyEntitiesPacket(player.getId())); //if the vanishing players cannot see vanished players now, remove them for the vanishing player
				}
			}
		}

		//We can safely send the tracking update for the vanishing or unvanishing player to everyone, the more strict and player-aware filter gets applied in MixinChunkMapTrackedEntity. But we don't need to do that ourselves if the player has not been added yet (for example before it has fully joined the server)
		if (chunkProvider.chunkMap.entityMap.containsKey(currentPlayer.getId())) {
			chunkProvider.chunkMap.entityMap.remove(currentPlayer.getId()); //we don't want an error in our log because the entity to be tracked is already on that list
			chunkProvider.addEntity(currentPlayer);
		}

		currentPlayer.connection.send(new STitlePacket(Type.ACTIONBAR, VanishUtil.getVanishedStatusText(currentPlayer)));
	}

	public static void sendJoinOrLeaveMessageToPlayers(List<ServerPlayerEntity> playerList, ServerPlayerEntity sender, boolean leaveMessage) {
		if (VanishConfig.CONFIG.sendFakeJoinLeaveMessages.get() && sender.server.getPlayerList().getPlayers().contains(sender)) { //Only send fake messages if the player has actually "joined" the server before this method is invoked
			IFormattableTextComponent message = new TranslationTextComponent(leaveMessage ? "multiplayer.player.left" : "multiplayer.player.joined", sender.getDisplayName()).withStyle(TextFormatting.YELLOW);

			for (ServerPlayerEntity receiver : playerList) {
				receiver.sendMessage(message, sender.getUUID());
			}

			if (ModList.get().isLoaded("mc2discord"))
				Mc2DiscordCompat.sendFakeJoinLeaveMessage(sender, leaveMessage);
		}
	}

	public static void updateVanishedStatus(ServerPlayerEntity player, boolean vanished) {
		CompoundNBT persistentData = player.getPersistentData();
		CompoundNBT deathPersistentData = persistentData.getCompound(PlayerEntity.PERSISTED_NBT_TAG);

		deathPersistentData.putBoolean("Vanished", vanished);
		persistentData.put(PlayerEntity.PERSISTED_NBT_TAG, deathPersistentData); //Because the deathPersistentData could have been created newly by getCompound if it didn't exist before

		if (ModList.get().isLoaded("mc2discord"))
			Mc2DiscordCompat.hidePlayer(player, vanished);

		updateVanishedPlayerList(player, vanished);
		MinecraftForge.EVENT_BUS.post(new PlayerVanishEvent(player, vanished));
	}

	public static void updateVanishedPlayerList(ServerPlayerEntity player, boolean vanished) {
		if (vanished)
			vanishedPlayers.add(player);
		else
			vanishedPlayers.remove(player);

		SoundSuppressionHelper.updateVanishedPlayerMap(player, vanished);
	}

	public static TranslationTextComponent getVanishedStatusText(ServerPlayerEntity player) {
		return new TranslationTextComponent(VanishUtil.isVanished(player) ? VanishConfig.CONFIG.onVanishQuery.get() : VanishConfig.CONFIG.onUnvanishQuery.get(), player.getDisplayName());
	}

	public static boolean addToQueue(String playerName) {
		return vanishingQueue.add(playerName);
	}

	public static boolean removeFromQueue(String playerName) {
		return vanishingQueue.remove(playerName);
	}

	public static boolean canSeeVanishedPlayers(Entity entity) {
		if (entity instanceof PlayerEntity)
			return (VanishConfig.CONFIG.vanishedPlayersSeeEachOther.get() && VanishUtil.isVanished((PlayerEntity)entity)) || (VanishConfig.CONFIG.seeVanishedPermissionLevel.get() >= 0 && entity.hasPermissions(VanishConfig.CONFIG.seeVanishedPermissionLevel.get()));

		return false;
	}

	public static boolean isVanished(PlayerEntity player) {
		return isVanished(player, null);
	}

	public static boolean isVanished(PlayerEntity player, Entity forPlayer) {
		if (player != null && !player.level.isClientSide) {
			boolean isVanished = player.getPersistentData().getCompound(PlayerEntity.PERSISTED_NBT_TAG).getBoolean("Vanished");

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
