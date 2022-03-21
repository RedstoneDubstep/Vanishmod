package redstonedubstep.mods.vanishmod.mixin.world;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.SleepStatus;
import redstonedubstep.mods.vanishmod.VanishUtil;

@Mixin(SleepStatus.class)
public class MixinSleepStatus {
	@ModifyVariable(method = "update", at = @At(value = "HEAD"), argsOnly = true)
	public List<ServerPlayer> updatePlayers(List<ServerPlayer> original) {
		return VanishUtil.formatPlayerList(original);
	}
}
