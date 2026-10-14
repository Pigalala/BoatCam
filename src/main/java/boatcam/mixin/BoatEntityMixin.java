package boatcam.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static boatcam.config.BoatCamConfig.getConfig;

@Mixin(AbstractBoat.class)
public class BoatEntityMixin {

    @Inject(
            method = "clampRotation",
            at = @At("HEAD"),
            cancellable = true
    )
    private void clampRotation(Entity passenger, CallbackInfoReturnable<Float> cir) {
        if (passenger == Minecraft.getInstance().player && getConfig().isTurnLimitDisabled()) {
            float yaw = ((Entity) (Object) this).getYRot();
            passenger.setYBodyRot(yaw);
            passenger.setYHeadRot(passenger.getYRot());
            cir.setReturnValue(0f);
        }
    }
}