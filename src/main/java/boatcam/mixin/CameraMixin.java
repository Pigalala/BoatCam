package boatcam.mixin;

import boatcam.BoatCamMod;
import net.minecraft.client.render.Camera;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Camera.class)
public class CameraMixin {

    @ModifyVariable(
            method = "moveBy",
            at = @At(
                    value = "STORE",
                    ordinal = 0
            )
    )
    Vector3f moveBy(Vector3f vec) {
        return BoatCamMod.instance().getCameraDistance(vec);
    }
}
