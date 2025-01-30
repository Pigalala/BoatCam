package boatcam;

import boatcam.config.BoatCamConfig;
import boatcam.event.LookDirectionChangingEvent;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.DropdownBoxEntry;
import net.fabricmc.api.ModInitializer;
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

import java.lang.reflect.Field;
import java.util.List;

import static boatcam.config.BoatCamConfig.getConfig;
import static java.lang.Math.*;
import static net.minecraft.client.util.InputUtil.Type.KEYSYM;
import static net.minecraft.util.Formatting.GREEN;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_B;

public class BoatCamMod implements ModInitializer, LookDirectionChangingEvent {
	// key binds
	private final KeyBinding TOGGLE = new KeyBinding("key.boatcam.toggle", KEYSYM, GLFW_KEY_B, "BoatCam");
	private final KeyBinding LOOK_BEHIND = new KeyBinding("key.boatcam.lookbehind", KEYSYM, -1, "BoatCam");
	private final KeyBinding LOOK_LEFT = new KeyBinding("key.boatcam.lookleft", KEYSYM, -1, "BoatCam");
	private final KeyBinding LOOK_RIGHT = new KeyBinding("key.boatcam.lookright", KEYSYM, -1, "BoatCam");

	// things to remember temporarily
	private Perspective perspective = null;
	private Perspective previousPerspective = null;
	private Vec3d boatPos = null;
	private float previousYaw;

	// states
	private boolean lookingBehind = false;

	@Override
	public void onInitialize() {
		AutoConfig.register(BoatCamConfig.class, JanksonConfigSerializer::new);
		KeyBindingHelper.registerKeyBinding(TOGGLE);
		KeyBindingHelper.registerKeyBinding(LOOK_BEHIND);
		KeyBindingHelper.registerKeyBinding(LOOK_LEFT);
		KeyBindingHelper.registerKeyBinding(LOOK_RIGHT);
		ClientTickEvents.START_WORLD_TICK.register(this::onClientEndWorldTick);
		LookDirectionChangingEvent.EVENT.register(this);
		AutoConfig.getGuiRegistry(BoatCamConfig.class).registerPredicateTransformer(
			(guis, s, f, c, d, g) -> dropdownToEnumList(guis, f),
			field -> BoatCamConfig.Perspective.class.isAssignableFrom(field.getType())
		);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List<AbstractConfigListEntry> dropdownToEnumList(List<AbstractConfigListEntry> guis, Field field) {
		return guis.stream()
			.filter(DropdownBoxEntry.class::isInstance)
			.map(DropdownBoxEntry.class::cast)
			// transform dropdown menu into enum list
			.map(dropdown -> ConfigEntryBuilder.create()
				.startEnumSelector(dropdown.getFieldName(), BoatCamConfig.Perspective.class, (BoatCamConfig.Perspective) dropdown.getValue())
				.setDefaultValue((BoatCamConfig.Perspective) dropdown.getDefaultValue().orElse(null))
				.setSaveConsumer(p -> {
					try {
						field.set(getConfig(), p);
					} catch (IllegalAccessException ignored) { }
				})
				.setEnumNameProvider(perspective -> switch ((BoatCamConfig.Perspective) perspective) {
					case FIRST_PERSON -> Text.translatable("text.autoconfig.boatcam.option.perspective.firstPerson");
					case THIRD_PERSON -> Text.translatable("text.autoconfig.boatcam.option.perspective.thirdPerson");
					case NONE -> Text.translatable("text.autoconfig.boatcam.option.perspective.none");
				})
				.build())
			.map(AbstractConfigListEntry.class::cast)
			.toList();
	}

	private void onClientEndWorldTick(ClientWorld world) {
		MinecraftClient client = MinecraftClient.getInstance();
		// key bind logic
		if (TOGGLE.wasPressed()) {
			getConfig().toggleBoatMode();
			client.inGameHud.setOverlayMessage(Text.literal(getConfig().isBoatMode() ? "Boat mode" : "Normal mode").styled(s -> s.withColor(GREEN)), false);
		}
		// camera logic
		assert client.player != null;
		if (getConfig().isBoatMode() && client.player.getVehicle() instanceof AbstractBoatEntity boat) {
			calculateYaw(client.player, boat);
			// first tick riding in boat mode
			if (perspective == null) {
				// fix pitch if configured
				if (getConfig().shouldFixPitch()) {
					client.player.setPitch(getConfig().getPitch());
				}
				// init look behind
				lookingBehind = false;
				// save perspective
				perspective = client.options.getPerspective();
				// set perspective
				switch (getConfig().getPerspective()) {
					case FIRST_PERSON -> client.options.setPerspective(Perspective.FIRST_PERSON);
					case THIRD_PERSON -> client.options.setPerspective(Perspective.THIRD_PERSON_BACK);
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
			// save state
			lookingBehind = LOOK_BEHIND.isPressed();
			// handle change
			invertPitch();
			if (lookingBehind) {
				// set look back perspective
				previousPerspective = client.options.getPerspective();
				client.options.setPerspective(Perspective.THIRD_PERSON_FRONT);
			} else {
				// reset perspective
				client.options.setPerspective(previousPerspective);
				perspective = null;
			}
		}
	}

	private void invertPitch() {
		ClientPlayerEntity player = MinecraftClient.getInstance().player;
		assert player != null;
		player.setPitch(-player.getPitch());
	}

	private void resetPerspective() {
		if (perspective != null) {
			MinecraftClient.getInstance().options.setPerspective(perspective);
			perspective = null;
		}
	}

	private void calculateYaw(ClientPlayerEntity player, AbstractBoatEntity boat) {
		// yaw calculations
		float yaw = boat.getYaw();
		if (boatPos != null) {
			if (LOOK_LEFT.isPressed()) {
				yaw -= 90f;
				player.setYaw(yaw);
			} else if (LOOK_RIGHT.isPressed()) {
				yaw += 90;
				player.setYaw(yaw);
			} else {
				double dz = boat.getZ() - boatPos.z, dx = boat.getX() - boatPos.x;
				if (dx != 0 || dz != 0) {
					float vel = (float) hypot(dz, dx);
					float direction = (float) toDegrees(atan2(dz, dx)) - 90;
					float t = min(1, vel / 3); // max 70 m/s = 3.5 m/tick on blue ice, cut off at 3
					yaw = AngleUtil.lerp(t, yaw, direction);
				}
			}
			yaw = AngleUtil.lerp(getConfig().getSmoothness(), previousYaw, yaw);
		}

		// Only update player yaw only if they are moving, otherwise boatcam would be too silly :P
		if (boat.getVelocity().length() >= 0.02) {
			player.setYaw(yaw);
		}
		previousYaw = yaw;
		boatPos = boat.getPos();
	}

	@Override
	public boolean onLookDirectionChanging(double dx, double dy) {
		ClientPlayerEntity player = MinecraftClient.getInstance().player;
		assert player != null;
		if(!(player.getVehicle() instanceof AbstractBoatEntity b)) return false;
		if (getConfig().isBoatMode() && b.getVelocity().length() >= 0.02) {
			if (dx != 0 || getConfig().shouldFixPitch() && dy != 0) {
				// prevent horizontal camera movement and cancel camera change by returning true
				// prevent vertical movement as well if configured
				player.changeLookDirection(0, getConfig().shouldFixPitch() ? 0 : dy);
				return true;
			}
		}
		return false;
	}
}