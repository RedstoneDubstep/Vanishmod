package redstonedubstep.mods.vanishmod.mixin.sound;

import java.util.Optional;
import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.datafixers.util.Either;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.profiler.IProfiler;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.ISpawnWorldInfo;
import redstonedubstep.mods.vanishmod.VanishConfig;
import redstonedubstep.mods.vanishmod.VanishUtil;
import redstonedubstep.mods.vanishmod.misc.SoundSuppressionHelper;

@Mixin(ServerWorld.class)
public abstract class MixinServerWorld extends World {
	//Level needs a constructor, so here we go
	private MixinServerWorld(ISpawnWorldInfo levelData, RegistryKey<World> dimension, DimensionType dimensionType, Supplier<IProfiler> profiler, boolean isClientSide, boolean isDebug, long biomeZoomSeed) {
		super(levelData, dimension, dimensionType, profiler, isClientSide, isDebug, biomeZoomSeed);
	}

	//Prevents some sound events that are produced by, but not directly related to a vanished player from being broadcast. The player argument is to be ignored because it is always null if the sound event is of relevance for us (see VanishEventListener#onPlaySound)
	@Inject(method = "playSound(Lnet/minecraft/entity/player/PlayerEntity;DDDLnet/minecraft/util/SoundEvent;Lnet/minecraft/util/SoundCategory;FF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/management/PlayerList;broadcast(Lnet/minecraft/entity/player/PlayerEntity;DDDDLnet/minecraft/util/RegistryKey;Lnet/minecraft/network/IPacket;)V"), cancellable = true)
	private void onBroadcastSoundEvent(PlayerEntity caster, double x, double y, double z, SoundEvent sound, SoundCategory category, float volume, float pitch, CallbackInfo callbackInfo) {
		if (VanishConfig.CONFIG.hidePlayersFromWorld.get() && shouldSuppressSoundEvent(Either.left(new Vector3d(x, y, z))))
			callbackInfo.cancel();
	}

	//Prevents some sound events that are produced by, but not directly related to a vanished player from being broadcast. The player argument is to be ignored because it is always null if the sound event is of relevance for us (see VanishEventListener#onPlaySound)
	@Inject(method = "playSound(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/entity/Entity;Lnet/minecraft/util/SoundEvent;Lnet/minecraft/util/SoundCategory;FF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/management/PlayerList;broadcast(Lnet/minecraft/entity/player/PlayerEntity;DDDDLnet/minecraft/util/RegistryKey;Lnet/minecraft/network/IPacket;)V"), cancellable = true)
	private void onBroadcastEntitySoundEvent(PlayerEntity caster, Entity entity, SoundEvent sound, SoundCategory category, float volume, float pitch, CallbackInfo callbackInfo) {
		if (VanishConfig.CONFIG.hidePlayersFromWorld.get() && shouldSuppressSoundEvent(Either.right(entity)))
			callbackInfo.cancel();
	}

	//Prevent some (sound) level events produced by vanished players from being heard. This mixin needs to exist because the PlaySoundAtEntityEvent doesn't fire if a level event happens
	@Inject(method = "globalLevelEvent", at = @At(value = "HEAD"), cancellable = true)
	private void onBroadcastGlobalLevelEvent(int type, BlockPos pos, int data, CallbackInfo callbackInfo) {
		if (VanishConfig.CONFIG.hidePlayersFromWorld.get() && shouldSuppressLevelEvent(Optional.empty(), pos))
			callbackInfo.cancel();
	}

	//Prevent some (sound) level events produced by vanished players from being heard. This mixin needs to exist because the PlaySoundAtEntityEvent doesn't fire if a level event happens
	@Inject(method = "levelEvent", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/management/PlayerList;broadcast(Lnet/minecraft/entity/player/PlayerEntity;DDDDLnet/minecraft/util/RegistryKey;Lnet/minecraft/network/IPacket;)V"), cancellable = true)
	private void onBroadcastLevelEvent(PlayerEntity caster, int type, BlockPos pos, int data, CallbackInfo callbackInfo) {
		if (VanishConfig.CONFIG.hidePlayersFromWorld.get() && shouldSuppressLevelEvent(Optional.ofNullable(caster), pos))
			callbackInfo.cancel();
	}

	//Returns true if it is determined that a vanished player was indirectly causing a sound, and that it thus should not be broadcast
	@Unique
	private boolean shouldSuppressSoundEvent(Either<Vector3d, Entity> soundOrigin) {
		if (soundOrigin.map(vec -> SoundSuppressionHelper.areVanishedPlayersAt(this, vec), entity -> SoundSuppressionHelper.areVanishedPlayersAt(this, entity.position())))
			return true;
		else if (soundOrigin.map(vec -> SoundSuppressionHelper.vanishedPlayerVehicleAt(this, vec), SoundSuppressionHelper::isVanishedPlayerVehicle))
			return true;
		else if (soundOrigin.map(vec -> SoundSuppressionHelper.vanishedPlayersInteractWith(this, new BlockPos(vec)), entity -> false))
			return true;
		else
			return soundOrigin.map(pos -> false, entity -> SoundSuppressionHelper.vanishedPlayersInteractWith(this, entity));
	}

	//Returns true if a vanished player directly produced the event, or if it is determined that a vanished player was indirectly causing it, and that it thus should not be broadcast
	@Unique
	private boolean shouldSuppressLevelEvent(Optional<PlayerEntity> player, BlockPos soundOrigin) {
		if (player.isPresent() && VanishUtil.isVanished(player.get()))
			return true;
		else return SoundSuppressionHelper.areVanishedPlayersAt(this, new Vector3d(soundOrigin.getX(), soundOrigin.getY(), soundOrigin.getZ())) || SoundSuppressionHelper.vanishedPlayersInteractWith(this, soundOrigin);
	}
}