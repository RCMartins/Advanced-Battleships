package pt.rmartins.battleships.objects.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import pt.rmartins.battleships.objects.Coordinate;
import pt.rmartins.battleships.objects.Game;
import pt.rmartins.battleships.objects.Game.Mark;
import pt.rmartins.battleships.objects.GameClass;
import pt.rmartins.battleships.objects.Message;
import pt.rmartins.battleships.objects.Message.MessageUnit;
import pt.rmartins.battleships.objects.Message.MessageUnit.TypesMessageUnits;
import pt.rmartins.battleships.objects.PlayerClass;
import pt.rmartins.battleships.objects.Ship;
import pt.rmartins.battleships.objects.ShipClass;
import pt.rmartins.battleships.objects.ai.ShipComputer.SearchStatus;
import pt.rmartins.battleships.objects.modes.GameMode;
import pt.rmartins.battleships.utilities.MyPair;
import android.util.Log;

public class ComputerAI extends PlayerClass {

	private static final String TAG = ComputerAI.class.getSimpleName();

	public static final boolean ACTIVATE_DEBUG_AI = false;

	public static final boolean DEBUG_AI_SHOW_SCREEN = ACTIVATE_DEBUG_AI && true;
	public static final boolean DEBUG_AI = ACTIVATE_DEBUG_AI && DEBUG_AI_SHOW_SCREEN && true;
	public static final boolean DEBUG_AI_AUTO_SHOOT = ACTIVATE_DEBUG_AI && true;
	public static final boolean SHOW_MASSIVE_LOGS = Game.DEVELOPER_MODE && false;

	private final List<ShipComputer> searchShips;
	private final LinkedList<MessageAI> searchMessages;
	public final int[] shipCount;
	private boolean shipsLeftToFind;

	public static class CoordinateValue extends Coordinate {

		public final int value;

		public CoordinateValue(int x, int y, int value) {
			super(x, y);
			this.value = value;
		}

		@Override
		public int compareTo(Coordinate o) {
			if (o instanceof CoordinateValue) {
				final CoordinateValue other = (CoordinateValue) o;
				return this.value - other.value;
			}
			final int dx = this.x - o.x;
			if (dx == 0) {
				return this.y - o.y;
			} else {
				return dx;
			}
		}

		@Override
		public String toString() {
			return "(" + x + "," + y + "){" + value + "}";
		}
	}

	/**
	 * Coordinates to search for new ships to hit, the value the smaller, the less interference there is with previous
	 * shots fired.
	 */
	private final List<CoordinateValue> clearShots;
	public final List<Coordinate> markWaterList;

	public ComputerAI(String name, List<Ship> shipsToPlace, GameMode mode, GameClass game, boolean isPlayer1,
			boolean playingFirst) {
		super(name, shipsToPlace, mode, game, isPlayer1, playingFirst);

		shipCount = new int[ShipClass.numberOfShips()];
		for (final Ship ship : shipsToPlace) {
			shipCount[ship.getId()]++;
		}

		searchShips = new ArrayList<ShipComputer>();
		searchMessages = new LinkedList<MessageAI>();
		shipsLeftToFind = true;
		clearShots = new ArrayList<CoordinateValue>(maxX * maxY);
		markWaterList = new ArrayList<Coordinate>();
	}

	public void initialize(List<Ship> shipsToPlace) {
		for (final Ship ship : shipsToPlace) {
			searchShips.add(new ShipComputer(ship.getId(), this));
		}
	}

	private static class SearchShot {

		public final int knowledge, someKnowledge;

		public SearchShot(int knowledge, int someKnowledge) {
			this.knowledge = knowledge;
			this.someKnowledge = someKnowledge;
		}

		@Override
		public String toString() {
			return "SearchShot [" + knowledge + "," + someKnowledge + "]";
		}
	}

	/**
	 * Calculate where is the best places to shot
	 */
	public void playYourTurn() {
		if (DEBUG_AI) {
			synchronized (game) {
				playYourTurnAux();
			}
		} else {
			playYourTurnAux();
		}
	}

