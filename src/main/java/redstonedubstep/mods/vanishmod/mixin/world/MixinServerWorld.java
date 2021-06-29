package redstonedubstep.mods.vanishmod.mixin.world;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import redstonedubstep.mods.vanishmod.VanishConfig;
import redstonedubstep.mods.vanishmod.VanishUtil;

@Mixin(ServerWorld.class)
public abstract class MixinServerWorld {

	//Prevent some sound events produced by vanished players from being heard, note that this will only work if the caster is not null. This mixin needs to exist because the PlaySoundAtEntityEvent doesn't fire if an event happens
	@Redirect(method = "playEvent", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/management/PlayerList;sendToAllNearExcept(Lnet/minecraft/entity/player/PlayerEntity;DDDDLnet/minecraft/util/RegistryKey;Lnet/minecraft/network/IPacket;)V"))
	public void redirectSendToAllNearExcept(PlayerList playerList, PlayerEntity caster, double x, double y, double z, double radius, RegistryKey<World> dimension, IPacket<?> packet) {
		if (!VanishConfig.CONFIG.hidePlayersFromWorld.get() || !VanishUtil.isVanished(caster)) {
			playerList.sendToAllNearExcept(caster, x, y, z, radius, dimension, packet);
		}
	}
}