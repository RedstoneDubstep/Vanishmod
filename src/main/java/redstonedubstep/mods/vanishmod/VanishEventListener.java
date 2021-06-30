package redstonedubstep.mods.vanishmod;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
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
				StringTextComponent blurredName = new StringTextComponent("vanished");

				blurredName.mergeStyle(TextFormatting.GRAY);
				event.setComponent(new TranslationTextComponent("chat.type.text", blurredName, ((TranslationTextComponent)message).getFormatArgs()[1]));
			}
		}
	}
}