	public void playYourTurnAux() {
		updateGameStatus();

		final List<Coordinate> shots = new ArrayList<Coordinate>();

		turnTargetsLockWrite.lock();
		final int size = turnTargets.size();
		turnTargetsLockWrite.unlock();

		final List<SearchShot> shotType = new ArrayList<SearchShot>();

		final List<ShipComputer> knowledgeShips = getKnowledgeShips();
		final List<Coordinate> knowledgeSpots = new ArrayList<Coordinate>();
		final List<ShipComputer> someKnowledgeShips = getSomeKnowledgeShips();
		final List<Coordinate> someKnowledgeSpots = new ArrayList<Coordinate>(someKnowledgeShips.size());

		for (final ShipComputer ship : knowledgeShips) {
			final List<Coordinate> list = ship.getKnownPlaces();
			knowledgeSpots.addAll(list);
		}

		List<List<Coordinate>> listOfKnowledge = new ArrayList<List<Coordinate>>(someKnowledgeShips.size());
		for (final ShipComputer ship : someKnowledgeShips) {
			final List<Coordinate> mostProbablePlaces = ship.getMostProbablePlaces(5);
			if (!mostProbablePlaces.isEmpty())
				listOfKnowledge.add(mostProbablePlaces);
		}
		for (;;) {
			int index = interferenceOnList(listOfKnowledge);

			if (index == -1)
				break;
			else {
				final List<Coordinate> list = listOfKnowledge.get(index);
				list.remove(0);
				if (list.isEmpty())
					listOfKnowledge.remove(index);
			}
		}

		for (List<Coordinate> list : listOfKnowledge) {
			if (!list.isEmpty()) {
				Coordinate coor = list.get(0);
				if (!someKnowledgeSpots.contains(coor)) {
					someKnowledgeSpots.add(coor);
				}
			}
		}

		if (SHOW_MASSIVE_LOGS) {
			Log.i(TAG, "knowledgeSpots=" + knowledgeSpots);
			Log.i(TAG, "someKnowledgeSpots=" + someKnowledgeSpots);
			Log.i(TAG, "searchShips=" + searchShips);
			Log.i(TAG, "clearShots=" + clearShots);
			Log.i(TAG, "########################################################");
		}

		if (shipsLeftToFind) {
			for (int i = size - 1; i >= 0; i--) {
				for (int j = 0; j <= i; j++) {
					shotType.add(new SearchShot(j, i - j));
				}
			}

			for (final SearchShot searchShot : shotType) {
				if (knowledgeSpots.size() >= searchShot.knowledge
						&& someKnowledgeSpots.size() >= searchShot.someKnowledge) {
					for (int k = 0; k < searchShot.knowledge; k++) {
						final Coordinate shot = knowledgeSpots.get(k);
						shots.add(shot);
					}
					for (int k = 0; k < searchShot.someKnowledge; k++) {
						final Coordinate shot = someKnowledgeSpots.get(k);
						shots.add(shot);
					}
					shots.addAll(getSearchShots(shots, size - shots.size()));
					break;
				}
			}
		} else {
			int shotsLeft = size - shots.size() - knowledgeSpots.size() - someKnowledgeSpots.size();
			if (shotsLeft >= 0) {
				shots.addAll(knowledgeSpots);
				shots.addAll(someKnowledgeSpots);
			} else {
				shotsLeft = size - shots.size();
				for (int i = shotsLeft; i >= 0; i--) {
					for (int j = 0; j <= i; j++) {
						shotType.add(new SearchShot(j, i - j));
					}
				}

				for (final SearchShot searchShot : shotType) {
					if (knowledgeSpots.size() >= searchShot.knowledge
							&& someKnowledgeSpots.size() >= searchShot.someKnowledge) {
						for (int k = 0; k < searchShot.knowledge; k++) {
							final Coordinate shot = knowledgeSpots.get(k);
							shots.add(shot);
						}
						for (int k = 0; k < searchShot.someKnowledge; k++) {
							final Coordinate shot = someKnowledgeSpots.get(k);
							shots.add(shot);
						}
						break;
					}
				}
			}

			shotsLeft = size - shots.size();
			if (shotsLeft > 0) {
				moreSomeKnowledgeShots(shots, shotsLeft, someKnowledgeShips);
			}
		}

		synchronized (game) {
			for (final Coordinate shot : shots)
				this.chooseTarget(shot);
		}

		if (shotAll() != ShotAllResults.ShotsFired) {
			Log.i(TAG, "<Possible Blocked (or end game play)>");
			if (SHOW_MASSIVE_LOGS) {
				Log.i(TAG, "knowledgeSpots=" + knowledgeSpots);
				Log.i(TAG, "someKnowledgeSpots=" + someKnowledgeSpots);
				Log.i(TAG, "searchShips=" + searchShips);
				Log.i(TAG, "clearShots=" + clearShots);
				Log.i(TAG, "########################################################");
			}

			System.out.println("Shooting at random locations...");

			synchronized (game) {
				turnTargetsLockWrite.lock();

				int shotsLeft = size - shots.size();

				fors: for (int y = 0; y < maxY; y++) {
					for (int x = 0; x < maxX; x++) {
						Coordinate newPosition = new Coordinate(x, y);
						if (messageAt(x, y) == null && !shots.contains(newPosition)) {
							setPosition(newPosition);
							chooseTarget();

							shotsLeft--;
							if (shotsLeft == 0)
								break fors;
						}
					}
				}
				turnTargetsLockWrite.unlock();

				shotAll();
			}
		}
	}

