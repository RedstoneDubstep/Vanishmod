package redstonedubstep.mods.vanishmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.authlib.GameProfile;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import redstonedubstep.mods.vanishmod.VanishConfig;
import redstonedubstep.mods.vanishmod.VanishUtil;

@Mixin(ServerPlayer.class)
public abstract class MixinServerPlayer extends Player {
	//player entity needs a constructor, so here we go
	public MixinServerPlayer(Level world, BlockPos pos, float angle, GameProfile gameProfile) {
		super(world, pos, angle, gameProfile);
	}

	//hacky mixin that should improve mod compat: mods should always respect spectator mode when targeting players, and this mixin lets isSpectator also check if the player is vanished (and thus should also not be targeted); but don't interfere with Vanilla's isSpectator() calls, else weird glitches can happen
	@Inject(method = "isSpectator", at = @At("HEAD"), cancellable = true)
	public void onIsSpectator(CallbackInfoReturnable<Boolean> callback) {
		if (VanishConfig.CONFIG.fixModCompatibility.get() && VanishUtil.isVanished(this)) {
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
