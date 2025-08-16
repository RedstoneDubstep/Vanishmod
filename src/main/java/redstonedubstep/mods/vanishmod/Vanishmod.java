package redstonedubstep.mods.vanishmod;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@Mod(Vanishmod.MODID)
public class Vanishmod {
	public static final String MODID = "vmod"; //This is Vanishmod v1.1.18.1 for 1.21.8!
	public static boolean mc2discordDetected = false;

	public Vanishmod(ModContainer container) {
		NeoForge.EVENT_BUS.addListener(this::registerCommands);
		container.registerConfig(ModConfig.Type.SERVER, VanishConfig.SERVER_SPEC, "vanishmod-server.toml");
	}

	public void registerCommands(RegisterCommandsEvent event) {
		VanishCommand.register(event.getDispatcher());
	}
}
