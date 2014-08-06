package pt.rmartins.battleships.objects;

import java.util.ArrayList;
import java.util.List;

public class Fleet {

	private final List<Integer> fleet;
	public final int maxX, maxY;
	private int size, totalPieces;

	public Fleet(Game game, Fleet fleet) {
		this(game.getMaxX(), game.getMaxY(), new ArrayList<Integer>(fleet.fleet));
	}

	public Fleet(int maxX, int maxY, List<Integer> fleet) {
		this.maxX = maxX;
		this.maxY = maxY;
		this.fleet = fleet;
		this.size();
		this.calcVars();
	}

	private void calcVars() {
		this.size = 0;
		this.totalPieces = 0;
		for (int id = 0; id < fleet.size(); id++) {
			this.totalPieces += ShipClass.getNumberPieces(id) * fleet.get(id);
			this.size += fleet.get(id);
		}
	}

	public int getTotalPieces() {
		return totalPieces;
	}

	public List<Ship> getFleet() {
		final List<Ship> list = new ArrayList<Ship>(size);
		for (int id = 0; id < fleet.size(); id++) {
			for (int k = 0; k < fleet.get(id); k++) {
				list.add(new ShipClass(id, 0, 0, 0));
			}
		}
		return list;
	}

	public List<Integer> getFleetNumbers() {
		return fleet;
	}

	public int size() {
		return size;
	}

	public boolean isAIready() {
		for (int i = 1; i < fleet.size(); i++) {
			int amount = fleet.get(i);
			if (amount > 1)
				return false;
		}
		return true;
	}
}
