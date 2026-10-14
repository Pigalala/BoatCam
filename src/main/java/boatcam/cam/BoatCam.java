package boatcam.cam;

public interface BoatCam {

    void enable();

    void disable();

    void startTick();

    default void endTick() {}

    boolean isOverrideCamera();

    boolean shouldExit();
}
