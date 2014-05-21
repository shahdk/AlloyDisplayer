import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;
import kodkod.engine.fol2sat.HigherOrderDeclException;
import edu.mit.csail.sdg.alloy4.A4Reporter;
import edu.mit.csail.sdg.alloy4.Computer;
import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.ErrorFatal;
import edu.mit.csail.sdg.alloy4.ErrorType;
import edu.mit.csail.sdg.alloy4.ErrorWarning;
import edu.mit.csail.sdg.alloy4.Util;
import edu.mit.csail.sdg.alloy4.Version;
import edu.mit.csail.sdg.alloy4.XMLNode;
import edu.mit.csail.sdg.alloy4.Util.BooleanPref;
import edu.mit.csail.sdg.alloy4compiler.ast.Command;
import edu.mit.csail.sdg.alloy4compiler.ast.Expr;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprVar;
import edu.mit.csail.sdg.alloy4compiler.ast.Module;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.Field;
import edu.mit.csail.sdg.alloy4compiler.parser.CompModule;
import edu.mit.csail.sdg.alloy4compiler.parser.CompUtil;
import edu.mit.csail.sdg.alloy4compiler.sim.SimInstance;
import edu.mit.csail.sdg.alloy4compiler.sim.SimTuple;
import edu.mit.csail.sdg.alloy4compiler.sim.SimTupleset;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Options;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;
import edu.mit.csail.sdg.alloy4compiler.translator.A4SolutionReader;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Tuple;
import edu.mit.csail.sdg.alloy4compiler.translator.A4TupleSet;
import edu.mit.csail.sdg.alloy4compiler.translator.TranslateAlloyToKodkod;

@SuppressWarnings("javadoc")
public class AlloyCodeAnalysisManager {

	public static File alloyFolder;
	private ArrayList<String> xmlFilePaths = new ArrayList<String>();
	private ArrayList<String> commandNames = new ArrayList<String>();
	private static String errorMessage = "";
	private Computer evaluator;

	private static final BooleanPref ImplicitThis = new BooleanPref(
		"ImplicitThis");

	/** This enum defines the set of possible message verbosity levels. */
	private enum Verbosity {
		/** Level 0. */
		DEFAULT("0", "low"),
		/** Level 1. */
		VERBOSE("1", "medium"),
		/** Level 2. */
		DEBUG("2", "high"),
		/** Level 3. */
		FULLDEBUG("3", "debug only");
		/** Returns true if it is greater than or equal to "other". */
		@SuppressWarnings("unused")
		public boolean geq(Verbosity other) {
			return ordinal() >= other.ordinal();
		}
		/** This is a unique String for this value; it should be kept consistent in future versions. */
		private final String id;
		/** This is the label that the toString() method will return. */
		private final String label;
		/** Constructs a new Verbosity value with the given id and label. */
		private Verbosity(String id, String label) {
			this.id = id;
			this.label = label;
		}
		/** Given an id, return the enum value corresponding to it (if there's no match, then return DEFAULT). */
		private static Verbosity parse(String id) {
			for (Verbosity vb : values())
				if (vb.id.equals(id))
					return vb;
			return DEFAULT;
		}
		/** Returns the human-readable label for this enum value. */
		@Override
		public final String toString() {
			return this.label;
		}
		/** Reads the current value of the Java preference object (if it's not set, then return DEFAULT). */
		private static Verbosity get() {
			return parse(Preferences.userNodeForPackage(Util.class).get(
				"Verbosity",
				""));
		}
	}

	public AlloyCodeAnalysisManager(String alloyModelPath) throws Exception {
		alloyFolder = new File(alloyModelPath);
	}

