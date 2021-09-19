package redstonedubstep.mods.vanishmod.mixin.chat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameRules.BooleanValue;
import net.minecraft.world.level.GameRules.Key;
import redstonedubstep.mods.vanishmod.VanishUtil;

@Mixin(PlayerAdvancements.class)
public abstract class MixinPlayerAdvancements {
	@Shadow
	private ServerPlayer player;

	//suppress advancement messages for vanished players
	@Redirect(method = "award", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/GameRules;getBoolean(Lnet/minecraft/world/level/GameRules$Key;)Z"))
	private boolean redirectGetBoolean(GameRules gameRules, Key<BooleanValue> key) {
		if (VanishUtil.isVanished(player))
			return false;

		return gameRules.getBoolean(key);
	}
}
