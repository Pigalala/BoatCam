package boatcam.cam;

import boatcam.AngleUtil;
import boatcam.BoatCamKeybinds;
import boatcam.BoatCamMod;
import boatcam.yaw.mode.Legacy;
import boatcam.yaw.mode.Velocity;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.phys.Vec3;

import static boatcam.config.BoatCamConfig.getConfig;

public class BoatCamera implements BoatCam {

    private final LocalPlayer player;
    private final AbstractBoat boat;
    private final CameraType originalCameraType;
    public boolean overrideCamera;
    private Vec3 prevBoatPos;
    private float previousYaw;
    private boolean lookingBehind;

    public BoatCamera(LocalPlayer player, AbstractBoat boat) {
        this.player = player;
        this.boat = boat;
        this.originalCameraType = Minecraft.getInstance().options.getCameraType();

        this.prevBoatPos = boat.position();
        this.previousYaw = boat.getYRot();
    }

    @Override
    public void enable() {
        switch (getConfig().perspective) {
            case FIRST_PERSON -> Minecraft.getInstance().options.setCameraType(CameraType.FIRST_PERSON);
            case THIRD_PERSON -> Minecraft.getInstance().options.setCameraType(CameraType.THIRD_PERSON_BACK);
            default -> {
            }
        }

        if (getConfig().fixedPitch) {
            player.setXRot(getConfig().pitch);
        }
    }

    public void disable() {
        Minecraft.getInstance().options.setCameraType(originalCameraType);
        if (lookingBehind) {
            invertPitch();
        }
    }

    public void tick() {
        BoatCamKeybinds keybinds = BoatCamMod.instance().getKeybinds();

        overrideCamera = BoatCamMod.instance().shouldOverrideCamera(boat);

        if (overrideCamera) {
            updateYaw();
            updatePitch();
        }

        setLookingBehind(keybinds.lookBehind().isDown());
    }

    void updateYaw() {
        BoatCamKeybinds keybinds = BoatCamMod.instance().getKeybinds();
        float yaw = calculateYaw(keybinds.lookLeft().isDown(), keybinds.lookRight().isDown());
        player.setYRot(yaw);
    }

    void updatePitch() {
        float targetPitch = lookingBehind ? -getConfig().pitch : getConfig().pitch;
        if (getConfig().fixedPitch && player.getXRot() != targetPitch) {
            player.setXRot(targetPitch);
        }
    }

    float calculateYaw(boolean lookLeft, boolean lookRight) {
        float yaw = boat.getYRot();

        double dx = boat.getX() - prevBoatPos.x;
        double dz = boat.getZ() - prevBoatPos.z;

        // TODO: Implement look left and look right, maybe
        switch (getConfig().cachedYawMode) {
            case Legacy legacy -> {
                if (dx != 0 || dz != 0) {
                    float vel = (float) Math.hypot(dz, dx);
                    float direction = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90;
                    float t = Math.min(1, vel / 3); // max 70 m/s = 3.5 m/tick on blue ice, cut off at 3
                    yaw = AngleUtil.lerp(t, yaw, direction);
                }
                yaw = AngleUtil.lerp(legacy.smoothness / 100f, previousYaw, yaw);
            }
            case Velocity velocity -> {
                float strength = velocity.strength;
                if (dx != 0 || dz != 0) {
                    double yawRad = Math.toRadians(yaw + 90);
                    double yawUnitX = Math.cos(yawRad);
                    double yawUnitZ = Math.sin(yawRad);

                    double cameraX = dx * strength + 1.28f * yawUnitX * (100 - strength);
                    double cameraZ = dz * strength + 1.28f * yawUnitZ * (100 - strength);

                    yaw = (float) Math.toDegrees(Math.atan2(cameraZ, cameraX)) - 90;
                }
                yaw = AngleUtil.lerp(strength / 100f, previousYaw, yaw);
            }
        }

//        float directionOffset = 0f;
//        if (getConfig().snapSidewaysView) {
//            if (lookLeft) {
//                yaw -= 90f;
//            } else if (lookRight) {
//                yaw += 90f;
//            } else {
//                if (dx != 0 || dz != 0) {
//                    float vel = (float) Math.hypot(dz, dx);
//                    float direction = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90;
//                    float t = Math.min(1, vel / 3); // max 70 m/s = 3.5 m/tick on blue ice, cut off at 3
//                    yaw = AngleUtil.lerp(t, yaw, direction);
//                }
//                yaw = AngleUtil.lerp(getConfig().getSmoothness(), previousYaw, yaw);
//            }
//        } else {
//            if (lookLeft) {
//                yaw -= 90f;
//                directionOffset = -90f;
//            } else if (lookRight) {
//                yaw += 90f;
//                directionOffset = 90f;
//            }
//
//            if (dx != 0 || dz != 0) {
//                float vel = (float) Math.hypot(dz, dx);
//                float direction = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90;
//                float t = Math.min(1, vel / 3); // max 70 m/s = 3.5 m/tick on blue ice, cut off at 3
//                yaw = AngleUtil.lerp(t, yaw, direction + directionOffset);
//            }
//            yaw = AngleUtil.lerp(getConfig().getSmoothness(), previousYaw, yaw);
//        }

        previousYaw = yaw;
        prevBoatPos = boat.position();
        return yaw;
    }

    void setLookingBehind(boolean lookingBehind) {
        if (this.lookingBehind != lookingBehind) {
            if (lookingBehind) {
                // Now looking behind
                Minecraft.getInstance().options.setCameraType(CameraType.THIRD_PERSON_FRONT);
            } else {
                // No longer looking behind
                switch (getConfig().perspective) {
                    case FIRST_PERSON -> Minecraft.getInstance().options.setCameraType(CameraType.FIRST_PERSON);
                    case THIRD_PERSON -> Minecraft.getInstance().options.setCameraType(CameraType.THIRD_PERSON_BACK);
                    default -> {
                    }
                }
            }

            invertPitch();
        }

        this.lookingBehind = lookingBehind;
    }

    void invertPitch() {
        player.setXRot(-player.getXRot());
    }

    @Override
    public boolean isOverrideCamera() {
        return overrideCamera;
    }

    @Override
    public boolean shouldExit() {
        return !getConfig().boatMode || !(player.getVehicle() instanceof AbstractBoat);
    }
}
