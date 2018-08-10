package stuff;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

public class GamePanel extends JPanel implements Runnable {
	private static final int PWIDTH = 800;
	private static final int PHEIGHT = 600;
	private volatile boolean running = false;
	private volatile boolean isPaused = false;
	private volatile boolean gameOver = false;
	private Graphics g;
	private Image dbImage = null;
	
	private double score = 0;
	private Circle player = new Circle(PWIDTH/2, PHEIGHT-20, 20, 0, 0, 0);
	private Circle[] balls = new Circle[1000];
	private int n = 0;
	
	private Circle[] bullets = new Circle[1000]; // array of bullets
	private int m = 0; // "size" of bullets
	private int cd = 0; // counter for bullet
	private double energy = 100; // current energy
	private double maxenergy = 100; // max energy
	private double regen = 2; // energy regen speed
	private int used = 0; // counter for energy regen
	private int regencd = 80; // cooldown for energy regen
	private int laserr = 10; // radius of laser
	private double speed = 3; // move speed
	private double attackspeed = 10; // bullets per second
	private double bdamage = 2; // bullet damage;
	private double ldamage = 0.4; // laser damage;
	
	private boolean leftDown = false;
	private boolean rightDown = false;
	private boolean upDown = false;
	private boolean downDown = false;
	private boolean zdown = false;
	private boolean xdown = false;
	
	private Image orb;
	private Image ship;
	
	private Powerup p;
	private int pcd = 0;
	private int pcdmax = 1000;
	private int pcdmin = 500;
	
	private String message = "";
	private int mcd = 0;
		
	private void testPress(int keyCode) {
		if (keyCode == KeyEvent.VK_LEFT)
			leftDown = true;
		if (keyCode == KeyEvent.VK_RIGHT)
			rightDown = true;	
		if (keyCode == KeyEvent.VK_UP)
			upDown = true;	
		if (keyCode == KeyEvent.VK_DOWN)
			downDown = true;	
		if (keyCode == KeyEvent.VK_Z)
			zdown = true;
		if (keyCode == KeyEvent.VK_X)
			xdown = true;
	}

	private void testRelease(int keyCode) {
		if (keyCode == KeyEvent.VK_LEFT)
			leftDown = false;
		if (keyCode == KeyEvent.VK_RIGHT)
			rightDown = false;
		if (keyCode == KeyEvent.VK_UP)
			upDown = false;	
		if (keyCode == KeyEvent.VK_DOWN)
			downDown = false;	
		if (keyCode == KeyEvent.VK_Z)
			zdown = false;
		if (keyCode == KeyEvent.VK_X)
			xdown = false;
	}
	
	private void updatePlayer() {
		if (leftDown)
			player.x -= speed;
		if (rightDown)
			player.x += speed;
		if (upDown)
			player.y -= speed;
		if (downDown)
			player.y += speed;
		if (zdown && cd <= 0)
		{
			bullets[m] = new Circle(player.x-10, player.y, 2, 0, -15, 0);
			m++;
			bullets[m] = new Circle(player.x+10, player.y, 2, 0, -15, 0);
			m++;
			
			cd = (int)(80/attackspeed);
		}
		if (xdown && energy > 0)
		{
			energy--;
			used = regencd;
		}
		else if (used <= 0)
			energy += regen;
		if (energy > maxenergy)
			energy = maxenergy;
		cd--;
		used--;
		
		if (player.x-player.r < 0)
			player.x = player.r;
		if (player.x+player.r > PWIDTH)
			player.x = PWIDTH - player.r;
		if (player.y-player.r < 0)
			player.y = player.r;
		if (player.y+player.r > PHEIGHT)
			player.y = PHEIGHT - player.r;
	}
	
	private void updatePowerup() {
		pcd--;
		if (pcd <= 0)
		{
			p = new Powerup((int)(Math.random()*PWIDTH), (int)(Math.random()*PHEIGHT), 15, 3, 3, 0);
			pcd = pcdmax;
		}
		if (p != null)
			p.move(PWIDTH, PHEIGHT);
	}
	
