package redstonedubstep.mods.vanishmod.mixin.sound;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import redstonedubstep.mods.vanishmod.misc.SoundSuppressionHelper;

@Mixin(Entity.class)
public class EntityMixin {
	//Invalidates the hit results of a vanished player if its position changes, because then their crosshair is most likely on a different block
	@Inject(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;setPos(DDD)V", ordinal = 1))
	private void vanishmod$onActualMove(MoverType type, Vec3 pos, CallbackInfo callbackInfo) {
		if (SoundSuppressionHelper.shouldCapturePlayers() && (Object) this instanceof ServerPlayer player && player.containerMenu == player.inventoryMenu)
			SoundSuppressionHelper.invalidateHitResults(player);
	}
}
