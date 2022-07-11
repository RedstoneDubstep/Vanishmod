package redstonedubstep.mods.vanishmod.mixin.sound;

import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.datafixers.util.Either;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.phys.Vec3;
import redstonedubstep.mods.vanishmod.VanishConfig;
import redstonedubstep.mods.vanishmod.VanishUtil;
import redstonedubstep.mods.vanishmod.misc.SoundSuppressionHelper;

@Mixin(ServerLevel.class)
public abstract class MixinServerLevel extends Level {
	//Level needs a constructor, so here we go
	private MixinServerLevel(WritableLevelData levelData, ResourceKey<Level> dimension, Holder<DimensionType> dimensionType, Supplier<ProfilerFiller> profiler, boolean isClientSide, boolean isDebug, long biomeZoomSeed) {
		super(levelData, dimension, dimensionType, profiler, isClientSide, isDebug, biomeZoomSeed);
	}

	//Prevents some sound events that are produced by, but not directly related to a vanished player from being broadcast. The player argument is to be ignored because it is always null if the sound event is of relevance for us (see VanishEventListener#onPlaySound)
	@Inject(method = "playSound(Lnet/minecraft/world/entity/player/Player;DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcast(Lnet/minecraft/world/entity/player/Player;DDDDLnet/minecraft/resources/ResourceKey;Lnet/minecraft/network/protocol/Packet;)V"), cancellable = true)
	private void onBroadcastSoundEvent(Player caster, double x, double y, double z, SoundEvent sound, SoundSource category, float volume, float pitch, CallbackInfo callbackInfo) {
		if (VanishConfig.CONFIG.hidePlayersFromWorld.get() && shouldSuppressSoundEvent(Either.left(new Vec3(x, y, z))))
			callbackInfo.cancel();
	}

	//Prevents some sound events that are produced by, but not directly related to a vanished player from being broadcast. The player argument is to be ignored because it is always null if the sound event is of relevance for us (see VanishEventListener#onPlaySound)
	@Inject(method = "playSound(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcast(Lnet/minecraft/world/entity/player/Player;DDDDLnet/minecraft/resources/ResourceKey;Lnet/minecraft/network/protocol/Packet;)V"), cancellable = true)
	private void onBroadcastEntitySoundEvent(Player caster, Entity entity, SoundEvent sound, SoundSource category, float volume, float pitch, CallbackInfo callbackInfo) {
		if (VanishConfig.CONFIG.hidePlayersFromWorld.get() && shouldSuppressSoundEvent(Either.right(entity)))
			callbackInfo.cancel();
	}

	//Prevent some (sound) level events produced by vanished players from being heard. This mixin needs to exist because the PlaySoundAtEntityEvent doesn't fire if a level event happens
	@Inject(method = "globalLevelEvent", at = @At(value = "HEAD"), cancellable = true)
	private void onBroadcastGlobalLevelEvent(int type, BlockPos pos, int data, CallbackInfo callbackInfo) {
		if (VanishConfig.CONFIG.hidePlayersFromWorld.get() && shouldSuppressLevelEvent(null, pos))
			callbackInfo.cancel();
	}

	//Prevent some (sound) level events produced by vanished players from being heard. This mixin needs to exist because the PlaySoundAtEntityEvent doesn't fire if a level event happens
	@Inject(method = "levelEvent", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcast(Lnet/minecraft/world/entity/player/Player;DDDDLnet/minecraft/resources/ResourceKey;Lnet/minecraft/network/protocol/Packet;)V"), cancellable = true)
	private void onBroadcastLevelEvent(Player caster, int type, BlockPos pos, int data, CallbackInfo callbackInfo) {
		if (VanishConfig.CONFIG.hidePlayersFromWorld.get() && shouldSuppressLevelEvent(caster, pos))
			callbackInfo.cancel();
	}

	//Returns true if it is determined that a vanished player was indirectly causing a sound, and that it thus should not be broadcast
	@Unique
	private boolean shouldSuppressSoundEvent(Either<Vec3, Entity> soundOrigin) {
		if (!VanishConfig.CONFIG.indirectSoundSuppression.get())
			return false;

		if (SoundSuppressionHelper.areVanishedPlayersAt(this, soundOrigin.map(vec -> vec, Entity::position)))
			return true;
		else if (soundOrigin.map(vec -> SoundSuppressionHelper.vanishedPlayerVehicleAt(this, vec), SoundSuppressionHelper::isVanishedPlayerVehicle))
			return true;
		else
			return soundOrigin.map(vec -> SoundSuppressionHelper.vanishedPlayersInteractWith(this, new BlockPos(vec)), entity -> SoundSuppressionHelper.vanishedPlayersInteractWith(this, entity));
	}

	//Returns true if a vanished player directly produced the event, or if it is determined that a vanished player was indirectly causing it, and that it thus should not be broadcast
	@Unique
	private boolean shouldSuppressLevelEvent(Player player, BlockPos soundOrigin) {
		if (VanishUtil.isVanished(player))
			return true;
		else
			return VanishConfig.CONFIG.indirectSoundSuppression.get() && SoundSuppressionHelper.areVanishedPlayersAt(this, new Vec3(soundOrigin.getX(), soundOrigin.getY(), soundOrigin.getZ())) || SoundSuppressionHelper.vanishedPlayersInteractWith(this, soundOrigin);
	}
}