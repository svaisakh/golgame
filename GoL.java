package com.convai;

import javax.swing.*;	// for the gui
import javax.swing.filechooser.FileNameExtensionFilter;	// to filter for gol files
import java.awt.*;	// for coloring
import javax.swing.event.MouseInputListener;	// for getting user input via mouse clicks
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelListener;	// for modifying speed
import java.awt.event.MouseWheelEvent;
import java.awt.event.KeyListener;	// for resizing cells
import java.awt.event.KeyEvent;
import java.io.*;	// for serialization

public class GoL {	// the main game class

	protected int frameWidth = 600; 	// holds the width of the frame in pixels
	protected int frameHeight = 600;	// holds the height of the frame in pixels
	protected double  rowSpacing = 30; 	// holds the spacing of each row in pixels
	protected double  columnSpacing = 30;	// holds the spacing of each column in pixels
	protected int numberOfRows = frameHeight/ (int)  rowSpacing;
	protected int numberOfColumns = frameWidth / (int)  columnSpacing;
	protected double fps = 0.2;		// the speed of the simulation (in FPS)
	protected JFrame frame;	// the main frame
	protected City city = new City((int) columnSpacing, (int) rowSpacing, numberOfRows, numberOfColumns);	// the city of cells within the game
	protected boolean gameActive = false;		// flag for whether the game should be in process
	protected boolean gameOver = true;	// tells if the game is over
	protected boolean gridOn = false;	// true if we want the grid on
	protected boolean isMonochrome = false;	// good ol' B&W if true
	protected boolean isCurvy = false;	// makes cells oval shaped if true

	public static void main(String[] args) {
		GoL game = new GoL();
		game.play();
	}

	protected void fullScreen() {	// toggles the game fullScreen
		if (frame.getExtendedState() != JFrame.MAXIMIZED_BOTH)	frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.dispose();
		frame.setUndecorated(!frame.isUndecorated());
		frame.setVisible(true);
	}

	/* Getter Methods */

	public double getFPS() {
		return fps;
	}

	/* Setter Methods */

	public void setFPS(double fps) {
		this.fps = fps;
	}

	/* Public methods */

	public void play() {	// the main funciton which does the execution
		buildGui();
		gameOver = false;	// game has just begun!
		while (!gameOver) {
			while (gameActive) {
				render();
			}
			try{Thread.sleep(1);} catch (Exception e) {}	// give a chance for others to execute
		}
	}

