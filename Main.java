import javax.swing.SwingUtilities;
public class Main {
    public static void main(String[] args) {
        try {
            Class<?> laf = Class.forName("com.formdev.flatlaf.FlatLightLaf");
            laf.getMethod("setup").invoke(null);
        } catch (Exception ignored) {
        }
        SwingUtilities.invokeLater(() -> new MediCareGUI().setVisible(true));
    }
}
