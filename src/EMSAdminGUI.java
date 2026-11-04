import com.ems.ui.AdminDashboard;
import com.ems.util.UITheme;

import javax.swing.*;

/**
 * Legacy entry point for Admin Dashboard.
 */
public class EMSAdminGUI extends JFrame {

    public EMSAdminGUI() {
        UITheme.setupLookAndFeel();
        SwingUtilities.invokeLater(() -> new AdminDashboard(null).setVisible(true));
    }

    public static void main(String[] args) {
        UITheme.setupLookAndFeel();
        SwingUtilities.invokeLater(() -> new AdminDashboard(null).setVisible(true));
    }
}
