package redstonedubstep.mods.vanishmod.mixin.world;

import java.util.function.Predicate;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.EntityGetter;
import redstonedubstep.mods.vanishmod.VanishConfig;
import redstonedubstep.mods.vanishmod.VanishUtil;

@Mixin(EntityGetter.class)
public interface EntityGetterMixin {
	//Fixes mobs spawning naturally around vanished players, and experience orbs being magnetized towards them
	@WrapOperation(method = "getNearestPlayer(DDDDLjava/util/function/Predicate;)Lnet/minecraft/world/entity/player/Player;", at = @At(value = "INVOKE", target = "Ljava/util/function/Predicate;test(Ljava/lang/Object;)Z"))
	default <T> boolean vanishmod$checkNearestPlayerVanished(Predicate<T> predicate, T input, Operation<Boolean> original) {
		if (input instanceof Player player && VanishConfig.CONFIG.hidePlayersFromWorld.get()) {
			if ((predicate == EntitySelector.NO_CREATIVE_OR_SPECTATOR || predicate == EntitySelector.NO_SPECTATORS) && VanishUtil.isVanished(player))
				return false;
		}

		return original.call(predicate, input);
	}

	//Fixes mob spawners activating around vanished players
	@WrapOperation(method = "hasNearbyAlivePlayer", at = @At(value = "INVOKE", target = "Ljava/util/function/Predicate;test(Ljava/lang/Object;)Z", ordinal = 0))
	default <T> boolean vanishmod$checkNearbyAlivePlayerVanished(Predicate<T> predicate, T input, Operation<Boolean> original) {
		if (input instanceof Player player && VanishConfig.CONFIG.hidePlayersFromWorld.get() && VanishUtil.isVanished(player))
			return false;

		return original.call(predicate, input);
	}
}
