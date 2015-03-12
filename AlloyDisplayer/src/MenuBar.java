import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

@SuppressWarnings("javadoc")
public class MenuBar extends JMenuBar {

	private static final long serialVersionUID = 5929734920276998115L;
	private JMenu fileMenu;
	private JMenu themeMenu;
	private JMenu runMenu;
	private JMenu openRecent;

	private JMenuItem openFile;
	private JMenuItem exit;
	private JMenuItem loadTheme;
	private JMenuItem saveTheme;
	private JMenuItem runAll;
	private JMenuItem runCommand;
	private String themeFile;

	public MenuBar(String themeFile) {
		this.themeFile = themeFile;
		initMenus();
		initMenuItems();
		addFileMenuItems();
		addThemeMenuItems();
		addRunMenuItems();
		addMenus();
	}

	private void addMenus() {
		this.add(this.fileMenu);
		this.add(this.themeMenu);
		this.add(this.runMenu);
	}

	private void initMenus() {
		this.fileMenu = new JMenu("File");
		this.themeMenu = new JMenu("Theme");
		this.runMenu = new JMenu("Run");
		this.openRecent = new JMenu("Open Recent...");
	}

	private void initMenuItems() {
		this.openFile = new JMenuItem("Open Alloy Model");
		this.exit = new JMenuItem("Exit");
		this.loadTheme = new JMenuItem("Load Theme");
		this.saveTheme = new JMenuItem("Save Theme");
		this.runAll = new JMenuItem("Run All Commands");
		this.runCommand = new JMenuItem("Run Current Command");
	}

	private void addFileMenuItems() {
		this.fileMenu.add(this.openFile);
		this.fileMenu.add(this.openRecent);
		this.fileMenu.add(this.exit);
	}

	private void addThemeMenuItems() {
		this.themeMenu.add(this.loadTheme);
		this.themeMenu.add(this.saveTheme);
	}

	private void addRunMenuItems() {
		this.runMenu.add(this.runAll);
		this.runMenu.add(this.runCommand);
	}

	public void addRecentFile(ArrayList<String> listOfFiles) {
		for (int i = 0; i < listOfFiles.size(); i++) {
			final JMenuItem file = new JMenuItem(listOfFiles.get(i));
			final String theme = this.themeFile;
			file.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent ae) {
					String fileName = file.getText();
					AlloyFrame frame =
						new AlloyFrame(
							fileName,
							theme,
							fileName);
					frame.setVisible(true);
				}
			});
			this.openRecent.add(file);
		}
	}
}