	private void gameUpdate() {
		if (!isPaused && !gameOver) // if game is still playing
		{
			score += 0.02+score/10000.0;
			updatePlayer();
			updatePowerup();
			if (p != null && p.collides(player))
			{
				switch (p.power){
				case 1:
					laserr += 4;
					message = "Laser Size Upgraded!";
					break;
				case 2:
					maxenergy += 30;
					message = "Laser Energy Upgraded!";
					break;
				case 3:
					regencd = regencd*9/10;
					regen += 0.5;
					message = "Laser Regen Upgraded!";
					break;
				case 4:
					ldamage += 0.1;
					message = "Laser Damage Upgraded!";
					break;
				case 5:
					attackspeed += 4;
					message = "Attack Speed Upgraded!";
					break;
				case 6:
					bdamage += 0.5;
					message = "Bullet Damage Upgraded!";
					break;
				}
				
				mcd = 100;
				pcd = pcdmin;
				p = null;
			}
			
			if (Math.random() < 0.1) // add ball 10% of the time
				for (int i = 0; i < Math.sqrt(score)/20+1; i++)
					addBall();
			
			for (int i = 0; i < m; i++) // loop through every bullet
			{
				bullets[i].move();
				if (bullets[i].offScreen(PWIDTH, PHEIGHT))
				{
					bullets[i] = bullets[--m];
					continue;
				}
					
				for (int j = 0; j < n; j++) // loop through every ball
					if (bullets[i].collides(balls[j])) // bullet-ball collision
					{	
						bullets[i] = bullets[--m];
						balls[j].damage(bdamage);
						break;
					}
			}
			
			for (int i = 0; i < n; i++) // loop through every ball
			{
				balls[i].move();
				if (balls[i].offScreen(PWIDTH, PHEIGHT)) // delete balls that are off screen
					balls[i] = balls[--n]; // no points for flying off screen
				
				if (xdown && energy > 0 && Math.abs(balls[i].x-player.x) < laserr+balls[i].r && balls[i].y <= player.y) // laser collision
					balls[i].damage(ldamage);
				
				if (balls[i].isDead())
				{
					if (balls[i].level > 0)
					{
						balls[n++] = new Circle(balls[i].x, balls[i].y, 0, balls[i].vx+0.5, balls[i].vy*3/4,  balls[i].level-1);
						balls[n++] = new Circle(balls[i].x, balls[i].y, 0, balls[i].vx-0.5, balls[i].vy*3/4,  balls[i].level-1);
					}
					score += balls[i].level; // increase score based on level of orb
					balls[i] = balls[--n];
				}
				
				if (balls[i].collides(player)) // check collision with player
					gameOver = true;
			}
		}
	}
	
	private void addBall() {
		
		double vx = 0;
		double vy = 2+Math.random();
		if (score > 100)
			vx = 2*Math.random()-1;
		if (score > 400)
			vx = 4*Math.random()-2;
		
		vy += Math.random()*score/100;

		int level = 0;
		if (score > 100)
			if (Math.random() < Math.min(0.4,score/1000.0))
				level = 1;
		if (score > 400)
			if (Math.random() <  Math.min(0.4,score/4000.0))
				level = 2;
		balls[n++] = new Circle(PWIDTH*Math.random(), 0, 0, vx, vy, level); // randomly generated ball
	}
	
	private void gameRender() {
		if (dbImage == null) {
			dbImage = createImage(PWIDTH, PHEIGHT);
			g = dbImage.getGraphics();
		}

		g.setColor(Color.black); // black background
		g.fillRect(0, 0, PWIDTH, PHEIGHT);
		
		g.setColor(Color.white);
		g.drawString("Score: "+(int)score, 10, 20); // draw score
		
		g.setColor(Color.RED); // draw energy bar
		g.fillRect(10, 40, (int)(maxenergy/2), 15);
		g.setColor(Color.GREEN);
		g.fillRect(10, 40, (int)(energy/2), 15);
		
		if (xdown && energy > 0) // draw laser
			for (int s = 100; s >= 0; s-= 10)
			{
				int d = laserr/2+laserr*s/200;
				g.setColor(new Color(Color.HSBtoRGB((float)(ldamage/2+0.35), s*0.01f, 1)));
				g.fillRect((int)(player.x-d), 0, 2*d, (int)player.y);
			}
		
		player.drawImg(g,ship); // draw ship
		for (int i = 0; i < n; i++) // draw orbs
			balls[i].drawImg(g,orb);
		g.setColor(new Color(Color.HSBtoRGB((float)(bdamage/6),1,1)));
		for (int i = 0; i < m; i++) // draw bullets
			bullets[i].drawBullet(g);
		if (pcd > pcdmin && p != null) // draw upgrade
			p.draw(g);
		
		mcd--;
		if (mcd > 0) // draw message
		{ 
			Font font = new Font("SansSerif", Font.BOLD, 30);
			FontMetrics metrics = this.getFontMetrics(font);	
			int x = (PWIDTH - metrics.stringWidth(message)) / 2;
			int y = PHEIGHT -10;

			g.setFont(font);
			g.setColor(Color.WHITE);
			g.drawString(message, x, y);
			g.setFont(new JLabel().getFont());
		}
		
		if (gameOver)
			gameOverMessage(g);
	}
		
