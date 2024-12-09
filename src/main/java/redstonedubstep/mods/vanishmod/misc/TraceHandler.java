package redstonedubstep.mods.vanishmod.misc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.google.common.collect.Sets;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import redstonedubstep.mods.vanishmod.VanishConfig;
import redstonedubstep.mods.vanishmod.VanishUtil;

public class TraceHandler {
	public static final MutableComponent TRACE_PREFIX = Component.literal("").append(Component.literal("[").withStyle(ChatFormatting.WHITE)).append(Component.literal("§7Trace§r").withStyle(s -> s.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to disable"))).withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/vanish trace disable")))).append(Component.literal("] ").withStyle(ChatFormatting.WHITE));
	private static final Map<UUID, Map<String, Set<String>>> traceEntries = new HashMap<>();

	public static boolean isTracing(Player player) {
		return traceEntries.containsKey(player.getUUID());
	}

	public static void setTracing(ServerPlayer player, boolean shouldTrace) {
		UUID uuid = player.getUUID();

		if (shouldTrace)
			traceEntries.put(uuid, new HashMap<>());
		else
			traceEntries.remove(uuid);
	}

	public static void sendTraceStatus(ServerPlayer player) {
		int seeVanishedOpLevel = VanishConfig.CONFIG.seeVanishedPermissionLevel.get();
		List<String> permissionText = new ArrayList<>();

		if (VanishConfig.CONFIG.vanishedPlayersSeeEachOther.get())
			permissionText.add("Other vanished players");

		if (VanishConfig.CONFIG.seeVanishedTeamPlayers.get())
			permissionText.add("Members of your team");

		if (seeVanishedOpLevel >= 0)
			permissionText.add("All players with an operator level of " + seeVanishedOpLevel + " or higher");

		String permissionString = !permissionText.isEmpty() ? String.join(" + ", permissionText) : "Only yourself";

		MutableComponent visibleForComponent = Component.literal("# §nPlayers permitted to see you§r: " + permissionString + " ");
		List<Component> visibleForPlayerNames = player.server.getPlayerList().getPlayers().stream().filter(p -> p != player && !VanishUtil.isVanished(player, p)).map(Player::getDisplayName).toList();

		if (!visibleForPlayerNames.isEmpty())
			visibleForComponent.append(Component.literal("§7(...)").withStyle(s -> s.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Currently: ").append(ComponentUtils.formatList(visibleForPlayerNames, ComponentUtils.DEFAULT_SEPARATOR))))));

		player.sendSystemMessage(Component.literal("# §nTrace Status§r:"));
		player.sendSystemMessage(Component.literal("# §bAlways enabled§r: ").append(Component.literal("§7(...)").withStyle(s -> s.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Hiding from the tab list, hiding your skin, hiding join/leave/death/advancement/command feedback messages"))))));
		player.sendSystemMessage(getTracePrefix(VanishConfig.CONFIG.hidePlayersFromWorld.get()).append("Hiding from the world, like through suppressing sounds and particles"));
		player.sendSystemMessage(getTracePrefix(VanishConfig.CONFIG.hidePlayersFromPlayerLists.get()).append("Hiding from other player lists, like the server player list in the multiplayer screen"));
		player.sendSystemMessage(getTracePrefix(VanishConfig.CONFIG.disableCommandTargeting.get()).append("Hiding from player selectors in commands, like the /give command"));
		player.sendSystemMessage(getTracePrefix(VanishConfig.CONFIG.hideChatMessages.get()).append("Hiding chat and /teammsg messages"));
		player.sendSystemMessage(getTracePrefix(VanishConfig.CONFIG.hidePlayerNameInChat.get()).append("Hiding your player name in /say, /me and /msg messages, replacing it with \"§7vanished§r\""));
		player.sendSystemMessage(visibleForComponent);
	}

	private static MutableComponent getTracePrefix(boolean enabled) {
		return Component.empty().append(enabled ? "# §aEnabled§r: " : "# §7Disabled§r: ");
	}

	public static void trace(Player player, String group, String identifier) {
		if (isTracing(player)) {
			Map<String, Set<String>> playerTraceEntries = traceEntries.get(player.getUUID());

			if (playerTraceEntries.containsKey(group))
				playerTraceEntries.get(group).add(identifier);
			else
				playerTraceEntries.put(group, Sets.newHashSet(identifier));
		}
	}

	public static void sendTraceEntries(ServerPlayer player) {
		if (isTracing(player)) {
			Map<String, Set<String>> playerTraceEntries = traceEntries.get(player.getUUID());

			if (playerTraceEntries.isEmpty())
				return;

			player.sendSystemMessage(VanishUtil.VANISHMOD_PREFIX.copy().append(TRACE_PREFIX).append("Concealed the following events:"));

			for (Map.Entry<String, Set<String>> traceEntry : playerTraceEntries.entrySet()) {
				player.sendSystemMessage(Component.literal("# §5" + traceEntry.getKey() + "§r: " + traceEntry.getValue().size() + " ").append(Component.literal("§7(...)").withStyle(s -> s.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, ComponentUtils.formatList(traceEntry.getValue()))))));
			}

			playerTraceEntries.clear();
		}
	}
}
