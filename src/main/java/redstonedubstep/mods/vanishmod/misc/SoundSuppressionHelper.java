package redstonedubstep.mods.vanishmod.misc;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import redstonedubstep.mods.vanishmod.VanishUtil;

public class SoundSuppressionHelper {
	private static final Map<ServerPlayerEntity, Pair<BlockPos, Entity>> vanishedPlayersAndHitResults = new HashMap<>();

	public static void updateVanishedPlayerMap(ServerPlayerEntity player, boolean vanished) {
		if (vanished)
			vanishedPlayersAndHitResults.put(player, null);
		else
			vanishedPlayersAndHitResults.remove(player);
	}
	public static void updateBlockHitResult(ServerPlayerEntity player, BlockRayTraceResult rayTraceResult) {
		if (VanishUtil.isVanished(player)) {
			Pair<BlockPos, Entity> oldHitResults = vanishedPlayersAndHitResults.get(player);
			vanishedPlayersAndHitResults.put(player, oldHitResults == null ? Pair.of(rayTraceResult.getBlockPos(), null) : Pair.of(rayTraceResult.getBlockPos(), oldHitResults.getRight()));
		}
	}

	public static void updateEntityHitResult(ServerPlayerEntity player, Entity hitEntity) {
		if (VanishUtil.isVanished(player))
			vanishedPlayersAndHitResults.put(player, Pair.of(hitEntity.blockPosition(), hitEntity));
	}

	public static void invalidateHitResults(ServerPlayerEntity player) {
		if (VanishUtil.isVanished(player)) {
			vanishedPlayersAndHitResults.put(player, null);
		}
	}

	public static boolean areVanishedPlayersAt(World world, Vector3d pos) {
		VoxelShape shape = VoxelShapes.block().move(pos.x, pos.y, pos.z);
		return vanishedPlayersAndHitResults.keySet().stream().filter(p -> p.level.equals(world) && p.gameMode.getGameModeForPlayer() != GameType.SPECTATOR).anyMatch(p -> VoxelShapes.joinIsNotEmpty(shape, VoxelShapes.create(p.getBoundingBox()), IBooleanFunction.AND));
	}


	public static boolean vanishedPlayerVehicleAt(World world, Vector3d pos) {
		VoxelShape shape = VoxelShapes.block().move(pos.x, pos.y, pos.z);
		return vanishedPlayersAndHitResults.keySet().stream().filter(p -> p.level.equals(world) && p.gameMode.getGameModeForPlayer() != GameType.SPECTATOR).map(Entity::getVehicle).filter(Objects::nonNull).anyMatch(v -> VoxelShapes.joinIsNotEmpty(shape, VoxelShapes.create(v.getBoundingBox()), IBooleanFunction.AND));
	}

	public static boolean isVanishedPlayerVehicle(Entity entity) {
		return vanishedPlayersAndHitResults.keySet().stream().anyMatch(p -> p.gameMode.getGameModeForPlayer() != GameType.SPECTATOR && entity.equals(p.getVehicle()));
	}

	public static boolean vanishedPlayersInteractWith(World world, BlockPos pos) {
		return vanishedPlayersAndHitResults.entrySet().stream().filter(e -> e.getKey().level.equals(world)).anyMatch(p -> p.getValue() != null && p.getValue().getLeft().equals(pos));
	}

	public static boolean vanishedPlayersInteractWith(World world, Entity entity) {
		return vanishedPlayersAndHitResults.entrySet().stream().filter(e -> e.getKey().level.equals(world)).anyMatch(p -> p.getValue() != null && p.getValue().getRight().equals(entity));
	}
}
