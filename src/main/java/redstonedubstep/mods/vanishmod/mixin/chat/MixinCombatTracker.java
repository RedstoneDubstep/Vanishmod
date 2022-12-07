package redstonedubstep.mods.vanishmod.mixin.chat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.CombatTracker;
import net.minecraftforge.server.ServerLifecycleHooks;
import redstonedubstep.mods.vanishmod.VanishUtil;

@Mixin(CombatTracker.class)
public abstract class MixinCombatTracker {
	@Unique
	private boolean skipInject;

	@Shadow
	public abstract Component getDeathMessage();

	//Change the death message of an unvanished player to the generic one if it was killed by a vanished player.
	//I would love to use the LVT instead of recursion to get the intended death message, but the LVT of getDeathMessage is different in development than in the actual code for some reason.
	@Inject(method = "getDeathMessage", at = @At("HEAD"), cancellable = true)
	private void onGetDeathMessage(CallbackInfoReturnable<Component> callbackInfo) {
		if (!skipInject) {
			skipInject = true;

			Component deathMessage = getDeathMessage();

			skipInject = false;

			if (deathMessage instanceof TranslatableComponent component && component.getArgs().length > 1 && component.getArgs()[1] instanceof Component playerName) {
				for (ServerPlayer killer : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
					if (killer.getDisplayName().getString().equals(playerName.getString()) && VanishUtil.isVanished(killer))
						callbackInfo.setReturnValue(new TranslatableComponent("death.attack.generic", component.getArgs()[0]));
				}
			}
		}
	}
}
