package redstonedubstep.mods.vanishmod;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.STitlePacket;
import net.minecraft.network.play.server.STitlePacket.Type;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class VanishCommand {
	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(alias("v"));
		dispatcher.register(alias("vanish"));
	}

	private static LiteralArgumentBuilder<CommandSource> alias(String prefix) {
		return Commands.literal(prefix).requires(player -> player.hasPermission(VanishConfig.CONFIG.requiredPermissionLevel.get())).executes(ctx -> vanish(ctx, ctx.getSource().getPlayerOrException()))
				.then(Commands.literal("toggle").executes(ctx -> vanish(ctx, ctx.getSource().getPlayerOrException()))
						.then(Commands.argument("player", EntityArgument.player()).executes(ctx -> vanish(ctx, EntityArgument.getPlayer(ctx, "player")))))
				.then(Commands.literal("get").executes(ctx -> getVanishedStatus(ctx, ctx.getSource().getPlayerOrException()))
						.then(Commands.argument("player", EntityArgument.player()).executes(ctx -> getVanishedStatus(ctx, EntityArgument.getPlayer(ctx, "player")))));
	}

	private static int vanish(CommandContext<CommandSource> ctx, ServerPlayerEntity player) {
		boolean vanishes = !VanishUtil.isVanished(player);

		VanishUtil.updateVanishedStatus(player, vanishes);
		ctx.getSource().sendSuccess(new TranslationTextComponent(vanishes ? VanishConfig.CONFIG.onVanishMessage.get() : VanishConfig.CONFIG.onUnvanishMessage.get(), player.getDisplayName()), true);

		if (vanishes) {
			player.sendMessage(new StringTextComponent("Note: You can still see yourself in the tab list for technical reasons, but you are vanished for other players."), Util.NIL_UUID);
			player.sendMessage(new StringTextComponent("Note: Be careful when producing noise near other players, because while most sounds will get suppressed, some won't due to technical limitations."), Util.NIL_UUID);
		}

		VanishUtil.sendJoinOrLeaveMessageToPlayers(ctx.getSource().getLevel().players(), player, vanishes);
		VanishUtil.sendPacketsOnVanish(player, ctx.getSource().getLevel(), vanishes);
		return 1;
	}

	private static int getVanishedStatus(CommandContext<CommandSource> ctx, ServerPlayerEntity player) {
		TranslationTextComponent vanishedStatus = VanishUtil.getVanishedStatusText(player);
		Entity currentEntity = ctx.getSource().getEntity();

		ctx.getSource().sendSuccess(vanishedStatus, false);

		if (currentEntity instanceof ServerPlayerEntity)
			((ServerPlayerEntity)currentEntity).connection.send(new STitlePacket(Type.ACTIONBAR, vanishedStatus));

		return 1;
	}
}
