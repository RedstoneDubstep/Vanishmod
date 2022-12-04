package redstonedubstep.mods.vanishmod;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraftforge.event.VanillaGameEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.TabListNameFormat;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import redstonedubstep.mods.vanishmod.misc.SoundSuppressionHelper;

@EventBusSubscriber(modid = Vanishmod.MODID)
public class VanishEventListener {
	@SubscribeEvent
	public static void onPlayerJoin(PlayerLoggedInEvent event) {
		if (event.getEntity() instanceof ServerPlayer player && VanishUtil.isVanished(player)) {
			player.sendSystemMessage(VanishUtil.VANISHMOD_PREFIX.copy().append("Note: You are currently vanished"));
			VanishUtil.updateVanishedPlayerList(player, true);
		}
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public static void onTabListName(TabListNameFormat event) {
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
	public static void onInteractBlock(RightClickBlock event) {
		if (SoundSuppressionHelper.shouldCapturePlayers() && event.getEntity() instanceof ServerPlayer player && player.gameMode.getGameModeForPlayer() != GameType.SPECTATOR)
			SoundSuppressionHelper.updateBlockHitResult(player, event.getHitVec());
	}

	@SubscribeEvent
	public static void onInteractEntity(EntityInteract event) {
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
