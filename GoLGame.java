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

class GoLGame extends GoL {

	private Player player;
	private double refreshBarWidth = frameWidth / 2.0;

	public static void main(String[] args) {
		GoLGame game = new GoLGame();
		game.play();
	}

	public void setFPS(double fps) {	// we need to repaint since the label at the top changes
		super.setFPS(fps);
		frame.repaint();
	}

	protected void buildGui() {	// builds the GUI part of the game
		frame = new JFrame("GOL Game");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GoLPanel panel = new GoLPanel();
		panel.addAllListeners();
		frame.getContentPane().add(BorderLayout.CENTER, panel);
		frame.getContentPane().add(BorderLayout.NORTH, new RefreshBarPanel());
		frame.setSize(800, 800);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setUndecorated(true);
		frame.setVisible(true);
		gridOn = false;
		isCurvy = true;
		setFPS(0.2);
		resizeCity(5, 5);
		player = new Player(city);
		clearCity();
	}

	protected void evolve() {
		super.evolve();
		if (!player.isAlive())		gameActive = false;
	}

	protected void clearCity() {
		super.clearCity();
		player.setHealth(100);
		frame.repaint();
	}

	protected void randomize() {
		super.randomize();
		city.cells[player.getRow()][player.getColumn()].isAlive = true;
		player.setHealth(100);
	}

	protected void render() {
		evolve();	// let the configuration evolve
		frame.repaint();	// repaint the new configuration
		repaintRefreshBar();
	}

	protected void resizeCity(double rowResizeAmount, double columnResizeAmount) {
		super.resizeCity(rowResizeAmount, columnResizeAmount);
		refreshBarWidth = frameWidth / 2.0;
	}

	private void repaintRefreshBar() {	// repaints the refresh bar
		while (refreshBarWidth  > frameWidth / 2000.0) {
			refreshBarWidth -= frameWidth / 2000.0;
			frame.repaint();
			try{
				Thread.sleep( (long) ( 1 / getFPS() ) );	// sleep according to the FPS
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		refreshBarWidth = frameWidth/2.0;
	}

	public class GoLPanel extends GoL.GoLPanel {	// the panel upon which everything appears

		GoLPanel() {
			setOpaque(false);
		}

		public void addAllListeners() {
			addMouseListener(new ClickListener());
			addMouseMotionListener(new ClickListener());
			addMouseWheelListener(new ScrollListener());
			addKeyListener(new KeyBoardListener());
			setFocusable(true);
			setFocusTraversalKeysEnabled(false);
		}

		public void paintComponent(Graphics g) {	// draws a heart for the current player
			super.paintComponent(g);

			if (!gameActive)	return;

			Graphics2D g2d = (Graphics2D) g;
			g2d.setRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));

			int numberOfNeighbours = city.countAliveNeighbours(player.getRow(), player.getColumn());

			if (numberOfNeighbours == 2 || numberOfNeighbours == 3)	player.setHealth(player.getHealth() + 1.25 * getFPS());
		    	else	player.setHealth(player.getHealth() - 0.05 / getFPS());

			player.colourCell(numberOfNeighbours, isMonochrome);
			player.render(g, isCurvy, city.cellWidth(), city.cellHeight());

		}
	
	}

	public class KeyBoardListener extends GoL.KeyBoardListener{

		public void keyPressed(KeyEvent e) {
			super.keyPressed(e);
			if (e.getKeyCode() == KeyEvent.VK_LEFT)	player.move(city, -1, 0);
			if (e.getKeyCode() == KeyEvent.VK_RIGHT)	player.move(city, 1, 0);
			if (e.getKeyCode() == KeyEvent.VK_UP)	player.move(city, 0, -1);
			if (e.getKeyCode() == KeyEvent.VK_DOWN)	player.move(city, 0, 1);
			frame.repaint();
		}

	}

	public class ScrollListener extends GoL.ScrollListener {	// scrolling up/down speeds up/down the game

		public void mouseWheelMoved(MouseWheelEvent e) {
			if (e.getWheelRotation() > 0) {
				if (getFPS() >= 0.2)	setFPS(getFPS() * 0.99);
			}
			else
				if (getFPS() <= 0.9)	setFPS(getFPS() * 1.01);
		}

	}

	private class RefreshBarPanel extends JPanel {

		public void paintComponent(Graphics g) {
			Graphics2D g2d = (Graphics2D) g;
			g2d.setRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
			if (!gameActive)	return;
			g2d.setColor(player.color);
			g2d.fillRect((int) (getWidth() * 0.25), (int) (getHeight() * 0.25), (int) refreshBarWidth, (int) (getHeight() * 0.5));
		}

	}

	private class RefreshBarShortener implements Runnable{

		public void run() {
			repaintRefreshBar();
		}

	}

}