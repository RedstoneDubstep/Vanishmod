package redstonedubstep.mods.vanishmod;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class VanishCommand {
	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(alias("v"));
		dispatcher.register(alias("vanish"));
	}

	private static LiteralArgumentBuilder<CommandSource> alias(String prefix) {
		return Commands.literal(prefix).requires(player -> player.hasPermissionLevel(2)).executes(ctx -> vanish(ctx, ctx.getSource().asPlayer()))
				.then(Commands.literal("toggle").executes(ctx -> vanish(ctx, ctx.getSource().asPlayer()))
						.then(Commands.argument("player", EntityArgument.player()).executes(ctx -> vanish(ctx, EntityArgument.getPlayer(ctx, "player")))))
				.then(Commands.literal("get").executes(ctx -> getVanishedStatus(ctx, ctx.getSource().asPlayer()))
						.then(Commands.argument("player", EntityArgument.player()).executes(ctx -> getVanishedStatus(ctx, EntityArgument.getPlayer(ctx, "player")))));
	}

	private static int vanish(CommandContext<CommandSource> ctx, ServerPlayerEntity player) {
		boolean vanishes = !VanishUtil.isVanished(player);

		VanishUtil.updateVanishedStatus(player, vanishes);

		if (vanishes) {
			ctx.getSource().sendFeedback(new TranslationTextComponent("%s vanished", player.getDisplayName()), true);
			player.sendMessage(new StringTextComponent("Note: You can still see yourself in the tab list for technical reasons, but you are vanished for other players."), Util.DUMMY_UUID);
			player.sendMessage(new StringTextComponent("Note: Be careful when producing noise near other players, because while most sounds will get suppressed, some won't due to technical limitations."), Util.DUMMY_UUID);
		}
		else {
			ctx.getSource().sendFeedback(new TranslationTextComponent("%s appeared again", player.getDisplayName()), true);
		}

		VanishUtil.sendJoinOrLeaveMessageToPlayers(ctx.getSource().getWorld().getPlayers(), player, vanishes);
		VanishUtil.sendPacketsOnVanish(player, ctx.getSource().getWorld(), vanishes);
		return 1;
	}

	private static int getVanishedStatus(CommandContext<CommandSource> ctx, ServerPlayerEntity player) {
		if (VanishUtil.isVanished(player)) {
			ctx.getSource().sendFeedback(new TranslationTextComponent("%s is currently vanished.", player.getDisplayName()), false);
		}
		else {
			ctx.getSource().sendFeedback(new TranslationTextComponent("%s is currently not vanished.", player.getDisplayName()), false);
		}

		return 1;
	}
}
