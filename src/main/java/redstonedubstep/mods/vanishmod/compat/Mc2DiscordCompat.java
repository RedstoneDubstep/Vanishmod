package redstonedubstep.mods.vanishmod.compat;

import com.mojang.authlib.GameProfile;

import ml.denisd3d.minecraft2discord.core.Minecraft2Discord;
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
}