	public void process() throws Exception {

		A4Reporter reporter = new A4Reporter() {
			
			@Override
			public void warning(ErrorWarning msg) {
				System.out.println("================");
				System.out.println("Warning");
				System.out.println(msg.dump());
//				AlloyCodeAnalysisManager.errorMessage = msg.dump();
			}
		};

		int count = 0;
		try {
			String fileName = alloyFolder.getAbsolutePath();
			this.evaluator = this.createComputer();
			CompModule world = null;
			try {
				world =
					CompUtil.parseEverything_fromFile(reporter, null, fileName);
			}
			catch (Exception e) {
				AlloyCodeAnalysisManager.errorMessage = e.getMessage();
			}
			if (world == null) {
				return;
			}
			
			A4Options options = new A4Options();
			options.solver = A4Options.SatSolver.SAT4J;

			for (Command command : world.getAllCommands()) {
				String[] commandString = command.toString().split("\\s+");
				this.commandNames.add(commandString[1]);

				A4Solution ans =
					TranslateAlloyToKodkod.execute_command(
						reporter,
						world.getAllReachableSigs(),
						command,
						options);
				if (ans.satisfiable()) {

					String outputFileName = "runCmd" + count++ + ".xml";
					ans.writeXML(System.getProperty("user.dir") + "\\"
							+ outputFileName);
					//ConstMap.make(this.map)
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

	private Computer createComputer() {
		Computer evaluator = new Computer() {
			private String filename = null;
			@Override
			public final String compute(final Object input) throws Exception {
				if (input instanceof File) {
					this.filename = ((File) input).getAbsolutePath();
					return "";
				}
				if (!(input instanceof String))
					return "";
				final String str = (String) input;
				if (str.trim().length() == 0)
					return ""; // Empty line
				Module root = null;
				A4Solution ans = null;
				try {
					Map<String, String> fc =
						new LinkedHashMap<String, String>();
					XMLNode x = new XMLNode(new File(this.filename));
					if (!x.is("alloy"))
						throw new Exception();
					String mainname = null;
					for (XMLNode sub : x)
						if (sub.is("instance")) {
							mainname = sub.getAttribute("filename");
							break;
						}
					if (mainname == null)
						throw new Exception();
					for (XMLNode sub : x)
						if (sub.is("source")) {
							String name = sub.getAttribute("filename");
							String content = sub.getAttribute("content");
							fc.put(name, content);
						}
					root =
						CompUtil.parseEverything_fromFile(
							A4Reporter.NOP,
							null,
							AlloyCodeAnalysisManager.alloyFolder.getAbsolutePath(),
							(Version.experimental && ImplicitThis.get()) ? 2
									: 1);
					ans = A4SolutionReader.read(root.getAllReachableSigs(), x);
					for (ExprVar a : ans.getAllAtoms()) {
						root.addGlobal(a.label, a);
					}
					for (ExprVar a : ans.getAllSkolems()) {
						root.addGlobal(a.label, a);
					}
				}
				catch (Throwable ex) {
					throw new ErrorFatal(
						"Failed to read or parse the XML file.");
				}
				try {
					Expr e = CompUtil.parseOneExpression_fromString(root, str);
					if ("yes".equals(System.getProperty("debug"))
							&& Verbosity.get() == Verbosity.FULLDEBUG) {
						SimInstance simInst =
							AlloyCodeAnalysisManager.convert(root, ans);
						return simInst.visitThis(e).toString()
								+ (simInst.wasOverflow() ? " (OF)" : "");
					}
					else
						return ans.eval(e).toString();
				}
				catch (HigherOrderDeclException ex) {
					throw new ErrorType(
						"Higher-order quantification is not allowed in the evaluator.");
				}
			}
		};
		return evaluator;
	}

	/** Converts an A4Solution into a SimInstance object. */
	private static SimInstance convert(Module root, A4Solution ans) throws Err {
		SimInstance ct =
			new SimInstance(root, ans.getBitwidth(), ans.getMaxSeq());
		for (Sig s : ans.getAllReachableSigs()) {
			if (!s.builtin)
				ct.init(s, convert(ans.eval(s)));
			for (Field f : s.getFields())
				if (!f.defined)
					ct.init(f, convert(ans.eval(f)));
		}
		for (ExprVar a : ans.getAllAtoms())
			ct.init(a, convert(ans.eval(a)));
		for (ExprVar a : ans.getAllSkolems())
			ct.init(a, convert(ans.eval(a)));
		return ct;
	}

	/** Converts an A4TupleSet into a SimTupleset object. */
	private static SimTupleset convert(Object object) throws Err {
		if (!(object instanceof A4TupleSet))
			throw new ErrorFatal(
				"Unexpected type error: expecting an A4TupleSet.");
		A4TupleSet s = (A4TupleSet) object;
		if (s.size() == 0)
			return SimTupleset.EMPTY;
		List<SimTuple> list = new ArrayList<SimTuple>(s.size());
		int arity = s.arity();
		for (A4Tuple t : s) {
			String[] array = new String[arity];
			for (int i = 0; i < t.arity(); i++)
				array[i] = t.atom(i);
			list.add(SimTuple.make(array));
		}
		return SimTupleset.make(list);
	}

	public ArrayList<String> getXMLFilePaths() {
		return this.xmlFilePaths;
	}

	public ArrayList<String> getCommandNames() {
		return this.commandNames;
	}

	public Computer getEvaluator() {
		return this.evaluator;
	}
	
	public String getErrorMessageString(){
		return AlloyCodeAnalysisManager.errorMessage;
	}
}
