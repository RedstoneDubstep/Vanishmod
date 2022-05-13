[![](http://cf.way2muchnoise.eu/full_vanishmod_downloads.svg)](https://www.curseforge.com/minecraft/mc-mods/vanishmod) [![](http://cf.way2muchnoise.eu/versions/For%20MC_vanishmod_all.svg)](https://www.curseforge.com/minecraft/mc-mods/vanishmod/files)

Vanishmod
=============

Source code for the Forge mod "Vanishmod".

This forge mod allows admins (by default, it is also possible to grant everyone access to vanishing via the config file) to get vanished.

A vanished player won't show up in the tab list, it will get invisible and its join/leave/deathmessages and most sounds of them will be suppressed. There are also other, fully configurable features for players to hide themselves as best as possible, like modification of the /list command and the Server Status within the Multiplayer Screen.

Usage: 
    
    /v (or /vanish) to vanish yourself, /v to unvanish again.
    /v toggle <player> to vanish/unvanish another player.
    /v get <player> to get the vanished status of another player.

**IMPORTANT: In the current version of the mod, most sounds will get suppressed (like placing and breaking blocks, direct block interactions), but some (like finishing to eat, indirect block interactions like powering a dispenser, etc.) won't due to technical limitations.**

It is also wise to not write chat messages while vanished, as these won't get suppressed and other players then know that you're still on the server.

This mod uses a ton of mixins, so there will be bugs. If you encounter one, please report it on the Mod's GitHub issue tracker.

The Vanishmod only needs to be installed on the server to work.

Huge thanks to [bl4ckscor3](https://www.curseforge.com/members/bl4ckscor3/projects) for helping me out in the process of creating this mod. He's also created some cool mods, go check them out!
