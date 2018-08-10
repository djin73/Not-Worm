package stuff;

import java.awt.*;

public class Circle {

	double x;
	double y;
	double r;
	double vx;
	double vy;
	double health;
	int level;
	
	public Circle(double x, double y, double r, double vx, double vy, int level) {
		this.x = x;
		this.y = y;
		this.r = r;
		if (this.r == 0)
			this.r = 10+level*5;
		this.vx = vx;
		this.vy = vy;
		this.health = 1+level*2;
		this.level = level;
	}

	public void move() {
		x += vx;
		y += vy;
	}
	
	public void damage (double damage) {
		health -= damage;
	}
	
	public boolean offScreen(int width, int height) {
		if (x+r < 0 || x-r > width || y-r > height || y+r < 0)
			return true;
		return false;
	}
	
	public boolean collides(Circle c) {
		double d = Math.sqrt((x-c.x)*(x-c.x)+(y-c.y)*(y-c.y));
		if (d <= r+c.r)
			return true;
		return false;
	}
	
	public void drawImg(Graphics g, Image img) {
		g.drawImage(img,(int)(x-r), (int)(y-r), (int)(2*r), (int)(2*r), null);
	}
	
	public void draw(Graphics g) {
		g.fillOval((int)(x-r), (int)(y-r), (int)(2*r), (int)(2*r));
	}

	public void drawBullet(Graphics g) {
		g.fillRect((int)(x-r), (int)(y-3*r), (int)(2*r), (int)(6*r));
	}

	public boolean isDead() {
		return health <= 0;
	}
}
