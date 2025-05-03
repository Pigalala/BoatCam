package boatcam;

public final class AngleUtil {

    // lerps two angles the shortest way (e.g. lerp(0.5, -135, 135) -> -180)
    // resulting angle should be in range [-180, 180)
    public static float lerp(float t, float a, float b) {
        a = mod(a);
        b = mod(b);

        float delta = b - a;
        if (delta > 180) {
            delta -= 360;
        } else if (delta < -180) {
            delta += 360;
        }

        return mod(a + t * delta);
    }

    // makes sure the angle is in range [-180, 180)
    public static float mod(float angle) {
        return angle - (float) Math.floor(angle / 360 + 0.5) * 360;
    }
}
