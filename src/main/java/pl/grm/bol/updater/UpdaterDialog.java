package pl.grm.bol.updater;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.WindowConstants;
import javax.swing.border.EtchedBorder;

import Effect.BarConstant;
import Effect.EffectProgressBarCoord;
import Effect.EffectProgressBarUI;

public class UpdaterDialog extends JFrame {
	private static final long	serialVersionUID	= 1L;
	private final JPanel		contentPanel		= new JPanel();
	private JProgressBar		progressBar;
	private EffectProgressBarUI	barUI;
	private Color[]				colors				= {
			Color.BLUE, Color.CYAN, Color.GRAY, Color.MAGENTA, Color.ORANGE, Color.PINK, Color.RED,
			Color.YELLOW							};
	
	/**
	 * Create the dialog.
	 */
	public UpdaterDialog() {
		super("BoL Launcher Updater");
		setPreferredSize(setupBounds());
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		createProgressBar();
		contentPanel.add(progressBar, BorderLayout.CENTER);
		setSize(400, 100);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setResizable(false);
		setVisible(true);
	}
	
	private void createProgressBar() {
		progressBar = new JProgressBar();
		barUI = new EffectProgressBarUI(progressBar);
		barUI.setRoundCorner(EffectProgressBarCoord.RoundOVAL);
		barUI.setDarkLightColors(colors);
		barUI.setBorder(1);
		barUI.setBorderColor(Color.GRAY);
		barUI.setHighQuality(true);
		barUI.setShadowPainted(true);
		barUI.setSelectionBackground(Color.RED);
		barUI.setSelectionForeground(Color.YELLOW);
		barUI.setIllusionDirection(BarConstant.LEFT_TO_RIGHT);
		barUI.getGradientFactory().setGrad(45);
		progressBar.setUI(barUI);
		progressBar.setForeground(Color.RED);
		progressBar.setFont(new Font("Arial", Font.BOLD, 20));
		progressBar.setValue(0);
		progressBar.setStringPainted(true);
		progressBar.setBackground(Color.BLACK);
		progressBar.setBorder(BorderFactory.createEtchedBorder(Color.WHITE, Color.CYAN));
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
	
	public static void main(String[] args) {
		UpdaterDialog dia = new UpdaterDialog();
	}
}
