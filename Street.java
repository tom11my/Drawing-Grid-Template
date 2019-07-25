import java.util.ArrayList;
public class Street {
	private ArrayList<Vec2> nodes;
	private String name;
	public Street (Vec2 start) {
		nodes = new ArrayList<Vec2>();
		nodes.add(start);
	}
	public Street (ArrayList<Vec2> nodes) {
		this.nodes = nodes;
	}
	public void addNode(Vec2 node) {
		nodes.add(node);
	}
	//removes the most recently added node
	public void removeNode() {
		nodes.remove(nodes.size()-1);
	}
	public ArrayList<Vec2> getNodes() {
		return nodes;
	}
	public void setNodes(ArrayList<Vec2> nodes) {
		this.nodes = nodes;
	}
	public ArrayList<Intersection> findIntersections (ArrayList<Street> streets) {
		for(int i = 0; i < nodes.size()-1; i++) {
			Line first = new Line(nodes.get(i), nodes.get(i+1));
			for(Street s: streets) {
				ArrayList<Vec2> curNodes = s.getNodes();
				for(int j = 0; j < streets.size(); j++) {
					Line second = new Line(curNodes.get(j), curNodes.get(j+1));
				}
			}
		}
		return null;
	}

	public ArrayList<Vec2> findColoredLocs () {
		ArrayList<Vec2> locs = new ArrayList<Vec2>();
		System.out.println("FRESH ADD");
		for(int j = 0; j < nodes.size() -1; j++) {
			Vec2 start = nodes.get(j);
			Vec2 end = nodes.get(j+1);
			Vec2 dir = end.minus(start);
			Line l = new Line(start, dir);
			
			//accounts for change of direction of line
			int dirFactor = 1;
			float dy = (int) l.getDir().getY();
			float dx = (int) l.getDir().getX();
			System.out.println(new Vec2(dx, dy));
			if(Math.abs(dy) > Math.abs(dx)) {
				if(end.getY() < start.getY())
					dirFactor = -1;
				
				for(int i = 0; i < Math.abs(dy) + 1; i++) {
					Vec2 point = l.findPoint((double)i*dirFactor/dy);
					//System.out.println("t " + (double)(i*dirFactor)/dx);
					locs.add(point);
				}
			}
			else {
				if(end.getX() < start.getX())
					dirFactor = -1;
				for(int i = 0; i < Math.abs(dx) + 1; i++) {

					System.out.println("t " + (double)(i*dirFactor)/dx);
					Vec2 point = l.findPoint((double)(i*dirFactor)/dx);
					//System.out.println(point);
					//System.out.println(new Vec2((int)point.getX(), (int)point.getY()));

					locs.add(point);
				}
			}
		}
		return locs;
	}
	public ArrayList<Vec2> findColoredLocsBetweenNodes (Vec2 start, Vec2 end) {
		ArrayList<Vec2> locs = new ArrayList<Vec2>();
		Vec2 dir = end.minus(start);
		Line l = new Line(start, dir);
		
		//accounts for change of direction of line
		int dirFactor = 1;
		//originally dy and dx treated as int
		float dy = (int) l.getDir().getY();
		float dx = (int) l.getDir().getX();
		if(Math.abs(dy) > Math.abs(dx)) {
			if(end.getY() < start.getY())
				dirFactor = -1;
			
			for(int i = 0; i < Math.abs(dy) + 1; i++) {
				Vec2 point = l.findPoint((double)i*dirFactor/dy);
				//System.out.println("t " + (double)(i*dirFactor)/dx);
				locs.add(point);
			}
		}
		else {
			if(end.getX() < start.getX())
				dirFactor = -1;
			for(int i = 0; i < Math.abs(dx) + 1; i++) {

				Vec2 point = l.findPoint((double)(i*dirFactor)/dx);
				//System.out.println(point);
				//System.out.println(new Vec2((int)point.getX(), (int)point.getY()));

				locs.add(point);
			}
		}
		return locs;
	}
	/*public Vec2 findDirection(Vec2 node1, Vec2 node2) {
		
	}*/
	//inner class Line (2D) associated with Street to allow intersection calculations
	class Line {
		private Vec2 start;
		private Vec2 dir;
		public Line(Vec2 start, Vec2 dir) {
			this.setStart(start);
			this.setDir(dir);
		}
		
		public Vec2 findDir (Vec2 start, Vec2 end) {
			return end.minus(start);
		}
		public Vec2 getStart() {
			return start;
		}
		public void setStart(Vec2 start) {
			this.start = start;
		}
		public Vec2 getDir() {
			return dir;
		}
		public void setDir(Vec2 slope) {
			this.dir = slope;
		}
		public Intersection findIntersection(Line l) {
			//Two cases: 1. Same slope; 2. Not same slope
			if(this.dir == l.getDir())	
				return null;
			Vec2 dirTwo = l.getDir();
			Vec2 startTwo = l.getStart();
			return new Intersection(findPoint((startTwo.getX() - this.start.getX())/(this.dir.getX() -dirTwo.getX())));
		}
		//finds Vec2 (point) along line given a constant describing how far along line
		public Vec2 findPoint(double constant) {
			return (dir.scaledBy(constant)).plus(start);
		}
	}
	public String toString () {
		String s = "";
		for(Vec2 loc: nodes) {
			s += loc + " ";
		}
		return s;
	}
}
