package redstonedubstep.mods.vanishmod;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerPlayer;

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
		ctx.getSource().sendSuccess(new TranslatableComponent(vanishes ? VanishConfig.CONFIG.onVanishMessage.get() : VanishConfig.CONFIG.onUnvanishMessage.get(), player.getDisplayName()), true);

		if (vanishes) {
			player.sendMessage(new TextComponent("Note: You can still see yourself in the tab list for technical reasons, but you are vanished for other players."), Util.NIL_UUID);
			player.sendMessage(new TextComponent("Note: Be careful when producing noise near other players, because while most sounds will get suppressed, some won't due to technical limitations."), Util.NIL_UUID);
		}

		VanishUtil.sendJoinOrLeaveMessageToPlayers(ctx.getSource().getLevel().players(), player, vanishes);
		VanishUtil.sendPacketsOnVanish(player, ctx.getSource().getLevel(), vanishes);
		return 1;
	}

	private static int getVanishedStatus(CommandContext<CommandSourceStack> ctx, ServerPlayer player) {
		TranslatableComponent vanishedStatus = VanishUtil.getVanishedStatusText(player);

		ctx.getSource().sendSuccess(vanishedStatus, false);
		player.connection.send(new ClientboundSetActionBarTextPacket(vanishedStatus));
		return 1;
	}
}
