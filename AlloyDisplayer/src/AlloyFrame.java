import java.util.ArrayList;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import edu.mit.csail.sdg.alloy4.Computer;
import edu.mit.csail.sdg.alloy4viz.VizGUI;

@SuppressWarnings("javadoc")
public class AlloyFrame extends JFrame {

	private JTabbedPane tabs;
	private ArrayList<String> xmlFiles = new ArrayList<String>();
	private ArrayList<String> commandNames = new ArrayList<String>();
	private Computer evaluator;
	private String themeFile;
	private String errorMessage = "";
	private AlloyCodeAnalysisManager manager;
	private MenuBar menuBar;

	public AlloyFrame(
		String alloyModelPath,
		String themeFilePath,
		String fileName) {
		super("Alloy Displayer for " + fileName);
		this.setSize(1500, 900);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.tabs = new JTabbedPane();

		try {
			alloyModelPath = alloyModelPath.replace("\\", "/");
			this.manager = new AlloyCodeAnalysisManager(alloyModelPath);
			this.manager.process();
			this.xmlFiles = this.manager.getXMLFilePaths();
			this.commandNames = this.manager.getCommandNames();
			this.evaluator = this.manager.getEvaluator();
			this.errorMessage = this.manager.getErrorMessageString();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		this.menuBar = new MenuBar(this.themeFile);
		this.themeFile = themeFilePath;
		this.setJMenuBar(this.menuBar);
		this.displayInstance();
	}

	private void displayInstance() {
		try {
			int numOfInstances = this.xmlFiles.size();
			int numOfCommands = this.commandNames.size();
			if (numOfCommands == 0 && numOfInstances == 0) {
				JLabel label =
					new JLabel(this.errorMessage, SwingConstants.CENTER);
				this.tabs.addTab("Error", null, label, "Error");
			}
			else {
				for (int i = 0; i < numOfInstances; i++) {
					VizGUI vizGui =
						new VizGUI(
							true,
							this.xmlFiles.get(i),
							null,
							null,
							this.evaluator,
							false);
					if (this.themeFile.length() > 0)
						vizGui.loadThemeFile(this.themeFile);
					vizGui.doShowViz();

					JSplitPane displayPanel = vizGui.getPanel();
					if (displayPanel != null) {
						this.tabs.addTab(
							this.commandNames.get(i),
							null,
							displayPanel,
							this.commandNames.get(i));
					}
				}

				if (numOfCommands > numOfInstances) {
					for (int i = numOfInstances; i < numOfCommands; i++) {
						ImageIcon image = new ImageIcon("instanceNotFound.jpg");
						JLabel label =
							new JLabel("", image, SwingConstants.CENTER);
						this.tabs.addTab(
							this.commandNames.get(i),
							null,
							label,
							this.commandNames.get(i));
					}
				}
			}
			this.add(this.tabs);
		}
		catch (Exception e) {
			//
		}
	}
}
