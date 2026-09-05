package boatcam.config;

import boatcam.BoatCamMod;
import boatcam.yaw.YawModeMapAdapter;
import boatcam.yaw.mode.Legacy;
import boatcam.yaw.mode.Velocity;
import boatcam.yaw.mode.YawMode;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.JsonAdapter;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public final class BoatCamConfig {

    private static BoatCamConfig INSTANCE;

    private static final Path CONFIG_PATH = Path.of("config", "boatcam.json5");

    @Expose
    public boolean boatMode = true;
    @Expose
    public boolean stationaryLookAround = true;
    @Expose
    public Perspective perspective = Perspective.THIRD_PERSON;
    @Expose
    public boolean fixedPitch = false;
    @Expose
    public int pitch = 25;
    @Expose
    public boolean turnLimitDisabled = true;
    @Expose
    public boolean snapSidewaysView = false;
    @Expose
    public String selectedYawMode;
    @Expose
    @JsonAdapter(YawModeMapAdapter.class)
    public Map<String, YawMode> yawModes = new Object2ObjectArrayMap<>();

    // Now a legacy option, store for migration
    @Expose(serialize = false)
    public Integer smoothness;

    public YawMode cachedYawMode;

    private BoatCamConfig() {}

    public void validatePostLoad() {
        pitch = Math.clamp(pitch, -90, 90);

        if (perspective == null) {
            perspective = Perspective.THIRD_PERSON;
        }

        if (selectedYawMode == null) {
            if (smoothness == null) {
                var mode = new Velocity();
                yawModes.put(mode.typeName(), mode);
                selectedYawMode = "velocity";
            } else {
                // Poopy migration
                var mode = new Legacy();
                mode.smoothness = Math.clamp(smoothness, 0, 100);
                yawModes.put(mode.typeName(), mode);
                selectedYawMode = "legacy";
            }
        }

        cachedYawMode = getYawMode();
    }

    public String getSelectedYawModeName() {
        return selectedYawMode;
    }

    public void setSelectedYawModeName(String name) {
        System.out.println("Set to " + name);
        this.selectedYawMode = name;
        cachedYawMode = getYawMode();
    }

    private YawMode getYawMode() {
        YawMode mode = BoatCamConfig.getConfig().yawModes.get(BoatCamConfig.getConfig().selectedYawMode);
        if (mode == null) {
            mode = new Velocity();
            BoatCamConfig.getConfig().yawModes.put(mode.typeName(), mode);
        }
        return mode;
    }

    public void toggleBoatMode() {
        boatMode = !boatMode;
        save();
    }

    public Perspective getPerspective() {
        return perspective;
    }

    public boolean isTurnLimitDisabled() {
        return turnLimitDisabled;
    }

    public enum Perspective {
        NONE, FIRST_PERSON, THIRD_PERSON;
    }

    public void save() {
        try {
            if (Files.notExists(CONFIG_PATH)) {
                Files.createFile(CONFIG_PATH);
            }

            Files.writeString(CONFIG_PATH, BoatCamMod.GSON.toJson(this));
        } catch (Exception e) {
            System.err.println("Could not write config: " + e.getMessage());
        }
    }

    public static void load() {
        if (Files.notExists(CONFIG_PATH)) {
            INSTANCE = new BoatCamConfig();
            return;
        }

        BoatCamConfig config;
        try {
            config = BoatCamMod.GSON.fromJson(Files.readString(CONFIG_PATH), BoatCamConfig.class);
        } catch (JsonSyntaxException | IOException e) {
            throw new RuntimeException("Could not read config", e);
        } catch (Exception e) {
            e.printStackTrace();
            config = new BoatCamConfig();
        }

        INSTANCE = config;
        INSTANCE.validatePostLoad();
    }

    public static BoatCamConfig getConfig() {
        return INSTANCE;
    }
}