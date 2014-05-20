import java.io.File;
import java.util.ArrayList;
import edu.mit.csail.sdg.alloy4.A4Reporter;
import edu.mit.csail.sdg.alloy4.ErrorWarning;
import edu.mit.csail.sdg.alloy4compiler.ast.Command;
import edu.mit.csail.sdg.alloy4compiler.parser.CompModule;
import edu.mit.csail.sdg.alloy4compiler.parser.CompUtil;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Options;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;
import edu.mit.csail.sdg.alloy4compiler.translator.TranslateAlloyToKodkod;

@SuppressWarnings("javadoc")
public class AlloyCodeAnalysisManager {

	private File alloyFolder;
	private ArrayList<String> xmlFilePaths = new ArrayList<String>();

	public AlloyCodeAnalysisManager(String alloyModelPath) throws Exception {
		this.alloyFolder = new File(alloyModelPath);
	}

	public void process() throws Exception {

		//		File[] srcFiles = this.alloyFolder.listFiles(
		//				new FileFilter() {
		//					@Override
		//					public boolean accept(File pathname) {
		//						if(pathname.isFile() && pathname.getName().endsWith("als"))
		//							return true;
		//						return false;
		//					}
		//				}
		//		);

		// Create error reporter
		A4Reporter reporter = new A4Reporter() {
			@Override
			public void warning(ErrorWarning msg) {
				System.out.println("Error");
			}
		};

		int count = 0;

		try {
			String fileName = this.alloyFolder.getAbsolutePath();

			CompModule world = null;
			try {
				world =
					CompUtil.parseEverything_fromFile(reporter, null, fileName);
			}
			catch (Exception e) {
				//
			}

			if (world == null) {
				return;
			}

			// Choosing default options for how you want to execute the commands
			A4Options options = new A4Options();
			options.solver = A4Options.SatSolver.SAT4J;

			for (Command command : world.getAllCommands()) {
				A4Solution ans =
					TranslateAlloyToKodkod.execute_command(
						reporter,
						world.getAllReachableSigs(),
						command,
						options);
				if (ans.satisfiable()) {

					String outputFileName = "runCmd" + count++ + ".xml";
					ans.writeXML(outputFileName);
					this.xmlFilePaths.add(System.getProperty("user.dir") + "\\"
							+ outputFileName);
					System.out.println("Done " + System.getProperty("user.dir")
							+ "\\" + outputFileName);
				}
			}
		}
		catch (Exception e) {
			//
		}

	}

	public ArrayList<String> getXMLFilePaths() {
		return this.xmlFilePaths;
	}
}
