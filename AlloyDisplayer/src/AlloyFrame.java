import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import edu.mit.csail.sdg.alloy4viz.VizGUI;

@SuppressWarnings("javadoc")
public class AlloyFrame extends JFrame {

	private JTabbedPane tabs;
	private ArrayList<String> xmlFiles = new ArrayList<String>();
	private String themeFile;

	public AlloyFrame(String alloyModelPath, String themeFilePath, String fileName) {
		super("Alloy Displayer for "+fileName);
		this.setSize(1500, 900);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.tabs = new JTabbedPane();

		try {
			alloyModelPath = alloyModelPath.replace("\\", "/");
			AlloyCodeAnalysisManager manager =
				new AlloyCodeAnalysisManager(alloyModelPath);
			manager.process();
			this.xmlFiles = manager.getXMLFilePaths();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		this.themeFile = themeFilePath;
		this.displayInstance();
	}

	private void displayInstance() {
		try {
			for (int i = 0; i < this.xmlFiles.size(); i++) {
				VizGUI vizGui = new VizGUI(true, "", null, null, null, false);
				vizGui.loadXML(this.xmlFiles.get(i), true);
				if (this.themeFile.length() > 0)
					vizGui.loadThemeFile(this.themeFile);
				vizGui.doShowViz();
				
				JSplitPane displayPanel = vizGui.getPanel();
				if (displayPanel != null) {
					this.tabs.addTab("Run "+i, null, displayPanel,
                "Run "+i);
				}
				
			}
			this.add(this.tabs);
		}
		catch (Exception e) {
			//
		}
	}
}
