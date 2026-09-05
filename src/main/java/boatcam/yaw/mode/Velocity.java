package boatcam.yaw.mode;

import boatcam.yaw.TypeName;
import com.google.gson.annotations.Expose;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import net.minecraft.network.chat.Component;

@TypeName("velocity")
public final class Velocity implements YawMode {

    @Expose
    public int strength = 50;

    @Override
    public OptionGroup createOptions() {
        return OptionGroup.createBuilder()
                .option(
                        Option.<Integer>createBuilder()
                                .name(Component.literal("Strength"))
                                .description(OptionDescription.of(Component.literal("1 - Camera follows velocity tightly, feels close to non-boatcam camera.\n100 - Camera is super lazy.")))
                                .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                        .range(1, 100)
                                        .formatValue(val -> Component.literal(String.valueOf(val)))
                                        .step(1)
                                )
                                .binding(50, () -> strength, val -> strength = val)
                                .build())
                .build();
    }
}
