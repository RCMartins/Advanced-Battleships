package pt.rmartins.battleships.objects;

public class Coordinate2Editor implements Coordinate {

	private int x, y;

	public Coordinate2Editor() {
		x = 0;
		y = 0;
	}

	public Coordinate2Editor(int x, int y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public int getX() {
		return x;
	}

	@Override
	public int getY() {
		return y;
	}

	public Coordinate2Editor set(int x, int y) {
		this.x = x;
		this.y = y;
		return this;
	}

	public Coordinate2Editor translate(int x, int y) {
		this.x += x;
		this.y += y;
		return this;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Coordinate) {
			final Coordinate other = (Coordinate) obj;
			return x == other.getX() && y == other.getY();
		}
		return false;
	}

	public boolean near(Iterable<Coordinate2> list) {
		for (final Coordinate2 coor : list) {
			if (Math.abs(this.x - coor.x) <= 1 && Math.abs(this.y - coor.y) <= 1)
				return true;
		}
		return false;
	}

}
