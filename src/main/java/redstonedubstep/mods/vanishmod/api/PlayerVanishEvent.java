package redstonedubstep.mods.vanishmod.api;

import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public class PlayerVanishEvent extends PlayerEvent {
	private final boolean vanished;

	public PlayerVanishEvent(Player player, boolean vanished) {
		super(player);
		this.vanished = vanished;
	}

	public boolean isVanished() {
		return vanished;
	}
}
