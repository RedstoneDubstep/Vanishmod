package redstonedubstep.mods.vanishmod.mixin.world;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particles.IParticleData;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import redstonedubstep.mods.vanishmod.VanishConfig;
import redstonedubstep.mods.vanishmod.VanishUtil;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity {

	//Entity needs a constructor, so here we go
	private MixinLivingEntity(EntityType<?> entityType, World world) {
		super(entityType, world);
	}

	//Prevent particles from being created when a vanished player falls
	@Redirect(method = "checkFallDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/server/ServerWorld;sendParticles(Lnet/minecraft/particles/IParticleData;DDDIDDDD)I"))
	public <T extends IParticleData> int redirectSendParticles(ServerWorld serverWorld, T type, double posX, double posY, double posZ, int particleCount, double xOffset, double yOffset, double zOffset, double speed) {
		if (!VanishConfig.CONFIG.hidePlayersFromWorld.get() || !VanishUtil.isVanished(getUUID(), (ServerWorld)getCommandSenderWorld()))
			serverWorld.sendParticles(type, posX, posY, posZ, particleCount, xOffset, yOffset, zOffset, speed);

		return 0;
	}

	//Prevent pickup animation from being sent when a vanished player picks up an item. This fixes that the unvanished client thinks that it picked up an item (and thus shows a pickup animation for the local player) while in reality a vanished player did
	@Inject(method = "take", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/server/ServerChunkProvider;broadcast(Lnet/minecraft/entity/Entity;Lnet/minecraft/network/IPacket;)V"), cancellable = true)
	public void redirectBroadcast(Entity entity, int amount, CallbackInfo callbackInfo) {
		if (VanishConfig.CONFIG.hidePlayersFromWorld.get() && VanishUtil.isVanished(getUUID(), (ServerWorld)getCommandSenderWorld()))
			callbackInfo.cancel();
	}
}
