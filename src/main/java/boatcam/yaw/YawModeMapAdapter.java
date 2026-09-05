package boatcam.yaw;

import boatcam.yaw.mode.YawMode;
import com.google.gson.*;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;

import java.lang.reflect.Type;
import java.util.Map;

public class YawModeMapAdapter implements JsonSerializer<Map<String, YawMode>>, JsonDeserializer<Map<String, YawMode>> {
    @Override
    public Map<String, YawMode> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        Map<String, YawMode> result = new Object2ObjectArrayMap<>();

        for (var entry : jsonElement.getAsJsonObject().entrySet()) {
            String key = entry.getKey();
            Class<? extends YawMode> yawModeType = YawMode.getClassFromTypeName(key);
            if (yawModeType == null) {
                continue;
            }

            YawMode mode = context.deserialize(entry.getValue(), yawModeType);
            result.put(key, mode);
        }
        return result;
    }

    @Override
    public JsonElement serialize(Map<String, YawMode> map, Type type, JsonSerializationContext context) {
        JsonObject obj = new JsonObject();
        for (var entry : map.entrySet()) {
            obj.add(entry.getKey(), context.serialize(entry.getValue(), entry.getValue().getClass()));
        }

        return obj;
    }
}
