import java.awt.Color;

public class Rectangle {
	//location associated with grid of squares
	private Vec2 loc;
	//location associated with left corner on the screen
	private float  width, height;
	private Color color;
	public Rectangle (Vec2 loc, float  w, float  h) {
		this.setLoc(loc);
		this.setWidth(w);
		this.setHeight(h);
		this.setColor(Color.WHITE);
	}
	public Rectangle (Vec2 loc, float  w, float  h, Color c) {
		this.setLoc(loc);
		this.setWidth(w);
		this.setHeight(h);
		this.setColor(c);
	}
	public Rectangle (Vec2 start, Vec2 end, Color c) {
		this.setColor(c);
		if(end.getX() > start.getX()) {
			this.setWidth(end.getX() - start.getX());
			if(end.getY() > start.getY()) {
				this.setLoc(start);
				this.setHeight(end.getY() - start.getY());
			} else {
				this.setLoc(new Vec2(start.getX(), end.getY()));
				this.setHeight(start.getY() - end.getY());
			}
		} else {
			this.setWidth(start.getX() - end.getX());
			if(end.getY() > start.getY()) {
				this.setLoc(new Vec2(end.getX(), start.getY()));
				this.setHeight(end.getY() - start.getY());
			} else {
				this.setLoc(new Vec2(end.getX(), end.getY()));
				this.setHeight(start.getY() - end.getY());
			}
		}
	}
	//a square (rectangle really) can also be described by four points
	public Rectangle (Vec2 leftTop, Vec2 leftBot, Vec2 rightTop, Vec2 rightBot, Color c) {
		this.setLoc(leftTop);
		//this.setWidth(rightTop.getX() - leftTop.getX());
		//this.setHeight(rightBot.getY() - rightTop.getY());
		this.setColor(c);
	}
	//method returning four points given leftTop location, width, and height
	//Points = {leftTop, rightTop, leftBot, rightBot}
	public Vec2[] getPoints () {
		float  leftTopX = this.loc.getX();
		float  leftTopY = this.loc.getY();
		Vec2[] points = {this.loc,  new Vec2(leftTopX + this.width, leftTopY), new Vec2(leftTopX, leftTopY + this.height),new Vec2(leftTopX + width, leftTopY + height)};
		return points;
	}
	public float getHeight() {
		return height;
	}

	public void setHeight(float height) {
		this.height = height;
	}

	public float  getWidth() {
		return width;
	}

	public void setWidth(float width) {
		this.width = width;
	}

	public Vec2 getLoc() {
		return loc;
	}

	public void setLoc(Vec2 loc) {
		this.loc = loc;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}
	
	public Rectangle translate (Vec2 point) {
		//System.out.println("point: " + point +" and location: " + this.loc);
		return new Rectangle (this.loc.minus(point), this.width, this.height);
		//return new Square (point.minus(this.loc), this.width, this.height);
	}
	
	public Rectangle scaledBy (double s) {
		Vec2[] sqPoints = this.getPoints();
		//return new Square(sqPoints[0].scaledBy(s), sqPoints[1].scaledBy(s), sqPoints[2].scaledBy(s), sqPoints[3].scaledBy(s));
		return new Rectangle (this.loc.scaledBy(s), (int)(this.width*s), (int)(this.height*s));
	}
	public String toString() {
		return this.loc + " width: " + this.width + " height: " + this.height;
	}
}