	protected void render() {	// renders the frame
		evolve();	// let the configuration evolve
		frame.repaint();	// repaint the new configuration
		try{
			Thread.sleep( (long) ( 1000 / getFPS() ) );	// sleep according to the FPS
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/* Protected methods */

	protected void buildGui() {	// builds the GUI part of the game
		frame = new JFrame("Convay's Game of Life");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GoLPanel panel = new GoLPanel();
		panel.addAllListeners();
		frame.getContentPane().add(BorderLayout.CENTER, panel);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setUndecorated(true);
		frame.setVisible(true);
	}

	protected void resizeCity(double rowResizeAmount, double columnResizeAmount) {	// resizes the city by the desired multipliers
		rowSpacing = rowSpacing * rowResizeAmount;
		columnSpacing = columnSpacing * columnResizeAmount;
		numberOfRows = frameHeight/ (int) rowSpacing;
		numberOfColumns = frameWidth / (int) columnSpacing;

		city.resize((int) columnSpacing, (int) rowSpacing, numberOfRows, numberOfColumns);
	}

	protected void evolve() {	// evolves the pattern according to the rules
		if (!gameActive)	return;
		
		City newCity = city.nextGeneration();
		if (newCity.equals(city))	gameActive = false;	// if the patern is static, why evolve further?
		city = newCity;
	}

	protected void clearCity() {	// clears the city
		city.kill();
	}

	protected void randomize() {	// randomly makes 20 % of the cells alive
		city.randomize(20);
	}

	protected void savePattern(File file) {	// saves the current pattern to a file
		try {
			FileOutputStream fileStream = new FileOutputStream(file);
			ObjectOutputStream os = new ObjectOutputStream(fileStream);
			os.writeObject(frameWidth);
			os.writeObject(frameHeight);
			os.writeObject(rowSpacing);
			os.writeObject(columnSpacing);
			os.writeObject(getFPS());
			os.writeObject(city);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	protected void loadPattern(File file) {	// loads a pattern from a file
		try {
			FileInputStream fileStream = new FileInputStream(file);
			ObjectInputStream os = new ObjectInputStream(fileStream);
			frameWidth = (int) os.readObject();
			frameHeight = (int) os.readObject();
			rowSpacing = (double) os.readObject();
			columnSpacing = (double) os.readObject();
			setFPS((double) os.readObject());
			city = (City) os.readObject();
			gameActive = false;
			frame.setSize(frameWidth, frameHeight);
			resizeCity(1, 1);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	

	public class GoLPanel extends JPanel {	// the panel upon which everything appears

		public void addAllListeners() {	// mainly to add listeners
			addMouseListener(new ClickListener());
			addMouseMotionListener(new ClickListener());
			addMouseWheelListener(new ScrollListener());
			addKeyListener(new KeyBoardListener());
			setFocusable(true);
			setFocusTraversalKeysEnabled(false);
		}

		public void paintComponent(Graphics g) {	// repaint calls this

			Graphics2D g2d = (Graphics2D) g;
		    	g2d.setRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));

			g2d.setColor(Color.white);
			g2d.fillRect(0, 0, getWidth(), getHeight());	// the background

			if (frame.getWidth() != frameWidth || frame.getHeight() != frameHeight) {	// if the frame is resized, resize the city accordingly
				frameWidth = frame.getWidth();
				frameHeight = frame.getHeight();
				resizeCity(1, 1);
			}

			if (gridOn) {	// draw the grid
				for (int i = 1; i < numberOfRows; i++) {	// draw all the row divider lines
					g2d.setColor(Color.black);
					g2d.drawLine(0, i*(int) rowSpacing, getWidth(), i*(int) rowSpacing);
				}

				for (int i = 1; i < numberOfColumns; i++) {	// draw all the column divider lines
					g2d.setColor(Color.black);
					g2d.drawLine(i*(int) columnSpacing, 0, i*(int) columnSpacing, getHeight());
				}
			}

			city.render(g, isCurvy, isMonochrome);
		}
	
	}

	public class ClickListener implements MouseInputListener {	// listens for mouse events

		public void mouseClicked(MouseEvent e) {		// the mouse is clicked
			if (e.getButton() == MouseEvent.BUTTON1){	// left click creates the cell
				int x = e.getX();
				int y = e.getY();
				int row = y / (int) rowSpacing;
				int column = x / (int) columnSpacing;
				city.cells[row][column].isAlive = true;
				frame.repaint();
			}
			if (e.getButton() == MouseEvent.BUTTON3){	// right click kills the cell
				int x = e.getX();
				int y = e.getY();
				int row = y / (int) rowSpacing;
				int column = x / (int) columnSpacing;
				city.cells[row][column].isAlive = false;
				frame.repaint();
			}
			if (e.getButton() == MouseEvent.BUTTON2){	// scroll button click toggles the game active
				gameActive = !gameActive;
			}
		}

		public void mouseDragged(MouseEvent e) {
			if (SwingUtilities.isLeftMouseButton(e)){	// left click creates the cell
				int x = e.getX();
				int y = e.getY();
				int row = y / (int) rowSpacing;
				int column = x / (int) columnSpacing;
				if (row >= numberOfRows || row < 0 || column >= numberOfColumns || column < 0)	return;
				city.cells[row][column].isAlive = true;
				frame.repaint();
			}
			if (SwingUtilities.isRightMouseButton(e)){	// right click kills the cell
				int x = e.getX();
				int y = e.getY();
				int row = y / (int) rowSpacing;
				int column = x / (int) columnSpacing;
				if (row >= numberOfRows || row < 0 || column >= numberOfColumns || column < 0)	return;
				city.cells[row][column].isAlive = false;
				frame.repaint();
			}
		}

		public void mouseEntered(MouseEvent e) {}

		public void mouseExited(MouseEvent e) {}

		public void mouseMoved(MouseEvent e) {}

		public void mousePressed(MouseEvent e) {}

		public void mouseReleased(MouseEvent e) {}

	}

	public class ScrollListener implements MouseWheelListener {	// scrolling up/down speeds up/down the game

		public void mouseWheelMoved(MouseWheelEvent e) {
			if (e.getWheelRotation() > 0) {
				if (getFPS() >= 0.2)	setFPS(getFPS() * 0.97);
			}
			else
				if (getFPS() <= 100)	setFPS(getFPS() * 1.03);
		}

	}

	public class KeyBoardListener implements KeyListener {	// all the keyboard commands
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_SPACE)	gameActive = !gameActive;
			if (e.getKeyCode() == KeyEvent.VK_G)	gridOn = !gridOn;
			if (e.getKeyCode() == KeyEvent.VK_C)	isMonochrome = !isMonochrome;
			if (e.getKeyCode() == KeyEvent.VK_N)	clearCity();
			if (e.getKeyCode() == KeyEvent.VK_R)	randomize();
			if (e.getKeyCode() == KeyEvent.VK_T)	isCurvy = !isCurvy;
			if (e.getKeyCode() == KeyEvent.VK_P)	city.isPeriodic = !city.isPeriodic;
			if (e.getKeyCode() == KeyEvent.VK_F11)	fullScreen();

			if (e.getKeyCode() == KeyEvent.VK_S)	{
				JFileChooser fileSave = new JFileChooser("Save Pattern");
				fileSave.setCurrentDirectory(new File(System.getProperty("user.dir")));
				fileSave.setFileFilter(new FileNameExtensionFilter("GoL Files", "gol"));
				fileSave.showSaveDialog(frame);
				if (!fileSave.getSelectedFile().getName().contains(".gol") ) savePattern(new File(fileSave.getSelectedFile().getName() + ".gol"));
				else	savePattern(fileSave.getSelectedFile());
			}

			if (e.getKeyCode() == KeyEvent.VK_O)	{
				JFileChooser fileOpen = new JFileChooser("Load Pattern");
				fileOpen.setCurrentDirectory(new File(System.getProperty("user.dir")));
				fileOpen.setFileFilter(new FileNameExtensionFilter("GoL Files", "gol"));
				fileOpen.showOpenDialog(frame);
				loadPattern(fileOpen.getSelectedFile());
			}

			if (e.getKeyCode() == KeyEvent.VK_ADD || e.getKeyCode() == KeyEvent.VK_PLUS || e.getKeyCode() == KeyEvent.VK_EQUALS)
				resizeCity(1.03, 1.03);
			else if (e.getKeyCode() == KeyEvent.VK_SUBTRACT || e.getKeyCode() == KeyEvent.VK_MINUS)
				resizeCity(0.97, 0.97);
			frame.repaint();
		}

		public void keyReleased(KeyEvent e) {}

		public void keyTyped(KeyEvent e) {}
	}


}