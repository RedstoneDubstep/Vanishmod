package redstonedubstep.mods.vanishmod.compat;

import java.util.Collections;

import com.mojang.authlib.GameProfile;

import fr.denisd3d.mc2discord.core.M2DUtils;
import fr.denisd3d.mc2discord.core.Mc2Discord;
import fr.denisd3d.mc2discord.core.MessageManager;
import fr.denisd3d.mc2discord.core.config.M2DConfig;
import fr.denisd3d.mc2discord.core.entities.Entity;
import fr.denisd3d.mc2discord.core.entities.PlayerEntity;
import fr.denisd3d.mc2discord.core.storage.HiddenPlayerEntry;
import fr.denisd3d.mc2discord.core.storage.HiddenPlayerList;
import net.minecraft.server.level.ServerPlayer;

public class Mc2DiscordCompat {
	public static void hidePlayer(ServerPlayer player, boolean hide) {
		HiddenPlayerList list = Mc2Discord.INSTANCE.hiddenPlayerList;
		GameProfile profile = player.getGameProfile();

		if (hide) {
			if (!list.contains(profile.getId()))
				list.add(new HiddenPlayerEntry(profile.getId()));
		}
		else if (list.contains(profile.getId()))
			list.remove(profile.getId());
	}

	public static void sendFakeJoinLeaveMessage(ServerPlayer player, boolean left) {
		if (M2DUtils.isNotConfigured())
			return;

		PlayerEntity playerEntity = new PlayerEntity(player.getGameProfile().getName(), player.getDisplayName().getString(), player.getGameProfile().getId());
		M2DConfig config = Mc2Discord.INSTANCE.config;
		MessageManager.sendInfoMessage("vanish", Entity.replace(left ? config.messages.leave.asString() : config.messages.join.asString(), Collections.singletonList(playerEntity))).subscribe();
	}
}
