package boatcam.yaw.mode;

import boatcam.yaw.ModeId;
import com.google.gson.annotations.Expose;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import net.minecraft.network.chat.Component;

@ModeId("velocity")
public final class Velocity implements YawMode {

    @Expose
    public int strength = 50;

    @Override
    public OptionGroup createOptions() {
        return OptionGroup.createBuilder()
                .name(Component.literal("Velocity mode options"))
                .option(
                        Option.<Integer>createBuilder()
                                .name(Component.literal("Strength"))
                                .description(OptionDescription.of(Component.literal("0 - Camera feels super lazy.\n100 - Camera follows boat velocity tightly.")))
                                .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                        .range(0, 100)
                                        .formatValue(val -> Component.literal(String.valueOf(val)))
                                        .step(1)
                                )
                                .binding(50, () -> strength, val -> strength = val)
                                .build())
                .build();
    }
}
