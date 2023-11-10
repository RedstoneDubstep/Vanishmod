package redstonedubstep.mods.vanishmod;

import net.neoforged.fml.IExtensionPoint;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.NetworkConstants;

@Mod(Vanishmod.MODID)
public class Vanishmod {
	public static final String MODID = "vmod"; //This is Vanishmod v1.1.14 for 1.20.1!

	public Vanishmod() {
		ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (a, b) -> true));
		NeoForge.EVENT_BUS.addListener(this::registerCommands);
		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, VanishConfig.SERVER_SPEC, "vanishmod-server.toml");
	}

	public void registerCommands(RegisterCommandsEvent event) {
		VanishCommand.register(event.getDispatcher());
	}
}
