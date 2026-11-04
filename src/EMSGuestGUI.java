import com.ems.ui.GuestDashboard;
import com.ems.util.UITheme;

import javax.swing.*;

/**
 * Legacy entry point for Guest / Attendee Portal.
 */
public class EMSGuestGUI extends JFrame {

    public EMSGuestGUI() {
        UITheme.setupLookAndFeel();
        SwingUtilities.invokeLater(() -> new GuestDashboard(null).setVisible(true));
    }

    public static void main(String[] args) {
        UITheme.setupLookAndFeel();
        SwingUtilities.invokeLater(() -> new GuestDashboard(null).setVisible(true));
    }
}
