package redstonedubstep.mods.vanishmod;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.scores.PlayerTeam;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.VanillaGameEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import redstonedubstep.mods.vanishmod.misc.FieldHolder;
import redstonedubstep.mods.vanishmod.misc.SoundSuppressionHelper;

@Mod.EventBusSubscriber(modid = Vanishmod.MODID)
public class VanishEventListener {
	@SubscribeEvent
	public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getEntity() instanceof ServerPlayer player && VanishUtil.isVanished(player)) {
			player.sendSystemMessage(VanishUtil.VANISHMOD_PREFIX.copy().append("Note: You are currently vanished"));
			VanishUtil.updateVanishedPlayerList(player, true);
		}

		if (event.getEntity().equals(FieldHolder.joiningPlayer))
			FieldHolder.joiningPlayer = null; //Reset the joiningPlayer field due to it being obsolete at the time the event is fired
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

	@SubscribeEvent
	public static void onChangeTarget(LivingChangeTargetEvent event) {
		if (VanishConfig.CONFIG.hidePlayersFromWorld.get()) {
			if (event.getNewTarget() instanceof ServerPlayer player && VanishUtil.isVanished(player))
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
