
public class Intersection extends Vec2{
	private String name;
	private Street a;
	private Street b;
	public Intersection(float x, float y) {
		super(x, y);
	}
	public Intersection(Vec2 v) {
		super(v.getX(), v.getY());
	}

}
