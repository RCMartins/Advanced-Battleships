package pt.rmartins.battleships.objects;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Coordinate implements Comparable<Coordinate> {

	public final int x, y;

	public Coordinate(Coordinate coor) {
		x = coor.x;
		y = coor.y;
	}

	public Coordinate(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public boolean equals(int x, int y) {
		return this.x == x && this.y == y;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Coordinate) {
			final Coordinate other = (Coordinate) obj;
			return x == other.x && y == other.y;
		}
		return false;
	}

	@Override
	public String toString() {
		return "(" + x + "," + y + ")";
	}

	public Coordinate trim(Game game) {
		return Coordinate.trim(game, this);
	}

	public Coordinate translate(int x, int y) {
		return new Coordinate(this.x + x, this.y + y);
	}

	public Coordinate translate(Coordinate otherCoor) {
		return new Coordinate(this.x + otherCoor.x, this.y + otherCoor.y);
	}

	public boolean near(Coordinate coor) {
		return near(coor.x, coor.y, 1);
	}

	public boolean near(Iterable<Coordinate> list) {
		for (final Coordinate coor : list) {
			if (near(coor.x, coor.y, 1)) {
				return true;
			}
		}
		return false;
	}

	public boolean near(int x, int y) {
		return near(x, y, 1);
	}

	public boolean near(int x, int y, int dist) {
		return Math.abs(this.x - x) <= dist && Math.abs(this.y - y) <= dist;
	}

	public int dist(Coordinate other) {
		return Math.max(Math.abs(x - other.x), Math.abs(y - other.y));
	}

	@Override
	public int compareTo(Coordinate o) {
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

	public static Set<Coordinate> allAround(Iterable<Coordinate> list) {
		final Set<Coordinate> result = new HashSet<Coordinate>();
		for (final Coordinate coordinate : list) {
			for (int j = -1; j <= 1; j++) {
				for (int i = -1; i <= 1; i++) {
					result.add(coordinate.translate(i, j));
				}
			}
		}
		return result;
	}

	public static Coordinate trim(Game game, Coordinate coor) {
		int x = Math.max(0, Math.min(coor.x, game.getMaxX() - 1));
		int y = Math.max(0, Math.min(coor.y, game.getMaxY() - 1));

		return new Coordinate(x, y);
	}

	public static Coordinate randomCoordinate(Game game) {
		return Coordinate.randomListCoordinates(game, 1).get(0);
	}

	public static List<Coordinate> randomListCoordinates(Game game, int howMany) {
		final List<Coordinate> list = new ArrayList<Coordinate>(howMany);
		for (int i = 0; i < howMany; i++) {
			list.add(new Coordinate((int) (Math.random() * game.getMaxX()), (int) (Math.random() * game.getMaxY())));
		}
		return list;
	}

	public static boolean contains(Iterable<Coordinate> list1, Iterable<Coordinate> list2, Coordinate ignoreCoor) {
		for (final Coordinate coor1 : list1) {
			if (!coor1.equals(ignoreCoor)) {
				for (final Coordinate coor2 : list2) {
					if (coor1.equals(coor2))
						return true;
				}
			}
		}
		return false;
	}

	public static List<Coordinate> intersect(Iterable<Coordinate> list1, Iterable<Coordinate> list2,
			Coordinate ignoreCoor) {
		final List<Coordinate> list = new ArrayList<Coordinate>();
		for (final Coordinate coor1 : list1) {
			if (!coor1.equals(ignoreCoor)) {
				for (final Coordinate coor2 : list2) {
					if (coor1.equals(coor2))
						list.add(coor1);
				}
			}
		}
		return list;
	}

	public static boolean intersectAny(Iterable<Coordinate> list1, Iterable<Coordinate> list2) {
		for (final Coordinate coor1 : list1) {
			for (final Coordinate coor2 : list2) {
				if (coor1.equals(coor2))
					return true;
			}
		}
		return false;
	}

}
