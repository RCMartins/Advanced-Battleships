package pt.rmartins.battleships.objects;

import java.util.List;

public interface Ship extends Iterable<Coordinate2> {

	public int getId();

	public int getRotation();

	public String getName();

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

	public void moveTo(Coordinate2 position);

	public void moveTo(int nX, int nY);

	public boolean near(int x, int y);

	public boolean near(int x, int y, int dist);

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

	public Coordinate2 rotateClockwise(int maxX, int maxY);

	public Coordinate2 rotateTo(int index, int maxX, int maxY);

	public int sizeX();

	public int sizeY();

	public int getSpace();

	public Coordinate2 trimShip(int maxX, int maxY);

	@Override
	public boolean equals(Object other);

	/**
	 * @param other
	 *            o barco a comparar
	 * @return se os barcos são iguais (id, rotação, (x,y))
	 */
	public boolean equalsIRXY(Ship other);

	public List<Coordinate2> getListPieces();

	public boolean isSunk(Player player);

}
