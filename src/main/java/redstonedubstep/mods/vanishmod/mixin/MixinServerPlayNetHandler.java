package redstonedubstep.mods.vanishmod.mixin;

import java.util.UUID;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.IServerPlayNetHandler;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.ITextComponent;
import redstonedubstep.mods.vanishmod.VanishUtil;

@Mixin(ServerPlayNetHandler.class)
public abstract class MixinServerPlayNetHandler implements IServerPlayNetHandler {
	@Shadow
	public ServerPlayerEntity player;

	//Block leave message when player is vanished
	@Redirect(method = "onDisconnect", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/management/PlayerList;func_232641_a_(Lnet/minecraft/util/text/ITextComponent;Lnet/minecraft/util/text/ChatType;Ljava/util/UUID;)V"))
	public void redirectFunc_232641_a_(PlayerList playerList, ITextComponent content, ChatType chatType, UUID uuid) {
		if (!VanishUtil.isVanished(player))
			playerList.func_232641_a_(content, chatType, uuid);
	}
}
