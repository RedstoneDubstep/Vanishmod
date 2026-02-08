package redstonedubstep.mods.vanishmod;

import java.net.URI;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerPlayer;
import redstonedubstep.mods.vanishmod.misc.TraceHandler;

public class VanishCommand {
	private static final Component HELP_TEXT = VanishUtil.VANISHMOD_PREFIX.copy().append(Component.literal("""
				§7§nVanishmod§r is a mod that allows you to become completely undetectable for other players. Most features can be accessed using the §7/vanish§r (or §7/v§r) command.
				§nCommand Usage§r:
				§7/v get [<player>]§r: Queries the current vanished status of the given player.
				§7/v help§r: Shows this message.
				§7/v queue [<player>]§r: Adds the given player name to the vanishing queue. If the player is online, they are immediately vanished. If not, they are vanished as soon as they join the server.
				§7/v toggle [<player>]§r (or §7/v§r): Vanishes or unvanishes the given player, depending on their prior vanished status.
				§7/v trace§r: Enables and disables tracing, which is a debug functionality that reports any event caused by you that has been concealed for other players.
				
				A lot of the features of this mod are customizable via the configuration file, which is located in the "config" folder of your server, so check that out if you're interested!
				If you have a suggestion or found a bug, feel free to open an issue in""")).append(Component.literal(" §7§nthe mod's GitHub repository§r.").withStyle(s -> s.withClickEvent(new ClickEvent.OpenUrl(URI.create("https://github.com/RedstoneDubstep/Vanishmod")))));

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(alias("v"));
		dispatcher.register(alias("vanish"));
	}

	private static LiteralArgumentBuilder<CommandSourceStack> alias(String prefix) {
		return Commands.literal(prefix).requires(ctx -> VanishUtil.getPermissionCheckFromLevel(VanishConfig.CONFIG.vanishCommandPermissionLevel.get()).check(ctx.permissions())).executes(ctx -> vanish(ctx, ctx.getSource().getPlayerOrException()))
				.then(Commands.literal("get").executes(ctx -> getVanishedStatus(ctx, ctx.getSource().getPlayerOrException()))
						.then(Commands.argument("player", EntityArgument.player()).executes(ctx -> getVanishedStatus(ctx, EntityArgument.getPlayer(ctx, "player")))))
				.then(Commands.literal("help").executes(VanishCommand::sendHelpText))
				.then(Commands.literal("queue").executes(ctx -> queue(ctx, ctx.getSource().getPlayerOrException().getGameProfile().name()))
						.then(Commands.argument("player", StringArgumentType.word()).executes(ctx -> queue(ctx, StringArgumentType.getString(ctx, "player")))))
				.then(Commands.literal("toggle").executes(ctx -> vanish(ctx, ctx.getSource().getPlayerOrException()))
						.then(Commands.argument("player", EntityArgument.player()).executes(ctx -> vanish(ctx, EntityArgument.getPlayer(ctx, "player")))))
				.then(Commands.literal("trace")
						.then(Commands.literal("enable").executes(ctx -> setTrace(ctx, null, true)))
						.then(Commands.literal("disable").executes(ctx -> setTrace(ctx, null, false))));
	}

	private static int getVanishedStatus(CommandContext<CommandSourceStack> ctx, ServerPlayer player) {
		MutableComponent vanishedStatus = VanishUtil.getVanishedStatusText(player, VanishUtil.isVanished(player));

		ctx.getSource().sendSuccess(() -> VanishUtil.VANISHMOD_PREFIX.copy().append(vanishedStatus), false);

		if (ctx.getSource().getEntity() instanceof ServerPlayer currentPlayer)
			currentPlayer.connection.send(new ClientboundSetActionBarTextPacket(vanishedStatus));

		return 1;
	}

	private static int sendHelpText(CommandContext<CommandSourceStack> ctx) {
		ctx.getSource().sendSystemMessage(HELP_TEXT);
		return 1;
	}

	private static int queue(CommandContext<CommandSourceStack> ctx, String playerName) throws CommandSyntaxException {
		ServerPlayer player = ctx.getSource().getServer().getPlayerList().getPlayerByName(playerName);

		if (player != null) {
			if (VanishUtil.isVanished(player)) {
				ctx.getSource().sendFailure(VanishUtil.VANISHMOD_PREFIX.copy().append(String.format("Could not add already vanished player %s to the vanishing queue", playerName)));
				return 0;
			}

			vanish(ctx, player);
		}
		else if (VanishingHandler.removeFromQueue(playerName))
			ctx.getSource().sendSuccess(() -> VanishUtil.VANISHMOD_PREFIX.copy().append(String.format("Removed %s from the vanishing queue", playerName)), true);
		else if (VanishingHandler.addToQueue(playerName))
			ctx.getSource().sendSuccess(() -> VanishUtil.VANISHMOD_PREFIX.copy().append(String.format("Added %s to the vanishing queue", playerName)), true);

		return 1;
	}

	private static int vanish(CommandContext<CommandSourceStack> ctx, ServerPlayer player) throws CommandSyntaxException {
		boolean isVanishing = !VanishUtil.isVanished(player);

		if (!isVanishing) {
			ctx.getSource().sendSuccess(() -> VanishUtil.VANISHMOD_PREFIX.copy().append(Component.translatable(VanishConfig.CONFIG.onUnvanishMessage.get(), player.getDisplayName())), true);

			if (TraceHandler.isTracing(player))
				setTrace(ctx, player, false);
		}

		VanishingHandler.toggleVanish(player);

		if (isVanishing)
			ctx.getSource().sendSuccess(() -> VanishUtil.VANISHMOD_PREFIX.copy().append(Component.translatable(VanishConfig.CONFIG.onVanishMessage.get(), player.getDisplayName())), true);

		return 1;
	}

	private static int setTrace(CommandContext<CommandSourceStack> ctx, ServerPlayer playerOverride, boolean shouldTrace) throws CommandSyntaxException {
		CommandSourceStack source = ctx.getSource();
		ServerPlayer player = playerOverride != null ? playerOverride : source.getPlayerOrException();
		boolean isTracing = TraceHandler.isTracing(player);

		if (!VanishUtil.isVanished(player)) {
			source.sendFailure(VanishUtil.VANISHMOD_PREFIX.copy().append("You need to be vanished to configure tracing!"));
			return 0;
		}
		else if (isTracing == shouldTrace) {
			source.sendFailure(VanishUtil.VANISHMOD_PREFIX.copy().append("Tracing is already " + (isTracing ? "enabled!" : "disabled!")));
			return 0;
		}

		TraceHandler.setTracing(player, shouldTrace);

		if (shouldTrace) {
			source.sendSystemMessage(VanishUtil.VANISHMOD_PREFIX.copy().append("Tracing is now enabled."));
			TraceHandler.sendTraceStatus(player);
		}
		else
			source.sendSystemMessage(VanishUtil.VANISHMOD_PREFIX.copy().append("Tracing is now disabled."));

		return 1;
	}
}
