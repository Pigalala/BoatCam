package boatcam.yaw.mode;

import boatcam.yaw.TypeName;
import com.google.gson.annotations.Expose;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import net.minecraft.network.chat.Component;

@TypeName("legacy")
public final class Legacy implements boatcam.yaw.mode.YawMode {

    @Expose
    public int smoothness = 50;

    @Override
    public OptionGroup createOptions() {
        return OptionGroup.createBuilder()
                .option(
                        Option.<Integer>createBuilder()
                                .name(Component.literal("Smoothness"))
                                .description(OptionDescription.of(Component.literal("1 - Smooth camera, might even lag behind.\n100 - Camera angle might change very abruptly.")))
                                .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                        .range(1, 100)
                                        .formatValue(val -> Component.literal(String.valueOf(val)))
                                        .step(1)
                                )
                                .binding(50, () -> smoothness, val -> smoothness = val)
                                .build()
                )
                .build();
    }
}
