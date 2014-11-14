package pl.grm.bol.updater;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

public class UpdaterDialog extends JDialog {
	
	private final JPanel	contentPanel	= new JPanel();
	private JProgressBar	progressBar;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			UpdaterDialog dialog = new UpdaterDialog();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Create the dialog.
	 */
	public UpdaterDialog() {
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setLayout(new FlowLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		Container content = getContentPane();
		progressBar = new JProgressBar();
		progressBar.setValue(0);
		progressBar.setStringPainted(true);
		Border border = BorderFactory.createTitledBorder("Updating Launcher ...");
		progressBar.setBorder(border);
		content.add(progressBar, BorderLayout.NORTH);
		setSize(300, 100);
	}
	
}
