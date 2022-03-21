package redstonedubstep.mods.vanishmod.mixin.world;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import redstonedubstep.mods.vanishmod.VanishConfig;
import redstonedubstep.mods.vanishmod.VanishUtil;

@Mixin(ServerWorld.class)
public abstract class MixinServerWorld {
	//Prevent some (sound) level events produced by vanished players from being heard, note that this will only work if the caster is not null. This mixin needs to exist because the PlaySoundAtEntityEvent doesn't fire if a level event happens
	@Redirect(method = "levelEvent", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/management/PlayerList;broadcast(Lnet/minecraft/entity/player/PlayerEntity;DDDDLnet/minecraft/util/RegistryKey;Lnet/minecraft/network/IPacket;)V"))
	public void redirectBroadcast(PlayerList playerList, PlayerEntity caster, double x, double y, double z, double radius, RegistryKey<World> dimension, IPacket<?> packet) {
		if (!VanishConfig.CONFIG.hidePlayersFromWorld.get() || !VanishUtil.isVanished(caster))
			playerList.broadcast(caster, x, y, z, radius, dimension, packet);
	}

	//Fixes that vanished players are taken into account when calculating if the night can be skipped with the current amount of sleeping players
	@Redirect(method = "updateSleepingPlayerList", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/ServerPlayerEntity;isSpectator()Z"))
	public boolean redirectIsSpectator(ServerPlayerEntity instance) {
		if (VanishConfig.CONFIG.hidePlayersFromWorld.get() && VanishUtil.isVanished(instance))
			return true;

		return instance.isSpectator();
	}

	//Fixes that vanished players are taken into account when calculating if enough players are sleeping long enough for the night to be skipped
	@Redirect(method = "lambda$tick$4", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/ServerPlayerEntity;isSpectator()Z"))
	private static boolean redirectIsSpectatorInTick(ServerPlayerEntity instance) {
		if (VanishConfig.CONFIG.hidePlayersFromWorld.get() && VanishUtil.isVanished(instance))
			return true;

		return instance.isSpectator();
	}
}