	public GamePanel(GameMain wc, int period, Image orb, Image ship) {
		this.period = period;
		this.orb = orb;
		this.ship = ship;

		setBackground(Color.white);
		setPreferredSize(new Dimension(PWIDTH, PHEIGHT));

		setFocusable(true);
		requestFocus();
		readyForTermination();
		
		addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (!gameOver)
					testPress(e.getKeyCode());
			}
			public void keyReleased(KeyEvent e) {
				if (!gameOver)
					testRelease(e.getKeyCode());
			}
		});
	}
	
	private void readyForTermination() {
		addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				int keyCode = e.getKeyCode();
				if (keyCode == KeyEvent.VK_ESCAPE || keyCode == KeyEvent.VK_Q)  
					running = false;
			}
		});
	}

	public void addNotify() {
		super.addNotify();
		startGame();
	}
	
	private void startGame() {
		if (animator == null || !running) {
			animator = new Thread(this);
			animator.start();
		}
	}

	private void gameOverMessage(Graphics g) {
		String msg = "GAME OVER";

		Font font = new Font("SansSerif", Font.BOLD, 100);
		FontMetrics metrics = this.getFontMetrics(font);	
		int x = (PWIDTH - metrics.stringWidth(msg)) / 2;
		int y = (PHEIGHT - metrics.getHeight()) / 2;

		g.setFont(font);
		g.setColor(Color.red);
		g.drawString(msg, x, y);
		
		msg = "SCORE: "+(int)score;
		x = (PWIDTH - metrics.stringWidth(msg)) / 2;
		y = (PHEIGHT + metrics.getHeight()) / 2;
		g.drawString(msg, x, y);

		g.setFont(new JLabel().getFont());
	}
	
	public void run() {
		long beforeTime, afterTime, timeDiff, sleepTime;
		int overSleepTime = 0;
		int noDelays = 0;
		int excess = 0;

		gameStartTime = System.currentTimeMillis();
		beforeTime = gameStartTime;

		running = true;

		while (running) {
			gameUpdate();
			gameRender();
			paintScreen();

			afterTime = System.currentTimeMillis();
			timeDiff = afterTime - beforeTime;
			sleepTime = (period - timeDiff) - overSleepTime;

			if (sleepTime > 0) {
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException ex) {
				}
				overSleepTime = (int) ((System.currentTimeMillis() - afterTime) - sleepTime);
			} else {
				excess -= sleepTime;
				overSleepTime = 0;

				if (++noDelays >= NO_DELAYS_PER_YIELD) {
					Thread.yield();
					noDelays = 0;
				}
			}

			beforeTime = System.currentTimeMillis();

			int skips = 0;
			while ((excess > period) && (skips < MAX_FRAME_SKIPS)) {
				excess -= period;
				gameUpdate();
				skips++;
			}
		}
		System.exit(0);
	}
	
	private void paintScreen() {
		Graphics g;
		try {
			g = this.getGraphics();
			if ((g != null) && (dbImage != null))
				g.drawImage(dbImage, 0, 0, null);
			Toolkit.getDefaultToolkit().sync(); 
												
			g.dispose();
		} catch (Exception e) {
			System.out.println("Graphics error: " + e);
		}
	}
	
	public void resumeGame() {isPaused = false;}
	public void pauseGame() {isPaused = true;}
	public void stopGame() {running = false;}
	
	private static final int NO_DELAYS_PER_YIELD = 16;
	private static int MAX_FRAME_SKIPS = 5;
	private long gameStartTime;
	private Thread animator;
	private int period;
}