package redstonedubstep.mods.vanishmod;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.scores.PlayerTeam;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.VanillaGameEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import redstonedubstep.mods.vanishmod.compat.Mc2DiscordCompat;
import redstonedubstep.mods.vanishmod.misc.FieldHolder;
import redstonedubstep.mods.vanishmod.misc.SoundSuppressionHelper;
import redstonedubstep.mods.vanishmod.misc.TraceHandler;

@EventBusSubscriber(modid = Vanishmod.MODID)
public class VanishEventListener {
	@SubscribeEvent
	public static void onServerStarted(ServerStartedEvent event) {
		if (ModList.get().isLoaded("mc2discord"))
			Vanishmod.mc2discordDetected = true;
	}

	@SubscribeEvent
	public static void onServerStopped(ServerStoppedEvent event) {
		VanishUtil.VANISHED_PLAYERS.clear();
	}

	@SubscribeEvent
	public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			PlayerList list = player.server.getPlayerList();

			if (VanishUtil.isVanished(player)) {
				player.sendSystemMessage(VanishUtil.VANISHMOD_PREFIX.copy().append("Note: You are currently vanished"));

				for (ServerPlayer otherPlayer : list.getPlayers()) {
					if (!otherPlayer.equals(player) && !VanishUtil.isVanished(player, otherPlayer)) //When the event is fired, the joining player has already been added to the player list
						otherPlayer.sendSystemMessage(VanishUtil.VANISHMOD_PREFIX.copy().append("Note: ").append(player.getDisplayName()).append(" is currently vanished"));
				}
			}
			else {
				for (ServerPlayer otherPlayer : list.getPlayers()) { //If the joining player is unvanished and is able to see vanished players, they could potentially expose them by e.g. mentioning their name in chat. This notification should help to prevent that.
					if (!otherPlayer.equals(player) && VanishUtil.isVanished(otherPlayer) && !VanishUtil.isVanished(otherPlayer, player)) {
						player.sendSystemMessage(VanishUtil.VANISHMOD_PREFIX.copy().append("Note: At least one player visible for you is vanished for other players, be careful to not accidentally reveal them"));
						break;
					}
				}
			}
		}

		if (event.getEntity().equals(FieldHolder.joiningPlayer))
			FieldHolder.joiningPlayer = null; //Reset the joiningPlayer field due to it being obsolete at the time the event is fired
	}

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			if (Vanishmod.mc2discordDetected && VanishConfig.CONFIG.forceSyncHiddenList.get()) {
				boolean isVanished = VanishUtil.isVanished(player);

				if (isVanished != Mc2DiscordCompat.isHidden(player))
					Mc2DiscordCompat.hidePlayer(player, isVanished);
			}

			if (player.level().getGameTime() % 20 == 0) //We post the trace notifications once per second, to not make them too overwhelming (in case some mod spams a message)
				TraceHandler.sendTraceEntries(player);
		}
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public static void onTabListName(PlayerEvent.TabListNameFormat event) {
		if (VanishUtil.isVanished(event.getEntity())) { //Appending a prefix to the name here won't give away vanished players, as their tab list names are only displayed for players that are allowed to see vanished players
			MutableComponent vanishedName = Component.literal("").withStyle(ChatFormatting.ITALIC);

			vanishedName.append(Component.literal("[").withStyle(ChatFormatting.DARK_GRAY))
					.append(Component.literal("Vanished").withStyle(ChatFormatting.GRAY))
					.append(Component.literal("] ").withStyle(ChatFormatting.DARK_GRAY))
					.append(event.getDisplayName() == null ? PlayerTeam.formatNameForTeam(event.getEntity().getTeam(), event.getEntity().getName()) : event.getDisplayName());
			event.setDisplayName(vanishedName);
		}
	}

	@SubscribeEvent
	public static void onInteractBlock(PlayerInteractEvent.RightClickBlock event) {
		if (SoundSuppressionHelper.shouldCapturePlayers() && event.getEntity() instanceof ServerPlayer player && player.gameMode.getGameModeForPlayer() != GameType.SPECTATOR)
			SoundSuppressionHelper.updateBlockHitResult(player, event.getHitVec());
	}

	@SubscribeEvent
	public static void onInteractEntity(PlayerInteractEvent.EntityInteract event) {
		if (SoundSuppressionHelper.shouldCapturePlayers() && event.getEntity() instanceof ServerPlayer player && player.gameMode.getGameModeForPlayer() != GameType.SPECTATOR)
			SoundSuppressionHelper.updateEntityHitResult(player, event.getTarget());
	}

	@SubscribeEvent
	public static void onAttackEntity(AttackEntityEvent event) {
		if (SoundSuppressionHelper.shouldCapturePlayers() && event.getEntity() instanceof ServerPlayer player && player.gameMode.getGameModeForPlayer() != GameType.SPECTATOR)
			SoundSuppressionHelper.updateEntityHitResult(player, event.getTarget());
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public static void onChangeTarget(LivingChangeTargetEvent event) {
		if (VanishConfig.CONFIG.hidePlayersFromWorld.get()) {
			if (event.getNewAboutToBeSetTarget() instanceof ServerPlayer player && VanishUtil.isVanished(player))
				event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void onProjectileImpact(ProjectileImpactEvent event) {
		if (VanishConfig.CONFIG.hidePlayersFromWorld.get()) {
			if (event.getRayTraceResult() instanceof EntityHitResult hitResult && VanishUtil.isVanished(hitResult.getEntity(), event.getProjectile().getOwner()))
				event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void onVanillaGameEvent(VanillaGameEvent event) {
		if (event.getCause() instanceof ServerPlayer player) {
			if (VanishConfig.CONFIG.hidePlayersFromWorld.get() && VanishUtil.isVanished(player))
				event.setCanceled(true);
		}
	}
}
