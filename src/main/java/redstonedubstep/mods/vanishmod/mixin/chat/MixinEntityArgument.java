package redstonedubstep.mods.vanishmod.mixin.chat;

import java.util.List;
import java.util.stream.Collectors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.mojang.brigadier.context.CommandContext;

import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import redstonedubstep.mods.vanishmod.VanishConfig;
import redstonedubstep.mods.vanishmod.VanishUtil;

@Mixin(EntityArgument.class)
public abstract class MixinEntityArgument {

	//Prevent non-admins from targeting vanished players through their name or a selector, admins bypass this filtering
	@Redirect(method = "getPlayers", at = @At(value = "INVOKE", target = "Ljava/util/List;isEmpty()Z"))
	private static boolean redirectIsEmpty(List<ServerPlayerEntity> list, CommandContext<CommandSource> context) {
		if (VanishConfig.CONFIG.hidePlayersFromCommandSelectors.get() && !context.getSource().hasPermissionLevel(1)) {
			List<ServerPlayerEntity> filteredList = VanishUtil.formatPlayerList(list);

			return filteredList.isEmpty();
		}

		return list.isEmpty();
	}
}
