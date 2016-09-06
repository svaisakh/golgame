package com.convai;

import java.awt.*;	// for coloring
import java.io.*;	// for serialization

public class Cell implements Serializable{	// a Cell organism

	protected int width;	// the pixel width
	protected int height;	// the pixel height
	public boolean isAlive = false;	// indicates whether the Cell is alive
	public Color color = Color.black;	// the color of the cell

	public Cell(int width, int height) {	// constructs a Cell with certain dimensions
		this.width = width;
		this.height = height;
	}

	/* Getter Methods */

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public void colourCell(int numberOfNeighbours, boolean isMonochrome) {
		if (isMonochrome) {	// if B&W
			if (isAlive)	color = Color.black;
			else	color = Color.white;
		}
		
		else {
			if (isAlive) {	// shade RGB
				if (numberOfNeighbours > 3)	color = Color.red;
				else if (numberOfNeighbours < 2)	color = Color.blue;
				else	color = Color.green;
			}
			else {
				if (numberOfNeighbours == 3)	color = new Color(250, 250, 250);
				else	color = Color.white;
			}
		}
	}

	public void render(City city, Graphics g, boolean isCurvy, int i, int j) {	// renders the whole thing at row i, column j of a city with this cell (If isCurvy, draws an oval instead of a rectangle)
		g.setColor(color);
		if (isCurvy)	g.fillOval(j*(int) city.cellWidth(), i*(int) city.cellHeight(), (int) getWidth(), (int) getHeight());
		else	g.fillRect(j*(int) city.cellWidth(), i*(int) city.cellHeight(), (int) getWidth(), (int) getHeight());
	}

}