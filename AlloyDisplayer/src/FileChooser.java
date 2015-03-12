import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;

@SuppressWarnings("javadoc")
public class FileChooser extends JFrame implements ActionListener {

	private static final long serialVersionUID = -6068567275463392611L;
	private JLabel alloyModel;
	private JLabel themeFile;
	private JLabel alloyDirLabel;

	private JTextField alloyFilePath;
	private JTextField themeFilePath;
	private JTextField alloyDirPath;

	private JPanel alloyPathPanel;
	private JPanel themeFilePanel;
	private JPanel alloyDirPanel;

	private JButton alloyButton;
	private JButton themeButton;
	private JButton alloyDirButton;
	private JButton runButton;

	public FileChooser() {
		super("Choose File");
		setSize(350, 200);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setLocationRelativeTo(null);

		this.initComponents();

		this.setLayout(new GridLayout(7, 1));

		this.add(this.alloyModel, 0);
		this.add(this.alloyPathPanel, 1);
		this.add(this.themeFile, 2);
		this.add(this.themeFilePanel, 3);
		this.add(this.alloyDirLabel, 4);
		this.add(this.alloyDirPanel, 5);
		this.add(this.runButton, 6);
	}

	public void initComponents() {
		this.alloyModel = new JLabel("Alloy Model:");
		this.themeFile = new JLabel("Theme file:");
		this.alloyDirLabel = new JLabel("Alloy Submissions Dir (for graders):");

		this.alloyPathPanel = new JPanel();
		this.themeFilePanel = new JPanel();
		this.alloyDirPanel = new JPanel();
		this.alloyPathPanel.setLayout(new BorderLayout());
		this.themeFilePanel.setLayout(new BorderLayout());
		this.alloyDirPanel.setLayout(new BorderLayout());

		this.alloyFilePath = new JTextField("rdt22.als");
		this.themeFilePath = new JTextField("mytheme.thm");
		this.alloyDirPath = new JTextField("Submissions");
		this.alloyFilePath.setSize(20, 130);
		this.themeFilePath.setSize(20, 130);
		this.alloyDirPath.setSize(20, 130);

		this.alloyButton = new JButton("Browse..");
		this.themeButton = new JButton("Browse..");
		this.alloyDirButton = new JButton("Browse..");
		this.runButton = new JButton("Run Alloy Model(s)");
		this.alloyButton.setSize(20, 50);
		this.themeButton.setSize(20, 50);
		this.alloyDirButton.setSize(20, 50);
		this.alloyButton.addActionListener(this);
		this.themeButton.addActionListener(this);
		this.alloyDirButton.addActionListener(this);
		this.runButton.addActionListener(this);

		this.alloyPathPanel.add(this.alloyFilePath, BorderLayout.CENTER);
		this.themeFilePanel.add(this.themeFilePath, BorderLayout.CENTER);
		this.alloyDirPanel.add(this.alloyDirPath, BorderLayout.CENTER);
		this.alloyPathPanel.add(this.alloyButton, BorderLayout.EAST);
		this.themeFilePanel.add(this.themeButton, BorderLayout.EAST);
		this.alloyDirPanel.add(this.alloyDirButton, BorderLayout.EAST);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(this.alloyButton)) {
			String fileName = this.getFileName();
			this.alloyFilePath.setText(fileName);
		} else if (e.getSource().equals(this.themeButton)) {
			String fileName = this.getFileName();
			this.themeFilePath.setText(fileName);
		} else if (e.getSource().equals(this.themeButton)) {
			String fileName = this.getDirName();
			this.alloyDirPath.setText(fileName);
		} else if (e.getSource().equals(this.runButton)) {
			this.runButton.setText("Running Alloy Model...");

			if (this.alloyDirPath.getText().length() == 0) {
				String[] names = new String[2];
				names = this.alloyFilePath.getText().split("\\.");
				String fileName = "";
				if (names[1].equals("als"))
					fileName = names[0];
				AlloyFrame frame = new AlloyFrame(this.alloyFilePath.getText(),
						this.themeFilePath.getText(), fileName);
				frame.setVisible(true);
			} else {

				File file = new File(this.alloyDirPath.getText());
				String[] students = file.list(new FilenameFilter() {
					@Override
					public boolean accept(File current, String name) {
						return new File(current, name).isDirectory();
					}
				});

				for (String selectedStudent : students) {
					String alloyPath = this.alloyDirPath.getText() + "\\" + selectedStudent;

					File alloyFile = new File(alloyPath);

					String alloyModelPath = "";
					String themeFilePathLoc = "";

					File[] files = alloyFile.listFiles();
					for (File dirFile : files) {
						String filePath = dirFile.getAbsolutePath();
						if (filePath.contains(".als")) {
							alloyModelPath = filePath;
						} else if (filePath.contains(".thm")) {
							themeFilePathLoc = filePath;
						}
					}
					
					String[] names = new String[2];
					names = alloyModelPath.split("\\.");
					String fileName = "";
					if (names[1].equals("als"))
						fileName = names[0];
					
					AlloyFrame frame = new AlloyFrame(alloyModelPath, themeFilePathLoc, fileName);
					frame.setVisible(true);
				}

			}
			this.dispose();
		}
	}

	public String getFileName() {
		String fileName = "";
		FileSystemView fav = FileSystemView.getFileSystemView();
		JFileChooser chooser = new JFileChooser(fav.getRoots()[0]);
		int option = chooser.showOpenDialog(FileChooser.this);
		if (option == JFileChooser.APPROVE_OPTION) {
			File sf = chooser.getSelectedFile();
			fileName = sf.getAbsolutePath();
		}
		return fileName;
	}

	public String getDirName() {
		String fileName = "";
		FileSystemView fav = FileSystemView.getFileSystemView();
		JFileChooser chooser = new JFileChooser(fav.getRoots()[0]);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int option = chooser.showOpenDialog(FileChooser.this);
		if (option == JFileChooser.APPROVE_OPTION) {
			File sf = chooser.getSelectedFile();
			fileName = sf.getAbsolutePath();
		}
		return fileName;
	}

	public String getAlloyModelFilePath() {
		return this.alloyFilePath.getText();
	}

	public String getThemeFilePath() {
		return this.themeFilePath.getText();
	}
}
