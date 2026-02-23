package redstonedubstep.mods.vanishmod.mixin.sound;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import redstonedubstep.mods.vanishmod.VanishConfig;
import redstonedubstep.mods.vanishmod.VanishUtil;
import redstonedubstep.mods.vanishmod.misc.SoundSuppressionHelper;

@Mixin(Entity.class)
public class EntityMixin {
	//Invalidates the hit results of a vanished player if its position changes, because then their crosshair is most likely on a different block
	@Inject(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;setPos(Lnet/minecraft/world/phys/Vec3;)V"))
	private void vanishmod$onActualMove(MoverType type, Vec3 pos, CallbackInfo callbackInfo) {
		if ((Object) this instanceof ServerPlayer player && SoundSuppressionHelper.shouldCapturePlayers() && !player.hasContainerOpen())
			SoundSuppressionHelper.invalidateHitResults(player);
	}

	//Makes a vanished player pretend that they are invisible serverside, so e.g. minimap mods hide those players
	@Inject(method = "isInvisible", at = @At("HEAD"), cancellable = true)
	private void vanishmod$isInvisible(CallbackInfoReturnable<Boolean> callbackInfo) {
		if (VanishUtil.isVanished(this, p -> VanishConfig.CONFIG.spoofVanishedPlayerInvisibility.get()))
			callbackInfo.setReturnValue(true);
	}
}
