
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

	private static final long serialVersionUID = -7229650795045639833L;
	private JTabbedPane tabs;
	private ArrayList<String> xmlFiles = new ArrayList<String>();
	private ArrayList<String> commandNames = new ArrayList<String>();
	private Computer evaluator;
	private String themeFile;
	private String errorMessage = "";
	private AlloyCodeAnalysisManager manager;
//	private JPanel selectStudentPanel;
//	private JPanel displayPanel;
	private MenuBar menuBar;
	private String dirPath;
//	private String[] students;

	public AlloyFrame(String alloyModelPath, String themeFilePath,
			String fileName) {
		super("Alloy Displayer for " + fileName);
		try {
			alloyModelPath = alloyModelPath.replace("\\", "/");
			this.manager = new AlloyCodeAnalysisManager(alloyModelPath);
			this.manager.process();
			this.xmlFiles = this.manager.getXMLFilePaths();
			this.commandNames = this.manager.getCommandNames();
			this.evaluator = this.manager.getEvaluator();
			this.errorMessage = this.manager.getErrorMessageString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.themeFile = themeFilePath;
		init();
		this.displayInstance();
	}

//	public AlloyFrame(String dirPath) {
//		this.dirPath = dirPath;
//		init();
//		loadStudentDirs();
//		this.loadSelectedStudentFiles(0);
//	}

//	private void loadStudentDirs() {
//		File file = new File(dirPath);
//		this.students = file.list(new FilenameFilter() {
//		  @Override
//		  public boolean accept(File current, String name) {
//		    return new File(current, name).isDirectory();
//		  }
//		});
//	}

	private void init() {
		this.setSize(1500, 900);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setLocationRelativeTo(null);
		this.tabs = new JTabbedPane();
		this.menuBar = new MenuBar(this.themeFile);
		this.setJMenuBar(this.menuBar);
	}

//	private void initForGraders() {
//		this.displayPanel = new JPanel();
//		this.selectStudentPanel = new StudentPanel(this, students, 1500, 30);
//
//		this.displayPanel.setPreferredSize(new Dimension(1000, 900));
//
//		this.add(this.selectStudentPanel, BorderLayout.NORTH);
//		this.add(this.displayPanel, BorderLayout.CENTER);
//	}

	private void displayInstance() {
		this.tabs = new JTabbedPane();
		try {
			int numOfInstances = this.xmlFiles.size();
			int numOfCommands = this.commandNames.size();
			if (numOfCommands == 0 && numOfInstances == 0) {
				JLabel label = new JLabel(this.errorMessage,
						SwingConstants.CENTER);
				this.tabs.addTab("Error", null, label, "Error");
			} else {
				for (int i = 0; i < numOfInstances; i++) {
					VizGUI vizGui = new VizGUI(true, this.xmlFiles.get(i),
							null, null, this.evaluator, false);
					if (this.themeFile.length() > 0)
						vizGui.loadThemeFile(this.themeFile);
					vizGui.doShowViz();

					JSplitPane displayPanel = vizGui.getPanel();
					if (displayPanel != null) {
						this.tabs.addTab(this.commandNames.get(i), null,
								displayPanel, this.commandNames.get(i));
					}
				}

				if (numOfCommands > numOfInstances) {
					for (int i = numOfInstances; i < numOfCommands; i++) {
						ImageIcon image = new ImageIcon("instanceNotFound.jpg");
						JLabel label = new JLabel("", image,
								SwingConstants.CENTER);
						this.tabs.addTab(this.commandNames.get(i), null, label,
								this.commandNames.get(i));
					}
				}
			}
			this.add(this.tabs);
		} catch (Exception e) {
			//
		}
	}
	
//	public void loadSelectedStudentFiles(int index){
//		String selectedStudent = this.students[index];
//		String alloyPath = this.dirPath + "\\" + selectedStudent;
//		
//		File alloyFile = new File(alloyPath);
//		
//		String alloyModelPath = "";
//		this.themeFile = "";
//		
//		File[] files = alloyFile.listFiles();
//		for(File file: files){
//			String filePath = file.getAbsolutePath();
//			if (filePath.contains(".als")){
//				alloyModelPath = filePath;
//			} else if (filePath.contains(".thm")){
//				this.themeFile = filePath;
//			}
//		}
//		
//		try {
//			alloyModelPath = alloyModelPath.replace("\\", "/");
//			this.manager = new AlloyCodeAnalysisManager(alloyModelPath);
//			this.manager.process();
//			this.xmlFiles = this.manager.getXMLFilePaths();
//			this.commandNames = this.manager.getCommandNames();
//			this.evaluator = this.manager.getEvaluator();
//			this.errorMessage = this.manager.getErrorMessageString();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		
//		this.initForGraders();
//		this.displayInstance();
//	}
	
	public String getDirPath(){
		return this.dirPath;
	}
}
