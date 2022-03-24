package redstonedubstep.mods.vanishmod.mixin.chat;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import redstonedubstep.mods.vanishmod.VanishConfig;
import redstonedubstep.mods.vanishmod.VanishUtil;

@Mixin(EntityArgument.class)
public abstract class MixinEntityArgument {
	//Prevent player that are not allowed to see vanished players from targeting them through their name or a selector
	@ModifyVariable(method = "getPlayers", at = @At(value = "INVOKE", target = "Ljava/util/List;isEmpty()Z", shift = Shift.BEFORE))
	private static List<ServerPlayer> redirectIsEmpty(List<ServerPlayer> originalList, CommandContext<CommandSourceStack> context) {
		if (VanishConfig.CONFIG.hidePlayersFromCommandSelectors.get() && context.getSource().getEntity() != null) //only filter commands from players, not command blocks/console/datapacks
			originalList = VanishUtil.formatPlayerList(originalList, context.getSource().getEntity());

		return originalList;
	}
}
