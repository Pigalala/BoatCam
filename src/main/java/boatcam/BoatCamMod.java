package boatcam;

import boatcam.cam.BoatCam;
import boatcam.cam.BoatCamera;
import boatcam.cam.NonBoatCamera;
import boatcam.config.BoatCamConfig;
import boatcam.config.BoatCamConfigScreen;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;

import static boatcam.config.BoatCamConfig.getConfig;
import static net.minecraft.ChatFormatting.GREEN;

public final class BoatCamMod implements ClientModInitializer {

	public static final Gson GSON = new GsonBuilder()
			.excludeFieldsWithoutExposeAnnotation()
			.create();

	private static BoatCamMod INSTANCE;

	private BoatCamKeybinds keybinds;
	private BoatCam activeCam;

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

        KeyMapping.Category keyBindingCategory = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("boatcam", "boatcam"));
		this.keybinds = new BoatCamKeybinds(keyBindingCategory);

		ClientTickEvents.START_CLIENT_TICK.register(this::tick);
	}

	public BoatCamKeybinds getKeybinds() {
		return keybinds;
	}

	void tick(Minecraft client) {
		if (client.player == null) {
			return;
		}

		if (keybinds.menu().consumeClick()) {
			client.setScreen(new BoatCamConfigScreen(client.screen).screen);
			return;
		}

		if (keybinds.toggle().consumeClick()) {
			getConfig().toggleBoatMode();
			client.gui.setOverlayMessage(Component.literal(getConfig().boatMode ? "Boat mode" : "Normal mode").withStyle(s -> s.withColor(GREEN)), false);
		}

		tickActiveCam(client);
	}

	void tickActiveCam(Minecraft client) {
		if (activeCam != null) {
			if (activeCam.shouldExit()) {
				activeCam.disable();
				activeCam = null;
				tickActiveCam(client); // Check for new mode instantly to prevent weird camera things
			} else {
				activeCam.tick();
			}
		} else {
			if (getConfig().boatMode && client.player.getVehicle() instanceof AbstractBoat boat) {
				// Should enter new activeCam
				activeCam = new BoatCamera(client.player, boat);
			} else {
				activeCam = new NonBoatCamera(client.player);
			}

            activeCam.enable();
            activeCam.tick();
        }
	}

	// If returns true, look direction change should be cancelled
	public boolean onLookDirectionChanging(double dx, double dy) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (activeCam != null && activeCam.isOverrideCamera()) {
			if (dx != 0 || getConfig().fixedPitch && dy != 0) {
				player.turn(0, getConfig().fixedPitch ? 0 : dy);
				return true;
			}
		}

		return false;
	}

	public boolean shouldOverrideCamera(AbstractBoat boat) {
		return !getConfig().stationaryLookAround ||
				keybinds.lookLeft().isDown() ||
				keybinds.lookRight().isDown() ||
				boat.getDeltaMovement().lengthSqr() >= 0.01 * 0.01;
	}

	public static BoatCamMod instance() {
		return INSTANCE;
	}
}
