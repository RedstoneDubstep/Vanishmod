package redstonedubstep.mods.vanishmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.authlib.GameProfile;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameRules.BooleanValue;
import net.minecraft.world.GameRules.RuleKey;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import redstonedubstep.mods.vanishmod.VanishUtil;

@Mixin(ServerPlayerEntity.class)
public abstract class MixinServerPlayerEntity extends PlayerEntity {

	//player entity needs a constructor, so here we go
	public MixinServerPlayerEntity(World world, BlockPos pos, float f, GameProfile gameProfile) {
		super(world, pos, f, gameProfile);
	}

	//suppress death messages when player is vanished by modification of the method that usually gets the value of the Gamerule showDeathMessages
	@Redirect(method = "onDeath", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/GameRules;getBoolean(Lnet/minecraft/world/GameRules$RuleKey;)Z", ordinal = 0))
	public boolean redirectGetBoolean(GameRules gameRules, RuleKey<BooleanValue> key) {
		if (VanishUtil.isVanished(this.getUniqueID(), (ServerWorld)this.getEntityWorld()))
			return false;

		return gameRules.getBoolean(key);
	}

	//hacky mixin that should improve mod compat: mods should always respect spectator mode when targeting players, and this mixin lets isSpectator also check if the player is vanished (and thus should also not be targeted); but don't interfere with Vanilla's isSpectator() calls, else weird glitches can happen
	@Inject(method = "isSpectator", at = @At("HEAD"), cancellable = true)
	public void onIsSpectator(CallbackInfoReturnable<Boolean> callback) {
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		String className = stackTrace[3].getClassName(); //0 is getStackTrace(), 1 is this mixin's lambda, 2 is isSpectator(), 3 is the caller of isSpectator()

		try {
			Class<?> clazz = Class.forName(className);

			if (!clazz.getPackage().getName().startsWith("net.minecraft.") && VanishUtil.isVanished(this)) { //if a mod calls this, check for the vanished status
				callback.setReturnValue(true);
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
