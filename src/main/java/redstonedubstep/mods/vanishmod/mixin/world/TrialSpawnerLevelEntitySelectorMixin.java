package redstonedubstep.mods.vanishmod.mixin.world;

import java.util.List;
import java.util.function.Predicate;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import redstonedubstep.mods.vanishmod.VanishConfig;
import redstonedubstep.mods.vanishmod.VanishUtil;

@Mixin(targets = {"net.minecraft.world.level.block.entity.trialspawner.PlayerDetector$EntitySelector$1"})
public class TrialSpawnerLevelEntitySelectorMixin {
	//Prevents vanished players from revealing themselves by activating trial spawners and vaults when close to them
	@WrapOperation(method = "getPlayers", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;getPlayers(Ljava/util/function/Predicate;)Ljava/util/List;"))
	private List<? extends Player> vanishmod$filterTrialSpawnerPlayerList(ServerLevel instance, Predicate<? super ServerPlayer> playerCheck, Operation<List<ServerPlayer>> original) {
		List<? extends Player> nearbyPlayers = original.call(instance, playerCheck);

		if (!nearbyPlayers.isEmpty() && VanishConfig.CONFIG.hidePlayersFromWorld.get())
			nearbyPlayers = VanishUtil.removeVanishedFromPlayerList(nearbyPlayers, null);

		return nearbyPlayers;
	}
}
