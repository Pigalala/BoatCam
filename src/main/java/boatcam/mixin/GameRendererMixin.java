package boatcam.mixin;

import boatcam.config.BoatCamConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.vehicle.AbstractBoatEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static boatcam.config.BoatCamConfig.getConfig;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(
            method = "getFov",
            at = @At("HEAD"),
            cancellable = true
    )
    void getFov(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Float> cir) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null && player.getVehicle() instanceof AbstractBoatEntity && getConfig().isBoatMode()) {
            int fov = BoatCamConfig.getConfig().getFov();
            if (fov > 0) {
                cir.setReturnValue((float) fov);
            }
        }
    }
}
