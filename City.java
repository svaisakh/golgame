package com.convai;

import java.io.*;	// for serialization
import java.util.*;	// for the Arrays utility class
import java.awt.*;	// for coloring

public class City implements Serializable {	// a City canvas/grid in which the Cells reside (really a collection of Cells)

	static final long serialVersionUID = 7861917293099169103L;
	public boolean isPeriodic = false;	// wraps the City to itself to form a periodic space with no edges
	public Cell[][] cells;	// the Cell array
	private int cellWidth;
	private int cellHeight;

	City(int width, int height, int numberOfRows, int numberOfColumns) {	// constructs a city with a certain number of avenues and streets and puts in Cells with given dimensions
		cells = newCells(width, height, numberOfRows, numberOfColumns);
		cellWidth = width;
		cellHeight = height;
	}

	public int cellWidth() {
		return cellWidth;
	}

	public int cellHeight() {
		return cellHeight;
	}

	public int numberOfRows() {
		return cells.length;
	}

	public int numberOfColumns() {
		return cells[0].length;
	}

	public int getWidth() {
		return cellWidth() * numberOfColumns();
	}

	public int getHeight() {
		return cellHeight() * numberOfRows();
	}

	public int getRow(Cell cell) {	// returns the row of the cell within this city
		int index = -1;
		for (int i = 0; i < numberOfRows(); i++) {
			index = Arrays.asList(cells[i]).indexOf(cell);
			if (index != -1)	return i;
		}
		return -1;
	}

	public int getColumn(Cell cell) {	// returns the column of the cell within this city
		return Arrays.asList(cells[getRow(cell)]).indexOf(cell);
	}

	public int getNumberOfCells() {	// returns the number of cells
		if (numberOfRows() == 0)	return 0;
		return numberOfRows() * numberOfColumns();
	}

	public int getNumberOfLiveCells() {	// returns the number of cells that are alive
		int result = 0;
		for (int i = 0; i < numberOfRows(); i++)
			for (int j = 0; j < numberOfColumns(); j++)	
				if (cells[i][j].isAlive)	result++;

		return result;
	}

	public int getNumberOfDeadCells() {	// returns the number of cells that are dead
		return getNumberOfCells() - getNumberOfLiveCells();
	}

	private Cell[][] newCells(int width, int height, int numberOfRows, int numberOfColumns) {	// makes new cells
		Cell[][] result = new Cell[numberOfRows][numberOfColumns];
		for (int i = 0; i < numberOfRows; i++)
			for (int j = 0; j < numberOfColumns; j++)
				result[i][j] = new Cell(width, height);

		return result;
	}

	public void resize(int width, int height, int numberOfRows, int numberOfColumns) {	// resizes the cells
		this.cellWidth = width;
		this.cellHeight = height;
		Cell[][] result = newCells(width, height, numberOfRows, numberOfColumns);
		for (int i = 0; i < Math.min(numberOfRows, numberOfRows()); i++)
			for (int j = 0; j < Math.min(numberOfColumns, numberOfColumns()); j++)
				result[i][j].isAlive = this.cells[i][j].isAlive;

		this.cells = result; 
	}

	public void kill() {	// kills all residents
		for (int i = 0; i < numberOfRows(); i++)
			for (int j = 0; j < numberOfColumns(); j++)	cells[i][j].isAlive = false;
	}

	public void randomize(double percentChanceOfLife) {	// randomly gives life to cells based on the percent chance parameter (eg. if it is 10, then 10% of all cells are likely to be alive)
		kill();
		for (int i = 0; i < numberOfRows(); i++)
			for (int j = 0; j < numberOfColumns(); j++)	
				if (Math.random() < percentChanceOfLife * Math.random() / 100.0)	cells[i][j].isAlive = true;
	}

	public City nextGeneration() {	// returns the next generation based on Convay's rules for the Game of Life
		City result = new City(this.cellWidth(), this.cellHeight(), this.numberOfRows(), this.numberOfColumns());	// make the new city
		result.isPeriodic = this.isPeriodic;

		int numberOfNeighbours;
		for (int i = 0; i <  this.numberOfRows(); i++) 
			for (int j = 0; j <  this.numberOfColumns(); j++) {
				numberOfNeighbours = this.countAliveNeighbours(i, j);
				if (numberOfNeighbours == 3)	result.cells[i][j].isAlive = true;
				else if (numberOfNeighbours < 2 || numberOfNeighbours > 3)	result.cells[i][j].isAlive = false;
				else result.cells[i][j].isAlive = this.cells[i][j].isAlive;
			}

		return result;
	}

	public boolean equals(City city) {	// returns true if the city is identical to this (doesn't count periodicity)
		if (city.numberOfRows() != this.numberOfRows() || city.numberOfColumns() != this.numberOfColumns())	return false;

		for (int i = 0; i <  this.numberOfRows(); i++) 
			for (int j = 0; j <  this.numberOfColumns(); j++)
				if (this.cells[i][j].isAlive != city.cells[i][j].isAlive)	return false;

		return true;
	}

	public Cell[] neighbours(int row, int column) {	// returns all the neighbours of a Cell in a particular row and column. First, the three to the left, then the three to the right, then top and finally bottom.
		Cell[] result = new Cell[8];
		// assume that these are valid
		int upRow = row - 1;
		int downRow = row + 1;
		int leftColumn = column - 1;
		int rightColumn = column + 1;

		if (isPeriodic) {	// modify according to periodicity
			if (leftColumn == -1)	leftColumn = numberOfColumns() - 1;
			if (rightColumn == numberOfColumns())	rightColumn = 0;
			if (upRow == -1)	upRow = numberOfRows() - 1;
			if (downRow == numberOfRows())	downRow = 0;
		}

		if (leftColumn != -1) {	// there are cells to the left
			result[0] = cells[row][leftColumn];
			if (upRow != -1) {	// there are cells to the top
				result[1] = cells[upRow][leftColumn];
				result[2] = cells[upRow][column];
			}
			if (downRow != numberOfRows()) {	// there are cells to the bottom
				result[3] = cells[downRow][leftColumn];
				result[4] = cells[downRow][column];
			}
		}

		if (rightColumn != numberOfColumns()) {	// there are cells to the right
			result[5] = cells[row][rightColumn];
			if (upRow != -1) {	// there are cells to the top
				result[6] = cells[upRow][rightColumn];
			}
			if (downRow != numberOfRows()) {	// there are cells to the bottom
				result[7] = cells[downRow][rightColumn];
			}
		}

		return result;
	}

	public int countAliveNeighbours(int row, int column) {	// counts the number of live neighbours a Cell in a particular row and column has
		int count = 0;	// counts the live neighbours
		Cell[] neighbours = neighbours(row, column);	// holds the neighbours

		for (int i = 0; i < neighbours.length; i++)
			if (neighbours[i] != null)
				if (neighbours[i].isAlive)	count++;

		return count;
	}

	public void render(Graphics g, boolean isCurvy, boolean isMonochrome) {
		for (int i = 0; i < numberOfRows(); i++)	// fill all the live cells
			for (int j = 0; j < numberOfColumns(); j++) {
				Cell currentCell = cells[i][j];
				int numberOfNeighbours = countAliveNeighbours(i, j);

				currentCell.colourCell(numberOfNeighbours, isMonochrome);

				if (! (!currentCell.isAlive && numberOfNeighbours != 3)) {		// fill the cells
					currentCell.render(this, g, isCurvy, i, j);
				}
			}
	}

	public void addPlayer(Player player) {
		player.setRow((int) (Math.random() * numberOfRows()));
		player.setColumn((int) (Math.random() * numberOfColumns()));
	}

}