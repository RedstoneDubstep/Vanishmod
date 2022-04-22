package redstonedubstep.mods.vanishmod.mixin.sound;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.vector.Vector3d;
import redstonedubstep.mods.vanishmod.misc.SoundSuppressionHelper;

@Mixin(Entity.class)
public class MixinEntity {
	//Invalidates the hit results of a vanished player if its position changes, because then their crosshair is most likely on a different block
	@Inject(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;setBoundingBox(Lnet/minecraft/util/math/AxisAlignedBB;)V", ordinal = 1))
	private void onActualMove(MoverType type, Vector3d pos, CallbackInfo callbackInfo) {
		if ((Object)this instanceof ServerPlayerEntity)
			SoundSuppressionHelper.invalidateHitResults((ServerPlayerEntity)(Object)this);
	}
}
