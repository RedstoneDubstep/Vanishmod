package redstonedubstep.mods.vanishmod;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class PlayerVanishEvent extends PlayerEvent {
    private final boolean vanished;

    public PlayerVanishEvent(PlayerEntity player, boolean vanished) {
        super(player);
        this.vanished = vanished;
    }

    public boolean isVanished() {
        return vanished;
    }
}
