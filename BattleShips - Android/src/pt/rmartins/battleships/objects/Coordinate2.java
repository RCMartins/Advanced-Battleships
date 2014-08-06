package pt.rmartins.battleships.objects;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Coordinate2 implements Coordinate, Comparable<Coordinate2> {

	public static int COORDINATE_COUNTER = 0;

	public final int x, y;

	public Coordinate2(int x, int y) {
		this.x = x;
		this.y = y;
		COORDINATE_COUNTER++;
	}

	@Override
	public int getX() {
		return x;
	}

	@Override
	public int getY() {
		return y;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Coordinate) {
			final Coordinate other = (Coordinate) obj;
			return x == other.getX() && y == other.getY();
		}
		return false;
	}

	@Override
	public String toString() {
		return "(" + x + "," + y + ")";
	}

	public Coordinate2 trim(Game game) {
		return Coordinate2.trim(game, this);
	}

	public Coordinate2 translate(int x, int y) {
		return new Coordinate2(this.x + x, this.y + y);
	}

	public Coordinate2 translate(Coordinate2 otherCoor) {
		return new Coordinate2(this.x + otherCoor.x, this.y + otherCoor.y);
	}

	public boolean near(Coordinate2 coor) {
		return Math.abs(this.x - coor.x) <= 1 && Math.abs(this.y - coor.y) <= 1;
	}

	public boolean near(Iterable<Coordinate2> list) {
		for (final Coordinate2 coor : list) {
			if (Math.abs(this.x - coor.x) <= 1 && Math.abs(this.y - coor.y) <= 1)
				return true;
		}
		return false;
	}

	public boolean near(int x, int y) {
		return Math.abs(this.x - x) <= 1 && Math.abs(this.y - y) <= 1;
	}

	public boolean near(int x, int y, int dist) {
		return Math.abs(this.x - x) <= dist && Math.abs(this.y - y) <= dist;
	}

	public int dist(Coordinate2 other) {
		return Math.max(Math.abs(x - other.x), Math.abs(y - other.y));
	}

	@Override
	public int compareTo(Coordinate2 o) {
		final int dx = this.x - o.x;
		if (dx == 0)
			return this.y - o.y;
		else
			return dx;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
		return result;
	}

	public String toStringOnlyNumbers() {
		return x + " " + y;
	}

	public String toStringSmall() {
		return "" + ((char) ('A' + x)) + (y + 1);
	}

	public static Set<Coordinate2> allAround(Iterable<Coordinate2> list) {
		final Set<Coordinate2> result = new HashSet<Coordinate2>();
		for (final Coordinate2 coordinate : list) {
			for (int j = -1; j <= 1; j++) {
				for (int i = -1; i <= 1; i++) {
					result.add(coordinate.translate(i, j));
				}
			}
		}
		return result;
	}

	public static Coordinate2 trim(Game game, Coordinate2 coor) {
		int x = Math.max(0, Math.min(coor.x, game.getMaxX() - 1));
		int y = Math.max(0, Math.min(coor.y, game.getMaxY() - 1));

		return new Coordinate2(x, y);
	}

	public static Coordinate2 randomCoordinate(Game game) {
		return Coordinate2.randomListCoordinates(game, 1).get(0);
	}

	public static List<Coordinate2> randomListCoordinates(Game game, int howMany) {
		final List<Coordinate2> list = new ArrayList<Coordinate2>(howMany);
		for (int i = 0; i < howMany; i++) {
			list.add(new Coordinate2((int) (Math.random() * game.getMaxX()), (int) (Math.random() * game.getMaxY())));
		}
		return list;
	}

	public static boolean contains(Iterable<Coordinate2> list1, Iterable<Coordinate2> list2, Coordinate2 ignoreCoor) {
		for (final Coordinate2 coor1 : list1) {
			if (!coor1.equals(ignoreCoor)) {
				for (final Coordinate2 coor2 : list2) {
					if (coor1.equals(coor2))
						return true;
				}
			}
		}
		return false;
	}

	public static List<Coordinate2> intersect(Iterable<Coordinate2> list1, Iterable<Coordinate2> list2,
			Coordinate2 ignoreCoor) {
		final List<Coordinate2> list = new ArrayList<Coordinate2>();
		for (final Coordinate2 coor1 : list1) {
			if (!coor1.equals(ignoreCoor)) {
				for (final Coordinate2 coor2 : list2) {
					if (coor1.equals(coor2))
						list.add(coor1);
				}
			}
		}
		return list;
	}

	public static boolean intersectAny(Iterable<Coordinate2> list1, Iterable<Coordinate2> list2) {
		for (final Coordinate2 coor1 : list1) {
			for (final Coordinate2 coor2 : list2) {
				if (coor1.equals(coor2))
					return true;
			}
		}
		return false;
	}

}
