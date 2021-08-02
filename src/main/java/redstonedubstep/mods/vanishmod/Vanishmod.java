package redstonedubstep.mods.vanishmod;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.network.FMLNetworkConstants;

@Mod(Vanishmod.MODID)
public class Vanishmod {
	public static final String MODID = "vanishmod"; //This is Vanishmod v1.1.1 for 1.16.5!

	public Vanishmod() {
		ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true));
		MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, VanishConfig.SERVER_SPEC);
	}

	public void registerCommands(RegisterCommandsEvent event) {
		VanishCommand.register(event.getDispatcher());
	}
}
