package redstonedubstep.mods.vanishmod;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class VanishCommand {
	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(Commands.literal("v").requires(player -> player.hasPermissionLevel(2)).executes(VanishCommand::vanish));
		dispatcher.register(Commands.literal("vanish").requires(player -> player.hasPermissionLevel(2)).executes(VanishCommand::vanish));
	}

	private static int vanish(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
		ServerPlayerEntity player = ctx.getSource().asPlayer();

		if (!VanishUtil.isVanished(player)) { //when the player isn't already vanished
			VanishUtil.updateVanishedStatus(player, true);
			ctx.getSource().sendFeedback(new TranslationTextComponent("%s vanished", player.getDisplayName()), true);
			player.sendMessage(new StringTextComponent("Note: You can still see yourself in the tab list for technical reasons, but you are vanished for other players."), Util.DUMMY_UUID);
			player.sendMessage(new StringTextComponent("Note: It is preferable to be in spectator mode while vanished, because other players can still see the items you hold and wear and can interact with your hitbox."), Util.DUMMY_UUID);
			VanishUtil.sendMessageToAllPlayers(ctx.getSource().getWorld().getPlayers(), player, true);
			VanishUtil.sendPacketsOnVanish(ctx, true);
		}
		else {
			VanishUtil.updateVanishedStatus(player, false);
			ctx.getSource().sendFeedback(new TranslationTextComponent("%s appeared again", player.getDisplayName()), true);
			VanishUtil.sendMessageToAllPlayers(ctx.getSource().getWorld().getPlayers(), player, false);
			VanishUtil.sendPacketsOnVanish(ctx, false);
		}
		return 0;
	}
}
