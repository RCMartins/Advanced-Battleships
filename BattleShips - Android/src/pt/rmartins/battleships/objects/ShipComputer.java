package pt.rmartins.battleships.objects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import pt.rmartins.battleships.objects.Game.Mark;
import pt.rmartins.battleships.objects.ai.ComputerAI;
import pt.rmartins.battleships.objects.ai.ComputerAI.CoordinateValue;
import android.util.Pair;

public class ShipComputer extends ShipClass {

	private static final String TAG = ShipComputer.class.getSimpleName();

	@Override
	public String toString() {
		return super.toString() + "[possible=" + possibleSpots + ", known=" + knownSpots + ", searchStatus="
				+ searchStatus + ", isDestroyed=" + isDestroyed + "]";
	}

	public enum SearchStatus {
		/**
		 * No information about the ship location
		 */
		None,
		/**
		 * There is some information about the ship location
		 */
		Some,
		/**
		 * The exact position of the ship is known
		 */
		All;
	}

	private SearchStatus searchStatus;

	private List<Coordinate> knownSpots;
	private List<Pair<Message, Boolean>> possibleSpots;
	private final List<Ship> allPlaces, unmodifiableAllPlaces;
	private boolean isDestroyed;
	private ComputerAI comp;

	public ShipComputer(int id, ComputerAI comp) {
		super(id, 0, new Coordinate(0, 0));

		final int maxX = comp.game.getMaxX();
		final int maxY = comp.game.getMaxY();
		//		allPlaces = ShipClass.createAllFieldPossibilities(id, maxX, maxY);
		allPlaces = new LinkedList<Ship>(ShipClass.createAllFieldPossibilities(id, maxX, maxY));
		unmodifiableAllPlaces = Collections.unmodifiableList(allPlaces);

		initialize(SearchStatus.None, comp);
	}

	private void initialize(SearchStatus searchStatus, ComputerAI comp) {
		this.comp = comp;
		this.searchStatus = searchStatus;
		this.knownSpots = new ArrayList<Coordinate>();
		this.possibleSpots = new ArrayList<Pair<Message, Boolean>>();
		this.isDestroyed = false;
	}

	public List<Ship> getAllPlaces() {
		return unmodifiableAllPlaces;
	}

	/**
	 * @return All places possible for the ship with the current information
	 */
	public List<Ship> updateAllPlaces() {
		if (searchStatus != SearchStatus.All) {
			calcAll();
			if (allPlaces.size() == 1) {
				final Ship ship = allPlaces.get(0);
				this.rotation = ship.getRotation();
				this.x = ship.minX();
				this.y = ship.minY();
				tick++; // So that the pieces list is updated
				searchStatus = SearchStatus.All;
				possibleSpots.clear();
				knownSpots.clear();
				markShip();
			}
		}
		return unmodifiableAllPlaces;
	}

	private void calcAll() {
		if (possibleSpots.isEmpty()) {
			updatePlacesKnown();
		} else {
			calculatePossiblePlaces();
		}
	}

	public boolean addKnownSpots(Iterable<Coordinate> coors) {
		for (final Coordinate coor : coors) {
			if (isAValidKnownSpot(coor)) {
				knownSpots.add(coor);
			} else {
				return false;
			}
		}
		update();
		return true;
	}

	public boolean addKnownSpot(Coordinate coor) {
		if (isAValidKnownSpot(coor)) {
			knownSpots.add(coor);
			update();
			return true;
		}
		return false;
	}

	public boolean addPossibleSpot(Message message, boolean killShot) {
		if (isAValidPossibleSpot(message, killShot)) {
			possibleSpots.add(new Pair<Message, Boolean>(message, killShot));
			update();
			return true;
		}
		return false;
	}

	public boolean removePossibleSpot(Message message, boolean killShot) {
		for (int i = 0; i < possibleSpots.size(); i++) {
			final Pair<Message, Boolean> pair = possibleSpots.get(i);
			if (pair.first == message && pair.second == killShot) {
				possibleSpots.remove(i);
				update();
				return true;
			}
		}
		return false;
	}

	private void update() {
		updateAllPlaces();
		if (searchStatus == SearchStatus.None) {
			if (!possibleSpots.isEmpty() || !knownSpots.isEmpty()) {
				searchStatus = SearchStatus.Some;
			}
		}
	}

	public boolean isAValidKnownSpot(Coordinate coor) {
		if (knownSpots.contains(coor))
			return false;

		final List<Ship> list = getAllPlaces();
		if (list.isEmpty())
			return true;

		for (final Ship ship : list) {
			if (ship.pieceAt(coor))
				return true;
		}
		return false;
	}

