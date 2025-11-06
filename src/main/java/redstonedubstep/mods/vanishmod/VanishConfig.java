package redstonedubstep.mods.vanishmod;

import org.apache.commons.lang3.tuple.Pair;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;

public class VanishConfig {
	public static final ModConfigSpec SERVER_SPEC;
	public static final Config CONFIG;

	static {
		final Pair<Config, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(Config::new);

		SERVER_SPEC = specPair.getRight();
		CONFIG = specPair.getLeft();
	}

	public static class Config {
		public BooleanValue hidePlayersFromWorld;
		public BooleanValue hidePlayersFromPlayerLists;
		public BooleanValue disableCommandTargeting;
		public BooleanValue hideChatMessages;
		public BooleanValue hideSystemMessages;
		public BooleanValue hidePlayerNameInChat;
		public BooleanValue hidePlayerNameInSystemMessages;
		public BooleanValue sendFakeJoinLeaveMessages;
		public BooleanValue fixPlayerDetectionModCompatibility;
		public BooleanValue removeModdedSystemMessageReferences;
		public BooleanValue vanishedPlayersSeeEachOther;
		public BooleanValue seeVanishedTeamPlayers;
		public BooleanValue indirectSoundSuppression;
		public BooleanValue indirectParticleSuppression;
		public BooleanValue spoofVanishedPlayerInvisibility;
		public BooleanValue forceSyncHiddenList;
		public IntValue vanishCommandPermissionLevel;
		public IntValue seeVanishedPermissionLevel;
		public ConfigValue<String> onVanishMessage;
		public ConfigValue<String> onUnvanishMessage;
		public ConfigValue<String> onVanishQuery;
		public ConfigValue<String> onUnvanishQuery;
		public ConfigValue<String> vanishedPlayerNameReplacement;

