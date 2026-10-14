package boatcam;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;

public record BoatCamKeybinds(
        KeyMapping menu,
        KeyMapping toggle,
        KeyMapping lookBehind,
        KeyMapping lookLeft,
        KeyMapping lookRight
) {

    public BoatCamKeybinds(KeyMapping.Category category) {
        this(
                KeyMappingHelper.registerKeyMapping(new KeyMapping("key.boatcam.menu", InputConstants.KEY_B, category)),
                KeyMappingHelper.registerKeyMapping(new KeyMapping("key.boatcam.toggle", -1, category)),
                KeyMappingHelper.registerKeyMapping(new KeyMapping("key.boatcam.lookbehind", -1, category)),
                KeyMappingHelper.registerKeyMapping(new KeyMapping("key.boatcam.lookleft", -1, category)),
                KeyMappingHelper.registerKeyMapping(new KeyMapping("key.boatcam.lookright", -1, category))
        );
    }
}
