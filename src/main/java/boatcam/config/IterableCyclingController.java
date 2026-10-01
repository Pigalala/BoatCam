package boatcam.config;

import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.gui.controllers.cycling.CyclingListController;
import net.minecraft.network.chat.Component;

import java.util.function.Function;

public class IterableCyclingController<T> extends CyclingListController<T> {

    public IterableCyclingController(Option<T> option, Iterable<T> values, Function<T, Component> valueFormatter) {
        super(option, values, valueFormatter);
    }
}
