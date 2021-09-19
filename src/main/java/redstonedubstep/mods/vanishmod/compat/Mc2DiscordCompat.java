package redstonedubstep.mods.vanishmod.compat;

import net.minecraft.server.level.ServerPlayer;

public class Mc2DiscordCompat {
	public static void hidePlayer(ServerPlayer player, boolean hide) {
		/*HiddenPlayerList list = ((MinecraftImpl)Mc2Discord.INSTANCE.iMinecraft).hiddenPlayerList;
		GameProfile profile = player.getGameProfile();

		if (hide) {
			if (!list.isHidden(profile))
				list.add(new HiddenPlayerEntry(profile));
		}
		else if (list.isHidden(profile)) {
			list.remove(new HiddenPlayerEntry(profile));
		}*/
	}

	public static void sendPlayerStatusMessage(ServerPlayer player, boolean left) {
		/*Player mc2dcplayer = new Player(player.getGameProfile().getName(), player.getDisplayName().getString(), Optional.ofNullable(player.getGameProfile().getId()).orElse(null));
		M2DConfig config = Mc2Discord.INSTANCE.config;

		Mc2Discord.INSTANCE.messageManager.sendInfoMessage(Entity.replace(left ? config.leave_message : config.join_message, Collections.singletonList(mc2dcplayer)));*/
	}
}
