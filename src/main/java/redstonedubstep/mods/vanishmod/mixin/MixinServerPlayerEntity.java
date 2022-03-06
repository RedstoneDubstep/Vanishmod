package redstonedubstep.mods.vanishmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.authlib.GameProfile;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import redstonedubstep.mods.vanishmod.VanishConfig;
import redstonedubstep.mods.vanishmod.VanishUtil;

@Mixin(ServerPlayerEntity.class)
public abstract class MixinServerPlayerEntity extends PlayerEntity {

	//player entity needs a constructor, so here we go
	public MixinServerPlayerEntity(World world, BlockPos pos, float f, GameProfile gameProfile) {
		super(world, pos, f, gameProfile);
	}

	//hacky mixin that should improve mod compat: mods should always respect spectator mode when targeting players, and this mixin lets isSpectator also check if the player is vanished (and thus should also not be targeted); but don't interfere with Vanilla's isSpectator() calls, else weird glitches can happen
	@Inject(method = "isSpectator", at = @At("HEAD"), cancellable = true)
	public void onIsSpectator(CallbackInfoReturnable<Boolean> callback) {
		if (VanishConfig.CONFIG.fixModCompat.get() && VanishUtil.isVanished(this)) {
			StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
			String className = stackTrace[3].getClassName(); //0 is getStackTrace(), 1 is this mixin's lambda, 2 is isSpectator(), 3 is the caller of isSpectator()

			try {
				Class<?> clazz = Class.forName(className);

				if (!clazz.getPackage().getName().startsWith("net.minecraft.")) { //if a mod calls this on a vanished player, then it is a spectator and should not be targeted
					callback.setReturnValue(true);
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
}
