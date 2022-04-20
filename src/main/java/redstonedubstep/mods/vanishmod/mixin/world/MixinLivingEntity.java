package redstonedubstep.mods.vanishmod.mixin.world;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
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
		if (!VanishConfig.CONFIG.hidePlayersFromWorld.get() || !VanishUtil.isVanished(getCommandSenderWorld().getPlayerByUUID(getUUID())))
			serverWorld.sendParticles(type, posX, posY, posZ, particleCount, xOffset, yOffset, zOffset, speed);

		return 0;
	}

	//Prevent pickup animation from being sent when a vanished player picks up an item. This fixes that the unvanished client thinks that it picked up an item (and thus shows a pickup animation for the local player) while in reality a vanished player did
	@Inject(method = "take", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerChunkCache;broadcast(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/network/protocol/Packet;)V"), cancellable = true)
	public void redirectBroadcast(Entity entity, int amount, CallbackInfo callbackInfo) {
		if (VanishConfig.CONFIG.hidePlayersFromWorld.get() && VanishUtil.isVanished(getCommandSenderWorld().getPlayerByUUID(getUUID())))
			callbackInfo.cancel();
	}

	//Suppress eating sound when a vanished player finishes eating
	@ModifyArg(method = "eat", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;playSound(Lnet/minecraft/world/entity/player/Player;DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V"))
	private SoundEvent removeEatingSound(SoundEvent soundEvent) {
		if (VanishConfig.CONFIG.hidePlayersFromWorld.get() && VanishUtil.isVanished(getCommandSenderWorld().getPlayerByUUID(getUUID())))
			soundEvent = null;

		return soundEvent;
	}
}
