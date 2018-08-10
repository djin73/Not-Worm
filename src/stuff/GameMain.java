package stuff;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

public class GameMain extends JFrame implements WindowListener {
	private static int DEFAULT_FPS = 80;

	private GamePanel gp;

	public GameMain(int period) {
		super("Not Worm");
		makeGUI(period);

		addWindowListener(this);
		pack();
		setResizable(false);
		setVisible(true);
	}

	private void makeGUI(int period) {
		Container c = getContentPane();

		Image orb = null;
		Image ship = null;
		try {
			orb = ImageIO.read(new File("src/stuff/orb.png"));
		} catch (IOException ex) {}
		try {
			ship = ImageIO.read(new File("src/stuff/ship.gif"));
		} catch (IOException ex) {}
		
		gp = new GamePanel(this, period, orb, ship);

		c.add(gp, "Center");
	}

	public static void main(String args[]) {
		int fps = DEFAULT_FPS;
		if (args.length != 0)
			fps = Integer.parseInt(args[0]);

		int period = (int) 1000.0 / fps;
		System.out.println("fps: " + fps + "; period: " + period + " ms");

		new GameMain(period);
	}
	
	public void windowActivated(WindowEvent e) {gp.resumeGame();}
	public void windowDeactivated(WindowEvent e) {gp.pauseGame();}
	public void windowDeiconified(WindowEvent e) {gp.resumeGame();}
	public void windowIconified(WindowEvent e) {gp.pauseGame();}
	public void windowClosing(WindowEvent e) {gp.stopGame();}
	public void windowClosed(WindowEvent e) {}
	public void windowOpened(WindowEvent e) {}
}