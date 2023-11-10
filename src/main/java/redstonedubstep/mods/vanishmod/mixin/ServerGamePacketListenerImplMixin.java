package redstonedubstep.mods.vanishmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import redstonedubstep.mods.vanishmod.misc.FieldHolder;

@Mixin(value = ServerGamePacketListenerImpl.class, priority = 1200) //Lower priority to ensure that the injectors of this mixin are run after other mixins in this class; particularly important for vanishmod$onFinishDisconnect, which needs to be the last method called
public class ServerGamePacketListenerImplMixin {
	@Shadow
	public ServerPlayer player;

	//Stores the player that is about to leave the server and get removed from the regular player list
	@Inject(method = "onDisconnect", at = @At("HEAD"))
	private void vanishmod$onStartDisconnect(Component reason, CallbackInfo callbackInfo) {
		FieldHolder.leavingPlayer = player;
	}

	//Removes the stored player after it has fully left the server
	@Inject(method = "onDisconnect", at = @At("TAIL"))
	private void vanishmod$onFinishDisconnect(Component reason, CallbackInfo callbackInfo) {
		FieldHolder.leavingPlayer = null;
	}
}
