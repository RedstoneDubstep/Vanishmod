package redstonedubstep.mods.vanishmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameRules.BooleanValue;
import net.minecraft.world.GameRules.RuleKey;
import redstonedubstep.mods.vanishmod.VanishUtil;

@Mixin(PlayerAdvancements.class)
public abstract class MixinPlayerAdvancements {
	@Shadow
	private ServerPlayerEntity player;

	//suppress advancement messages for vanished players
	@Redirect(method = "grantCriterion", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/GameRules;getBoolean(Lnet/minecraft/world/GameRules$RuleKey;)Z"))
	private boolean redirectGetBoolean(GameRules gameRules, RuleKey<BooleanValue> key) {
		if (VanishUtil.isVanished(this.player))
			return false;

		return gameRules.getBoolean(key);
	}
}