	private int interferenceOnList(List<List<Coordinate>> listOfKnowledge) {
		for (int i = 0; i < listOfKnowledge.size(); i++) {
			for (int j = i + 1; j < listOfKnowledge.size(); j++) {
				Coordinate c1 = listOfKnowledge.get(i).get(0);
				Coordinate c2 = listOfKnowledge.get(j).get(0);
				if (interferShots(c1, c2)) {
					return Math.random() < .5 ? i : j;
				}
			}
		}
		return -1;
	}

	public void playYourTurnRandom() {
		while (!allShotsPlaced()) {
			setPositionToRandomLocation(false);
			chooseTarget();
		}
		shotAll();
	}

	/**
	 * Find remaining spots left to shoot
	 */
	private void moreSomeKnowledgeShots(List<Coordinate> shots, int shotsLeft, List<ShipComputer> someKnowledgeShips) {
		int tries = 0;
		final int MAX_TRIES = 50;
		while (shotsLeft > 0 && tries <= MAX_TRIES) {

			final List<Coordinate> someKnowledgeSpots = new ArrayList<Coordinate>();
			for (final ShipComputer ship : someKnowledgeShips) {
				final List<Coordinate> list = ship.getMostProbablePlaces(1);
				for (final Coordinate coor : list) {
					if (!shots.contains(coor) && !someKnowledgeSpots.contains(coor)) {
						someKnowledgeSpots.add(coor);
					}
				}
			}

			/**
			 * has they are the last shots of the match it doesn't matter that they interfere with each other!
			 */

			Collections.shuffle(someKnowledgeSpots, GameClass.random);

			for (int i = 0; i < someKnowledgeSpots.size(); i++) {
				shots.add(someKnowledgeSpots.get(i));
				shotsLeft--;
				if (shotsLeft == 0)
					break;
			}

			tries++;
		}
	}

	private void generateClearShots() {
		final int MAX_LIMIT = 1000000;

		final int[][] field = new int[maxX][maxY];

		for (int x = 0; x < maxX; x++) {
			for (int y = 0; y < maxY; y++) {
				if (markAt(x, y).isWater())
					field[x][y] += MAX_LIMIT;
				else if (messageAt(x, y) != null)
					field[x][y] += MAX_LIMIT;
			}
		}

		for (final ShipComputer ship : searchShips) {
			final SearchStatus searchStatus = ship.getSearchStatus();
			if (searchStatus == SearchStatus.All) {
				for (final Coordinate coor : ship) {
					for (int i = -1; i < 1; i++) {
						for (int j = -1; j < 1; j++) {
							final int x = coor.x + i;
							final int y = coor.y + j;
							if (game.isInsideField(x, y)) {
								field[x][y] += MAX_LIMIT;
							}
						}
					}
				}
			} else if (searchStatus == SearchStatus.Some) {
				final List<Ship> allPlaces = ship.getAllPlaces();
				for (Iterator<Ship> iterator = allPlaces.iterator(); iterator.hasNext();) {
					Ship possibility = iterator.next();
					for (final Coordinate coor : possibility) {
						field[coor.x][coor.y] += 1;
						for (int i = -1; i < 1; i++) {
							for (int j = -1; j < 1; j++) {
								final int x = coor.x + i;
								final int y = coor.y + j;
								if (game.isInsideField(x, y))
									field[x][y] += 1;
							}
						}
					}
				}
			}
		}

		clearShots.clear();
		for (int x = 0; x < maxX; x++) {
			for (int y = 0; y < maxY; y++) {
				if (field[x][y] < MAX_LIMIT)
					clearShots.add(new CoordinateValue(x, y, field[x][y]));
			}
		}
		Collections.shuffle(clearShots, GameClass.random);
		Collections.sort(clearShots);
	}

