package redstonedubstep.mods.vanishmod.misc;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import redstonedubstep.mods.vanishmod.VanishUtil;

public class SoundSuppressionHelper {
	private static final Map<ServerPlayer, Pair<BlockPos, Entity>> vanishedPlayersAndHitResults = new HashMap<>();

	public static void updateVanishedPlayerMap(ServerPlayer player, boolean vanished) {
		if (vanished)
			vanishedPlayersAndHitResults.put(player, null);
		else
			vanishedPlayersAndHitResults.remove(player);
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
		if (VanishUtil.isVanished(player)) {
			vanishedPlayersAndHitResults.put(player, null);
		}
	}

	public static boolean areVanishedPlayersAt(Level level, Vec3 pos) {
		VoxelShape shape = Shapes.block().move(pos.x, pos.y, pos.z);
		return vanishedPlayersAndHitResults.keySet().stream().filter(p -> p.level.equals(level) && p.gameMode.getGameModeForPlayer() != GameType.SPECTATOR).anyMatch(p -> Shapes.joinIsNotEmpty(shape, Shapes.create(p.getBoundingBox()), BooleanOp.AND));
	}


	public static boolean vanishedPlayerVehicleAt(Level level, Vec3 pos) {
		VoxelShape shape = Shapes.block().move(pos.x, pos.y, pos.z);
		return vanishedPlayersAndHitResults.keySet().stream().filter(p -> p.level.equals(level) && p.gameMode.getGameModeForPlayer() != GameType.SPECTATOR).map(Entity::getVehicle).filter(Objects::nonNull).anyMatch(v -> Shapes.joinIsNotEmpty(shape, Shapes.create(v.getBoundingBox()), BooleanOp.AND));
	}

	public static boolean isVanishedPlayerVehicle(Entity entity) {
		return vanishedPlayersAndHitResults.keySet().stream().anyMatch(p -> p.gameMode.getGameModeForPlayer() != GameType.SPECTATOR && entity.equals(p.getVehicle()));
	}

	public static boolean vanishedPlayersInteractWith(Level level, BlockPos pos) {
		return vanishedPlayersAndHitResults.entrySet().stream().filter(e -> e.getKey().level.equals(level)).anyMatch(p -> p.getValue() != null && p.getValue().getLeft().equals(pos));
	}

	public static boolean vanishedPlayersInteractWith(Level level, Entity entity) {
		return vanishedPlayersAndHitResults.entrySet().stream().filter(e -> e.getKey().level.equals(level)).anyMatch(p -> p.getValue() != null && p.getValue().getRight().equals(entity));
	}
}
