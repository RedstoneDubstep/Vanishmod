package redstonedubstep.mods.vanishmod;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class VanishCommand {
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(vanish());
	}

	private static LiteralArgumentBuilder<CommandSource> vanish() {
		return Commands.literal("v").requires(player -> player.hasPermissionLevel(2)).executes(ctx -> {
			ServerPlayerEntity player = ctx.getSource().asPlayer();

			if (!VanishUtil.isVanished(player)) { //when the player isn't already vanished
				VanishUtil.updateVanishedList(player, true);
				ctx.getSource().sendFeedback(new TranslationTextComponent("vanishmod.command.message.vanished", player.getDisplayName()), true);
				player.sendMessage(new TranslationTextComponent("vanishmod.command.note.seeYourself"), Util.DUMMY_UUID);
				player.sendMessage(new TranslationTextComponent("vanishmod.command.note.spectatorMode"), Util.DUMMY_UUID);
				VanishUtil.sendMessageToAllPlayers(ctx.getSource().getWorld().getPlayers(), ctx.getSource().asPlayer(), true);
				VanishUtil.sendPacketsOnVanish(ctx, true);
			} else {
				VanishUtil.updateVanishedList(player, false);
				ctx.getSource().sendFeedback(new TranslationTextComponent("vanishmod.command.message.appeared", player.getDisplayName()), true);
				VanishUtil.sendMessageToAllPlayers(ctx.getSource().getWorld().getPlayers(), ctx.getSource().asPlayer(), false);
				VanishUtil.sendPacketsOnVanish(ctx, false);
			}
			return 0;
		});
	}
}
