package boatcam.config;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.gui.YACLScreen;
import dev.isxander.yacl3.impl.controller.TickBoxControllerBuilderImpl;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class BoatCamConfigScreen extends YACLScreen {

    public BoatCamConfigScreen(Screen parent) {
        super(createConfig(), parent);
    }

    static YetAnotherConfigLib createConfig() {
        return YetAnotherConfigLib.createBuilder()
                .title(Text.literal("BoatCam"))
                .category(ConfigCategory.createBuilder()
                        .name(Text.literal("BoatCam"))
                        .option(boatModeOption())
                        .option(smoothnessOption())
                        .option(fixedPitchOption())
                        .option(pitchOption())
                        .option(stationaryLookAroundOption())
                        .option(perspectiveOption())
                        .option(turnLimitDisabled())
                        .build()
                )
                .save(() -> {
                    try {
                        BoatCamConfig.getConfig().save();
                    } catch (Exception e) {
                        throw new RuntimeException(e); // :P
                    }
                })
                .build();
    }

    static Option<Boolean> boatModeOption() {
        return Option.<Boolean>createBuilder()
                .name(Text.literal("Boat Mode"))
                .description(OptionDescription.of(Text.literal("Whether the camera should be controlled by this mod or not.\nThis setting can be toggled using a key bind.")))
                .controller(TickBoxControllerBuilderImpl::new)
                .binding(true, () -> BoatCamConfig.getConfig().boatMode, val -> BoatCamConfig.getConfig().boatMode = val)
                .build();
    }

    static Option<Integer> smoothnessOption() {
        return Option.<Integer>createBuilder()
                .name(Text.literal("Smoothness"))
                .description(OptionDescription.of(Text.literal("1 - Smooth camera, might even lag behind.\n100 - Camera angle might change very abruptly.")))
                .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                        .range(1, 100)
                        .formatValue(val -> Text.literal(String.valueOf(val)))
                        .step(1)
                )
                .binding(50, () -> BoatCamConfig.getConfig().smoothness, val -> BoatCamConfig.getConfig().smoothness = val)
                .build();
    }

    static Option<Boolean> fixedPitchOption() {
        return Option.<Boolean>createBuilder()
                .name(Text.literal("Fixed Pitch"))
                .description(OptionDescription.of(Text.literal("Whether to fix the camera angle at a certain pitch.")))
                .controller(TickBoxControllerBuilderImpl::new)
                .binding(true, () -> BoatCamConfig.getConfig().fixedPitch, val -> BoatCamConfig.getConfig().fixedPitch = val)
                .build();
    }

    static Option<Integer> pitchOption() {
        return Option.<Integer>createBuilder()
                .name(Text.literal("Pitch"))
                .description(OptionDescription.of(Text.literal("Fixed vertical angle of the camera when fixedPitch is enabled.")))
                .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                        .range(-90, 90)
                        .formatValue(val -> Text.literal(String.valueOf(val)))
                        .step(1)
                )
                .binding(50, () -> BoatCamConfig.getConfig().pitch, val -> BoatCamConfig.getConfig().pitch = val)
                .build();
    }

    static Option<Boolean> stationaryLookAroundOption() {
        return Option.<Boolean>createBuilder()
                .name(Text.literal("Stationary Look Around"))
                .description(OptionDescription.of(Text.literal("Frees camera movement when not moving in a boat.")))
                .controller(TickBoxControllerBuilderImpl::new)
                .binding(true, () -> BoatCamConfig.getConfig().stationaryLookAround, val -> BoatCamConfig.getConfig().stationaryLookAround = val)
                .build();
    }

    static Option<BoatCamConfig.Perspective> perspectiveOption() {
        return Option.<BoatCamConfig.Perspective>createBuilder()
                .name(Text.literal("Perspective"))
                .description(OptionDescription.of(Text.literal("Perspective when riding in a boat in boat mode. Perspective wont change when this is set to none.")))
                .controller(opt -> EnumControllerBuilder.create(opt)
                        .formatValue(val -> Text.literal(val.name()))
                        .enumClass(BoatCamConfig.Perspective.class)
                )
                .binding(BoatCamConfig.Perspective.THIRD_PERSON, () -> BoatCamConfig.getConfig().perspective, val -> BoatCamConfig.getConfig().perspective = val)
                .build();
    }

    static Option<Boolean> turnLimitDisabled() {
        return Option.<Boolean>createBuilder()
                .name(Text.literal("Turn Limit"))
                .description(OptionDescription.of(Text.literal("Whether the vanilla turn limit in a boat should be applied or not.")))
                .controller(TickBoxControllerBuilderImpl::new)
                .binding(true, () -> !BoatCamConfig.getConfig().turnLimitDisabled, val -> BoatCamConfig.getConfig().turnLimitDisabled = !val)
                .build();
    }
}
