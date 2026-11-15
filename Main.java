import javax.swing.SwingUtilities;
public class Main {
    public static void main(String[] args) {
        try {
            Class<?> laf = Class.forName("com.formdev.flatlaf.FlatLightLaf");
            laf.getMethod("setup").invoke(null);
        } catch (Exception ignored) {
            // The app remains usable with the platform Swing look and feel when FlatLaf is unavailable.
        }
        SwingUtilities.invokeLater(() -> new MediCareGUI().setVisible(true));
    }
}
