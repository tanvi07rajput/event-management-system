import com.ems.Main;
import com.ems.ui.LoginFrame;
import com.ems.util.UITheme;

import javax.swing.*;

/**
 * Legacy entry point. Forwards directly to modern EMS v2.0 application.
 */
public class EMSLogin extends JFrame {

    public EMSLogin() {
        UITheme.setupLookAndFeel();
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }

    public static String getLoggedInEmail() {
        return "admin@ems.com";
    }

    public static class GradientHeaderPanel extends JPanel {
        // Maintained for backward compatibility
    }

    public static void main(String[] args) {
        Main.main(args);
    }
}