		Config(ModConfigSpec.Builder builder) {
			hidePlayersFromWorld = builder
					.comment(" --- Vanishmod Config File --- ",
							"",
							"Should vanished players be physically hidden from the world?",
							"If this config option is enabled, sounds and particles by vanished players will be suppressed and they will not be targeted by monsters, among other things.",
							"Note that the skins of vanished players will never show up for non-permitted players, even if this config option is disabled, due to a vanilla limitation involving the tab list.")
					.define("hidePlayersFromWorld", true);
			hidePlayersFromPlayerLists = builder
					.comment("Should vanished players be hidden from player lists such as the /list command and the list in the Multiplayer screen?")
					.define("hidePlayersFromPlayerLists", true);
			disableCommandTargeting = builder
					.comment("Should vanished players not be targetable by non-permitted players via command selectors?",
							"This prevents those players from uncovering the presence of vanished players through e.g. /msg or /give.")
					.define("disableCommandTargeting", true);
			hideChatMessages = builder
					.comment("Should chat and /teammsg messages from vanished players be suppressed for non-permitted players?",
							"Note that this does not affect emote commands such as /say and /me, as well as private message ones like /msg.")
					.define("hideChatMessages", true);
			hideSystemMessages = builder
					.comment("Should system messages from vanished players, e.g. join, leave, death and advancement messages, be suppressed for non-permitted players?")
					.define("hideSystemMessages", true);
			hidePlayerNameInChat = builder
					.comment("Should the name of vanished players within chat messages sent to non-permitted players be concealed by replacing it with a custom string?",
							"This applies to all chat messages, including ones from e.g. /say and /msg.",
							"The string that the player name will be replaced with can be customized through the \"vanishedPlayerNameReplacement\" config option.")
					.define("hidePlayerNameInChat", false);
			hidePlayerNameInSystemMessages = builder
					.comment("Should the name of vanished players within system messages, e.g. join, leave, death and advancement messages, be concealed by replacing it with a custom string?",
							"The string that the player name will be replaced with can be customized through the \"vanishedPlayerNameReplacement\" config option.")
					.define("hidePlayerNameInSystemMessages", true);
			sendFakeJoinLeaveMessages = builder
					.comment("Should vanished players send all non-permitted players a fake leave/join message when they vanish or unvanish?")
					.define("sendFakeJoinLeaveMessages", true);
			fixPlayerDetectionModCompatibility = builder
					.comment("Should there be a (potential) fix for other mods uncovering the presence of vanished players?",
							"This may severely decrease the game's performance and is thus not enabled by default.")
					.define("fixPlayerDetectionModCompatibility", false);
			removeModdedSystemMessageReferences = builder
					.comment("Should this mod unconditionally and strictly remove (mostly) all references of names of vanished players by system messages added by mods?",
							"This is experimental, disable the config if too many modded messages get removed.",
							"Vanilla messages mentioning vanished players, such as death and advancement messages, will be removed regardless of this config's state.")
					.define("removeModdedSystemMessageReferences", true);
			vanishedPlayersSeeEachOther = builder
					.comment("Should vanished players be able to see each other?")
					.define("vanishedPlayersSeeEachOther", false);
			seeVanishedTeamPlayers = builder
					.comment("Should vanished players be visible for all players within their vanilla team, if the \"seeFriendlyInvisibles\" option is enabled for that team?")
					.define("seeVanishedTeamPlayers", false);
			indirectSoundSuppression = builder
					.comment("Should sounds that vanished players cause indirectly (e.g. pressing a button or hitting an entity) be suppressed?",
							"This detection might accidentally suppress some sounds unrelated to vanished players, disable this detection if too many sound bugs occur")
					.define("indirectSoundSuppression", true);
			indirectParticleSuppression = builder
					.comment("Should particles that vanished players cause indirectly (e.g. eating or block breaking particles) be suppressed?",
							"This detection might accidentally suppress particles unrelated to vanished players, disable this detection if too many visual bugs occur")
					.define("indirectParticleSuppression", true);
			spoofVanishedPlayerInvisibility = builder
					.comment("Should vanished players be regarded as having the Invisibility status effect on the server side?",
							"This does not actually affect if the player is rendered or not, but it may allow vanished players to hide from certain serverside map tools like Dynmap.")
					.define("spoofVanishedPlayerInvisibility", true);
			forceSyncHiddenList = builder
					.comment("Should the \"Hidden Players\" list from mc2discord be constantly synchronised with a player's vanished status? (This might lead to worse performance)")
					.define("forceSyncHiddenList", false);

			vanishCommandPermissionLevel = builder
					.comment("What operator permission level should be the requirement for being able to execute /vanish? A value of 2 or lower allows command blocks to execute /vanish.")
					.defineInRange("vanishCommandPermissionLevel", 2, 0, 4);
			seeVanishedPermissionLevel = builder
					.comment("What operator permission level should be the requirement for non-vanished players to be able to see vanished players?",
							"A value of 0 means that every player is able to see vanished players. A value of -1 disables this feature, meaning that the operator level is not taken into account when determining who a player is vanished for.")
					.defineInRange("seeVanishedPermissionLevel", -1, -1, 4);

			onVanishMessage = builder
					.comment("What message should a player receive when they vanish? Insert %s as a placeholder for the name of the vanishing player.")
					.define("onVanishMessage", "%s vanished");
			onUnvanishMessage = builder
					.comment("What message should a player receive when they unvanish? Insert %s as a placeholder for the name of the unvanishing player.")
					.define("onUnvanishMessage", "%s unvanished");
			onVanishQuery = builder
					.comment("What message should a player receive if they query the vanished status of a vanished player? Insert %s as a placeholder for the name of the player that the status is queried of.")
					.define("onVanishQuery", "%s is currently vanished.");
			onUnvanishQuery = builder
					.comment("What message should a player receive if they query the vanished status of a visible player? Insert %s as a placeholder for the name of the player that the status is queried of.")
					.define("onUnvanishQuery", "%s is currently not vanished.");
			vanishedPlayerNameReplacement = builder
					.comment("What string should the name of vanished players be replaced with if the \"hidePlayerNameInChat\" config option is enabled?")
					.define("vanishedPlayerNameReplacement", "§7vanished");
		}
	}
}
