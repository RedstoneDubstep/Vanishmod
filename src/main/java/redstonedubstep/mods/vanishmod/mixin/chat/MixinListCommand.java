package redstonedubstep.mods.vanishmod.mixin.chat;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.command.CommandSource;
import net.minecraft.command.impl.ListCommand;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.management.PlayerList;
import redstonedubstep.mods.vanishmod.VanishConfig;
import redstonedubstep.mods.vanishmod.VanishUtil;


@Mixin(ListCommand.class)
public abstract class MixinListCommand {
	//Filter result of /list command when non-admins execute it
	@Redirect(method = "format", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/management/PlayerList;getPlayers()Ljava/util/List;"))
	private static List<ServerPlayerEntity> redirectGetPlayers(PlayerList playerList, CommandSource source) {
		if (VanishConfig.CONFIG.hidePlayersFromPlayerLists.get()) {
			return VanishUtil.formatPlayerList(playerList.getPlayers(), source.getEntity());
		}

		return playerList.getPlayers();
	}
}
