package redstonedubstep.mods.vanishmod.compat;

import java.util.Collections;
import java.util.Optional;

import com.mojang.authlib.GameProfile;

import ml.denisd3d.mc2discord.core.Mc2Discord;
import ml.denisd3d.mc2discord.core.config.M2DConfig;
import ml.denisd3d.mc2discord.core.entities.Entity;
import ml.denisd3d.mc2discord.core.entities.Player;
import ml.denisd3d.mc2discord.forge.MinecraftImpl;
import ml.denisd3d.mc2discord.forge.storage.HiddenPlayerEntry;
import ml.denisd3d.mc2discord.forge.storage.HiddenPlayerList;
import net.minecraft.entity.player.ServerPlayerEntity;

public class Mc2DiscordCompat {
	public static void hidePlayer(ServerPlayerEntity player, boolean hide) {
		HiddenPlayerList list = ((MinecraftImpl)Mc2Discord.INSTANCE.iMinecraft).hiddenPlayerList;
		GameProfile profile = player.getGameProfile();

		if (hide) {
			if (!list.isHidden(profile))
				list.add(new HiddenPlayerEntry(profile));
		}
		else if (list.isHidden(profile)) {
			list.remove(new HiddenPlayerEntry(profile));
		}
	}

	public static void sendFakeJoinLeaveMessage(ServerPlayerEntity player, boolean left) {
		Player mc2dcplayer = new Player(player.getGameProfile().getName(), player.getDisplayName().getString(), player.getGameProfile().getId());
		M2DConfig config = Mc2Discord.INSTANCE.config;

		Mc2Discord.INSTANCE.messageManager.sendInfoMessage(Entity.replace(left ? config.messages.leave : config.messages.join, Collections.singletonList(mc2dcplayer)));
	}
}
