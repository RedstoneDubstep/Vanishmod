package redstonedubstep.mods.vanishmod;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.network.NetworkConstants;

@Mod(Vanishmod.MODID)
public class Vanishmod {
	public static final String MODID = "vmod"; //This is Vanishmod v1.1.5 for 1.18.2!

	public Vanishmod() {
		ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (a, b) -> true));
		MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, VanishConfig.SERVER_SPEC, "vanishmod-server.toml");
	}

	public void registerCommands(RegisterCommandsEvent event) {
		VanishCommand.register(event.getDispatcher());
	}
}
