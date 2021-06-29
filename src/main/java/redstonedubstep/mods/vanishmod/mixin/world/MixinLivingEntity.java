package redstonedubstep.mods.vanishmod.mixin.world;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.IPacket;
import net.minecraft.particles.IParticleData;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerWorld;
import redstonedubstep.mods.vanishmod.VanishUtil;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity {

	//Entity needs a constructor, so here we go
	private MixinLivingEntity(EntityType<?> entityType, World world) {
		super(entityType, world);
	}

	//Prevent particles from being created when a vanished player falls
	@Redirect(method = "updateFallState", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/server/ServerWorld;spawnParticle(Lnet/minecraft/particles/IParticleData;DDDIDDDD)I"))
	public <T extends IParticleData> int redirectSpawnParticle(ServerWorld serverWorld, T type, double posX, double posY, double posZ, int particleCount, double xOffset, double yOffset, double zOffset, double speed) {
		if (!VanishUtil.isVanished(this.getUniqueID(), (ServerWorld)this.getEntityWorld()))
			serverWorld.spawnParticle(type, posX, posY, posZ, particleCount, xOffset, yOffset, zOffset, speed);

		return 0;
	}

	//Prevent pickup animation from being sent when a vanished player picks up an item. This fixes that the unvanished client thinks that it picked up an item while in reality a vanished player did (due to Minecraft's code)
	@Redirect(method = "onItemPickup", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/server/ServerChunkProvider;sendToAllTracking(Lnet/minecraft/entity/Entity;Lnet/minecraft/network/IPacket;)V"))
	public void redirectSendToAllTracking(ServerChunkProvider chunkProvider, Entity item, IPacket<?> packet) {
		if (!VanishUtil.isVanished(this.getUniqueID(), (ServerWorld)this.getEntityWorld())) {
			chunkProvider.sendToAllTracking(item, packet);
		}
	}
}
