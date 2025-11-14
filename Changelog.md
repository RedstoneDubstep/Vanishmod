-----------Changelog for v1.1.20-----------

- Added new config options for adjusting whether vanished players may reveal themselves through vanilla system messages, like join/leave/death messages
- Fixed error messages being logged when the hidePlayersFromWorld config option is disabled
- Fixed some logic related to vanished player sleeping not being controllable through the "hidePlayersFromWorld" config option
- Fixed vanished player names being suggested in some commands

-----------Changelog for v1.1.19.1-----------

- Fixed fake leave messages being sent by some other mods when joining while vanished

-----------Changelog for v1.1.19-----------

- Added config option to toggle serverside invisibility for vanished players

-----------Changelog for v1.1.18-----------

- Added messages on player join to inform about vanished status of other players
- Fixed memory leak on servers with many vanished players

-----------Changelog for v1.1.17.1-----------

- Fixed a crash that could occur with projectiles

-----------Changelog for v1.1.17-----------

- Added the "/v help" subcommand as an ingame explanation of all subcommands in this mod
- Added tracing, which is a debug functionality that, when enabled, reports any event caused by you that has been concealed for other players
- Added sound suppressing for projectiles owned by a vanished player
- Fixed the vanish command feedback message being able to reveal to admins that a player has vanished
- Potentially improved performance of the part of the mod responsible for checking whether a player is vanished

-----------Changelog for v1.1.16-----------

- Added configuration option to enable constant synchronisation between mc2discord's "Hidden Players" list and a player's vanished status

-----------Changelog for v1.1.15.1-----------

- Added compatibility for the JoinLeaveMessages mod to use its custom join/leave messages when vanishing/unvanishing

-----------Changelog for v1.1.15-----------

- Fixed server crash when trying to suppress certain sounds from other mods

-----------Changelog for v1.1.14-----------

- Optimised the construction of the custom server status that filters vanished players

-----------Changelog for v1.1.13.1-----------

- Added compatibility for mc2discord 4.1+

-----------Changelog for v1.1.13-----------

- Partially fixed some mods sending custom messages which reveal the presence of vanished players
  
-----------Changelog for v1.1.12.1-----------

- Added compatibility for mc2discord 4.x (Thanks DenisD3D!)

-----------Changelog for v1.1.12-----------

- Fixed incorrect online player count of servers with Vanishmod installed on the server list screen

-----------Changelog for v1.1.11-----------

- Updated to 1.20