package redstonedubstep.mods.vanishmod.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import redstonedubstep.mods.vanishmod.VanishUtil;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity {

	@Redirect(method = "updatePotionMetadata", at=@At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;setInvisible(Z)V"))
	public void onSetInvisible(LivingEntity livingEntity, boolean invisible) {
		if (livingEntity instanceof ServerPlayerEntity) {
			ServerPlayerEntity player = (ServerPlayerEntity)livingEntity;

			System.out.println("SetInvisible has been called!");
			player.setInvisible(invisible || VanishUtil.isVanished(player));
		}
	}
}