	private void updateGameStatus() {
		final long initialTime = System.currentTimeMillis();

		MessageAI lastMessage;
		{
			messagesLockRead.lock();
			final Message last = messages.isEmpty() ? null : messages.get(0);
			messagesLockRead.unlock();
			if (last == null)
				return;
			else if (last.isAllWater())
				lastMessage = null;
			else
				lastMessage = new MessageAI(last);
		}

		// TODO: AI fazer uma heuristica para que se uma jogada acertar em tudo e houver tiros "colados" ou seja dist
		// x,y <= 1, não podem ser todos barcos diferentes senão estavam pegados uns aos outros

		if (lastMessage != null) {
			searchMessages.add(lastMessage);

			final List<MessageUnit> parts = lastMessage.getParts();
			boolean all1spaceKills = true;
			int lastShipId = -1;
			for (final MessageUnit messageUnit : parts) {
				if (messageUnit.type != TypesMessageUnits.AKillerShot
						|| ShipClass.getNumberPieces(messageUnit.shipId) != 1
						|| (lastShipId != -1 && messageUnit.shipId != lastShipId)) {
					all1spaceKills = false;
					break;
				}
				lastShipId = messageUnit.shipId;
			}
			if (all1spaceKills) {
				for (final Coordinate coor : lastMessage.getCoors()) {
					for (final ShipComputer ship : searchShips) {
						if (ship.getId() == lastShipId && ship.getSearchStatus() == SearchStatus.None) {
							ship.addKnownSpot(coor);
							ship.setDestroyed();
							break;
						}
					}
				}
			} else {
				int maxDifferentShips;
				{
					maxDifferentShips = 0;
					final List<Coordinate> coors = lastMessage.getCoors();
					for (int i = 0; i < coors.size(); i++) {
						boolean near = false;
						for (int j = i + 1; j < coors.size(); j++) {
							if (coors.get(i).near(coors.get(j))) {
								near = true;
							}
						}
						if (!near)
							maxDifferentShips++;
					}
					maxDifferentShips = Math.min(maxDifferentShips, lastMessage.getHits());
				}
				int diferentShips = maxDifferentShips;

				for (final MessageUnit messageUnit : parts) {
					final int shipId = messageUnit.shipId;
					final TypesMessageUnits type = messageUnit.type;
					if (type == TypesMessageUnits.AShot) {

						boolean foundNewShip = false;

						if (shipCount[shipId] == 1) {
							/**
							 * Unique ship ==> 100% sure ship
							 */
							for (final ShipComputer ship : searchShips) {
								if (ship.getId() == shipId && !ship.isDestroyed()) {
									if (ship.getSearchStatus() == SearchStatus.None) {
										if (!foundNewShip && diferentShips > 0) {
											ship.addPossibleSpot(lastMessage, false);
											foundNewShip = true;
										}
									} else {
										ship.addPossibleSpot(lastMessage, false);
									}
									diferentShips--;
									break;
								}
							}
						} else {
							for (final ShipComputer ship : searchShips) {
								if (ship.getId() == shipId && !ship.isDestroyed()) {
									if (ship.getSearchStatus() == SearchStatus.None) {
										if (!foundNewShip && diferentShips > 0) {
											ship.addPossibleSpot(lastMessage, false);
											foundNewShip = true;
											diferentShips--;
										}
									} else {
										ship.addPossibleSpot(lastMessage, false);
									}
								}
							}
						}
					} else if (type == TypesMessageUnits.AKillerShot) {
						final int numberPieces = ShipClass.getNumberPieces(shipId);
						if (numberPieces == 1) {
							for (final ShipComputer ship : searchShips) {
								if (ship.getId() == shipId && ship.getSearchStatus() == SearchStatus.None) {
									ship.addPossibleSpot(lastMessage, true);
									ship.setDestroyed();
									break;
								}
							}
						} else {
							for (final ShipComputer ship : searchShips) {
								if (ship.getId() == shipId && ship.getSearchStatus() == SearchStatus.Some
										&& !ship.isDestroyed()) {
									if (ship.addPossibleSpot(lastMessage, true))
										ship.setDestroyed();
								}
							}
						}
					}
				}
			}
		}

		final long finalTime = System.currentTimeMillis();
		Log.i(TAG, "updateGameStatus: " + (finalTime - initialTime));

		/**
		 * Update all information until there are no more changes
		 */
		do {
		} while (updateAllRefecences());
	}

