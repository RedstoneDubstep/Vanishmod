package redstonedubstep.mods.vanishmod.compat;

import com.networkglitch.common.Definitions;
import com.networkglitch.joinleavemessages.Joinleavemessages;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class JoinLeaveMessagesCompat {
	public static Component getFakeJoinLeaveMessage(ServerPlayer player, boolean left) {
		if (left) {
			String playerName = player.getGameProfile().getName();
			Definitions.SendMessageResponse leaveMessage = Joinleavemessages.config.SendLeavingMessage(playerName);

			if (leaveMessage.getSendMessage()) {
				if (leaveMessage.getTranslateKey() == null)
					return Component.literal(leaveMessage.getCustomMessage());
				else
					return Component.translatable(leaveMessage.getTranslateKey(), playerName);
			}
		}
		else {
			Component playerName = player.getDisplayName();
			String plainPlayerName = playerName.getString();
			Definitions.SendMessageResponse joinMessage = Joinleavemessages.config.SendJoinMessage(plainPlayerName, plainPlayerName);

			if (joinMessage.getSendMessage()) {
				if (joinMessage.getTranslateKey() == null)
					return Component.literal(joinMessage.getCustomMessage());
				else
					return Component.translatable(joinMessage.getTranslateKey(), playerName, playerName);
			}
		}

		return null;
	}
}
