package redstonedubstep.mods.vanishmod;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.Util;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class VanishCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(alias("v"));
		dispatcher.register(alias("vanish"));
	}

	private static LiteralArgumentBuilder<CommandSourceStack> alias(String prefix) {
		return Commands.literal(prefix).requires(player -> player.hasPermission(VanishConfig.CONFIG.requiredPermissionLevel.get())).executes(ctx -> vanish(ctx, ctx.getSource().getPlayerOrException()))
				.then(Commands.literal("toggle").executes(ctx -> vanish(ctx, ctx.getSource().getPlayerOrException()))
						.then(Commands.argument("player", EntityArgument.player()).executes(ctx -> vanish(ctx, EntityArgument.getPlayer(ctx, "player")))))
				.then(Commands.literal("get").executes(ctx -> getVanishedStatus(ctx, ctx.getSource().getPlayerOrException()))
						.then(Commands.argument("player", EntityArgument.player()).executes(ctx -> getVanishedStatus(ctx, EntityArgument.getPlayer(ctx, "player")))));
	}

	private static int vanish(CommandContext<CommandSourceStack> ctx, ServerPlayer player) {
		boolean vanishes = !VanishUtil.isVanished(player);

		VanishUtil.updateVanishedStatus(player, vanishes);

		if (vanishes) {
			ctx.getSource().sendSuccess(new TranslatableComponent(VanishConfig.CONFIG.onVanishMessage.get(), player.getDisplayName()), true);
			player.sendMessage(new TextComponent("Note: You can still see yourself in the tab list for technical reasons, but you are vanished for other players."), Util.NIL_UUID);
			player.sendMessage(new TextComponent("Note: Be careful when producing noise near other players, because while most sounds will get suppressed, some won't due to technical limitations."), Util.NIL_UUID);
		}
		else
			ctx.getSource().sendSuccess(new TranslatableComponent(VanishConfig.CONFIG.onUnvanishMessage.get(), player.getDisplayName()), true);

		VanishUtil.sendJoinOrLeaveMessageToPlayers(ctx.getSource().getLevel().players(), player, vanishes);
		VanishUtil.sendPacketsOnVanish(player, ctx.getSource().getLevel(), vanishes);
		return 1;
	}

	private static int getVanishedStatus(CommandContext<CommandSourceStack> ctx, ServerPlayer player) {
		if (VanishUtil.isVanished(player)) {
			ctx.getSource().sendSuccess(new TranslatableComponent("%s is currently vanished.", player.getDisplayName()), false);
		}
		else {
			ctx.getSource().sendSuccess(new TranslatableComponent("%s is currently not vanished.", player.getDisplayName()), false);
		}

		return 1;
	}
}
