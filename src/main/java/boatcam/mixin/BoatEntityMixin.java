package boatcam.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.BoatEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static boatcam.config.BoatCamConfig.getConfig;

@Mixin(BoatEntity.class)
public class BoatEntityMixin {

	@Inject(
			method = "copyEntityData",
			at = @At("HEAD"),
			cancellable = true
	)
	private void copyEntityData(Entity entity, CallbackInfo info) {
		if (entity.equals(MinecraftClient.getInstance().player) && getConfig().isTurnLimitDisabled()) {
			float yaw = ((Entity) (Object) this).getYaw();
			entity.setBodyYaw(yaw);
			entity.setHeadYaw(entity.getYaw());
			info.cancel();
		}
	}
}