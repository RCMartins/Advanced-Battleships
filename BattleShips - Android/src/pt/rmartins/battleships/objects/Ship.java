package pt.rmartins.battleships.objects;

import java.util.List;

public interface Ship extends Iterable<Coordinate> {

	// public void centerShip(int maxX, int maxY);

	public int getId();

	public int getRotation();

	public String getName();

	// // /**
	// // * Return a copy of the list of the ship coordinates
	// // *
	// // * @return the list
	// // */
	// // public List<Coordinate> getShipParts();
	//
	// public boolean isInsideField(Game game);

	/**
	 * Coordinate with larger x
	 * 
	 * @return minX() + sizeX() - 1;
	 */
	public int maxX();

	/**
	 * Coordinate with larger y
	 * 
	 * @return minY() + sizeY() - 1;
	 */
	public int maxY();

	/**
	 * Minimmum coordinate X of ship
	 * 
	 * @return min X
	 */
	public int minX();

	/**
	 * Minimmum coordinate Y of ship
	 * 
	 * @return min Y
	 */
	public int minY();

	public int getNumberPieces();

	public void moveTo(Coordinate position);

	public void moveTo(int nX, int nY);

	public boolean near(Coordinate coor);

	public boolean near(int x, int y);

	public boolean near(Ship... ships);

	public boolean near(Iterable<Ship> ships);

	/**
	 * @param x
	 * @param y
	 * @return se a coordenada (x,y) pertence a este barco
	 */
	public boolean pieceAt(int x, int y);

	/**
	 * @param coordinate
	 * @return se a coordenada (x,y) pertence a este barco
	 */
	public boolean pieceAt(Coordinate coordinate);

	public Coordinate rotateClockwise(int maxX, int maxY);

	public Coordinate rotateTo(int index, int maxX, int maxY);

	public int sizeX();

	public int sizeY();

	public int getSpace();

	public Coordinate trimShip(int maxX, int maxY);

	public boolean equals(Object other);

	/**
	 * @param other
	 *            o barco a comparar
	 * @return se os barcos são iguais (id, rotação, (x,y))
	 */
	public boolean equalsIRXY(Ship other);

	List<Coordinate> getListPieces();

	public boolean isSunk(Player player);

}
