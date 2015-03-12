import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;


public class StudentPanel extends JPanel implements ActionListener{

	private static final long serialVersionUID = 4482825572566081812L;
	private JLabel studentLabel;
	private JComboBox<String> studentComboBox;
//	private AlloyFrame alloy;
	
	
	public StudentPanel(AlloyFrame alloy, String[] students, int w, int h){
		super();
		this.setPreferredSize(new Dimension(w, h));
//		this.alloy = alloy;
		initializeLabels();
		initializeComboBox(students);
		this.add(this.studentLabel, BorderLayout.WEST);
		this.add(this.studentComboBox, BorderLayout.CENTER);
	}

	private void initializeLabels() {
		this.studentLabel = new JLabel("Select Student to Grade");
		this.studentLabel.setSize(new Dimension(200, 30));
	}

	private void initializeComboBox(String[] students) {
		this.studentComboBox = new JComboBox<>(students);
		this.studentComboBox.setSize(new Dimension(1300, 30));
		this.studentComboBox.addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource().equals(this.studentComboBox)){
//			this.alloy.loadSelectedStudentFiles(this.studentComboBox.getSelectedIndex());
		}
	}
}
