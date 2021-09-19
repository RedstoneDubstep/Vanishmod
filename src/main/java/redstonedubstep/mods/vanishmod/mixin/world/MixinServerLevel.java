package redstonedubstep.mods.vanishmod.mixin.world;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.world.entity.player.Player;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.players.PlayerList;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import redstonedubstep.mods.vanishmod.VanishConfig;
import redstonedubstep.mods.vanishmod.VanishUtil;

@Mixin(ServerLevel.class)
public abstract class MixinServerLevel {

	//Prevent some (sound) level events produced by vanished players from being heard, note that this will only work if the caster is not null. This mixin needs to exist because the PlaySoundAtEntityEvent doesn't fire if a level event happens
	@Redirect(method = "levelEvent", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcast(Lnet/minecraft/world/entity/player/Player;DDDDLnet/minecraft/resources/ResourceKey;Lnet/minecraft/network/protocol/Packet;)V"))
	public void redirectBroadcast(PlayerList playerList, Player caster, double x, double y, double z, double radius, ResourceKey<Level> dimension, Packet<?> packet) {
		if (!VanishConfig.CONFIG.hidePlayersFromWorld.get() || !VanishUtil.isVanished(caster)) {
			playerList.broadcast(caster, x, y, z, radius, dimension, packet);
		}
	}
}