package redstonedubstep.mods.vanishmod.mixin.chat;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import redstonedubstep.mods.vanishmod.VanishConfig;
import redstonedubstep.mods.vanishmod.VanishUtil;

@Mixin(EntityArgument.class)
public abstract class MixinEntityArgument {
	//Prevent non-admins from targeting vanished players through their name or a selector, admins bypass this filtering
	@Redirect(method = "getPlayers", at = @At(value = "INVOKE", target = "Ljava/util/List;isEmpty()Z"))
	private static boolean redirectIsEmpty(List<ServerPlayer> list, CommandContext<CommandSourceStack> context) {
		if (VanishConfig.CONFIG.hidePlayersFromCommandSelectors.get()) {
			List<ServerPlayer> filteredList = VanishUtil.formatPlayerList(list, context.getSource().getEntity());

			return filteredList.isEmpty();
		}

		return list.isEmpty();
	}
}
