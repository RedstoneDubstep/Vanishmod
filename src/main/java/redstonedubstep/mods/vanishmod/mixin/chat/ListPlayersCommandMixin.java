package redstonedubstep.mods.vanishmod.mixin.chat;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.commands.ListPlayersCommand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import redstonedubstep.mods.vanishmod.VanishConfig;
import redstonedubstep.mods.vanishmod.VanishUtil;


@Mixin(ListPlayersCommand.class)
public class ListPlayersCommandMixin {
	//Filter result of /list command when non-admins execute it
	@Redirect(method = "format", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;getPlayers()Ljava/util/List;"))
	private static List<ServerPlayer> vanishmod$redirectGetPlayers(PlayerList playerList, CommandSourceStack source) {
		if (VanishConfig.CONFIG.hidePlayersFromPlayerLists.get())
			return VanishUtil.formatPlayerList(playerList.getPlayers(), source.getEntity());

		return playerList.getPlayers();
	}
}
