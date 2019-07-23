import java.awt.Color;

public class Building extends Rectangle{
	public Building() {
		super(new Vec2(10, 10), 100, 100);
	}
	public Building(Vec2 start, Vec2 end, Color c) {
		super(start, end, c);
	}
	
}
