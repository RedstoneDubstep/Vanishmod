package redstonedubstep.mods.vanishmod.compat;

import java.util.Collections;
import java.util.Optional;

import com.mojang.authlib.GameProfile;

import ml.denisd3d.minecraft2discord.core.Minecraft2Discord;
import ml.denisd3d.minecraft2discord.core.config.M2DConfig;
import ml.denisd3d.minecraft2discord.core.entities.Entity;
import ml.denisd3d.minecraft2discord.core.entities.Player;
import ml.denisd3d.minecraft2discord.forge.MinecraftImpl;
import ml.denisd3d.minecraft2discord.forge.storage.HiddenPlayerEntry;
import ml.denisd3d.minecraft2discord.forge.storage.HiddenPlayerList;
import net.minecraft.entity.player.ServerPlayerEntity;

public class Mc2DiscordCompat {
	public static void hidePlayer(ServerPlayerEntity player, boolean hide) {
		HiddenPlayerList list = ((MinecraftImpl)Minecraft2Discord.INSTANCE.iMinecraft).hiddenPlayerList;
		GameProfile profile = player.getGameProfile();

		if (hide) {
			if (!list.isHidden(profile))
				list.addEntry(new HiddenPlayerEntry(profile));
		}
		else if (list.isHidden(profile)) {
			list.removeEntry(new HiddenPlayerEntry(profile));
		}
	}

	public static void sendPlayerStatusMessage(ServerPlayerEntity player, boolean left) {
		Player mc2dcplayer = new Player(player.getGameProfile().getName(), player.getDisplayName().getString(), Optional.ofNullable(player.getGameProfile().getId()).orElse(null));
		M2DConfig config = Minecraft2Discord.INSTANCE.config;

		Minecraft2Discord.INSTANCE.messageManager.sendInfoMessage(Entity.replace(left ? config.leave_message : config.join_message, Collections.singletonList(mc2dcplayer)));
	}
}