	/**
	 * Updates: <br>
	 * - ship.calculateAllPlaces() (for all ships) <br>
	 * - shipsLeftToFind boolean var <br>
	 * 
	 * 
	 * @return if any information was updated
	 */
	private boolean updateAllRefecences() {
		final long initialTime = System.currentTimeMillis();
		int DEBUG_INDEX = 0;

		boolean updatedSomething = false;

		/**
		 * Update all ships
		 */
		for (final ShipComputer ship : searchShips) {
			ship.updateAllPlaces();
		}
		markWaterList.clear();

		writeDebug(initialTime, DEBUG_INDEX++);

		shipsLeftToFind = false;
		for (final ShipComputer ship : searchShips) {
			if (ship.getSearchStatus() == SearchStatus.None) {
				shipsLeftToFind = true;
				break;
			}
		}

		List<Coordinate> waterToRemoveList = new LinkedList<Coordinate>();
		for (final MessageAI message : searchMessages) {
			if (!message.isAllWater() && message.hasSomeWater()) {
				/**
				 * Verify if any of the coordinates of the message is water
				 */
				for (final Coordinate coor : message.getCoors()) {
					if (markAt(coor) == Mark.None) {
						found_label: {
							for (final ShipComputer ship : searchShips) {
								if (ship.getSearchStatus() == SearchStatus.Some) {
									final List<Ship> allPlaces = ship.getAllPlaces();
									for (final Ship possibility : allPlaces) {
										if (possibility.getListPieces().contains(coor)) {
											break found_label;
										}
									}
								} else if (ship.getSearchStatus() == SearchStatus.All) {
									if (ship.pieceAt(coor)) {
										break found_label;
									}
								}
							}
							waterToRemoveList.add(coor);
						}
					}
				}

				{
					/**
					 * test if the unknown message coors are water:
					 */
					final List<Coordinate> unknownMCoorList = new ArrayList<Coordinate>();
					int count = 0;
					for (final Coordinate coor : message.getCoors()) {
						if (markAt(coor).isWater()) {
							count++;
						} else if (!knownCoor(coor)) {
							unknownMCoorList.add(coor);
						}
					}
					if (count == message.getMisses() && !unknownMCoorList.isEmpty()) {
						// <<< Here only the messages that all the water is known pass >>>

						/**
						 * Marks remaining message coors with Mark.Ship
						 */
						for (final Coordinate coor : unknownMCoorList) {
							if (markAt(coor) == Mark.None) {
								setMarkAt(coor, Mark.Ship);
								updatedSomething = true;

								/**
								 * TODO: verifyif there is a ship there (of course there is!) <br>
								 * and update it's postiion!!!!!!!!!!!
								 */
							}
						}

						/**
						 * Try to update with the new info from the message
						 */
						for (final ShipComputer ship : searchShips) {
							final MyPair<MessageAI, Boolean> pair = ship.hasAnyMessage(message);
							if (ship.getSearchStatus() == SearchStatus.Some && pair != null) {
								for (final Coordinate coor : unknownMCoorList) {
									boolean coorOwner = true;
									final List<Ship> allPlaces = ship.getAllPlaces();
									for (final Ship possibility : allPlaces) {
										if (!possibility.pieceAt(coor)) {
											coorOwner = false;
											break;
										}
									}

									/**
									 * coorOwner == true ==> that coor must belong to the ship
									 */
									if (coorOwner) {
										ship.removePossibleSpot(pair.first, pair.second);
										ship.addKnownSpot(coor);
										if (markAt(coor) == Mark.None)
											setMarkAt(coor, Mark.Ship);
										updateMessageShips(pair, ship);
										updatedSomething = true;
									}
								}
							}
						}
					}
				}
				{
					/**
					 * try to find missing water
					 */
					final List<Coordinate> unknownMCoorList = new ArrayList<Coordinate>();
					int count = 0;
					for (final Coordinate coor : message.getCoors()) {
						if (markAt(coor).isShip()) {
							count++;
						} else if (!knownCoor(coor) && markAt(coor) == Mark.None) {
							unknownMCoorList.add(coor);
						}
					}
					if (count == message.getHits() && !unknownMCoorList.isEmpty()) {
						// <<< Here only the messages that all the ships is known pass >>>

						/**
						 * Marks remaining message coors with Mark.Water
						 */
						for (Coordinate coor : unknownMCoorList) {
							waterToRemoveList.add(coor);
						}
					}
				}
			}
		}

		if (!waterToRemoveList.isEmpty()) {
			for (Coordinate coor : waterToRemoveList) {
				setMarkAt(coor, Mark.Water);
			}
			updatedSomething = true;
		}
		for (Iterator<MessageAI> iterator = searchMessages.iterator(); iterator.hasNext();) {
			MessageAI message = iterator.next();
			if (message.getParts().isEmpty()) {
				iterator.remove();
				updatedSomething = true;
			}
		}

		writeDebug(initialTime, DEBUG_INDEX++);

		// testa se alguma das jogadas marcadas pelo barco, existe alguma coordenada que passa a "known"
		// exmeplo: na segunda jogada sabemos que na coordena (x,y) é o barco, e não existe ambiguidade, podemos marcar
		// essa nova informação

		// TODO: AI melhorar para também funcionar quando existem vários barcos do mesmo tipo
		for (final ShipComputer ship : searchShips) {
			if (ship.getSearchStatus() == SearchStatus.Some) {
				final List<MyPair<MessageAI, Boolean>> removeList = new ArrayList<MyPair<MessageAI, Boolean>>();
				for (final MyPair<MessageAI, Boolean> pair : ship.getPossibleSpotsMessages()) {
					final Message m = pair.first;
					int equalCount = -1;
					List<Coordinate> equalList = new ArrayList<Coordinate>();
					boolean allEqual = true;
					final List<Ship> allPlaces = ship.getAllPlaces();
					for (final Ship possibility : allPlaces) {
						int count = 0;
						final List<Coordinate> list = new ArrayList<Coordinate>();
						for (final Coordinate coor : m.getCoors()) {
							if (possibility.pieceAt(coor)) {
								list.add(coor);
								count++;
							}
						}
						if (count == m.getHits(ship.getId())) {
							if (equalCount == -1) {
								equalCount = count;
								equalList = list;
							} else if (equalCount != count || !equalList.equals(list)) {
								allEqual = false;
								break;
							}
						}
					}
					// TODO: AI testar isto
					if (allEqual && !equalList.isEmpty()) {
						removeList.add(pair);
						for (Coordinate coor : equalList) {
							ship.addKnownSpot(coor);
						}
						for (Coordinate coor : equalList) {
							if (markAt(coor) == Mark.None)
								setMarkAt(coor, Mark.Ship);
						}
						updateMessageShips(pair, ship);
						updatedSomething = true;
					}
				}
				for (final MyPair<MessageAI, Boolean> pair : removeList) {
					ship.removePossibleSpot(pair.first, pair.second);
				}
			}
		}

		writeDebug(initialTime, DEBUG_INDEX++);

		/**
		 * NEW STUFF :D
		 * 
		 * If all-1 coors in a message are known then the remaining coordinate is also a known coor
		 */
		for (final ShipComputer ship : searchShips) {
			if (ship.getSearchStatus() == SearchStatus.Some) {
				final List<MyPair<MessageAI, Boolean>> removeList = new ArrayList<MyPair<MessageAI, Boolean>>();
				if (ship.getNumberPieces() == 1) {
					for (final MyPair<MessageAI, Boolean> pair : ship.getPossibleSpotsMessages()) {
						final MessageAI message = pair.first;
						final List<Coordinate> coors = message.getCoors();
						final List<Coordinate> coorsCopy = new LinkedList<Coordinate>(coors);
						for (Iterator<Coordinate> iterator = coorsCopy.iterator(); iterator.hasNext();) {
							Coordinate coor = iterator.next();
							if (knownCoor(coor))
								iterator.remove();
						}
						if (coorsCopy.size() == 1) {
							removeList.add(pair);
							final Coordinate coor = coorsCopy.get(0);
							ship.addKnownSpot(coor);
							if (markAt(coor) == Mark.None)
								setMarkAt(coor, Mark.Ship);
							updateMessageShips(pair, ship);
							updatedSomething = true;
						}
					}
				}
				for (final MyPair<MessageAI, Boolean> pair : removeList) {
					ship.removePossibleSpot(pair.first, pair.second);
				}
			}
		}

		writeDebug(initialTime, DEBUG_INDEX++);

		/**
		 * Place water/ship in the common places of the ship possibilities
		 */
		for (final ShipComputer ship : searchShips) {
			if (ship.getSearchStatus() == SearchStatus.Some) {
				final Set<Coordinate> waterCommonParts = new HashSet<Coordinate>();
				final Set<Coordinate> shipCommonParts = new HashSet<Coordinate>();
				final List<Ship> allPlaces = ship.getAllPlaces();
				Iterator<Ship> iterator = allPlaces.iterator();

				Ship firstPosition = iterator.next();
				waterCommonParts.addAll(Coordinate.allAround(firstPosition));
				waterCommonParts.removeAll(firstPosition.getListPieces());
				shipCommonParts.addAll(firstPosition.getListPieces());
				for (; iterator.hasNext();) {
					final Ship possibility = iterator.next();
					if (!waterCommonParts.isEmpty()) {
						final Set<Coordinate> allAround = Coordinate.allAround(possibility);
						allAround.removeAll(possibility.getListPieces());
						waterCommonParts.retainAll(allAround);
					}
					if (!shipCommonParts.isEmpty())
						shipCommonParts.retainAll(possibility.getListPieces());
					if (waterCommonParts.isEmpty() && shipCommonParts.isEmpty())
						break;
				}
				for (final Coordinate coor : waterCommonParts) {
					if (game.isInsideField(coor) && markAt(coor) == Mark.None) {
						setMarkAt(coor, Mark.Water);
						updatedSomething = true;
					}
				}
				for (final Coordinate coor : shipCommonParts) {
					if (game.isInsideField(coor) && markAt(coor) == Mark.None) {
						setMarkAt(coor, Mark.Ship);
						updatedSomething = true;
					}
				}
			}
		}

		writeDebug(initialTime, DEBUG_INDEX++);
		return updatedSomething;
	}

