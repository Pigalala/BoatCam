package boatcam.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry.BoundedDiscrete;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.DropdownBoxEntry;
import net.minecraft.text.Text;

import java.lang.reflect.Field;
import java.util.List;

@SuppressWarnings({ "unused", "FieldCanBeLocal", "FieldMayBeFinal" })
@Config(name = "boatcam")
public final class BoatCamConfig implements ConfigData {

    @Comment("Whether the camera should be controlled by this mod or not.\nNOTE: This setting can be toggled using a key bind.")
    private boolean boatMode = true;

    @Comment("Frees camera movement when not moving in a boat.")
    private boolean stationaryLookAround = true;

    @Comment("1 - Smooth camera, might even lag behind.\n100 - Camera angle might change very abruptly.")
    @BoundedDiscrete(min = 1, max = 100)
    private int smoothness = 50;

    @Comment("Perspective when riding a boat in boat mode. Perspective wont change when this is set to none.")
    private Perspective perspective = Perspective.NONE;

    @Comment("Whether to fix the camera angle at a certain pitch.")
    private boolean fixedPitch = false;

    @Comment("Fixed vertical angle of the camera when fixedPitch is enabled.")
    @BoundedDiscrete(min = -90, max = 90)
    private int pitch = 25;

    @Comment("Disables the turn limit in a boat.\nNOTE: The turn limit is always disabled in boat mode!")
    private boolean turnLimitDisabled = false;

    private BoatCamConfig() {}

    @Override
    public void validatePostLoad() {
        if(smoothness < 1 || smoothness > 100) smoothness = 50;
        if(perspective == null) perspective = Perspective.NONE;
    }

    public static void saveConfig() {
        AutoConfig.getConfigHolder(BoatCamConfig.class).save();
    }

    public float getSmoothness() {
        return smoothness / 100f;
    }

    public boolean isBoatMode() {
        return boatMode;
    }

    public boolean isStationaryLookAround() {
        return stationaryLookAround;
    }

    public boolean shouldFixPitch() {
        return fixedPitch;
    }

    public int getPitch() {
        return pitch;
    }

    public void toggleBoatMode() {
        boatMode = !boatMode;
        saveConfig();
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

    public static BoatCamConfig getConfig() {
        return AutoConfig.getConfigHolder(BoatCamConfig.class).get();
    }

    public static void registerPerspectiveConfiguration() {
        AutoConfig.getGuiRegistry(BoatCamConfig.class).registerPredicateTransformer(
                (guis, s, f, c, d, g) -> dropdownToEnumList(guis, f),
                field -> BoatCamConfig.Perspective.class.isAssignableFrom(field.getType())
        );
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static List<AbstractConfigListEntry> dropdownToEnumList(List<AbstractConfigListEntry> guis, Field field) {
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
                            } catch (IllegalAccessException ignored) {}
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
}