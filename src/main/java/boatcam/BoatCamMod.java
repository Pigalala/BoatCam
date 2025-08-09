package boatcam;

import boatcam.config.BoatCamConfig;
import boatcam.config.BoatCamConfigScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.vehicle.AbstractBoatEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import static boatcam.config.BoatCamConfig.getConfig;
import static java.lang.Math.*;
import static net.minecraft.client.util.InputUtil.Type.KEYSYM;
import static net.minecraft.util.Formatting.GREEN;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_B;

public final class BoatCamMod implements ClientModInitializer {

	private static BoatCamMod INSTANCE;

	private final KeyBinding MENU = new KeyBinding("key.boatcam.menu", KEYSYM, GLFW_KEY_B, "BoatCam");
	private final KeyBinding TOGGLE = new KeyBinding("key.boatcam.toggle", KEYSYM, -1, "BoatCam");
	private final KeyBinding LOOK_BEHIND = new KeyBinding("key.boatcam.lookbehind", KEYSYM, -1, "BoatCam");
	private final KeyBinding LOOK_LEFT = new KeyBinding("key.boatcam.lookleft", KEYSYM, -1, "BoatCam");
	private final KeyBinding LOOK_RIGHT = new KeyBinding("key.boatcam.lookright", KEYSYM, -1, "BoatCam");

	private Perspective perspective = null;
	private Perspective previousPerspective = null;
	private Vec3d boatPos = Vec3d.ZERO;
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

		KeyBindingHelper.registerKeyBinding(MENU);
		KeyBindingHelper.registerKeyBinding(TOGGLE);
		KeyBindingHelper.registerKeyBinding(LOOK_BEHIND);
		KeyBindingHelper.registerKeyBinding(LOOK_LEFT);
		KeyBindingHelper.registerKeyBinding(LOOK_RIGHT);

		ClientTickEvents.START_WORLD_TICK.register(this::onClientStartWorldTick);
	}

	private void onClientStartWorldTick(ClientWorld world) {
		MinecraftClient client = MinecraftClient.getInstance();

		if (MENU.wasPressed()) {
			client.setScreen(new BoatCamConfigScreen(client.currentScreen));
			return;
		}

		if (TOGGLE.wasPressed()) {
			getConfig().toggleBoatMode();
			client.inGameHud.setOverlayMessage(Text.literal(getConfig().boatMode ? "Boat mode" : "Normal mode").styled(s -> s.withColor(GREEN)), false);
		}

		if (getConfig().boatMode && client.player.getVehicle() instanceof AbstractBoatEntity boat) {
			calculateYaw(client.player, boat);

			// Was stationary but now moving and vice versa
			if (unfixedCameraActive != shouldOverrideCamera(boat)) {
				unfixedCameraActive = !unfixedCameraActive;
				if (getConfig().fixedPitch) {
					client.player.setPitch(getConfig().pitch);
				}
			}

			// first tick riding in boat mode
			if (perspective == null) {
				if (lookingBehind) {
					invertPitch();
					lookingBehind = false;
				}

				// save perspective
				perspective = client.options.getPerspective();
				// set perspective
				switch (getConfig().getPerspective()) {
					case FIRST_PERSON -> client.options.setPerspective(Perspective.FIRST_PERSON);
					case THIRD_PERSON -> client.options.setPerspective(Perspective.THIRD_PERSON_BACK);
				}

				if (getConfig().fixedPitch) {
					client.player.setPitch(getConfig().pitch);
				}
			}
		} else {
			// first tick after disabling boat mode or leaving boat
			if (perspective != null) {
				resetPerspective();
				// invert pitch if looking behind
				if (lookingBehind) {
					invertPitch();
					lookingBehind = false;
				}
			}
		}

		// if pressed state changed
		if (LOOK_BEHIND.isPressed() != lookingBehind) {
			lookingBehind = LOOK_BEHIND.isPressed();
			invertPitch();

			if (lookingBehind) {
				previousPerspective = client.options.getPerspective();
				client.options.setPerspective(Perspective.THIRD_PERSON_FRONT);
			} else {
				client.options.setPerspective(previousPerspective);
				perspective = null;
			}
		}
	}

	private void invertPitch() {
		ClientPlayerEntity player = MinecraftClient.getInstance().player;
		player.setPitch(-player.getPitch());
	}

	// Assumes perspective is not null
	private void resetPerspective() {
		MinecraftClient.getInstance().options.setPerspective(perspective);
		perspective = null;
	}

	private void calculateYaw(ClientPlayerEntity player, AbstractBoatEntity boat) {
		float yaw = boat.getYaw();

		if (!shouldOverrideCamera(boat)) {
			previousYaw = yaw;
			boatPos = boat.getPos();
			return;
		}

		float directionOffset = 0f;
		if (LOOK_LEFT.isPressed()) {
			yaw -= 90f;
			directionOffset = -90f;
		} else if (LOOK_RIGHT.isPressed()) {
			yaw += 90;
			directionOffset = 90f;
		}

		double dx = boat.getX() - boatPos.x;
		double dz = boat.getZ() - boatPos.z;
		if (dx != 0 || dz != 0) {
			float vel = (float) hypot(dz, dx);
			float direction = (float) toDegrees(atan2(dz, dx)) - 90;
			float t = min(1, vel / 3); // max 70 m/s = 3.5 m/tick on blue ice, cut off at 3
			yaw = AngleUtil.lerp(t, yaw, direction + directionOffset);
		}
		yaw = AngleUtil.lerp(getConfig().getSmoothness(), previousYaw, yaw);

		player.setYaw(yaw);

		previousYaw = yaw;
		boatPos = boat.getPos();
	}

	// If returns true, look direction change should be cancelled
	public boolean onLookDirectionChanging(double dx, double dy) {
		ClientPlayerEntity player = MinecraftClient.getInstance().player;
		if (!(player.getVehicle() instanceof AbstractBoatEntity b)) {
			return false;
		}

		if (getConfig().boatMode && shouldOverrideCamera(b)) {
			if (dx != 0 || getConfig().fixedPitch && dy != 0) {
				player.changeLookDirection(0, getConfig().fixedPitch ? 0 : dy);
				return true;
			}
		}

		return false;
	}

	boolean shouldOverrideCamera(AbstractBoatEntity boat) {
		return !getConfig().stationaryLookAround || LOOK_LEFT.isPressed() || LOOK_RIGHT.isPressed() || boat.getVelocity().lengthSquared() >= 0.01 * 0.01;
	}

	public static BoatCamMod instance() {
		return INSTANCE;
	}
}
