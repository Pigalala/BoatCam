package boatcam.mixin;

import boatcam.BoatCamMod;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(AbstractClientPlayerEntity.class)
public class AbstractClientPlayerEntityMixin {

    @ModifyVariable(
            method = "getFovMultiplier",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true
    )
    private float modifyFov(float fov) {
        return BoatCamMod.instance().getFovModifier(fov);
    }
}
