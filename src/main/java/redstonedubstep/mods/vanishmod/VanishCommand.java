package redstonedubstep.mods.vanishmod;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.STitlePacket;
import net.minecraft.network.play.server.STitlePacket.Type;
import net.minecraft.util.text.TranslationTextComponent;

public class VanishCommand {
	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(alias("v"));
		dispatcher.register(alias("vanish"));
	}

	private static LiteralArgumentBuilder<CommandSource> alias(String prefix) {
		return Commands.literal(prefix).requires(player -> player.hasPermission(VanishConfig.CONFIG.vanishCommandPermissionLevel.get())).executes(ctx -> vanish(ctx, ctx.getSource().getPlayerOrException()))
				.then(Commands.literal("toggle").executes(ctx -> vanish(ctx, ctx.getSource().getPlayerOrException()))
						.then(Commands.argument("player", EntityArgument.player()).executes(ctx -> vanish(ctx, EntityArgument.getPlayer(ctx, "player")))))
				.then(Commands.literal("get").executes(ctx -> getVanishedStatus(ctx, ctx.getSource().getPlayerOrException()))
						.then(Commands.argument("player", EntityArgument.player()).executes(ctx -> getVanishedStatus(ctx, EntityArgument.getPlayer(ctx, "player")))))
				.then(Commands.literal("queue").executes(ctx -> queue(ctx, ctx.getSource().getPlayerOrException().getGameProfile().getName()))
						.then(Commands.argument("player", StringArgumentType.word()).suggests((ctx, suggestionsBuilder) -> ISuggestionProvider.suggest(ctx.getSource().getServer().getPlayerNames(), suggestionsBuilder)).executes(ctx -> queue(ctx, StringArgumentType.getString(ctx, "player")))));
	}

	private static int vanish(CommandContext<CommandSource> ctx, ServerPlayerEntity player) {
		ctx.getSource().sendSuccess(VanishUtil.VANISHMOD_PREFIX.copy().append(new TranslationTextComponent(!VanishUtil.isVanished(player) ? VanishConfig.CONFIG.onVanishMessage.get() : VanishConfig.CONFIG.onUnvanishMessage.get(), player.getDisplayName())), true);
		VanishUtil.toggleVanish(player);
		return 1;
	}

	private static int getVanishedStatus(CommandContext<CommandSource> ctx, ServerPlayerEntity player) {
		TranslationTextComponent vanishedStatus = VanishUtil.getVanishedStatusText(player);
		Entity currentEntity = ctx.getSource().getEntity();

		ctx.getSource().sendSuccess(VanishUtil.VANISHMOD_PREFIX.copy().append(vanishedStatus), false);

		if (currentEntity instanceof ServerPlayerEntity)
			((ServerPlayerEntity)currentEntity).connection.send(new STitlePacket(Type.ACTIONBAR, vanishedStatus));

		return 1;
	}

	private static int queue(CommandContext<CommandSource> ctx, String playerName) {
		ServerPlayerEntity player = ctx.getSource().getServer().getPlayerList().getPlayerByName(playerName);

		if (player != null) {
			 if (!VanishUtil.isVanished(player))
				 vanish(ctx, player);
			 else
				 ctx.getSource().sendFailure(VanishUtil.VANISHMOD_PREFIX.copy().append(new TranslationTextComponent("Could not add already vanished player %s to the vanishing queue", playerName)));

			return 1;
		}

		if (VanishUtil.addToQueue(playerName))
			ctx.getSource().sendSuccess(VanishUtil.VANISHMOD_PREFIX.copy().append(new TranslationTextComponent("Added %s to the vanishing queue", playerName)), true);
		else
			ctx.getSource().sendFailure(VanishUtil.VANISHMOD_PREFIX.copy().append(new TranslationTextComponent("Could not add already added player %s to the vanishing queue", playerName)));

		return 1;
	}
}
