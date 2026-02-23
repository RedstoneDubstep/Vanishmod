package redstonedubstep.mods.vanishmod.mixin.chat;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.CombatEntry;
import net.minecraft.world.damagesource.CombatTracker;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.damagesource.IDeathMessageProvider;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import redstonedubstep.mods.vanishmod.VanishConfig;
import redstonedubstep.mods.vanishmod.VanishUtil;

@Mixin(IDeathMessageProvider.class)
public interface IDeathMessageProviderMixin {
	//Change the death message of an unvanished player to the generic one if it was killed by a vanished player through falling
	@WrapOperation(method = "lambda$static$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/damagesource/CombatTracker;getFallMessage(Lnet/minecraft/world/damagesource/CombatEntry;Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/network/chat/Component;"))
	private static Component vanishmod$modifyFallDeathMessage(CombatTracker combatTracker, CombatEntry entry, Entity entity, Operation<Component> getFallMessage) {
		return vanishmod$filterDeathMessage(getFallMessage.call(combatTracker, entry, entity));
	}

	//Change the death message of an unvanished player to the generic one if it was killed by a vanished player
	@WrapOperation(method = "lambda$static$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/damagesource/DamageSource;getLocalizedDeathMessage(Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/network/chat/Component;"))
	private static Component vanishmod$modifyDeathMessage(DamageSource damageSource, LivingEntity entity, Operation<Component> getLocalizedDeathMessage) {
		return vanishmod$filterDeathMessage(getLocalizedDeathMessage.call(damageSource, entity));
	}

	@Unique
	private static Component vanishmod$filterDeathMessage(Component deathMessage) {
		List<ServerPlayer> players = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers();

		if (!players.isEmpty() && (VanishConfig.CONFIG.hideSystemMessages.get() || VanishConfig.CONFIG.hidePlayerNameInSystemMessages.get()) && deathMessage != null && deathMessage.getContents() instanceof TranslatableContents content && content.getArgs().length > 1 && content.getArgs()[1] instanceof Component playerName) {
			for (ServerPlayer killer : players) {
				if (killer.getDisplayName().getString().equals(playerName.getString()) && VanishUtil.isVanished(killer)) {
					if (VanishConfig.CONFIG.hideSystemMessages.get())
						deathMessage = Component.translatable("death.attack.generic", content.getArgs()[0]);
					else if (VanishConfig.CONFIG.hidePlayerNameInSystemMessages.get())
						content.getArgs()[1] = Component.literal(VanishConfig.CONFIG.vanishedPlayerNameReplacement.get());
				}

			}
		}

		return deathMessage;
	}
}
