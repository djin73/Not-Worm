package stuff;

import java.awt.*;
import java.io.*;
import javax.imageio.ImageIO;


public class Powerup extends Circle {

	int power; // which powerup;
	Image img;
	
	public Powerup(double x, double y, double r, double vx, double vy, int level) {
		super(x, y, r, vx, vy, level);
		power = (int)(Math.random()*6+1); // random powerup
		try {
			if (power <= 4)
				img = ImageIO.read(new File("src/stuff/powerup1.png")); // reusing images - couldn't find enough sprites :(
			else 
				img = ImageIO.read(new File("src/stuff/powerup2.png"));
		} catch (IOException ex) {}
	}
	
	public void move(int width, int height) {
		x += vx;
		y += vy;
		if (x-r < 0) // bounce off left wall
		{
			x = r;
			vx *= -1;
		}
		if (x+r > width) // bounce off right wall
		{
			x = width-r;
			vx *= -1;
		}
		if (y-r < 0) // bounce off top wall
		{
			y = r;
			vy *= -1;
		}
		if (y+r > height) // bounce off bottom wall
		{
			y = height-r;
			vy *= -1;
		}
	}

	public void draw(Graphics g) {
		drawImg(g, img);
	}
}
