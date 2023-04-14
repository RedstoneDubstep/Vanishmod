package redstonedubstep.mods.vanishmod.mixin.world;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import redstonedubstep.mods.vanishmod.VanishConfig;
import redstonedubstep.mods.vanishmod.VanishUtil;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
	//Entity needs a constructor, so here we go
	private LivingEntityMixin(EntityType<?> entityType, Level world) {
		super(entityType, world);
	}

	//Prevent entities like passive mobs or pufferfish from detecting vanished players
	@Inject(method = "canBeSeenByAnyone", at = @At("HEAD"), cancellable = true)
	public void vanishmod$onCanBeSeen(CallbackInfoReturnable<Boolean> callbackInfo) {
		if (VanishConfig.CONFIG.hidePlayersFromWorld.get() && VanishUtil.isVanished(this))
			callbackInfo.setReturnValue(false);
	}
}
