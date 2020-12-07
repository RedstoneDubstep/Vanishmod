package redstonedubstep.mods.vanishmod.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import redstonedubstep.mods.vanishmod.VanishUtil;

@Mixin(ServerWorld.class)
public abstract class MixinServerWorld {

	//Part 1 of a way to prevent sounds from vanished players being sent, other part is in MixinPlayerList#onSendToAlNearExcept, "playMovingSound" shoudl also be added
	@Redirect(method={"playSound", "playMovingSound", "playEvent"}, at=@At(value="INVOKE", target="Lnet/minecraft/server/management/PlayerList;sendToAllNearExcept(Lnet/minecraft/entity/player/PlayerEntity;DDDDLnet/minecraft/util/RegistryKey;Lnet/minecraft/network/IPacket;)V"))
	public void redirectSendToAllNearExcept(PlayerList playerList, PlayerEntity except, double x, double y, double z, double radius, RegistryKey<World> dimension, IPacket<?> packet) {
		VanishUtil.isMixinInvolved = true;
		playerList.sendToAllNearExcept(except, x, y, z, radius, dimension, packet);
	}
}
