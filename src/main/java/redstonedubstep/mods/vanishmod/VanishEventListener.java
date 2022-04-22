package redstonedubstep.mods.vanishmod;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.GameType;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
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
		if (event.getPlayer() instanceof ServerPlayerEntity && VanishUtil.isVanished(event.getPlayer())) {
			ServerPlayerEntity player = ((ServerPlayerEntity)event.getPlayer());

			player.sendMessage(VanishUtil.VANISHMOD_PREFIX.copy().append("Note: You are still vanished"), player.getUUID());
			VanishUtil.updateVanishedPlayerList(player, true);
		}
	}

	@SubscribeEvent
	public static void onPlaySound(PlaySoundAtEntityEvent event) {
		if (event.getEntity() instanceof ServerPlayerEntity) {
			ServerPlayerEntity player = (ServerPlayerEntity)event.getEntity();

			if (VanishConfig.CONFIG.hidePlayersFromWorld.get() && VanishUtil.isVanished(player))
				event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void onServerChat(ServerChatEvent event) {
		if (VanishUtil.isVanished(event.getPlayer()) && VanishConfig.CONFIG.hidePlayerNameInChat.get()) {
			ITextComponent message = event.getComponent();

			if (message instanceof TranslationTextComponent && ((TranslationTextComponent)message).getKey().contains("chat.type.text")) {
				event.setComponent(new TranslationTextComponent("chat.type.text", new StringTextComponent("vanished").withStyle(TextFormatting.GRAY), ((TranslationTextComponent)message).getArgs()[1]));
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public static void onTabListName(TabListNameFormat event) {
		if (VanishUtil.isVanished(event.getPlayer())) { //Appending a prefix to the name here won't give away vanished players, as their tab list names are only displayed for players that are allowed to see vanished players
			IFormattableTextComponent vanishedName = new StringTextComponent("").withStyle(TextFormatting.ITALIC);

			vanishedName.append(new StringTextComponent("[").withStyle(TextFormatting.DARK_GRAY))
					.append(new StringTextComponent("Vanished").withStyle(TextFormatting.GRAY))
					.append(new StringTextComponent("] ").withStyle(TextFormatting.DARK_GRAY))
					.append(event.getDisplayName() == null ? ScorePlayerTeam.formatNameForTeam(event.getPlayer().getTeam(), event.getPlayer().getName()) : event.getDisplayName());
			event.setDisplayName(vanishedName);
		}
	}

	@SubscribeEvent
	public static void onInteractBlock(RightClickBlock event) {
		if (VanishConfig.CONFIG.indirectSoundSuppression.get() && event.getPlayer() instanceof ServerPlayerEntity) {
			ServerPlayerEntity player = ((ServerPlayerEntity)event.getPlayer());

			if (player.gameMode.getGameModeForPlayer() != GameType.SPECTATOR)
				SoundSuppressionHelper.updateBlockHitResult(player, event.getHitVec());
		}
	}

	@SubscribeEvent
	public static void onInteractEntity(EntityInteract event) {
		if (VanishConfig.CONFIG.indirectSoundSuppression.get() && event.getPlayer() instanceof ServerPlayerEntity) {
			ServerPlayerEntity player = ((ServerPlayerEntity)event.getPlayer());

			if (player.gameMode.getGameModeForPlayer() != GameType.SPECTATOR)
				SoundSuppressionHelper.updateEntityHitResult(player, event.getTarget());
		}
	}

	@SubscribeEvent
	public static void onAttackEntity(AttackEntityEvent event) {
		if (VanishConfig.CONFIG.indirectSoundSuppression.get() && event.getPlayer() instanceof ServerPlayerEntity) {
			ServerPlayerEntity player = ((ServerPlayerEntity)event.getPlayer());

			if (player.gameMode.getGameModeForPlayer() != GameType.SPECTATOR)
				SoundSuppressionHelper.updateEntityHitResult(player, event.getTarget());
		}
	}
}
