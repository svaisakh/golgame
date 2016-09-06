package com.convai;

import java.awt.*;	// for coloring
import java.io.*;	// for serialization

class Player implements Serializable {	// a controllable (by human or cpu)	player that controlls another cell in a city

	private double health = 100;	// the health of the player (ranges from 0 to 100)
	private int row;	// the row in which the player is in the city
	private int column;	// the column in which the player is in the city
	public boolean isAlive = true;	// indicates whether the Cell is alive
	public Color color = Color.black;	// the color of the cell

	Player(City city)	{	// makes a player object within a city (a player cannot exist alone)
		city.addPlayer(this);
	}

// Getter Methods

	public double getWidth(int cellWidth) {		// overrides the Cell's method to have a resizable width
		return health / 100.0 * (double) cellWidth;
	}

	public double getHeight(int cellHeight) {		// overrides the Cell's method to have a resizable height
		return health / 100.0 * (double) cellHeight;
	}


	public double getHealth() {		// returns the health. If the player is killed abruptly, the health is zero
		if (!isAlive)	setHealth(0);
		return health;
	}

	public int getRow() {		// returns the row in the city
		return row;
	}

	public int getColumn() {		// returns the column in the city
		return column;
	}

	public boolean isAlive() {
		if (health < 0)	return false;
		else	return true;
	}

// Setter Methods

	public void setRow(int row) {		// sets the row in the city
		this.row = row;
	}

	public void setColumn(int column) {		// sets the column in the city
		this.column = column;
	}

	public void setHealth(double health) {		// sets the player health between 0 and 100. Simultaneously toggles the isAlive flag if needed.
		if (health < 0)	{
			this.health = 0;
			isAlive = false;
		}
		else if (health <= 100) {
			this.health = health;
			isAlive = true;
		}
		else {
			this.health = 100;
			isAlive = true;
		}
	}

	public void isAlive(boolean isAlive) {
		if (!isAlive)	health = 0;
		this.isAlive = isAlive;
	}

// Other Methods

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

	public void render(Graphics g, boolean isCurvy, int cityCellWidth, int cityCellHeight) {	// renders the whole thing (If isCurvy, draws an oval instead of a rectangle)
		g.setColor(Color.white);
		g.fillRect(getColumn() * cityCellWidth, getRow() * cityCellHeight, cityCellWidth, cityCellHeight);

		g.setColor(color);
		if (isCurvy)	g.fillOval(getColumn() * cityCellWidth + (int)((cityCellWidth - getWidth(cityCellWidth)) / 2.0), getRow() * cityCellHeight +  (int)((cityCellHeight - getHeight(cityCellHeight)) / 2.0), (int)getWidth(cityCellWidth), (int)getHeight(cityCellHeight));
		else	g.fillRect(getColumn() * cityCellWidth + (int)((cityCellWidth - getWidth(cityCellWidth)) / 2.0), getRow() * cityCellHeight +  (int)((cityCellHeight - getHeight(cityCellHeight)) / 2.0), (int)getWidth(cityCellWidth), (int)getHeight(cityCellHeight));
	}

	public boolean move(City city, int changeInX, int changeInY) {	// moves the player according to the parameters (eg. move(0, 1) moves the player down, move(-1, -1) moves it top-left). Returns true if moved successfully
		if (!isAlive)	return false;	// obviously, a dead player can't move
		if (getColumn() + changeInX < 0 || getColumn() + changeInX >= city.numberOfColumns() || getRow() + changeInY < 0 || getRow() + changeInY >= city.numberOfRows())
			return false;	// out of bounds
		if (city.cells[getRow() + changeInY][getColumn() + changeInX].isAlive)	return false;	// if neighbour is alive, don't move

		city.cells[getRow() + changeInY][getColumn() + changeInX].isAlive = true;	// move it
		city.cells[getRow()][getColumn()] = new Cell(city.cellWidth(), city.cellHeight());	// assign a new dead cell to the space left behind
		this.row = getRow() + changeInY;
		this.column = getColumn() + changeInX;
		return true;	// moved successfully
	}

}