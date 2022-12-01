-----------Changelog for v1.1.9-----------

- Made vanished players hear the sounds they produce
- Fixed some entities like passive mobs or pufferfish detecting vanished players
- The minimum required Forge version is now 40.1.84
  
-----------Changelog for v1.1.8-----------

- Fixed occasional crash when trying to interact with blocks or entities while vanished

-----------Changelog for v1.1.7-----------

- Added chat message suppression, which prevents chat messages sent by vanished players from being received by everyone
- Fixed fake join/leave messages being passed to mc2discord even though there is no connection to Discord

-----------Changelog for v1.1.6-----------

- Added "queue" command argument to be able to add players that are not online to a vanishing queue. The players in that queue will be vanished as soon as they join the server

-----------Changelog for v1.1.5-----------

- Added better sound suppression for eating sounds
- Added indirect sound suppression which aims to suppress sounds that are indirectly caused by vanished players, and a config option to disable it
- Fixed several issues occurring when a player vanishes or unvanishes while other players are in another dimension
- Fixed "Vanished" player name prefix being updated slightly too late, causing it to be briefly visible for other players when a player unvanishes
- Fixed vanished players being targetable by players that cannot see them through certain (operator-only) commands
- Fixed hostile mobs being able to target vanished players

-----------Changelog for v1.1.4-----------

Disclaimer: In this version, some config option names were changed, this may reset some config values so please make sure to update them appropriately!
- Added "Vanished" prefix to the tab list player name when the player is vanished
- Added "Vanished" prefix to the message that the joining player receives while being vanished
- Added config option to enable that vanished players can see each other, as well as a configurable op level at which players can see vanished players
- Changed the mod id of this mod from "vanishmod" to "vmod", this should not affect players
- Fixed that vanished players need to sleep in order for the night to be skipped
- Potentially fixed that vanished players could not join a server when Disguiselib is installed

-----------Changelog for v1.1.3.1-----------

- Potentially fixed some instances of the server not starting when the Vanishmod is installed alongside other mods
- Fixed that vanished players are able to trigger Sculk Sensors

-----------Changelog for v1.1.3-----------

- Added config to control whether other players should receive join/leave messages when a player (un-)vanishes
- Added configs to change some messages of this mod, for more info please check out the mod's config file
- Added subtitle message displaying the current vanished status when vanishing/unvanishing or querying the vanished status
- Added "Vanishmod" prefix to most of this mod's messages
- Changed "Note" messages to only be visible as a tooltip when hovering over text to reduce spam
- Fixed that Command Blocks are not able to execute /v by default

-----------Changelog for v1.1.2-----------

- Updated to 1.18