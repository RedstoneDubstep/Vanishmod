package redstonedubstep.mods.vanishmod.misc;

import net.minecraft.server.level.ServerPlayer;

public class FieldHolder {
	//The player that is currently in the process of joining the server. Required due to new players not being added to PlayerList when the join message for them is sent
	public static ServerPlayer joiningPlayer;
}
