package redstonedubstep.mods.vanishmod.mixin.chat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.CombatTracker;
import net.minecraftforge.server.ServerLifecycleHooks;
import redstonedubstep.mods.vanishmod.VanishUtil;

@Mixin(CombatTracker.class)
public class CombatTrackerMixin {
	//Change the death message of an unvanished player to the generic one if it was killed by a vanished player
	@ModifyVariable(method = "getDeathMessage", slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/world/damagesource/CombatEntry;getAttackerName()Lnet/minecraft/network/chat/Component;")), at = @At(value = "RETURN", shift = Shift.BEFORE), ordinal = 1)
	private Component vanishmod$modifyDeathMessage(Component deathMessage) {
		if (deathMessage != null && deathMessage.getContents() instanceof TranslatableContents content && content.getArgs().length > 1 && content.getArgs()[1] instanceof Component playerName) {
			for (ServerPlayer killer : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
				if (killer.getDisplayName().getString().equals(playerName.getString()) && VanishUtil.isVanished(killer))
					deathMessage = Component.translatable("death.attack.generic", content.getArgs()[0]);
			}
		}

		return deathMessage;
	}
}
