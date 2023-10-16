package redstonedubstep.mods.vanishmod.mixin.gui;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.authlib.GameProfile;

import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import redstonedubstep.mods.vanishmod.VanishConfig;
import redstonedubstep.mods.vanishmod.VanishUtil;
import redstonedubstep.mods.vanishmod.misc.FieldHolder;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
	@Shadow
	@Final
	private RandomSource random;
	@Shadow
	@Final
	private ServerStatus status;

	@Shadow
	public abstract PlayerList getPlayerList();

	@Shadow
	public abstract boolean hidesOnlinePlayers();

	//Constructs an alternative ServerStatus that accounts for vanished players after the main one has been constructed
	@Inject(method = "tickServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/status/ServerStatus;invalidateJson()V"))
	private void vanishmod$onBuildServerStatus(CallbackInfo callbackInfo) {
		if (VanishConfig.CONFIG.hidePlayersFromPlayerLists.get()) {
			PlayerList list = getPlayerList();
			List<ServerPlayer> unvanishedPlayers = VanishUtil.formatPlayerList(list.getPlayers(), null);
			int unvanishedPlayerCount = unvanishedPlayers.size();
			int maxPlayers = list.getMaxPlayers();
			ServerStatus mainServerStatus = status;
			ServerStatus.Players vanishedPlayerStatus = new ServerStatus.Players(maxPlayers, unvanishedPlayerCount);

			if (!hidesOnlinePlayers()) {
				int playerSampleSize = Math.min(unvanishedPlayers.size(), 12);
				GameProfile[] displayedPlayers = new GameProfile[playerSampleSize];
				int offset = Mth.nextInt(random, 0, unvanishedPlayerCount - playerSampleSize);

				for(int l = 0; l < playerSampleSize; ++l) {
					ServerPlayer player = unvanishedPlayers.get(offset + l);
					displayedPlayers[l] = player.allowsListing() ? player.getGameProfile() : MinecraftServer.ANONYMOUS_PLAYER_PROFILE;
				}

				Collections.shuffle(Arrays.asList(displayedPlayers));
				vanishedPlayerStatus.setSample(displayedPlayers);
			}

			if (mainServerStatus != null && FieldHolder.VANISHED_SERVER_STATUS.getDescription() == null) {
				FieldHolder.VANISHED_SERVER_STATUS.setForgeData(mainServerStatus.getForgeData());
				FieldHolder.VANISHED_SERVER_STATUS.setDescription(mainServerStatus.getDescription());
				FieldHolder.VANISHED_SERVER_STATUS.setVersion(mainServerStatus.getVersion());
				FieldHolder.VANISHED_SERVER_STATUS.setPreviewsChat(mainServerStatus.previewsChat());
				FieldHolder.VANISHED_SERVER_STATUS.setEnforcesSecureChat(mainServerStatus.enforcesSecureChat());
				FieldHolder.VANISHED_SERVER_STATUS.setFavicon(mainServerStatus.getFavicon());
			}

			FieldHolder.VANISHED_SERVER_STATUS.setPlayers(vanishedPlayerStatus);
		}
		else
			FieldHolder.VANISHED_SERVER_STATUS.setPlayers(null);
	}
}
