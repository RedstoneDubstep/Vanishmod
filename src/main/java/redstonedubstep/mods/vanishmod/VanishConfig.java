package redstonedubstep.mods.vanishmod;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;

public class VanishConfig {
	public static final ForgeConfigSpec SERVER_SPEC;
	public static final Config CONFIG;

	static {
		final Pair<Config, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Config::new);

		SERVER_SPEC = specPair.getRight();
		CONFIG = specPair.getLeft();
	}

	public static class Config {
		public BooleanValue hidePlayersFromWorld;
		public BooleanValue hidePlayersFromPlayerLists;
		public BooleanValue hidePlayersFromCommandSelectors;
		public BooleanValue hidePlayerNameInChat;
		public BooleanValue sendJoinLeaveMessages;
		public BooleanValue fixModCompat;
		public IntValue requiredPermissionLevel;
		public ConfigValue<String> onVanishMessage;
		public ConfigValue<String> onUnvanishMessage;
		public ConfigValue<String> onVanishQuery;
		public ConfigValue<String> onUnvanishQuery;

		Config(ForgeConfigSpec.Builder builder) {
			hidePlayersFromWorld = builder
					.comment("Should vanished players be physically hidden from the world? This includes sound and particle suppression")
					.define("hidePlayersFromWorld", true);
			hidePlayersFromPlayerLists = builder
					.comment("Should vanished players be hidden from player lists such as the /list command and the list in the Multiplayer screen?")
					.define("hidePlayersFromPlayerLists", true);
			hidePlayersFromCommandSelectors = builder
					.comment("Should vanished players not be targetable by normal players via command selectors (so non-admins cannot target vanished players with e.g. /msg)?")
					.define("hidePlayersFromCommandSelectors", true);
			hidePlayerNameInChat = builder
					.comment("When vanished players send a chat message, should the name of the player that sent the message be replaced with \"vanished\" (in gray color)?")
					.define("hidePlayerNameInChat", false);
			sendJoinLeaveMessages = builder
					.comment("Should other players see a join/leave message in their chat when a player (un-)vanishes?")
					.define("sendJoinLeaveMessages", true);
			fixModCompat = builder
					.comment("Should there be a (potential) fix for other mods uncovering the presence of vanished players? This may severely increase CPU usage and is thus not recommended")
					.define("fixModCompat", false);

			requiredPermissionLevel = builder
					.comment("What op permission level should be the requirement for being able to execute /vanish?")
					.defineInRange("requiredPermissionLevel", 2, 0, 4);

			onVanishMessage = builder
					.comment("What message should the vanishing player receive if they vanish? (%s will get replaced with the name of the vanishing player)")
					.define("onVanishMessage", "%s vanished");
			onUnvanishMessage = builder
					.comment("What message should the now visible player receive if they unvanish? (%s will get replaced with the name of the unvanishing player)")
					.define("onUnvanishMessage", "%s unvanished");
			onVanishQuery = builder
					.comment("What message should the player receive if they query their vanished status while they are vanished? (%s will get replaced with the name of the querying player)")
					.define("onVanishQuery", "%s is currently vanished.");
			onUnvanishQuery = builder
					.comment("What message should the player receive if they query their vanished status while they are not vanished? (%s will get replaced with the name of the querying player)")
					.define("onUnvanishQuery", "%s is currently not vanished.");
		}
	}
}
