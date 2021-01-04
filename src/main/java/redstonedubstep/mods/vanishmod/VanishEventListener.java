package redstonedubstep.mods.vanishmod;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = Vanishmod.MODID)
public class VanishEventListener {

	@SubscribeEvent
	public static void onPlaySoundAtEntity(PlaySoundAtEntityEvent event) {
		if (event.getEntity() instanceof ServerPlayerEntity) {
			ServerPlayerEntity player = (ServerPlayerEntity)event.getEntity();

			if (VanishUtil.isVanished(player))
				event.setCanceled(true);
		}
	}
}