	private void writeDebug(long initialTime, int i) {
		final long finalTime = System.currentTimeMillis();
		Log.i(TAG, "updateAllRefecences(" + i + "): " + (finalTime - initialTime));
	}

	private boolean knownCoor(Coordinate coor) {
		for (final ShipComputer ship : searchShips) {
			if (ship.isAKnownSpot(coor))
				return true;
		}
		return false;
	}

	public void computerPlaceYourShips(boolean justSimpleRandom) {
		placeShipsRandom(justSimpleRandom);
	}

	private void placeShipsRandom(boolean justSimpleRandom) {
		if (Game.DEBUG2) {
			int x = 0;
			while (x < maxX && getSelectedShip() != null) {
				movePositionAbsolute(x, 0);
				placeShip();
				x += 2;
			}
			if (getSelectedShip() == null) {
				tryToSetReady();
				return;
			}
		}

		placeAllShipsRandom(50, justSimpleRandom);
		tryToSetReady();
	}

	private boolean interferShots(Coordinate coor1, Coordinate coor2) {
		for (final ShipComputer ship : searchShips) {
			if (ship.canBeHited(coor1) && ship.canBeHited(coor2)) {
				return true;
			}
		}
		return false;
	}

	private List<Coordinate> getSearchShots(List<Coordinate> previousShots, int size) {
		final List<Coordinate> list = new ArrayList<Coordinate>(size);
		if (size > 0) {
			generateClearShots();

			/**
			 * We can add an aditional for or extract method to "run" this algorithm multiple times to get maximum shots
			 * possible with more random chance
			 * 
			 * 
			 */

			final int MAX = Math.min(3, Math.min(maxX, maxY));

			list.clear();
			return_label: for (int maxDistTry = MAX; maxDistTry >= 2; maxDistTry--) {
				for (final CoordinateValue coor : clearShots) {
					if (list.contains(coor))
						continue;

					boolean canAdd = true;
					for (Coordinate other : previousShots) {
						if (coor.dist(other) < maxDistTry) {
							canAdd = false;
							break;
						}
					}
					for (Coordinate other : list) {
						if (coor.dist(other) < maxDistTry) {
							canAdd = false;
							break;
						}
					}

					if (canAdd) {
						list.add(coor);
						size--;

						if (size == 0)
							break return_label;
					}
				}
			}
		}
		return list;
	}

