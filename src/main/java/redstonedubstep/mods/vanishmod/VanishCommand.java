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
				player.sendMessage(new StringTextComponent("You are now vanished."), Util.DUMMY_UUID);
				player.sendMessage(new StringTextComponent("Note: You can still see yourself in the tab list for technical reasons, but you are vanished for other players."), Util.DUMMY_UUID);
				player.sendMessage(new StringTextComponent("Note: It is preferable to be in spectator mode while vanished, because other players can still see the items you hold and wear and can interact with your hitbox."), Util.DUMMY_UUID);
				VanishUtil.sendPacketOnVanish(ctx, true);
				VanishUtil.sendMessageToAllPlayers(ctx.getSource().getWorld().getPlayers(), ctx.getSource().asPlayer(), true);
				ctx.getSource().sendFeedback(new TranslationTextComponent("%s vanished", player.getDisplayName()), true);
			} else {
				VanishUtil.updateVanishedList(player, false);
				player.sendMessage(new StringTextComponent("You are now no longer vanished."), Util.DUMMY_UUID);
				VanishUtil.sendPacketOnVanish(ctx, false);
				VanishUtil.sendMessageToAllPlayers(ctx.getSource().getWorld().getPlayers(), ctx.getSource().asPlayer(), false);
				ctx.getSource().sendFeedback(new TranslationTextComponent("%s appeared again", player.getDisplayName()), true);
			}
			return 0;
		});
	}
}
