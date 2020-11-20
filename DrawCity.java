
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
@SuppressWarnings("serial")
public class DrawCity extends JFrame {
	private DrawCanvas canvas;
	private final int CANVAS_WIDTH = 800;
	private final int CANVAS_HEIGHT = 600;
	private boolean buildingPressed;
	private boolean startStreetPressed, endStreetPressed;
	private Container cp;
	private final Rectangle[][] squares = new Rectangle[150][200];
	//3:4
	public DrawCity () {
		JPanel btnPanel = new JPanel(new FlowLayout());
		JButton buildingBtn = new JButton("Building");
		btnPanel.add(buildingBtn);
		buildingBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				canvas.isFirstClicked = false;
				canvas.isSecondClicked = false;
				
				buildingPressed = !buildingPressed;
				startStreetPressed = false;
				canvas.repaint();
			}
		});
		JButton startStreet = new JButton("Start Street");
		btnPanel.add(startStreet);
		startStreet.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				canvas.isFirstClicked = false;
				canvas.isSecondClicked = false;
				startStreetPressed = !startStreetPressed;
				buildingPressed = false;
				endStreetPressed = false;
				
				
			}
		});
		JButton endStreet = new JButton("End Street");
		btnPanel.add(endStreet);
		endStreet.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				canvas.isFirstClicked = false;
				canvas.isSecondClicked = false;
				endStreetPressed = !endStreetPressed;
				buildingPressed = false;
				startStreetPressed = false;
				canvas.repaint();
			}
		});
		JButton undo = new JButton("Undo");
		btnPanel.add(undo);
		undo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				canvas.isSecondClicked = false;
				canvas.isFirstClicked = false;
				canvas.fresh = true;
				endStreetPressed = false;
				buildingPressed = false;
				startStreetPressed = false;
				canvas.undoLatestAction();
			}
		});
		//Set up a custom drawing JPanel
		canvas = new DrawCanvas();
		
		cp = getContentPane();
		cp.setLayout(new BorderLayout());
		cp.add(canvas, BorderLayout.CENTER);
		cp.add(btnPanel, BorderLayout.SOUTH);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		setTitle("Draw City");
		pack();
		setVisible(true);
		
		int standWidth = CANVAS_WIDTH/squares[0].length;
		int standHeight = CANVAS_HEIGHT/squares.length;
		for(int row = 0; row < squares.length; row++) {
			for(int col = 0; col < squares[0].length; col++) {
				squares[row][col] = new Rectangle(new Vec2(col*standWidth, row*standHeight), standWidth, standHeight);
			}
		}
	}
	
	class DrawCanvas extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener{
		private PrintWriter printWriter;
		
		private boolean isOpen;
		private Street tempStreet;
		private ArrayList<Street> streets = new ArrayList<Street>();
		private ArrayList<Vec2> tempStreetLocs = new ArrayList<Vec2>();
		ArrayList<Vec2> previousNodes = new ArrayList<Vec2>();
		
		private ArrayList<Building> buildings = new ArrayList<Building>();
		private ArrayList<Vec2> tempBuildingLocs = new ArrayList<Vec2>();
		
		private ArrayList<Intersection> intersections = new ArrayList<Intersection>();
		private Vec2 curLoc = new Vec2(-1, -1);
		private Vec2 scrollLoc = new Vec2(0, 0);
		private Vec2 shift = new Vec2(0, 0);
		private Vec2 end = new Vec2(0, 0);
		private Vec2 firstClick; 
		private Vec2 secondClick; 
		private double zoomFactor = 0.0;
		private boolean isFirstClicked, isSecondClicked;
		private boolean fresh = true;
		private String latestObj;
		
		private final Color BUILDING_COLOR = Color.DARK_GRAY;
		private final Color STREET_COLOR = new Color(140, 0, 255);
		private Color TEMP_STREET_COLOR = new Color(180, 180, 255);
		private final Color TEMP_BUILDING_COLOR = Color.LIGHT_GRAY;
		//boolean used to ensure that firstClick is not changed more than once before secondClick is found and the rectangle drawn
		private Mat33 oldTransform = new Mat33();
		private Mat33 currentTransform = new Mat33();
		public DrawCanvas() {
			addMouseMotionListener(this);
			addMouseListener(this);
			addMouseWheelListener(this);
		}
		@Override
		public void paintComponent(Graphics g) {
			Graphics2D g2d = (Graphics2D) g;
			g2d.setColor(Color.BLACK);
			g2d.fillRect(0, 0,  CANVAS_WIDTH,  CANVAS_HEIGHT);
			Vec3 scrollLocMod = scrollLoc.convertTo3D();
			/*
			Steps for zoom: 
			 * 1. Translate the mouse loc at time of zoom to origin
			 * 2. Scale the Rectangle by scaling the left corner, width, and height
			 * 3. Translate back to original mouse loc
			 * Through Matrices
			 */
			float scalar = (float)Math.pow(2, zoomFactor);
			currentTransform = Mat33.findTransMat(shift.convertTo3D().getNeg()).multiply(Mat33.findTransMat(scrollLocMod.getNeg()).multiply(Mat33.findScaleMat(scalar).multiply((Mat33.findTransMat(scrollLocMod).multiply(oldTransform)))));
			for(int row = 0; row < squares.length; row++) {
				for(int col = 0; col < squares[0].length; col++) {
					Rectangle s = squares[row][col];
					Vec2[] points = s.getPoints();
					for(int i = 0; i < 4; i++) {
						points[i] = currentTransform.multipliedBy(points[i].convertTo3D()).trim();
					}
					s = new Rectangle(points[0], (points[1].getX() - points[0].getX()), (points[2].getY()-points[0].getY()), s.getColor());
					
					int xStart = (int)s.getLoc().getX();
					int yStart = (int)s.getLoc().getY();
					if (endStreetPressed) {
						removeTemporaryStreetColor();
						streets.add(tempStreet);
						tempStreet = null;
						
						isFirstClicked = false;
						isSecondClicked = false;
						fresh = true;
						endStreetPressed = false;
					}
					//(OnCanvas) - allows inner code to run when given square is on the canvas
					if(xStart <= CANVAS_WIDTH && yStart <= CANVAS_HEIGHT && xStart >= -s.getWidth() && yStart >= -s.getHeight()) {
						if(curLoc.getX() >= xStart && curLoc.getX() < xStart + s.getWidth() && curLoc.getY() >= yStart && curLoc.getY() < yStart + s.getHeight()) {
							s.setColor(Color.RED);							
							if(buildingPressed) {
								if(isFirstClicked && !isSecondClicked && fresh) {
									firstClick = new Vec2(col, row);
									fresh = false;
								}
								else if (isFirstClicked && !isSecondClicked && !fresh) {
									//refreshes building image based on changes to mouse location
									removeTemporaryBuildingColor();
									applyTemporaryBuildingColor(new Building(firstClick, new Vec2(col, row), Color.LIGHT_GRAY));
								}
								else if (isFirstClicked && isSecondClicked){
									secondClick = new Vec2(col, row);
									Building b = new Building(firstClick, secondClick, Color.GRAY);
									buildings.add(b);
									applyBuildingColor(b);
									isFirstClicked = false;
									isSecondClicked = false;
									fresh = true;
								}
							}
							else if (startStreetPressed) {
								//initialize tempStreet when button is pressed
								if(isFirstClicked && !isSecondClicked && fresh) {
									firstClick = new Vec2(col, row);
									tempStreet = new Street(firstClick);
									fresh = false;
								}
								else if(isFirstClicked && !isSecondClicked){
									removeTemporaryStreetColor();									
									applyTemporaryStreetColor(tempStreet, tempStreet.getNodes().get(tempStreet.getNodes().size()-1), new Vec2(col, row));
								}
								else if (isFirstClicked && isSecondClicked){
									secondClick = new Vec2(col, row);
									tempStreet.addNode(secondClick);
									applyStreetColor(tempStreet, tempStreet.getNodes().get(tempStreet.getNodes().size()-2), secondClick);
									isSecondClicked = false;
								}
							}
						}
						g2d.setColor(s.getColor());
						if((int)s.getWidth() != 1)
							g2d.fillRect(xStart +1, yStart+1, (int)s.getWidth()-1, (int)s.getHeight()-1);
						else g2d.fillRect(xStart, yStart, (int)s.getWidth(), (int)s.getHeight());
					}
				}
			}
			oldTransform = currentTransform;
			zoomFactor = 0;
			shift = new Vec2(0, 0);
			
		}
		//the handy dandy undo implementation
		public void undoLatestAction() {
			if(latestObj.equals("Street")) {
				removeTemporaryStreetColor();
				for(Vec2 v: previousNodes) {
					squares[(int)v.getY()][(int)v.getX()].setColor(Color.white);
				}
				System.out.println(tempStreet);
				tempStreet.removeNode();
			}
			else {
				removeTemporaryBuildingColor();
				removeBuildingColor(buildings.get(buildings.size()-1));
				buildings.remove(buildings.size()-1);
			}
			
			canvas.repaint();
		}
		//building related methods
		private void addBuildings() {
			Building b = new Building();
			applyBuildingColor(b);
		}
		public void applyBuildingColor (Building b) {
			for(int y = (int)b.getLoc().getY(); y <= (int)(b.getLoc().getY() + b.getHeight()); y++) {
				for(int x = (int)b.getLoc().getX(); x <= (int)(b.getLoc().getX() + b.getWidth()); x++) {
					//if(squares[y][x].getColor() == Color.LIGHT_GRAY)
						squares[y][x].setColor(BUILDING_COLOR);
				}
			}
			latestObj = "Building";
			canvas.repaint();
		}
		//shows "pseudo-building" so user can ensure that he adds the building he wants
		public void applyTemporaryBuildingColor (Building b) {
			for(int y = (int)b.getLoc().getY(); y <= (int)(b.getLoc().getY() + b.getHeight()); y++) {
				for(int x = (int)b.getLoc().getX(); x <= (int)(b.getLoc().getX() + b.getWidth()); x++) {
					if(squares[y][x].getColor() == Color.WHITE) {
						squares[y][x].setColor(TEMP_BUILDING_COLOR);
						tempBuildingLocs.add(new Vec2(x, y));
					}
				}
			}
			
		}
		public void removeTemporaryBuildingColor() {
			for(Vec2 v: tempBuildingLocs) {
				if(squares[(int)v.getY()][(int)v.getX()].getColor() == TEMP_BUILDING_COLOR)
					squares[(int)v.getY()][(int)v.getX()].setColor(Color.WHITE);
			}
			this.repaint();
		}
		public void removeBuildingColor(Building b) {
			for(int y = (int)b.getLoc().getY(); y <= (int)(b.getLoc().getY() + b.getHeight()); y++) {
				for(int x = (int)b.getLoc().getX(); x <= (int)(b.getLoc().getX() + b.getWidth()); x++) {
					if(squares[y][x].getColor() == BUILDING_COLOR) {
						squares[y][x].setColor(Color.white);
					}
				}
			}
		}
		
		//FLAW: Multiple connections between two line segments will create future confusion when a single car is traveling
		public void applyStreetColor(Street s, Vec2 start, Vec2 end) {
			ArrayList<Vec2> toBeColored = s.findColoredLocsBetweenNodes(start, end);
			Vec2 previous = toBeColored.get(0);
			//prevents multiple "intersections" from being performed due to multiple overlaps in surrounding colored squares
			boolean reached = false;
			int counter = 0;
			//counter used to set reached back to false so that multiple intersections can be detected along a single line
			ArrayList<Vec2> curColored = new ArrayList<Vec2>();
			//curColored keeps track of locations colored between the two nodes start and end
			for(int i = 0; i < toBeColored.size(); i++) {
				Vec2 loc = toBeColored.get(i);
				int x = (int)loc.getX();
				int y = (int)loc.getY();
				Vec2 surrounding = checkSurroundingForColor(x, y, STREET_COLOR);
				
				boolean isPartOfPrevious = false;
				for(Vec2 prev: previousNodes) {
					System.out.println(surrounding);
					if(surrounding != null && surrounding.equals(prev))
						isPartOfPrevious = true;
				}
				if(surrounding != null && !isPartOfPrevious &&(surrounding.getX() != previous.getX() || surrounding.getY() != previous.getY()) && !reached) {
					//FLAW 2: Every surrounding square that is colored is considered an intersection.
					//POSSIBLE FIX: Any perceived intersection (a surrounding square colored) must not be from the previous two nodes (two lines can't intersect at more than once place)
					intersections.add(new Intersection(x, y));
					reached = true;
					counter = 0;
				}
				if(counter > 3)
					reached = false;
				//prevents line from being "broken" when undo button is pressed for intersecting lines
				if(squares[y][x].getColor() != STREET_COLOR)
					curColored.add(new Vec2(x, y));
				squares[y][x].setColor(STREET_COLOR);
				previous = new Vec2(x, y);
				counter++;
			}
			previousNodes = curColored;
			latestObj = "Street";
		}
		public Vec2 checkSurroundingForColor(int x, int y, Color c) {
			
			//fails to account for border (out of bounds but oh well for now)
			for(int i = -1; i < 2; i++) {
				for(int j = -1; j < 2; j++) {
					if(squares[y+i][x+j].getColor() == c)
						return new Vec2(x+j, y+i);
				}
			}
			return null;
		}
		/*
		public void applyStreetColor(Street s) {
			//shoot lines from direction determined from nodes at index i and i+1
			//squares that are intercepted by the line are colored
			//assume looping through all nodes and two are acquired
			for(Vec2 loc: s.findColoredLocs()) {
				//add to intersection list if the square at loc already forms part of a street
				//problem is that it is reprinting over and over even if it does not need to
				if(squares[(int)loc.getY()][(int)loc.getX()].getColor() == STREET_COLOR)
					intersections.add(new Intersection((int)loc.getX(), (int)loc.getY()));
				squares[(int)loc.getY()][(int)loc.getX()].setColor(STREET_COLOR);
			}
			this.repaint();
		}
		*/
		public void applyTemporaryStreetColor(Street s, Vec2 startNode, Vec2 endNode) {
			//Street s is really just tempStreet
			for (Vec2 loc: s.findColoredLocsBetweenNodes(startNode, endNode)) {
				if(squares[(int)loc.getY()][(int)loc.getX()].getColor() == Color.WHITE) {
					squares[(int)loc.getY()][(int)loc.getX()].setColor(TEMP_STREET_COLOR);
					tempStreetLocs.add(new Vec2((int)loc.getX(), (int)loc.getY()));
				}
			}
		}
		public void removeTemporaryStreetColor() {
			for(Vec2 v: tempStreetLocs) {
				if(squares[(int)v.getY()][(int)v.getX()].getColor() == TEMP_STREET_COLOR)
					squares[(int)v.getY()][(int)v.getX()].setColor(Color.WHITE);
			}
		}
		@Override
		public void mouseDragged(MouseEvent e) {
			end = new Vec2(e.getX(), e.getY());
			shift = shift.plus(end.minus(curLoc));
			curLoc = end;
			this.repaint();
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			curLoc = new Vec2(e.getX(), e.getY());
			this.repaint();
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			curLoc = new Vec2(e.getX(), e.getY());
			if(!isFirstClicked)
				isFirstClicked = true;
			else {
				isSecondClicked = true;
			}
			this.repaint();

		}

		@Override
		public void mouseEntered(MouseEvent e) {
			// TODO Auto-generated method stub
		}

		@Override
		public void mouseExited(MouseEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mousePressed(MouseEvent e) {
			// TODO Auto-generated method stub
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			// TODO Auto-generated method stub
		}

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			double rotation = e.getPreciseWheelRotation();
			scrollLoc = new Vec2(e.getX(), e.getY());
			zoomFactor = -rotation;
			this.repaint();

		}
		public Dimension getPreferredSize() {
			return new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT);
		}
		public String getLatestObject () {
			return this.latestObj;
		}
		
	}
	
	public static void main(String[] args) {
		//Run GUI on the Event-Dispatcher Thread for thread safety
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new DrawCity();
			}
		});
		
	}

}
