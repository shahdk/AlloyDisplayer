import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.filechooser.FileSystemView;

@SuppressWarnings("javadoc")
public class FileChooser extends JFrame implements ActionListener {

	private JLabel alloyModel;
	private JLabel themeFile;

	private JTextField alloyFilePath;
	private JTextField themeFilePath;

	private JPanel alloyPathPanel;
	private JPanel themeFilePanel;

	private JButton alloyButton;
	private JButton themeButton;
	private JButton runButton;

	public FileChooser() {
		super("Choose File");
		setSize(350, 200);
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		this.initComponents();

		this.setLayout(new GridLayout(5, 1));
		
		this.add(this.alloyModel, 0);
		this.add(this.alloyPathPanel, 1);
		this.add(this.themeFile, 2);
		this.add(this.themeFilePanel, 3);
		this.add(this.runButton, 4);
	}

	public void initComponents() {
		this.alloyModel = new JLabel("Alloy Model:");
		this.themeFile = new JLabel("Theme file:");

		this.alloyPathPanel = new JPanel();
		this.themeFilePanel = new JPanel();
		this.alloyPathPanel.setLayout(new BorderLayout());
		this.themeFilePanel.setLayout(new BorderLayout());

		this.alloyFilePath = new JTextField();
		this.themeFilePath = new JTextField();
		this.alloyFilePath.setSize(20, 130);
		this.themeFilePath.setSize(20, 130);

		this.alloyButton = new JButton("Browse..");
		this.themeButton = new JButton("Browse..");
		this.runButton = new JButton("Run Alloy Model");
		this.alloyButton.setSize(20, 50);
		this.themeButton.setSize(20, 50);
		this.alloyButton.addActionListener(this);
		this.themeButton.addActionListener(this);
		this.runButton.addActionListener(this);

		this.alloyPathPanel.add(this.alloyFilePath, BorderLayout.CENTER);
		this.themeFilePanel.add(this.themeFilePath, BorderLayout.CENTER);
		this.alloyPathPanel.add(this.alloyButton, BorderLayout.EAST);
		this.themeFilePanel.add(this.themeButton, BorderLayout.EAST);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(this.alloyButton)) {
			String fileName = this.getFileName();
			this.alloyFilePath.setText(fileName);
		}
		else if (e.getSource().equals(this.themeButton)) {
			String fileName = this.getFileName();
			this.themeFilePath.setText(fileName);
		}
		else if (e.getSource().equals(this.runButton)) {
			this.runButton.setText("Running Alloy Model...");

			String[] names = new String[2];
			names = this.alloyFilePath.getText().split("\\.");
			String fileName = "";
			if (names[1].equals("als"))
				fileName = names[0];

			AlloyFrame frame =
				new AlloyFrame(
					this.alloyFilePath.getText(),
					this.themeFilePath.getText(),
					fileName);
			frame.setVisible(true);
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
			System.out.println(fileName);
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
