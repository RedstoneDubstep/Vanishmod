package redstonedubstep.mods.vanishmod.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameRules.BooleanValue;
import net.minecraft.world.GameRules.RuleKey;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import redstonedubstep.mods.vanishmod.VanishUtil;

@Mixin(ServerPlayerEntity.class)
public abstract class MixinServerPlayerEntity extends PlayerEntity {

	//player entity needs a constructor, so here we go
	public MixinServerPlayerEntity(World world, BlockPos pos, float f, GameProfile gameProfile) {
		super(world, pos, f, gameProfile);
	}

	//suppress death messages when player is vanished by modification of the method that usually gets the value of the Gamerule showDeathMessages
	@Redirect(method="onDeath", at=@At(value="INVOKE", target="Lnet/minecraft/world/GameRules;getBoolean(Lnet/minecraft/world/GameRules$RuleKey;)Z", ordinal=0))
	public boolean redirectGetBoolean(GameRules gameRules, RuleKey<BooleanValue> key) {
		if (VanishUtil.isVanished(this.getUniqueID(), (ServerWorld)this.getEntityWorld()))
			return false;
		return gameRules.get(key).get();
	}
}
