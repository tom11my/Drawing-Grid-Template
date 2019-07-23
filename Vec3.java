
public class Vec3 {
	private float x, y, z;
	public Vec3(float x, float y, float z) {
		this.setX(x);
		this.setY(y);
		this.setZ(z);
	}
	
	//trims Vec3 to Vec2 by eliminating z term
	public Vec2 trim () {
		return new Vec2(this.x, this.y);
	}
	public Vec3 getNeg() {
		return new Vec3 (-this.x, -this.y, -this.z);
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
	public float getZ() {
		return z;
	}
	public void setZ(float z) {
		this.z = z;
	}
	public String toString() {
		return "x: " + this.x + " y: " + this.y + " z: " + this.z;
	}
}
