import com.ems.ui.OrganizerDashboard;
import com.ems.util.UITheme;

import javax.swing.*;

/**
 * Legacy entry point for Organizer Dashboard.
 */
public class EMSOrganizerGUI extends JFrame {

    public EMSOrganizerGUI() {
        UITheme.setupLookAndFeel();
        SwingUtilities.invokeLater(() -> new OrganizerDashboard(null).setVisible(true));
    }

    public static void main(String[] args) {
        UITheme.setupLookAndFeel();
        SwingUtilities.invokeLater(() -> new OrganizerDashboard(null).setVisible(true));
    }
}
