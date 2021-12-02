-----------Changelog for v1.1.1-----------

- Fixed compatibility with latest mc2discord version (3.2.0)

-----------Changelog for v1.1.1-----------

- Added config option for a performance-heavy mod compatibility mixin
- Fixed heavily increased CPU usage of a mod compatibility mixin even though this mod's features were not used

-----------Changelog for v1.1.0-----------

- Added filtering for the output of the /list command (does not affect server admins)
- Added Config options with which you can toggle some features of the mod. The core features (hiding from tab list, not sending death messages, etc.) are not controllable, but toggleable features include rendering of vanished players, op level required for /vanish and filtering of /list
- Added config-toggleable feature of replacing the names of vanished players that send chat messages with "vanished". This feature is disabled by default, you can enable it in the mod's config file
- Fixed that vanished players can be targeted and identified by non-admins via the /msg command
- Fixed that unvanishing could cancel the invisibility granted by a currently active Invisibility effect

-----------Changelog for v1.0.9-----------

- Fixed that players could not get vanished in v1.0.8 if they didn't have any persistent player data stored

-----------Changelog for v1.0.8-----------

- Improved mod compatibility by marking vanished players as spectators for other mods
- Fixed fake join/leave messages not getting sent on discord servers via minecraft2discord
- Fixed that this mod deleted persistent player data from other mods

-----------Changelog for v1.0.7-----------

####DISCLAIMER: After updating the mod to this version, players will very likely not be vanished anymore despite being vanished before the update. This is caused by an internal change of where the vanished status is stored.

- Added support for minecraft2discord
- Added vanish event that modders can use to run actions when a player is vanished
- Added a feature to /vanish that tells players if they're vanished or not
- Fixed players not being vanished anymore after dying
- Blocked broadcasting of advancements gotten by vanished players

-----------Changelog for v1.0.6-----------

- Added player parameter to /v which allows admins to vanish other players
- Fixed that non-player entities couldn't get invisible with e.g. potions

-----------Changelog for v1.0.5-----------

- Fixed that the unvanished client thinks that it picked up an item while in reality a vanished player did
- Fixed that the vanished player list gets reset on server restart

-----------Changelog for v1.0.4-----------

- Moved parts of the sound suppression to an event
- Fixed vanished players causing particle effects
- Fixed that rejoining of a player would make vanished players visible in the world
- Fixed that vanished players get visible in the tablist when they rejoin

-----------Changelog for v1.0.3-----------

- Added /vanish as an alternative command prefix
- Fixed servers with this mod displaying an incompatible mod list warning
- Fixed that vanished players can be heard by other players
- Fixed some issues that occurred after rejoining a server while vanished
- Fixed vanished players not disappearing for hacked clients (#2)

-----------Changelog for v1.0.2-----------

- Fixed that vanished players show up in the multiplayer screen when hovering over the number of players online

-----------Changelog for v1.0.1-----------

- Added a message that tells vanished players that they're still vanished when they join a server
- Fixed that the player's armor doesn't get invisible when executing the command

-----------Changelog for v1.0-----------

- First release of the mod