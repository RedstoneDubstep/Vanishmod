package redstonedubstep.mods.vanishmod.mixin.chat;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.EntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import redstonedubstep.mods.vanishmod.VanishConfig;
import redstonedubstep.mods.vanishmod.VanishUtil;

@Mixin(EntitySelector.class)
public class MixinEntitySelector {
	//Prevent players that are not allowed to see vanished players from targeting them through their name or a selector (3/4)
	@ModifyVariable(method = "findSingleEntity", at = @At(value = "INVOKE", target = "Ljava/util/List;isEmpty()Z", shift = Shift.BEFORE))
	private List<? extends Entity> modifyEntityList(List<? extends Entity> originalList, CommandSource source) {
		if (VanishConfig.CONFIG.hidePlayersFromCommandSelectors.get() && source.getEntity() != null) //only filter commands from players, not command blocks/console/datapacks
			originalList = VanishUtil.formatEntityList(originalList, source.getEntity());

		return originalList;
	}

	//Prevent players that are not allowed to see vanished players from targeting them through their name or a selector (4/4)
	@ModifyVariable(method = "findSinglePlayer", at = @At(value = "INVOKE", target = "Ljava/util/List;size()I", shift = Shift.BEFORE))
	private List<ServerPlayerEntity> modifyPlayerList(List<ServerPlayerEntity> originalList, CommandSource source) {
		if (VanishConfig.CONFIG.hidePlayersFromCommandSelectors.get() && source.getEntity() != null) //only filter commands from players, not command blocks/console/datapacks
			originalList = VanishUtil.formatPlayerList(originalList, source.getEntity());

		return originalList;
	}
}
