package boatcam;

import boatcam.config.BoatCamConfig;
import boatcam.config.BoatCamConfigScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.phys.Vec3;

import static boatcam.config.BoatCamConfig.getConfig;
import static java.lang.Math.*;
import static com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM;
import static net.minecraft.ChatFormatting.GREEN;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_B;

public final class BoatCamMod implements ClientModInitializer {

	private static BoatCamMod INSTANCE;

	private final KeyMapping.Category KEY_BINDING_CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("boatcam", "boatcam"));

	private final KeyMapping MENU = new KeyMapping("key.boatcam.menu", KEYSYM, GLFW_KEY_B, KEY_BINDING_CATEGORY);
	private final KeyMapping TOGGLE = new KeyMapping("key.boatcam.toggle", KEYSYM, -1, KEY_BINDING_CATEGORY);
	private final KeyMapping LOOK_BEHIND = new KeyMapping("key.boatcam.lookbehind", KEYSYM, -1, KEY_BINDING_CATEGORY);
	private final KeyMapping LOOK_LEFT = new KeyMapping("key.boatcam.lookleft", KEYSYM, -1, KEY_BINDING_CATEGORY);
	private final KeyMapping LOOK_RIGHT = new KeyMapping("key.boatcam.lookright", KEYSYM, -1, KEY_BINDING_CATEGORY);

	private CameraType perspective = null;
	private CameraType previousPerspective = null;
	private Vec3 boatPos = Vec3.ZERO;
	private float previousYaw;
	private boolean unfixedCameraActive = false;
	private boolean lookingBehind = false;

	public BoatCamMod() {
		INSTANCE = this;
	}

	@Override
	public void onInitializeClient() {
		try {
			BoatCamConfig.load();
		} catch (Exception e) {
			System.out.println("Could not load config.");
			throw new RuntimeException(e);
		}


		ClientTickEvents.START_CLIENT_TICK.register(this::onClientStartWorldTick);
	}

	private void onClientStartWorldTick(Minecraft client) {
		if (client.player == null) return;
		if (MENU.consumeClick()) {
			client.setScreen(new BoatCamConfigScreen(client.screen));
			return;
		}

		if (TOGGLE.consumeClick()) {
			getConfig().toggleBoatMode();
			client.gui.setOverlayMessage(Component.literal(getConfig().boatMode ? "Boat mode" : "Normal mode").withStyle(s -> s.withColor(GREEN)), false);
		}

		if (getConfig().boatMode && client.player.getVehicle() instanceof AbstractBoat boat) {
			calculateYaw(client.player, boat);

			// Was stationary but now moving and vice versa
			if (unfixedCameraActive != shouldOverrideCamera(boat)) {
				unfixedCameraActive = !unfixedCameraActive;
				if (unfixedCameraActive && getConfig().fixedPitch) {
					if (lookingBehind) {
						client.player.setXRot(-getConfig().pitch);
					} else {
						client.player.setXRot(getConfig().pitch);
					}
				}
			}

			// first tick riding in boat mode
			if (perspective == null) {
				perspective = client.options.getCameraType();

				switch (getConfig().getPerspective()) {
					case FIRST_PERSON -> client.options.setCameraType(CameraType.FIRST_PERSON);
					case THIRD_PERSON -> {
						if (lookingBehind) {
							client.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
						} else {
							client.options.setCameraType(CameraType.THIRD_PERSON_BACK);
						}
					}
				}

				if (getConfig().fixedPitch) {
					if (lookingBehind) {
						client.player.setXRot(-getConfig().pitch);
					} else {
						client.player.setXRot(getConfig().pitch);
					}
				} else {
					if (lookingBehind) {
						client.player.setXRot(-getConfig().pitch);
					}
                }
			}
		} else {
			// first tick after disabling boat mode or leaving boat
			if (perspective != null) {
				if (!lookingBehind) {
                    Minecraft.getInstance().options.setCameraType(perspective);
                }
                perspective = null;
			}
		}

		// if pressed state changed
		if (LOOK_BEHIND.isDown() != lookingBehind) {
			lookingBehind = LOOK_BEHIND.isDown();
			invertPitch();
			toggleLookBehindPerspective();
		}
	}

private void toggleLookBehindPerspective() {
    Minecraft client = Minecraft.getInstance();
    if (lookingBehind) {
        previousPerspective = client.options.getCameraType();
        client.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
    } else {
        client.options.setCameraType(previousPerspective);
        perspective = null;
    }
}

	private void invertPitch() {
		LocalPlayer player = Minecraft.getInstance().player;
		player.setXRot(-player.getXRot());
	}

	private void calculateYaw(LocalPlayer player, AbstractBoat boat) {
		float yaw = boat.getYRot();

		if (!shouldOverrideCamera(boat)) {
			previousYaw = yaw;
			boatPos = boat.position();
			return;
		}

		double dx = boat.getX() - boatPos.x;
		double dz = boat.getZ() - boatPos.z;

		float directionOffset = 0f;
		if (getConfig().snapSidewaysView) {
			if (LOOK_LEFT.isDown()) {
				yaw -= 90f;
			} else if (LOOK_RIGHT.isDown()) {
				yaw += 90f;
			} else {
				if (dx != 0 || dz != 0) {
					float vel = (float) hypot(dz, dx);
					float direction = (float) toDegrees(atan2(dz, dx)) - 90;
					float t = min(1, vel / 3); // max 70 m/s = 3.5 m/tick on blue ice, cut off at 3
					yaw = AngleUtil.lerp(t, yaw, direction);
				}
				yaw = AngleUtil.lerp(getConfig().getSmoothness(), previousYaw, yaw);
			}
		} else {
			if (LOOK_LEFT.isDown()) {
				yaw -= 90f;
				directionOffset = -90f;
			} else if (LOOK_RIGHT.isDown()) {
				yaw += 90f;
				directionOffset = 90f;
			}

			if (dx != 0 || dz != 0) {
				float vel = (float) hypot(dz, dx);
				float direction = (float) toDegrees(atan2(dz, dx)) - 90;
				float t = min(1, vel / 3); // max 70 m/s = 3.5 m/tick on blue ice, cut off at 3
				yaw = AngleUtil.lerp(t, yaw, direction + directionOffset);
			}
			yaw = AngleUtil.lerp(getConfig().getSmoothness(), previousYaw, yaw);
		}



		player.setYRot(yaw);

		previousYaw = yaw;
		boatPos = boat.position();
	}

	// If returns true, look direction change should be cancelled
	public boolean onLookDirectionChanging(double dx, double dy) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (!(player.getVehicle() instanceof AbstractBoat b)) {
			return false;
		}

		if (getConfig().boatMode && shouldOverrideCamera(b)) {
			if (dx != 0 || getConfig().fixedPitch && dy != 0) {
				player.turn(0, getConfig().fixedPitch ? 0 : dy);
				return true;
			}
		}

		return false;
	}

	boolean shouldOverrideCamera(AbstractBoat boat) {
		return !getConfig().stationaryLookAround || LOOK_LEFT.isDown() || LOOK_RIGHT.isDown() || boat.getDeltaMovement().lengthSqr() >= 0.01 * 0.01;
	}

	public static BoatCamMod instance() {
		return INSTANCE;
	}
}
