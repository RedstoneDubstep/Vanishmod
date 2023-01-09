package redstonedubstep.mods.vanishmod;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerPlayer;

public class VanishCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(alias("v"));
		dispatcher.register(alias("vanish"));
	}

	private static LiteralArgumentBuilder<CommandSourceStack> alias(String prefix) {
		return Commands.literal(prefix).requires(player -> player.hasPermission(VanishConfig.CONFIG.vanishCommandPermissionLevel.get())).executes(ctx -> vanish(ctx, ctx.getSource().getPlayerOrException()))
				.then(Commands.literal("toggle").executes(ctx -> vanish(ctx, ctx.getSource().getPlayerOrException()))
						.then(Commands.argument("player", EntityArgument.player()).executes(ctx -> vanish(ctx, EntityArgument.getPlayer(ctx, "player")))))
				.then(Commands.literal("get").executes(ctx -> getVanishedStatus(ctx, ctx.getSource().getPlayerOrException()))
						.then(Commands.argument("player", EntityArgument.player()).executes(ctx -> getVanishedStatus(ctx, EntityArgument.getPlayer(ctx, "player")))))
				.then(Commands.literal("queue").executes(ctx -> queue(ctx, ctx.getSource().getPlayerOrException().getGameProfile().getName()))
						.then(Commands.argument("player", StringArgumentType.word()).executes(ctx -> queue(ctx, StringArgumentType.getString(ctx, "player")))));
	}

	private static int vanish(CommandContext<CommandSourceStack> ctx, ServerPlayer player) {
		ctx.getSource().sendSuccess(VanishUtil.VANISHMOD_PREFIX.copy().append(new TranslatableComponent(!VanishUtil.isVanished(player) ? VanishConfig.CONFIG.onVanishMessage.get() : VanishConfig.CONFIG.onUnvanishMessage.get(), player.getDisplayName())), true);
		VanishUtil.toggleVanish(player);
		return 1;
	}

	private static int getVanishedStatus(CommandContext<CommandSourceStack> ctx, ServerPlayer player) {
		TranslatableComponent vanishedStatus = VanishUtil.getVanishedStatusText(player, VanishUtil.isVanished(player));

		ctx.getSource().sendSuccess(VanishUtil.VANISHMOD_PREFIX.copy().append(vanishedStatus), false);

		if (ctx.getSource().getEntity() instanceof ServerPlayer currentPlayer)
			currentPlayer.connection.send(new ClientboundSetActionBarTextPacket(vanishedStatus));

		return 1;
	}

	private static int queue(CommandContext<CommandSourceStack> ctx, String playerName) {
		ServerPlayer player = ctx.getSource().getServer().getPlayerList().getPlayerByName(playerName);

		if (player != null) {
			if (!VanishUtil.isVanished(player))
				vanish(ctx, player);
			else
				ctx.getSource().sendFailure(VanishUtil.VANISHMOD_PREFIX.copy().append(new TranslatableComponent("Could not add already vanished player %s to the vanishing queue", playerName)));

			return 1;
		}

		if (VanishUtil.removeFromQueue(playerName))
			ctx.getSource().sendSuccess(VanishUtil.VANISHMOD_PREFIX.copy().append(new TranslatableComponent("Removed %s from the vanishing queue", playerName)), true);
		else if (VanishUtil.addToQueue(playerName))
			ctx.getSource().sendSuccess(VanishUtil.VANISHMOD_PREFIX.copy().append(new TranslatableComponent("Added %s to the vanishing queue", playerName)), true);

		return 1;
	}
}
