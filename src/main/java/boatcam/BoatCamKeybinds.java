package boatcam;

import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;

import static com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_B;

public record BoatCamKeybinds(
        KeyMapping menu,
        KeyMapping toggle,
        KeyMapping lookBehind,
        KeyMapping lookLeft,
        KeyMapping lookRight
) {

    public BoatCamKeybinds(KeyMapping.Category category) {
        this(
                KeyMappingHelper.registerKeyMapping(new KeyMapping("key.boatcam.menu", KEYSYM, GLFW_KEY_B, category)),
                KeyMappingHelper.registerKeyMapping(new KeyMapping("key.boatcam.toggle", KEYSYM, -1, category)),
                KeyMappingHelper.registerKeyMapping(new KeyMapping("key.boatcam.lookbehind", KEYSYM, -1, category)),
                KeyMappingHelper.registerKeyMapping(new KeyMapping("key.boatcam.lookleft", KEYSYM, -1, category)),
                KeyMappingHelper.registerKeyMapping(new KeyMapping("key.boatcam.lookright", KEYSYM, -1, category))
        );
    }
}
