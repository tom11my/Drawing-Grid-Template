
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
				//System.out.println("building button pressed");
				//nullify previous clicks
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
				//System.out.println("road button pressed");
				//nullify previous clicks
				canvas.isFirstClicked = false;
				canvas.isSecondClicked = false;
				//create a button for user to terminate a street instance
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
				//System.out.println("road button pressed");
				//nullify previous clicks
				canvas.isFirstClicked = false;
				canvas.isSecondClicked = false;
				//create a button for user to terminate a street instance
				endStreetPressed = !endStreetPressed;
				buildingPressed = false;
				startStreetPressed = false;
				canvas.repaint();
			}
		});
		//Set up a custom drawing JPanel
		canvas = new DrawCanvas();
		//canvas.setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));
		
		//Add both panels to this JFrame's content-pane
		cp = getContentPane();
		cp.setLayout(new BorderLayout());
		cp.add(canvas, BorderLayout.CENTER);
		cp.add(btnPanel, BorderLayout.SOUTH);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		setTitle("Draw City");
		pack();
		setVisible(true);
		
		//set up grid of locations
		int standWidth = CANVAS_WIDTH/squares[0].length;
		int standHeight = CANVAS_HEIGHT/squares.length;
		for(int row = 0; row < squares.length; row++) {
			for(int col = 0; col < squares[0].length; col++) {
				//preserves x y formatting
				squares[row][col] = new Rectangle(new Vec2(col*standWidth, row*standHeight), standWidth, standHeight);
			}
		}
	}
	
	//Define inner class DrawCanvas
	class DrawCanvas extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener{
		private PrintWriter printWriter;
		
		private boolean isOpen;
		private Street tempStreet;
		private ArrayList<Street> streets = new ArrayList<Street>();
		private ArrayList<Vec2> tempStreetLocs = new ArrayList<Vec2>();
		
		private ArrayList<Building> buildings = new ArrayList<Building>();
		private ArrayList<Vec2> tempBuildingLocs = new ArrayList<Vec2>();
		
		private Vec2 curLoc = new Vec2(-1, -1);
		private Vec2 scrollLoc = new Vec2(0, 0);
		private Vec2 shift = new Vec2(0, 0);
		private Vec2 end = new Vec2(0, 0);
		private Vec2 firstClick; 
		private Vec2 secondClick; 
		private double zoomFactor = 0.0;
		private boolean isFirstClicked, isSecondClicked;
		private boolean fresh = true;
		
		private final Color BUILDING_COLOR = Color.DARK_GRAY;
		private final Color STREET_COLOR = new Color(140, 0, 255);
		private final Color TEMP_STREET_COLOR = new Color(180, 180, 255);
		private final Color TEMP_BUILDING_COLOR = Color.LIGHT_GRAY;
		//new Color(0, 144, 201);
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
			//super.paintComponent(g);
			Graphics2D g2d = (Graphics2D) g;
			//blank screen each time repaint() is internally called
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
			//set up currentTransform to apply on each Rectangle
			//Because zoomFactor is most often 1.0 or -1.0, x^zoomFactor where x > 1 is a logical scalar
			//LIMITATION: For any x where x is not a whole number, x^zoomFactor creates unequal spaces between squares
			//NOTE: currentTransform is multiplied by oldTransform here
			float scalar = (float)Math.pow(2, zoomFactor);
			currentTransform = Mat33.findTransMat(shift.convertTo3D().getNeg()).multiply(Mat33.findTransMat(scrollLocMod.getNeg()).multiply(Mat33.findScaleMat(scalar).multiply((Mat33.findTransMat(scrollLocMod).multiply(oldTransform)))));
			for(int row = 0; row < squares.length; row++) {
				for(int col = 0; col < squares[0].length; col++) {
					Rectangle s = squares[row][col];
					//Transforming each individual point that makes up the Rectangle
					Vec2[] points = s.getPoints();
					for(int i = 0; i < 4; i++) {
						points[i] = currentTransform.multipliedBy(points[i].convertTo3D()).trim();
					}
					s = new Rectangle(points[0], (points[1].getX() - points[0].getX()), (points[2].getY()-points[0].getY()), s.getColor());
					
					//left top corner of Rectangle
					int xStart = (int)s.getLoc().getX();
					int yStart = (int)s.getLoc().getY();
					//must go outside the "onCanvas" loop below to allow real-time updates not based on mouseLoc
					if (endStreetPressed) {
						//startStreetPressed goes to false in action listener
						System.out.println("remove attempted");
						removeTemporaryStreetColor();
						streets.add(tempStreet);
						//reset initial conditions
						tempStreet = null;
						isFirstClicked = false;
						isSecondClicked = false;
						fresh = true;
						//prevents excessive strain by only running once
						endStreetPressed = false;
					}
					//(OnCanvas) - allows inner code to run when given square is on the canvas
					if(xStart <= CANVAS_WIDTH && yStart <= CANVAS_HEIGHT && xStart >= -s.getWidth() && yStart >= -s.getHeight()) {
						if(curLoc.getX() >= xStart && curLoc.getX() < xStart + s.getWidth() && curLoc.getY() >= yStart && curLoc.getY() < yStart + s.getHeight()) {
							s.setColor(Color.RED);
							//tracks clicks
							
							//creates building and applies its color based on clicks
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
									//refreshes street image based on changes to mouse location
									//code here
									removeTemporaryStreetColor();
									/*ArrayList<Vec2> clonedList = (ArrayList<Vec2>)tempStreet.getNodes().clone();
									clonedList.add(new Vec2(col, row));
									Street cloned = new Street(clonedList);*/
									
									applyTemporaryStreetColor(tempStreet, tempStreet.getNodes().get(tempStreet.getNodes().size()-1), new Vec2(col, row));
								}
								else if (isFirstClicked && isSecondClicked){
									secondClick = new Vec2(col, row);
									tempStreet.addNode(secondClick);
									applyStreetColor(tempStreet);
									isSecondClicked = false;
								}
							}
						}
						g2d.setColor(s.getColor());
						//grid lines appear by shortening the borders of all Rectangles as they are printed
						//logic accounts for zoom situation where all squares are 1 X 1 pixels
						if((int)s.getWidth() != 1)
							g2d.fillRect(xStart +1, yStart+1, (int)s.getWidth()-1, (int)s.getHeight()-1);
						else g2d.fillRect(xStart, yStart, (int)s.getWidth(), (int)s.getHeight());
					}
				}
			}
			//set current transform to old transform for the next cycle
			oldTransform = currentTransform;
			//set variables applied to transform to 0
			zoomFactor = 0;
			shift = new Vec2(0, 0);
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
		//street related methods
		public void applyStreetColor(Street s) {
			//shoot lines from direction determined from nodes at index i and i+1
			//squares that are intercepted by the line are colored
			//assume looping through all nodes and two are acquired
			for(Vec2 loc: s.findColoredLocs()) {
				squares[(int)loc.getY()][(int)loc.getX()].setColor(STREET_COLOR);
			}
			this.repaint();
		}
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
			// TODO Auto-generated method stub
			end = new Vec2(e.getX(), e.getY());
			//"plus" mitigates problem that mouseDragged is called many times before repaint runs (causing unintended skipping of space)
			shift = shift.plus(end.minus(curLoc));
			curLoc = end;
			this.repaint();
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			// TODO Auto-generated method stub
			curLoc = new Vec2(e.getX(), e.getY());
			this.repaint();
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			// TODO Auto-generated method stub		
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
			//Will most often be either -1.0 or 1.0
			double rotation = e.getPreciseWheelRotation();
			
			scrollLoc = new Vec2(e.getX(), e.getY());
			

			//zoomFactor = Math.max(0, zoomFactor-rotation*0.5);
			zoomFactor = -rotation;
			this.repaint();

		}
		public Dimension getPreferredSize() {
			return new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT);
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
		
		//new DrawCity();
	}

}
