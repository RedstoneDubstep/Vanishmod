package redstonedubstep.mods.vanishmod;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = Vanishmod.MODID)
public class VanishEventListener {

	@SubscribeEvent
	public static void onPlaySoundAtEntity(PlaySoundAtEntityEvent event) {
		if (event.getEntity() instanceof ServerPlayer) {
			ServerPlayer player = (ServerPlayer)event.getEntity();

			if (VanishConfig.CONFIG.hidePlayersFromWorld.get() && VanishUtil.isVanished(player))
				event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void onServerChat(ServerChatEvent event) {
		if (VanishUtil.isVanished(event.getPlayer()) && VanishConfig.CONFIG.hidePlayerNameInChat.get()) {
			Component message = event.getComponent();

			if (message instanceof TranslatableComponent && ((TranslatableComponent)message).getKey().contains("chat.type.text")) {
				TextComponent blurredName = new TextComponent("vanished");

				blurredName.withStyle(ChatFormatting.GRAY);
				event.setComponent(new TranslatableComponent("chat.type.text", blurredName, ((TranslatableComponent)message).getArgs()[1]));
			}
		}
	}
}
