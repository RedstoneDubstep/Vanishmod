package redstonedubstep.mods.vanishmod.mixin;

import java.util.UUID;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.IServerPlayNetHandler;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import redstonedubstep.mods.vanishmod.VanishUtil;

@Mixin(ServerPlayNetHandler.class)
public abstract class MixinServerPlayNetHandler implements IServerPlayNetHandler {
	@Shadow
	public ServerPlayerEntity player;

	@Redirect(method="onDisconnect", at=@At(value="INVOKE", target="Lnet/minecraft/server/management/PlayerList;func_232641_a_(Lnet/minecraft/util/text/ITextComponent;Lnet/minecraft/util/text/ChatType;Ljava/util/UUID;)V"))
	//Block join message when player is vanished
	public void redirectFunc_232641_a_(PlayerList playerList, ITextComponent content, ChatType chatType, UUID uuid) {
		if (!VanishUtil.isVanished(player))
			playerList.func_232641_a_(content, chatType, uuid);
	}
}