	public boolean isAValidPossibleSpot(Message message, boolean killShot) {
		if (searchStatus == SearchStatus.All)
			return false;
		if (killShot && !canBeDestroyed(message))
			return false;
		if (comp.shipCount[id] == 1)
			return true;
		if (getNumberPieces() == 1)
			return true;

		final List<Ship> list = getAllPlaces();
		if (list != null && !list.isEmpty()) {
			for (final Ship ship : list) {
				int nHits = message.getHits(ship.getId());
				for (final Pair<Message, Boolean> pair : possibleSpots) {
					if (pair.first == message) {
						nHits--;
					}
				}

				if (multipleMessageHits(message, ship, nHits, 0))
					return true;
			}
			return false;
		}
		return true;
	}

	private boolean multipleMessageHits(Message m, Ship ship, int hits, int index) {
		if (hits == 0)
			return true;

		for (int i = index; i < ship.getNumberPieces(); i++) {
			final Coordinate coor = ship.getListPieces().get(i);
			if (m.getCoors().contains(coor)) {
				return multipleMessageHits(m, ship, hits - 1, i + 1);
			}
		}
		return false;
	}

	private void updatePlacesKnown() {
		if (allPlaces.size() == 1)
			return;

		for (Iterator<Ship> iterator = allPlaces.iterator(); iterator.hasNext();) {
			final Ship ship = iterator.next();
			possible_label: {
				for (final Coordinate coor : knownSpots) {
					if (!ship.pieceAt(coor)) {
						break possible_label;
					}
				}
				for (final Coordinate coor : ship) {
					if (comp.markAt(coor).isWater())
						break possible_label;

					final Message m = comp.messageAt(coor);
					if (m != null && m.getHits(id) == 0)
						break possible_label;

					final ShipComputer knownNearShip = comp.nearKnownSpot(coor);
					if (knownNearShip != null && knownNearShip != this)
						break possible_label;
				}
				continue;
			}
			iterator.remove();
		}

		if (!comp.markWaterList.isEmpty()) {
			for (Iterator<Ship> iterator = allPlaces.iterator(); iterator.hasNext();) {
				final Ship ship = iterator.next();
				possible_label: {
					for (final Coordinate coor : ship) {
						if (comp.markAt(coor).isWater())
							break possible_label;
					}
					continue;
				}
				iterator.remove();
			}
		}
	}

	/**
	 * preCondition: !possibleSpots.isEmpty()
	 */
	private void calculatePossiblePlaces() {
		Message message = possibleSpots.get(0).first;
		int hits = message.getHits(id);
		int misses = message.getMisses();
		for (Iterator<Ship> iterator = allPlaces.iterator(); iterator.hasNext();) {
			final Ship ship = iterator.next();
			possible_label: {
				if (!Coordinate.intersectAny(ship, message.getCoors())) {
					for (int i = 0; i < 1; i++) {
					}

					break possible_label;
				}

				for (final Coordinate coor : ship) {

					if (comp.markAt(coor).isWater())
						break possible_label;

					final Message m = comp.messageAt(coor);
					if (m != null && m.getHits(id) == 0)
						break possible_label;

					final ShipComputer knownNearShip = comp.nearKnownSpot(coor);
					if (knownNearShip != null && knownNearShip != this)
						break possible_label;
				}

				/**
				 * Intersection between known spots
				 */
				for (final Coordinate coordinate : knownSpots) {
					if (!ship.pieceAt(coordinate)) {
						break possible_label;
					}
				}

				// testa se esta possibilidade não pode ser por acertar nas coordenadas desta própria jogada ?
				/**
				 * FIXME <br>
				 * TODO: VERIFY THIS !!!!!!!!!!!!!!!!!!!!!! <br>
				 * FIXME
				 */
				for (Coordinate messageCoor : message.getCoors()) {
					final List<Coordinate> listIntercept = Coordinate.intersect(ship, message.getCoors(), messageCoor);
					if (listIntercept.size() > hits) {
						for (int i = 0; i < 1; i++) {
						}

						break possible_label;
					}
				}

				continue;
			}
			iterator.remove();
		}

		if (possibleSpots.size() > 1) {
			for (int i = 1; i < possibleSpots.size(); i++) {
				Pair<Message, Boolean> pair = possibleSpots.get(i);
				message = pair.first;
				hits = message.getHits(id);
				misses = message.getMisses();

				final List<Ship> listPossibilities = new ArrayList<Ship>();
				for (final Coordinate coor : message.getCoors()) {
					listPossibilities.addAll(ShipClass.getAllPossibilities(id, coor));
				}

				for (Iterator<Ship> iterator = allPlaces.iterator(); iterator.hasNext();) {
					final Ship ship = iterator.next();
					boolean found = false;
					for (final Ship possibility : listPossibilities) {
						if (ship.equalsIRXY(possibility)) {
							found = true;
							break;
						}
					}
					if (!found) {
						iterator.remove();
					}
				}
			}
		}
	}

