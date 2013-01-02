/**
 *
 * @author agribu
 */

package bs.edu.gdb;

import javax.swing.JFrame;
import edu.fhge.gdb.GUIFactory;

public class Main {

    public static void main(String[] args) {
        JFrame fenster = GUIFactory.createMainFrame("Test", new DataAccessObjectImpl());
        fenster.setVisible(true);
    }
}
