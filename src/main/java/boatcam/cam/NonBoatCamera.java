package boatcam.cam;

import boatcam.BoatCamKeybinds;
import boatcam.BoatCamMod;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;

import static boatcam.config.BoatCamConfig.getConfig;

public class NonBoatCamera implements BoatCam {

    private final LocalPlayer player;
    private final CameraType originalCameraType;

    private boolean lookingBehind;
    private CameraType previousCameraType;

    public NonBoatCamera(LocalPlayer player) {
        this.player = player;
        this.originalCameraType = Minecraft.getInstance().options.getCameraType();

        this.previousCameraType = originalCameraType;
    }

    @Override
    public void enable() {
    }

    @Override
    public void disable() {
        Minecraft.getInstance().options.setCameraType(originalCameraType);

        if (lookingBehind) {
            invertPitch();
        }
    }

    @Override
    public void tick() {
        BoatCamKeybinds keybinds = BoatCamMod.instance().getKeybinds();
        setLookingBehind(keybinds.lookBehind().isDown());
    }

    @Override
    public boolean isOverrideCamera() {
        return false;
    }

    @Override
    public boolean shouldExit() {
        return getConfig().boatMode && player.getVehicle() instanceof AbstractBoat;
    }

    void invertPitch() {
        player.setXRot(-player.getXRot());
    }

    void setLookingBehind(boolean lookingBehind) {
        if (this.lookingBehind != lookingBehind) {
            if (lookingBehind) {
                // Now looking behind
                previousCameraType = Minecraft.getInstance().options.getCameraType();
                Minecraft.getInstance().options.setCameraType(CameraType.THIRD_PERSON_FRONT);
            } else {
                // No longer looking behind
                Minecraft.getInstance().options.setCameraType(previousCameraType);
            }

            invertPitch();
        }

        this.lookingBehind = lookingBehind;
    }
}
