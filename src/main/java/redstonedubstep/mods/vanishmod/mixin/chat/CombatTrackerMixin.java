package redstonedubstep.mods.vanishmod.mixin.chat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.CombatEntry;
import net.minecraft.world.damagesource.CombatTracker;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import redstonedubstep.mods.vanishmod.VanishUtil;

@Mixin(CombatTracker.class)
public abstract class CombatTrackerMixin {
	@Shadow
	protected abstract Component getFallMessage(CombatEntry entry, Entity entity);

	//Change the death message of an unvanished player to the generic one if it was killed by a vanished player through falling
	@Redirect(method = "getDeathMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/damagesource/CombatTracker;getFallMessage(Lnet/minecraft/world/damagesource/CombatEntry;Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/network/chat/Component;"))
	private Component vanishmod$modifyFallDeathMessage(CombatTracker instance, CombatEntry entry, Entity entity) {
		Component deathMessage = getFallMessage(entry, entity);

		return vanishmod$filterDeathMessage(deathMessage);
	}

	//Change the death message of an unvanished player to the generic one if it was killed by a vanished player
	@Redirect(method = "getDeathMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/damagesource/DamageSource;getLocalizedDeathMessage(Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/network/chat/Component;"))
	private Component vanishmod$modifyDeathMessage(DamageSource instance, LivingEntity entity) {
		Component deathMessage = instance.getLocalizedDeathMessage(entity);

		return vanishmod$filterDeathMessage(deathMessage);
	}

	@Unique
	private Component vanishmod$filterDeathMessage(Component deathMessage) {
		if (deathMessage != null && deathMessage.getContents() instanceof TranslatableContents content && content.getArgs().length > 1 && content.getArgs()[1] instanceof Component playerName) {
			for (ServerPlayer killer : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
				if (killer.getDisplayName().getString().equals(playerName.getString()) && VanishUtil.isVanished(killer))
					deathMessage = Component.translatable("death.attack.generic", content.getArgs()[0]);
			}
		}

		return deathMessage;
	}
}
