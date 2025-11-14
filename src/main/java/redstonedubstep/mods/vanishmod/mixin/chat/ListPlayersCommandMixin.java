package redstonedubstep.mods.vanishmod.mixin.chat;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.commands.ListPlayersCommand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import redstonedubstep.mods.vanishmod.VanishConfig;
import redstonedubstep.mods.vanishmod.VanishUtil;

@Mixin(ListPlayersCommand.class)
public class ListPlayersCommandMixin {
	//Filter result of the /list command when non-permitted players use it
	@WrapOperation(method = "format", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;getPlayers()Ljava/util/List;"))
	private static List<ServerPlayer> vanishmod$redirectGetPlayers(PlayerList playerList, Operation<List<ServerPlayer>> original, CommandSourceStack source) {
		List<ServerPlayer> originalList = original.call(playerList);

		if (VanishConfig.CONFIG.hidePlayersFromPlayerLists.get())
			return VanishUtil.removeVanishedFromPlayerList(originalList, source.getEntity());

		return originalList;
	}
}
