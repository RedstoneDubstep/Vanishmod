package redstonedubstep.mods.vanishmod;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.VanillaGameEvent;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.TabListNameFormat;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = Vanishmod.MODID)
public class VanishEventListener {

	@SubscribeEvent
	public static void onPlaySoundAtEntity(PlaySoundAtEntityEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			if (VanishConfig.CONFIG.hidePlayersFromWorld.get() && VanishUtil.isVanished(player))
				event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void onServerChat(ServerChatEvent event) {
		if (VanishUtil.isVanished(event.getPlayer()) && VanishConfig.CONFIG.hidePlayerNameInChat.get()) {
			Component message = event.getComponent();

			if (message instanceof TranslatableComponent component && component.getKey().contains("chat.type.text"))
				event.setComponent(new TranslatableComponent("chat.type.text", new TextComponent("vanished").withStyle(ChatFormatting.GRAY), ((TranslatableComponent)message).getArgs()[1]));
		}
	}

	@SubscribeEvent
	public static void onTabListName(TabListNameFormat event) {
		if (VanishUtil.isVanished(event.getPlayer())) { //if the player is vanished, its tab list name will only be seen by itself
			MutableComponent vanishedName = new TextComponent("").withStyle(ChatFormatting.ITALIC);

			vanishedName.append(new TextComponent("[").withStyle(ChatFormatting.DARK_GRAY))
					.append(new TextComponent("Vanished").withStyle(ChatFormatting.GRAY))
					.append(new TextComponent("] ").withStyle(ChatFormatting.DARK_GRAY))
					.append(event.getDisplayName() == null ? PlayerTeam.formatNameForTeam(event.getPlayer().getTeam(), event.getPlayer().getName()) : event.getDisplayName());
			event.setDisplayName(vanishedName);
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
