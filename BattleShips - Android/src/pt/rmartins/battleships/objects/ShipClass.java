package pt.rmartins.battleships.objects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import pt.rmartins.battleships.utilities.LanguageClass;

public class ShipClass implements Ship {

	protected final int id;
	protected int rotation;
	protected int x, y;
	private List<Coordinate2> updatedCoordinateList;
	protected int tick;
	private int iteratorTick;
	private final int pieceAtTick;
	private final Set<Coordinate> pieceAt;

	public ShipClass(int id, int rotation, Coordinate2 coor) {
		this(id, rotation, coor.x, coor.y);
	}

	public ShipClass(int id, int rotation, int x, int y) {
		if (rotation >= numberOfRotations(id))
			throw new RuntimeException();

		this.id = id;
		this.rotation = rotation;
		this.x = x;
		this.y = y;

		this.tick = 0;
		this.iteratorTick = -1;
		this.pieceAtTick = -1;
		pieceAt = new HashSet<Coordinate>(ShipClass.getNumberPieces(id));
	}

	public ShipClass(Ship otherShip) {
		this(otherShip, 0, 0);
	}

	public ShipClass(Ship otherShip, int x, int y) {
		this.id = otherShip.getId();
		this.rotation = otherShip.getRotation();
		this.x = otherShip.minX() + x;
		this.y = otherShip.minY() + y;

		this.tick = 0;
		this.iteratorTick = -1;
		this.pieceAtTick = -1;
		pieceAt = new HashSet<Coordinate>(ShipClass.getNumberPieces(id));
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public synchronized String getName() {
		return ShipClass.getName(id);
	}

	@Override
	public synchronized int getRotation() {
		return rotation;
	}

	@Override
	public synchronized int minX() {
		return x;
	}

	@Override
	public synchronized int minY() {
		return y;
	}

	@Override
	public synchronized int getNumberPieces() {
		return ShipClass.getNumberPieces(id);
	}

	@Override
	public synchronized boolean equals(Object obj) {
		if (obj instanceof ShipClass) {
			final ShipClass other = (ShipClass) obj;
			return id == other.id;
		}
		return false;
	}

	@Override
	public synchronized boolean equalsIRXY(Ship other) {
		return id == other.getId() && rotation == other.getRotation() && x == other.minX() && y == other.minY();
	}

	@Override
	public synchronized int sizeX() {
		return ShipClass.sizeX(id, rotation);
	}

	@Override
	public synchronized int sizeY() {
		return ShipClass.sizeY(id, rotation);
	}

	@Override
	public int getSpace() {
		return ShipClass.getSpace(id);
	}

	@Override
	public synchronized int maxX() {
		return minX() + sizeX() - 1;
	}

	@Override
	public synchronized int maxY() {
		return minY() + sizeY() - 1;
	}

	@Override
	public synchronized void moveTo(Coordinate2 position) {
		tick++;
		this.x = position.x;
		this.y = position.y;
	}

	@Override
	public synchronized void moveTo(int x, int y) {
		tick++;
		this.x = x;
		this.y = y;
	}

	private void translate(int x, int y) {
		this.x += x;
		this.y += y;
	}

	@Override
	public synchronized boolean near(int x, int y) {
		return COOR_CACHE.set(x, y).near(this);
	}

	@Override
	public synchronized boolean near(int x, int y, int dist) {
		for (final Coordinate2 coor : this) {
			if (coor.near(x, y, dist))
				return true;
		}
		return false;
	}

	@Override
	public synchronized boolean near(Iterable<Ship> ships) {
		for (final Ship ship : ships) {
			for (final Coordinate2 otherCoor : ship) {
				if (otherCoor.near(this)) {
					return true;
				}
			}
		}
		return false;
	}

	private final Coordinate2Editor COOR_CACHE = new Coordinate2Editor();

	@Override
	public synchronized boolean pieceAt(int x, int y) {
		return pieceAt(COOR_CACHE.set(x, y));
	}

	@Override
	public synchronized boolean pieceAt(Coordinate compareCoor) {
		//		if (tick != pieceAtTick) {
		//			pieceAtTick = tick;
		//
		//			pieceAt.clear();
		//			for (final Coordinate2 coor : this) {
		//				pieceAt.add(coor);
		//			}
		//		}
		//		return pieceAt.contains(compareCoor);
		for (final Coordinate2 coor : this) {
			if (coor.equals(compareCoor))
				return true;
		}
		return false;
	}

	@Override
	public synchronized String toString() {
		return this.getName() + ": R" + rotation + " (" + x + "," + y + ")";
	}

	@Override
	public synchronized Coordinate2 trimShip(int maxX, int maxY) {
		tick++;

		int mX = x;
		if (mX < 0) {
			translate(-mX, 0);
		}
		int mY = y;
		if (mY < 0) {
			translate(0, -mY);
		}

		mX = maxX();
		if (mX >= maxX) {
			translate(-mX + maxX - 1, 0);
		}
		mY = maxY();
		if (mY >= maxY) {
			translate(0, -mY + maxY - 1);
		}

		return new Coordinate2(minX(), minY());
	}

	@Override
	public synchronized Iterator<Coordinate2> iterator() {
		if (tick != iteratorTick) {
			iteratorTick = tick;

			updatedCoordinateList = new ArrayList<Coordinate2>(ShipClass.getShipParts(id, rotation));
			for (int i = 0; i < updatedCoordinateList.size(); i++) {
				updatedCoordinateList.set(i, updatedCoordinateList.get(i).translate(x, y));
			}
		}
		return updatedCoordinateList.iterator();
	}

	@Override
	public synchronized List<Coordinate2> getListPieces() {
		if (tick != iteratorTick) {
			iteratorTick = tick;

			updatedCoordinateList = new ArrayList<Coordinate2>(ShipClass.getShipParts(id, rotation));
			for (int i = 0; i < updatedCoordinateList.size(); i++) {
				updatedCoordinateList.set(i, updatedCoordinateList.get(i).translate(x, y));
			}
		}
		return updatedCoordinateList;
	}

	@Override
	public synchronized Coordinate2 rotateClockwise(int maxX, int maxY) {
		rotation = (rotation + 1) % numberOfRotations(id);
		return trimShip(maxX, maxY);
	}

	@Override
	public synchronized Coordinate2 rotateTo(int rotation, int maxX, int maxY) {
		if (rotation >= numberOfRotations(id))
			throw new RuntimeException();
		this.rotation = rotation;
		return trimShip(maxX, maxY);
	}

	@Override
	public synchronized boolean isSunk(Player player) {
		return ShipClass.isSunk(this, player);
	}

	private static List<List<ShipData>> ShipsInfo;

	// private static Game game;

	public static void initializeClass() {// Game game) {
		ShipClass.ShipsInfo = new ArrayList<List<ShipData>>();
		// ShipClass.game = game;
	}

	/**
	 * @param ship
	 *            the ship to verify
	 * @param player
	 *            the enemy player
	 * @return if the ship was destroyed by the player
	 */
	public static boolean isSunk(Ship ship, Player player) {
		for (final Coordinate2 coor : ship) {
			if (player.messageAt(coor) == null) {
				return false;
			}
		}
		return true;
	}

	private static void createAllRotationPossibilities(ShipData shipData) {
		final ArrayList<ShipData> list = new ArrayList<ShipData>(4);

		list.add(shipData);
		for (int k = 1; k <= 3; k++) {
			shipData = shipData.rotateShipClockwise();
			boolean equal = false;
			for (final ShipData s : list) {
				if (shipData.equalsAllPiecies(s)) {
					equal = true;
					break;
				}
			}
			if (!equal) {
				list.add(shipData);
			}
		}
		list.trimToSize();
		ShipClass.ShipsInfo.add(Collections.unmodifiableList(list));
	}

	/**
	 * Ensures that all ships are inside the field
	 */
	public static List<Ship> createAllFieldPossibilities(int id, int maxX, int maxY) {
		final int nShipRotations = ShipClass.getAllRotations(id).size();

		final List<Ship> allPossiblePositions = new ArrayList<Ship>(maxX * maxY * nShipRotations);

		for (int k = 0; k < nShipRotations; k++) {
			final int mX = maxX - ShipClass.sizeX(id, k) + 1;
			final int mY = maxY - ShipClass.sizeY(id, k) + 1;
			for (int y = 0; y < mY; y++) {
				for (int x = 0; x < mX; x++) {
					allPossiblePositions.add(new ShipClass(id, k, new Coordinate2(x, y)));
				}
			}
		}
		return allPossiblePositions;
	}

	/**
	 * maximum list size = 4
	 */
	public static List<ShipData> getAllRotations(int id) {
		return ShipsInfo.get(id);
	}

	public static final int SHIPDATASIZE = 4;

	public static byte[] getShipBytes(Ship ship) {
		final byte[] data = new byte[4];
		data[0] = (byte) ship.getId();
		data[1] = (byte) ship.getRotation();
		data[2] = (byte) ship.minX();
		data[3] = (byte) ship.minY();
		return data;
	}

	public static List<Ship> getAllPossibilities(int id, Coordinate2 coor) {
		final List<Ship> list = new LinkedList<Ship>();
		final List<ShipData> rotations = ShipsInfo.get(id);

		for (int nRotation = 0; nRotation < rotations.size(); nRotation++) {
			final ShipData shipData = rotations.get(nRotation);
			for (final Coordinate2 part : shipData.parts) {
				list.add(new ShipClass(id, nRotation, coor.x - part.x, coor.y - part.y));
			}
		}

		return list;
	}

	public static List<Coordinate2> getShipParts(int id, int rotation) {
		return ShipsInfo.get(id).get(rotation).parts;
	}

	public static String getName(int id) {
		return ShipsInfo.get(id).get(0).name.get(LanguageClass.getCurrentLanguage().getLanguage());
	}

	public static String getName(int id, Locale lang) {
		return ShipsInfo.get(id).get(0).name.get(lang);
	}

	public static List<String> getAllNames(int id) {
		List<String> list = new ArrayList<String>();
		for (Entry<String, String> entry : ShipsInfo.get(id).get(0).name.entrySet()) {
			list.add(entry.getValue());
		}
		return list;
	}

	public static int numberOfShips() {
		return ShipsInfo.size();
	}

	public static int numberOfRotations(int id) {
		return ShipsInfo.get(id).size();
	}

	public static int getNumberPieces(int id) {
		return ShipsInfo.get(id).get(0).parts.size();
	}

	public static int getSpace(int id) {
		return ShipsInfo.get(id).get(0).space;
	}

	public static int sizeX(int id, int rotation) {
		return ShipsInfo.get(id).get(rotation).sizeX;
	}

	public static int sizeY(int id, int rotation) {
		return ShipsInfo.get(id).get(rotation).sizeY;
	}

	public static class ShipData {

		private final Map<String, String> name;
		private final List<Coordinate2> parts;
		public final int sizeX, sizeY, space;

		public ShipData(Map<String, String> names, List<Coordinate2> parts) {
			this.name = names;
			this.parts = Collections.unmodifiableList(parts);
			sizeX = sizeX();
			sizeY = sizeY();
			space = space();
		}

		public boolean equalsAllPiecies(ShipData s) {
			final Set<Coordinate2> set1 = new TreeSet<Coordinate2>(parts);
			final Set<Coordinate2> set2 = new TreeSet<Coordinate2>(s.parts);
			return set1.equals(set2);
		}

		private int sizeX() {
			int sizeX = parts == null || parts.isEmpty() ? 0 : parts.get(0).x;
			for (final Coordinate2 coor : parts) {
				if (coor.x > sizeX) {
					sizeX = coor.x;
				}
			}
			return sizeX + 1;
		}

		private int sizeY() {
			int sizeY = parts == null || parts.isEmpty() ? 0 : parts.get(0).y;
			for (final Coordinate2 coor : parts) {
				if (coor.y > sizeY) {
					sizeY = coor.y;
				}
			}
			return sizeY + 1;
		}

		private int space() {
			final List<Coordinate2> p = new ArrayList<Coordinate2>();
			for (final Coordinate2 coor : this.parts) {
				p.add(coor.translate(1, 1));
			}
			final boolean occupied[][] = new boolean[this.sizeX + 2][this.sizeY + 2];
			for (final Coordinate2 coor : p) {
				for (int i = -1; i <= 1; i++) {
					for (int j = -1; j <= 1; j++) {
						occupied[coor.x + i][coor.y + j] = true;
					}
				}
			}
			int count = 0;
			for (int i = 0; i < this.sizeX + 2; i++) {
				for (int j = 0; j < this.sizeY + 2; j++) {
					if (occupied[i][j])
						count++;
				}
			}
			return count;
		}

		private static int minX(List<Coordinate2> list) {
			int minX = list == null || list.isEmpty() ? 0 : list.get(0).x;
			for (final Coordinate2 coor : list) {
				if (coor.x < minX) {
					minX = coor.x;
				}
			}
			return minX;
		}

		private static int minY(List<Coordinate2> list) {
			int minY = list == null || list.isEmpty() ? 0 : list.get(0).y;
			for (final Coordinate2 coor : list) {
				if (coor.y < minY) {
					minY = coor.y;
				}
			}
			return minY;
		}

		private ShipData rotateShipClockwise() {
			final List<Coordinate2> list = new ArrayList<Coordinate2>(this.parts.size());

			for (int i = 0; i < this.parts.size(); i++) {
				final int nX = -this.parts.get(i).y;
				final int nY = this.parts.get(i).x;

				list.add(i, new Coordinate2(nX, nY));
			}

			final int mX = ShipData.minX(list);
			if (mX < 0) {
				ShipData.translate(list, -mX, 0);
			}
			final int mY = ShipData.minY(list);
			if (mY < 0) {
				ShipData.translate(list, 0, -mY);
			}

			return new ShipData(name, list);
		}

		private static void translate(List<Coordinate2> list, int x, int y) {
			for (int i = 0; i < list.size(); i++) {
				list.set(i, new Coordinate2(list.get(i).x + x, list.get(i).y + y));
			}
		}

		@Override
		public String toString() {
			return name + " " + parts;
		}
	}

	public static void createNewShip(Map<String, String> names, List<Coordinate2> parts) {
		final ShipData shipData = new ShipData(names, parts);
		ShipClass.createAllRotationPossibilities(shipData);
	}

	public static int getId(String shipName) {
		for (int id = 0; id < ShipClass.numberOfShips(); id++) {
			List<String> names = ShipClass.getAllNames(id);
			for (String name : names) {
				if (name.equals(shipName)) {
					return id;
				}
			}
		}
		return -1;
	}

}
