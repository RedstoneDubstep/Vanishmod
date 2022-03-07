package redstonedubstep.mods.vanishmod;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.TabListNameFormat;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = Vanishmod.MODID)
public class VanishEventListener {

	@SubscribeEvent
	public static void onPlaySoundAtEntity(PlaySoundAtEntityEvent event) {
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

	@SubscribeEvent
	public static void onTabListName(TabListNameFormat event) {
		if (VanishUtil.isVanished(event.getPlayer())) { //if the player is vanished, its tab list name will only be seen by itself
			IFormattableTextComponent vanishedName = new StringTextComponent("").withStyle(TextFormatting.ITALIC);

			vanishedName.append(new StringTextComponent("[").withStyle(TextFormatting.DARK_GRAY))
					.append(new StringTextComponent("Vanished").withStyle(TextFormatting.GRAY))
					.append(new StringTextComponent("] ").withStyle(TextFormatting.DARK_GRAY))
					.append(event.getDisplayName() == null ? ScorePlayerTeam.formatNameForTeam(event.getPlayer().getTeam(), event.getPlayer().getName()) : event.getDisplayName());
			event.setDisplayName(vanishedName);
		}
	}
}
