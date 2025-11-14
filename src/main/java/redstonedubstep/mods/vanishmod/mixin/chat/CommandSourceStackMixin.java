package redstonedubstep.mods.vanishmod.mixin.chat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import redstonedubstep.mods.vanishmod.VanishConfig;
import redstonedubstep.mods.vanishmod.VanishUtil;

@Mixin(CommandSourceStack.class)
public abstract class CommandSourceStackMixin {
	@Shadow
	@Final
	private MinecraftServer server;

	@Shadow
	public abstract ServerPlayer getPlayer();

	@Shadow
	public abstract boolean isPlayer();

	//Filters the list of online players that is used for player name suggestions in the e.g. /team command, so vanished player names are not suggested for non-permitted players
	@Inject(method = "getOnlinePlayerNames", at = @At("HEAD"), cancellable = true)
	private void vanishmod$filterCommandSuggestionPlayerList(CallbackInfoReturnable<Collection<String>> callbackInfo) {
		if (VanishConfig.CONFIG.hidePlayersFromPlayerLists.get() && isPlayer()) {
			List<String> filteredOnlinePlayerNames = new ArrayList<>();

			for (String name : server.getPlayerNames()) {
				if (!VanishUtil.isVanished(server.getPlayerList().getPlayerByName(name), getPlayer()))
					filteredOnlinePlayerNames.add(name);
			}

			callbackInfo.setReturnValue(filteredOnlinePlayerNames);
		}
	}
}
