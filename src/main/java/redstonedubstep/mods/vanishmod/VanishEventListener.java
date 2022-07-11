package redstonedubstep.mods.vanishmod;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.GameType;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.VanillaGameEvent;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
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
		if (event.getPlayer() instanceof ServerPlayer player && VanishUtil.isVanished(player)) {
			player.sendMessage(VanishUtil.VANISHMOD_PREFIX.copy().append("Note: You are currently vanished"), player.getUUID());
			VanishUtil.updateVanishedPlayerList(player, true);
		}
	}

	@SubscribeEvent
	public static void onPlaySound(PlaySoundAtEntityEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			if (VanishConfig.CONFIG.hidePlayersFromWorld.get() && VanishUtil.isVanished(player))
				event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void onServerChat(ServerChatEvent event) {
		if (VanishConfig.CONFIG.hidePlayerNameInChat.get() && VanishUtil.isVanished(event.getPlayer())) {
			Component message = event.getComponent();

			if (message instanceof TranslatableComponent component && component.getKey().contains("chat.type.announcement"))
				event.setComponent(new TranslatableComponent("chat.type.announcement", new TextComponent("vanished").withStyle(ChatFormatting.GRAY), ((TranslatableComponent)message).getArgs()[1]));
		}
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public static void onTabListName(TabListNameFormat event) {
		if (VanishUtil.isVanished(event.getPlayer())) { //Appending a prefix to the name here won't give away vanished players, as their tab list names are only displayed for players that are allowed to see vanished players
			MutableComponent vanishedName = new TextComponent("").withStyle(ChatFormatting.ITALIC);

			vanishedName.append(new TextComponent("[").withStyle(ChatFormatting.DARK_GRAY))
					.append(new TextComponent("Vanished").withStyle(ChatFormatting.GRAY))
					.append(new TextComponent("] ").withStyle(ChatFormatting.DARK_GRAY))
					.append(event.getDisplayName() == null ? PlayerTeam.formatNameForTeam(event.getPlayer().getTeam(), event.getPlayer().getName()) : event.getDisplayName());
			event.setDisplayName(vanishedName);
		}
	}

	@SubscribeEvent
	public static void onInteractBlock(RightClickBlock event) {
		if (VanishConfig.CONFIG.indirectSoundSuppression.get() && event.getPlayer() instanceof ServerPlayer player && player.gameMode.getGameModeForPlayer() != GameType.SPECTATOR)
			SoundSuppressionHelper.updateBlockHitResult(player, event.getHitVec());
	}

	@SubscribeEvent
	public static void onInteractEntity(EntityInteract event) {
		if (VanishConfig.CONFIG.indirectSoundSuppression.get() && event.getPlayer() instanceof ServerPlayer player && player.gameMode.getGameModeForPlayer() != GameType.SPECTATOR)
			SoundSuppressionHelper.updateEntityHitResult(player, event.getTarget());
	}

	@SubscribeEvent
	public static void onAttackEntity(AttackEntityEvent event) {
		if (VanishConfig.CONFIG.indirectSoundSuppression.get() && event.getPlayer() instanceof ServerPlayer player && player.gameMode.getGameModeForPlayer() != GameType.SPECTATOR)
			SoundSuppressionHelper.updateEntityHitResult(player, event.getTarget());
	}

	@SubscribeEvent
	public static void onSetAttackTarget(LivingSetAttackTargetEvent event) {
		if (VanishConfig.CONFIG.hidePlayersFromWorld.get()) {
			if (event.getTarget() instanceof ServerPlayer player && event.getEntityLiving() instanceof Mob mob && VanishUtil.isVanished(player))
				mob.setTarget(null);
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
