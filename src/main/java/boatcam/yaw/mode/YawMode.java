package boatcam.yaw.mode;

import boatcam.yaw.ModeId;
import dev.isxander.yacl3.api.OptionGroup;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;

import java.util.Map;

public sealed interface YawMode permits Directional, Velocity {

    Map<String, Class<? extends YawMode>> MODES = createModesMap();

    OptionGroup createOptions();

    default String typeName() {
        return typeName(getClass());
    }

    static String typeName(Class<? extends YawMode> yawModeClass) {
        return yawModeClass.getAnnotation(ModeId.class).value();
    }

    static YawMode newYawModeFromType(Class<? extends YawMode> yawModeClass) {
        try {
            return yawModeClass.getConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    static Map<String, Class<? extends YawMode>> createModesMap() {
        Class<?>[] permitted = YawMode.class.getPermittedSubclasses();
        var map = new Object2ObjectArrayMap<String, Class<? extends YawMode>>(permitted.length);

        for (Class<?> subclass : permitted) {
            var subclassFr = (Class<? extends YawMode>) subclass;
            map.put(typeName(subclassFr), subclassFr);
        }

        return Object2ObjectMaps.unmodifiable(map);
    }
}
