package redstonedubstep.mods.vanishmod.mixin.world;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.network.protocol.Packet;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import redstonedubstep.mods.vanishmod.VanishConfig;
import redstonedubstep.mods.vanishmod.VanishUtil;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity {

	//Entity needs a constructor, so here we go
	private MixinLivingEntity(EntityType<?> entityType, Level world) {
		super(entityType, world);
	}

	//Prevent particles from being created when a vanished player falls
	@Redirect(method = "checkFallDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;sendParticles(Lnet/minecraft/core/particles/ParticleOptions;DDDIDDDD)I"))
	public <T extends ParticleOptions> int redirectSendParticles(ServerLevel serverWorld, T type, double posX, double posY, double posZ, int particleCount, double xOffset, double yOffset, double zOffset, double speed) {
		if (!VanishConfig.CONFIG.hidePlayersFromWorld.get() || !VanishUtil.isVanished(getUUID(), (ServerLevel)getCommandSenderWorld()))
			serverWorld.sendParticles(type, posX, posY, posZ, particleCount, xOffset, yOffset, zOffset, speed);

		return 0;
	}

	//Prevent pickup animation from being sent when a vanished player picks up an item. This fixes that the unvanished client thinks that it picked up an item while in reality a vanished player did (due to Minecraft's code)
	@Redirect(method = "take", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerChunkCache;broadcast(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/network/protocol/Packet;)V"))
	public void redirectBroadcast(ServerChunkCache chunkProvider, Entity item, Packet<?> packet) {
		if (!VanishConfig.CONFIG.hidePlayersFromWorld.get() || !VanishUtil.isVanished(getUUID(), (ServerLevel)getCommandSenderWorld())) {
			chunkProvider.broadcast(item, packet);
		}
	}
}
