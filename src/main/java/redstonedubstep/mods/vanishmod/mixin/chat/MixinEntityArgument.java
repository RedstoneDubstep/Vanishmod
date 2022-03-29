package redstonedubstep.mods.vanishmod.mixin.chat;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.mojang.brigadier.context.CommandContext;

import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import redstonedubstep.mods.vanishmod.VanishConfig;
import redstonedubstep.mods.vanishmod.VanishUtil;

@Mixin(EntityArgument.class)
public abstract class MixinEntityArgument {
	//Prevent players that are not allowed to see vanished players from targeting them through their name or a selector (1/4)
	@ModifyVariable(method = "getEntities", at = @At(value = "INVOKE", target = "Ljava/util/Collection;isEmpty()Z", shift = Shift.BEFORE))
	private static Collection<? extends Entity> modifyEntityList(Collection<? extends Entity> originalList, CommandContext<CommandSource> context) {
		if (VanishConfig.CONFIG.hidePlayersFromCommandSelectors.get() && context.getSource().getEntity() != null) //only filter commands from players, not command blocks/console/datapacks
			originalList = VanishUtil.formatEntityList(originalList.stream().collect(Collectors.toList()), context.getSource().getEntity());

		return originalList;
	}

	//Prevent players that are not allowed to see vanished players from targeting them through their name or a selector (2/4)
	@ModifyVariable(method = "getPlayers", at = @At(value = "INVOKE", target = "Ljava/util/List;isEmpty()Z", shift = Shift.BEFORE))
	private static List<ServerPlayerEntity> modifyPlayerList(List<ServerPlayerEntity> originalList, CommandContext<CommandSource> context) {
		if (VanishConfig.CONFIG.hidePlayersFromCommandSelectors.get() && context.getSource().getEntity() != null) //only filter commands from players, not command blocks/console/datapacks
			originalList = VanishUtil.formatPlayerList(originalList, context.getSource().getEntity());

		return originalList;
	}
}
