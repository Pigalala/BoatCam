package boatcam.cam;

public interface BoatCam {

    void enable();

    void disable();

    void tick();

    boolean isOverrideCamera();

    boolean shouldExit();
}
