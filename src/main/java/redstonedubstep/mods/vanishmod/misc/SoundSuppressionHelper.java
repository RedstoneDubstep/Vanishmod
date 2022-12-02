package redstonedubstep.mods.vanishmod.misc;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import redstonedubstep.mods.vanishmod.VanishConfig;
import redstonedubstep.mods.vanishmod.VanishUtil;

public class SoundSuppressionHelper {
	private static final Map<ServerPlayer, Pair<BlockPos, Entity>> vanishedPlayersAndHitResults = new HashMap<>();
	private static Pair<Packet<?>, Player> packetOrigin = null;

	public static boolean shouldCapturePlayers() {
		return VanishConfig.CONFIG.indirectSoundSuppression.get() || VanishConfig.CONFIG.indirectParticleSuppression.get();
	}

	public static void updateVanishedPlayerMap(ServerPlayer player, boolean vanished) {
		if (vanished)
			vanishedPlayersAndHitResults.put(player, null);
		else
			vanishedPlayersAndHitResults.remove(player);

		new HashSet<>(vanishedPlayersAndHitResults.keySet()).stream().filter(Entity::isRemoved).forEach(vanishedPlayersAndHitResults::remove);
	}

	public static void updateBlockHitResult(ServerPlayer player, BlockHitResult hitResult) {
		if (VanishUtil.isVanished(player)) {
			Pair<BlockPos, Entity> oldHitResults = vanishedPlayersAndHitResults.get(player);
			vanishedPlayersAndHitResults.put(player, oldHitResults == null ? Pair.of(hitResult.getBlockPos(), null) : Pair.of(hitResult.getBlockPos(), oldHitResults.getRight()));
		}
	}

	public static void updateEntityHitResult(ServerPlayer player, Entity hitEntity) {
		if (VanishUtil.isVanished(player))
			vanishedPlayersAndHitResults.put(player, Pair.of(hitEntity.blockPosition(), hitEntity));
	}

	public static void invalidateHitResults(ServerPlayer player) {
		if (VanishUtil.isVanished(player))
			vanishedPlayersAndHitResults.put(player, null);
	}

	public static void putSoundPacket(Packet<?> packet, Player player) {
		packetOrigin = Pair.of(packet, player);
	}

	public static Player getPlayerForPacket(Packet<?> packet) {
		return packetOrigin != null && packetOrigin.getLeft().equals(packet) ? packetOrigin.getRight() : null;
	}

	public static boolean shouldSuppressSoundEventFor(Player player, Level level, double x, double y, double z, Player forPlayer) {
		return shouldSuppressSoundEventFor(player, level, new Vec3(x, y, z), forPlayer);
	}

	//Returns true if a vanished player directly produced the sound, or if it is determined that a vanished player was indirectly causing a sound, and that it thus should not be broadcast
	public static boolean shouldSuppressSoundEventFor(Player player, Level level, Vec3 soundOrigin, Player forPlayer) {
		if (VanishUtil.isVanished(player, forPlayer))
			return true;

		if (!VanishConfig.CONFIG.indirectSoundSuppression.get() || VanishUtil.canSeeVanishedPlayers(forPlayer))
			return false;

		return SoundSuppressionHelper.areVanishedPlayersAt(level, soundOrigin) || SoundSuppressionHelper.vanishedPlayerVehicleAt(level, soundOrigin) || SoundSuppressionHelper.vanishedPlayersInteractWith(level, new BlockPos(soundOrigin));
	}

	//Returns true if a vanished player directly produced the sound, or if it is determined that a vanished player was indirectly causing a sound, and that it thus should not be broadcast
	public static boolean shouldSuppressSoundEventFor(Player player, Level level, Entity soundOrigin, Player forPlayer) {
		if (VanishUtil.isVanished(player, forPlayer))
			return true;

		if (!VanishConfig.CONFIG.indirectSoundSuppression.get() || VanishUtil.canSeeVanishedPlayers(forPlayer))
			return false;

		return SoundSuppressionHelper.areVanishedPlayersAt(level, soundOrigin.position()) || SoundSuppressionHelper.isVanishedPlayerVehicle(soundOrigin) || SoundSuppressionHelper.vanishedPlayersInteractWith(level, soundOrigin);
	}

	public static boolean shouldSuppressParticlesFor(Player player, Level level, double x, double y, double z, Player forPlayer) {
		Vec3 soundOrigin = new Vec3(x, y, z);

		if (VanishUtil.isVanished(player, forPlayer))
			return true;

		if (!VanishConfig.CONFIG.indirectParticleSuppression.get() || VanishUtil.canSeeVanishedPlayers(forPlayer))
			return false;

		return SoundSuppressionHelper.areVanishedPlayersAt(level, soundOrigin) || SoundSuppressionHelper.vanishedPlayerVehicleAt(level, soundOrigin) || SoundSuppressionHelper.vanishedPlayersInteractWith(level, new BlockPos(soundOrigin));
	}

	public static boolean areVanishedPlayersAt(Level level, Vec3 pos) {
		VoxelShape shape = Shapes.block().move(pos.x - 0.5D, pos.y - 0.5D, pos.z - 0.5D);
		return vanishedPlayersAndHitResults.keySet().stream().filter(p -> p.level.equals(level) && p.gameMode.getGameModeForPlayer() != GameType.SPECTATOR).anyMatch(p -> Shapes.joinIsNotEmpty(shape, Shapes.create(p.getBoundingBox()), BooleanOp.AND));
	}

	public static boolean vanishedPlayerVehicleAt(Level level, Vec3 pos) {
		VoxelShape shape = Shapes.block().move(pos.x - 0.5D, pos.y - 0.5D, pos.z - 0.5D);
		return vanishedPlayersAndHitResults.keySet().stream().filter(p -> p.level.equals(level) && p.gameMode.getGameModeForPlayer() != GameType.SPECTATOR).map(Entity::getVehicle).filter(Objects::nonNull).anyMatch(v -> Shapes.joinIsNotEmpty(shape, Shapes.create(v.getBoundingBox()), BooleanOp.AND));
	}

	public static boolean isVanishedPlayerVehicle(Entity entity) {
		return vanishedPlayersAndHitResults.keySet().stream().anyMatch(p -> p.gameMode.getGameModeForPlayer() != GameType.SPECTATOR && entity.equals(p.getVehicle()));
	}

	public static boolean vanishedPlayersInteractWith(Level level, BlockPos pos) {
		return vanishedPlayersAndHitResults.entrySet().stream().filter(e -> e.getKey().level.equals(level)).anyMatch(p -> p.getValue() != null && equalsThisOrConnected(pos, level, p.getValue().getLeft()));
	}

	public static boolean vanishedPlayersInteractWith(Level level, Entity entity) {
		return vanishedPlayersAndHitResults.entrySet().stream().filter(e -> e.getKey().level.equals(level)).anyMatch(p -> p.getValue() != null && entity.equals(p.getValue().getRight()));
	}

	public static boolean equalsThisOrConnected(BlockPos soundPos, Level level, BlockPos interactPos) {
		if (soundPos.equals(interactPos))
			return true;
		else if (interactPos != null) {
			BlockState state = level.getBlockState(interactPos);

			if (state.getBlock() instanceof ChestBlock)
				return soundPos.equals(interactPos.relative(ChestBlock.getConnectedDirection(state)));
		}

		return false;
	}
}
