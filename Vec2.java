import java.awt.Color;

//serves as both Vec2 and Location on x-y plane
public class Vec2 {
	private float x, y;
	
	private Color col;
	
	public Vec2 (float x, float y) {
		this.setX(x);
		this.setY(y);
	}
	
	public Vec2 (float x, float y, Color c) {
		this.setX(x);
		this.setY(y);
		this.setCol(c);
	}
	
	//adds a z component of value 1 to all Vec2's
	public Vec3 convertTo3D () {
		return new Vec3(this.x, this.y, 1f);
	}

	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}
	public float findSlope (Vec2 end) {
		return (end.getY() - this.y)/(end.getX() - this.x);
	}

	public Color getCol() {
		return col;
	}

	public void setCol(Color col) {
		this.col = col;
	}
	
	public Vec2 minus (Vec2 a) {
		return new Vec2(this.x - a.getX(), this.y - a.getY());
	}
	
	public Vec2 plus (Vec2 a) {
		return new Vec2(this.x + a.getX(), this.y + a.getY());
	}
	public Vec2 getNeg () {
		return new Vec2(-1*this.x, -1*this.y);
	}
	
	public Vec2 scaledBy (double d) {
		return new Vec2((float) (this.x*d), (float) (this.y*d));
	}
	public boolean equals (Vec2 v) {
		return this.x == v.getX() && this.y == v.getY();
	}
	public String toString() {
		return "x: " + x + " y: " + y;
	}
}
