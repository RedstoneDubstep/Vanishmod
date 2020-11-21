package redstonedubstep.mods.vanishmod;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Vanishmod.MODID)
public class Vanishmod {
    public static final String MODID = "vanishmod";

	public Vanishmod() {
		MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
	}

	public void registerCommands(RegisterCommandsEvent event){
		VanishCommand.register(event.getDispatcher());
	}
}
