package boatcam.config;

import com.google.gson.Gson;
import net.minecraft.client.MinecraftClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class BoatCamConfig {

    private static BoatCamConfig INSTANCE;

    private static final Path CONFIG_PATH = Path.of("config", "boatcam.json5");

    public boolean boatMode = true;
    public boolean stationaryLookAround = true;
    public int smoothness = 50;
    public Perspective perspective = Perspective.THIRD_PERSON;
    public boolean fixedPitch = false;
    public int pitch = 25;
    public boolean turnLimitDisabled = true;

    private BoatCamConfig() {}

    public void validatePostLoad() {
        smoothness = Math.clamp(smoothness, 1, 100);
        pitch = Math.clamp(pitch, -90, 90);

        if (perspective == null) {
            perspective = Perspective.THIRD_PERSON;
        }
    }

    public float getSmoothness() {
        return smoothness / 100f;
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

            var gson = new Gson();
            Files.writeString(CONFIG_PATH, gson.toJson(this));
        } catch (Exception e) {
            System.err.println("Could not write config: " + e.getMessage());
        }
    }

    public static void load() throws IOException {
        if (Files.notExists(CONFIG_PATH)) {
            INSTANCE = new BoatCamConfig();
            return;
        }

        var gson = new Gson();
        INSTANCE = gson.fromJson(Files.readString(CONFIG_PATH), BoatCamConfig.class);
        INSTANCE.validatePostLoad();
    }

    public static BoatCamConfig getConfig() {
        return INSTANCE;
    }
}