	private void markShip() {
		for (final Coordinate coor : this) {
			comp.setPosition(coor);
			if (!comp.markAt().isShip())
				comp.setMarkAt(Mark.Ship);
		}
		comp.fillWater(this);
	}

	public List<Coordinate> getKnownPlaces() {
		final List<Coordinate> result = new ArrayList<Coordinate>();
		if (searchStatus == SearchStatus.All) {
			for (final Coordinate coor : this) {
				if (comp.messageAt(coor) == null) {
					result.add(coor);
				}
			}
		}
		return result;
	}

	public List<Coordinate> getMostProbablePlaces(int howManyMax) { // TODO: AI melhorar algoritmo
		final int maxX = comp.game.getMaxX();
		final int maxY = comp.game.getMaxY();
		final List<Ship> allPlaces = getAllPlaces();
		final int[][] field = new int[maxX][maxY];
		for (final Ship ship : allPlaces) {
			for (final Coordinate coor : ship) {
				if (comp.messageAt(coor) == null)
					field[coor.x][coor.y]++;
			}
		}

		final List<CoordinateValue> listValues = new ArrayList<CoordinateValue>();
		for (int x = 0; x < maxX; x++) {
			for (int y = 0; y < maxY; y++) {
				listValues.add(new CoordinateValue(x, y, field[x][y]));
			}
		}

		Collections.shuffle(listValues, GameClass.random);
		Collections.sort(listValues);
		Collections.reverse(listValues);

		final List<Coordinate> listResult = new ArrayList<Coordinate>(howManyMax);
		for (int i = 0; i < howManyMax && i < listValues.size() && listValues.get(i).value > 0; i++) {
			listResult.add(listValues.get(i));
		}

		return listResult;
	}

	public SearchStatus getSearchStatus() {
		return searchStatus;
	}

	public boolean isDestroyed() {
		return isDestroyed;
	}

	public void setDestroyed() {
		this.isDestroyed = true;
	}

	public boolean canBeHited(Coordinate coor) {
		if (isDestroyed)
			return false;
		if (searchStatus == SearchStatus.None) {
			return false;
		} else if (searchStatus == SearchStatus.All) {
			return pieceAt(coor);
		} else {
			final List<Ship> list = getAllPlaces();
			for (final Ship ship : list) {
				if (ship.pieceAt(coor))
					return true;
			}
		}
		return false;
	}

	public boolean canBeDestroyed(Message message) {
		for (final Coordinate coor : message.getCoors()) {
			if (canBeDestroyed(coor))
				return true;
		}
		return false;
	}

	public boolean canBeDestroyed(Coordinate coor) {
		if (isDestroyed)
			return false;
		if (searchStatus == SearchStatus.All) {
			return isSunk(this, comp.getEnemy());
		} else if (searchStatus == SearchStatus.None && getNumberPieces() == 1) {
			return true;
		} else {
			final List<Ship> list = getAllPlaces();
			for (final Ship ship : list) {
				boolean possible = true;
				if (ship.pieceAt(coor)) {
					for (final Coordinate piece : ship) {
						if (comp.messageAt(piece) == null && !piece.equals(coor)) {
							possible = false;
						}
					}
					if (possible)
						return true;
				}
			}
		}
		return false;
	}

	public boolean hasAnyMessage(Message other) {
		for (final Pair<Message, Boolean> pair : possibleSpots) {
			if (pair.first == other) {
				return true;
			}
		}
		return false;
	}

	public boolean isAKnownSpot(Coordinate coor) {
		return knownSpots.contains(coor);
	}

	public List<Pair<Message, Boolean>> getPossibleSpotsMessages() {
		return possibleSpots;
	}

	/**
	 * return checks if coor is a knownSpot or is near a knownSpot
	 */
	public boolean nearKnownSpot(Coordinate coor) {
		for (final Coordinate knownSpot : knownSpots) {
			if (knownSpot.near(coor)) {
				return true;
			}
		}
		return false;
	}

	public void setSomeKnowledge() {
		if (searchStatus == SearchStatus.None) {
			searchStatus = SearchStatus.Some;
		}
	}
}
