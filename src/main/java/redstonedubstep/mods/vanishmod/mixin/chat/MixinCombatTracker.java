package redstonedubstep.mods.vanishmod.mixin.chat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.CombatEntry;
import net.minecraft.world.damagesource.CombatTracker;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.server.ServerLifecycleHooks;
import redstonedubstep.mods.vanishmod.VanishUtil;

@Mixin(CombatTracker.class)
public class MixinCombatTracker {
	//Change the death message of an unvanished player to the generic one if it was killed by a vanished player
	@Inject(method = "getDeathMessage", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
	private void onGetDeathMessage(CallbackInfoReturnable<Component> callbackInfo, CombatEntry combatEntry1, CombatEntry combatEntry2, Component attackerName, Entity source, Component deathMessage) {
		if (deathMessage.getContents() instanceof TranslatableContents content && content.getArgs().length > 1 && content.getArgs()[1] instanceof Component playerName) {
			for (ServerPlayer killer : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
				if (killer.getDisplayName().getString().equals(playerName.getString()) && VanishUtil.isVanished(killer))
					callbackInfo.setReturnValue(Component.translatable("death.attack.generic", content.getArgs()[0]));
			}
		}
	}
}
