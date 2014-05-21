import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import edu.mit.csail.sdg.alloy4.Computer;
import edu.mit.csail.sdg.alloy4viz.VizGUI;

@SuppressWarnings("javadoc")
public class AlloyFrame extends JFrame {

	private JTabbedPane tabs;
	private ArrayList<String> xmlFiles = new ArrayList<String>();
	private ArrayList<String> commandNames = new ArrayList<String>();
	private Computer evaluator;
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
			this.commandNames = manager.getCommandNames();
			this.evaluator = manager.getEvaluator();
			
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
				VizGUI vizGui = new VizGUI(true, this.xmlFiles.get(i), null, null, this.evaluator, false);
				if (this.themeFile.length() > 0)
					vizGui.loadThemeFile(this.themeFile);
				vizGui.doShowViz();
				
				JSplitPane displayPanel = vizGui.getPanel();
				if (displayPanel != null) {
					this.tabs.addTab(this.commandNames.get(i), null, displayPanel,
                this.commandNames.get(i));
				}
				
			}
			this.add(this.tabs);
		}
		catch (Exception e) {
			//
		}
	}
}