	private List<ShipComputer> getKnowledgeShips() {
		final List<ShipComputer> result = new ArrayList<ShipComputer>();
		for (final ShipComputer ship : searchShips) {
			if (ship.getSearchStatus() == SearchStatus.All && !ship.isDestroyed())
				result.add(ship);
		}
		Collections.shuffle(result, GameClass.random);
		return result;
	}

	private List<ShipComputer> getSomeKnowledgeShips() {
		final List<ShipComputer> result = new ArrayList<ShipComputer>();
		for (final ShipComputer ship : searchShips) {
			if (ship.getSearchStatus() == SearchStatus.Some)
				result.add(ship);
		}
		Collections.shuffle(result, GameClass.random);
		return result;
	}

	public ShipComputer nearKnownSpot(Coordinate coor) {
		for (final ShipComputer ship : searchShips) {
			if (ship.nearKnownSpot(coor))
				return ship;
		}
		return null;
	}

	@Override
	protected void setMarkAt(int x, int y, Mark mark) {
		super.setMarkAt(x, y, mark);
		if (mark.isWater()) {
			markWaterList.add(new Coordinate(x, y));

			Coordinate markCoor = new Coordinate(x, y);
			for (final ShipComputer ship : searchShips) {
				for (final MyPair<MessageAI, Boolean> pair : ship.getPossibleSpotsMessages()) {
					final MessageAI message = pair.first;
					if (message.getCoors().contains(markCoor)) {
						message.getCoors().remove(markCoor);
						message.removeWater();
						ship.updateAllPlaces();
					}
				}
			}

			for (MessageAI message : searchMessages) {
				if (message.getCoors().contains(markCoor)) {
					message.getCoors().remove(markCoor);
					message.removeWater();
					break;
				}
			}
		} else if (mark.isShip()) {
			Coordinate markCoor = new Coordinate(x, y);
			for (final ShipComputer ship : searchShips) {
				for (final MyPair<MessageAI, Boolean> pair : ship.getPossibleSpotsMessages()) {
					final MessageAI message = pair.first;
					if (message.getCoors().contains(markCoor)) {
						ship.updateAllPlaces();
						break;
					}
				}
			}
		}
	}

	public void updateMessageShips(MyPair<MessageAI, Boolean> destroyedPair, ShipComputer destroyedShip) {
		final MessageAI destroyedMessage = destroyedPair.first;
		final Boolean killerShot = destroyedPair.second;
		for (final ShipComputer ship : searchShips) {
			if (ship != destroyedShip) {
				for (final MyPair<MessageAI, Boolean> pair : ship.getPossibleSpotsMessages()) {
					final MessageAI message = pair.first;
					if (message.getTurnId().equals(destroyedMessage.getTurnId())) {
						for (Coordinate coor : destroyedShip.getListPieces()) {
							if (message.getCoors().contains(coor)) {
								message.getCoors().remove(coor);
								message.removeShip(destroyedShip.getId(), killerShot);
								ship.updateAllPlaces();
							}
						}
					}
				}
			}
		}
	}
}
