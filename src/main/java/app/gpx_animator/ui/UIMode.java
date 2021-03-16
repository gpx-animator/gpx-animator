package app.gpx_animator.ui;

import edu.umd.cs.findbugs.annotations.NonNull;

public enum UIMode {

    CLI,
    EXPERT;

    private static UIMode mode = EXPERT;

    public static void setMode(@NonNull final UIMode mode) {
        UIMode.mode = mode;
    }

    public static UIMode getMode() {
        return mode;
    }

}
