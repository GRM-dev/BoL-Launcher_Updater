package main.java.pl.grm.bol.updater;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

public class UpdaterDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private JProgressBar progressBar;

	/**
	 * Create the dialog.
	 */
	public UpdaterDialog() {
		setTitle("BoL Launcher Updater");
		setPreferredSize(setupBounds());
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		progressBar = new JProgressBar();
		contentPanel.add(progressBar, BorderLayout.NORTH);
		progressBar.setValue(0);
		progressBar.setStringPainted(true);
		Border border = BorderFactory
				.createTitledBorder("Updating Launcher ...");
		progressBar.setBorder(border);
		setSize(300, 100);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setVisible(true);
	}

	/**
	 * Calculates frame's dimensions
	 * 
	 * @return {@link Dimension} (x,y)
	 */
	private Dimension setupBounds() {
		Dimension dim;
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int screenHeight = (int) screenSize.getHeight();
		int screenWidth = (int) screenSize.getWidth();
		int frameWidth = screenWidth / 2 - screenWidth / 20;
		int frameHeight = frameWidth * 3 / 4;
		setBounds(2 * screenWidth / 5, 5 * screenHeight / 11, 200, 200);
		dim = new Dimension(frameWidth, frameHeight);
		return dim;
	}

	public JProgressBar getProgressBar() {
		return progressBar;
	}
}
