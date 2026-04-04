package boatcam.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static boatcam.config.BoatCamConfig.getConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;

@Mixin(AbstractBoat.class)
public class BoatEntityMixin {

    @Inject(
            method = "clampRotation",
            at = @At("HEAD"),
            cancellable = true
    )
    private void clampRotation(Entity entity, CallbackInfo info) {
        if (entity.equals(Minecraft.getInstance().player) && getConfig().isTurnLimitDisabled()) {
            float yaw = ((Entity) (Object) this).getYRot();
            entity.setYBodyRot(yaw);
            entity.setYHeadRot(entity.getYRot());
            info.cancel();
        }
    }
}