package boatcam.yaw.mode;

import boatcam.yaw.TypeName;
import dev.isxander.yacl3.api.OptionGroup;

import java.util.Arrays;
import java.util.List;

public sealed interface YawMode permits Legacy, Velocity {

    OptionGroup createOptions();

    default String typeName() {
        return getClass().getAnnotation(TypeName.class).value();
    }

    static List<String> getTypeNames() {
        return Arrays.stream(YawMode.class.getPermittedSubclasses())
                .map(c -> c.getAnnotation(TypeName.class).value())
                .toList();
    }

    static YawMode newYawModeFromType(String typeName) {
        return Arrays.stream(YawMode.class.getPermittedSubclasses())
                .filter(c -> c.getAnnotation(TypeName.class).value().equals(typeName))
                .map(c -> {
                    try {
                        return (YawMode) c.getConstructor().newInstance();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .findAny()
                .orElse(null);
    }

    @SuppressWarnings("unchecked")
    static Class<? extends YawMode> getClassFromTypeName(String name) {
        return (Class<? extends YawMode>) Arrays.stream(YawMode.class.getPermittedSubclasses())
                .filter(c -> c.getAnnotation(TypeName.class).value().equals(name))
                .findAny()
                .orElse(null);
    }
}
