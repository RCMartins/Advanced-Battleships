package pt.rmartins.battleships.objects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import pt.rmartins.battleships.objects.Game.GameState;
import pt.rmartins.battleships.objects.Game.Mark;
import pt.rmartins.battleships.objects.Game.PlayingMode;
import pt.rmartins.battleships.objects.Game.TurnState;
import pt.rmartins.battleships.objects.Game.TurnTypes;
import pt.rmartins.battleships.objects.Message.MessageUnit;
import pt.rmartins.battleships.objects.Message.MessageUnit.TypesMessageUnits;
import pt.rmartins.battleships.objects.PlayerClass.Shot.KindShot;
import pt.rmartins.battleships.objects.PlayerClass.Shot.SpecialEffectTypes;
import pt.rmartins.battleships.objects.modes.GameBonus;
import pt.rmartins.battleships.objects.modes.GameBonus.ExtraTime;
import pt.rmartins.battleships.objects.modes.GameMode;
import pt.rmartins.battleships.objects.modes.GameMode.BonusPlay;
import pt.rmartins.battleships.objects.modes.GameMode.ShipExtraInfo;
import pt.rmartins.battleships.objects.modes.GameMode.TimeLimitType;
import pt.rmartins.battleships.sound.SoundType;
import pt.rmartins.battleships.utilities.StopWatch;

public class PlayerClass implements Player {

	protected static final String SHIPHITFORMAT = "%s";
	protected static final String SHIPDESTROYEDFORMAT = "%s!";
	protected Player enemy;
	protected final Game game;
	protected final GameMode mode;

	protected final String playerName;
	protected final int maxX, maxY;
	protected final boolean imPlayer1;
	protected boolean shipFieldVisible;
	protected final List<Ship> ships, unmodifiableShips;
	protected final Message[][] messagesAt;
	protected final Mark[][] marks;
	protected final Ship[][] quickShips;
	protected final boolean[][] nearShips;

	protected Coordinate position;
	protected int nextTargetToPlace;
	protected Lock turnTargetsLockRead, turnTargetsLockWrite;
	protected final List<Shot> turnTargets, turnTargetsUnmodifiableList;
	protected List<Coordinate> targetList;
	protected List<Coordinate> networkCounterList;

	protected Ship selectedShip;
	protected final List<Ship> shipsLeftToPlace;
	protected boolean ready;
	protected Lock messagesLockRead, messagesLockWrite;
	protected List<Message> messages;
	protected Message lastMessage;

	protected int totalKilledShips;
	protected int extraTurns, explosionTurns;

	protected final List<GameBonus> bonusInNextTurn;
	protected StatisticsClass statistics;
	protected List<Ship> lastTurnKilledShips;
	protected final StopWatch timeOfPlayer;
	protected final boolean playingFirst;
	private int killingTurns;

	private boolean optimize;
	protected boolean rightClickMenuMode;

	public PlayerClass(String name, List<Ship> shipsToPlace, GameMode mode, Game game, boolean isPlayer1,
			boolean playingFirst) {
		this.game = game;
		this.mode = mode;

		this.playerName = name;
		this.maxX = game.getMaxX();
		this.maxY = game.getMaxY();
		this.imPlayer1 = isPlayer1;
		this.playingFirst = playingFirst;
		this.ships = new ArrayList<Ship>();
		this.unmodifiableShips = Collections.unmodifiableList(ships);

		optimize = false;
		messagesAt = new Message[maxX][maxY];
		marks = new Mark[maxX][maxY];
		quickShips = new ShipClass[maxX][maxY];
		nearShips = new boolean[maxX][maxY];
		for (int y = 0; y < maxY; y++) {
			for (int x = 0; x < maxX; x++) {
				messagesAt[x][y] = null;
				marks[x][y] = Mark.None;
				quickShips[x][y] = null;
				nearShips[x][y] = false;
			}
		}

		rightClickMenuMode = true;

		lastTurnKilledShips = new ArrayList<Ship>();

		position = new Coordinate(maxX / 2, maxY / 2);
		nextTargetToPlace = 0;
		final ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock();
		turnTargetsLockRead = reentrantReadWriteLock.readLock();
		turnTargetsLockWrite = reentrantReadWriteLock.writeLock();
		turnTargets = new ArrayList<Shot>();
		turnTargetsUnmodifiableList = Collections.unmodifiableList(turnTargets);
		targetList = new ArrayList<Coordinate>();
		ReadWriteLock messagesLock = reentrantReadWriteLock;
		messagesLockRead = messagesLock.readLock();
		messagesLockWrite = messagesLock.writeLock();
		messages = new LinkedList<Message>();
		lastMessage = null;
		bonusInNextTurn = new LinkedList<GameBonus>();

		statistics = new StatisticsClass(game, this);
		totalKilledShips = 0;
		extraTurns = explosionTurns = 0;
		shipFieldVisible = true;

		shipsLeftToPlace = new ArrayList<Ship>(shipsToPlace.size());
		for (final Ship ship : shipsToPlace) {
			shipsLeftToPlace.add(new ShipClass(ship));
		}

		if (shipsLeftToPlace != null && shipsLeftToPlace.size() > 0) {

			Collections.sort(shipsLeftToPlace, new Comparator<Ship>() {
				@Override
				public int compare(Ship o1, Ship o2) {
					// int s1 = o1.sizeX() * o1.sizeY();// o1.getNumberPieces();
					// int s2 = o2.sizeX() * o2.sizeY();// o2.getNumberPieces();
					// return s1 != s2 ? s2 - s1 : o1.getNumberPieces() - o2.getNumberPieces(); // o1.getId() -
					// o2.getId();
					int s1 = o1.getSpace();
					int s2 = o2.getSpace();
					if (s1 != s2)
						return s2 - s1;
					else {
						s1 = o1.getNumberPieces();
						s2 = o2.getNumberPieces();
						return s1 != s2 ? s2 - s1 : o2.getId() - o1.getId(); // o1.getId() -
					}

				}
			});

			setSelectedShip(shipsLeftToPlace.get(0), true);
			setPositionToRandomLocation(true);
		}

		timeOfPlayer = new StopWatch();

		ready = false;
	}

	protected void addMessage(Message m) {
		messagesLockWrite.lock();
		messages.add(0, m);
		messagesLockWrite.unlock();
	}

	// public void addShip(Ship ship) {
	// ships.add(ship);
	// }

	@Override
	public boolean canPlaceShip() {
		if (game.getGameState() == GameState.PlacingShips && selectedShip != null) {
			return canPlaceShip(selectedShip);
		}
		return false;
	}

	protected boolean canPlaceShip(Ship otherShip) {
		return !otherShip.near(ships);
	}

	protected void chooseSound() {
		if (lastMessage.getHits() > 0) {
			game.playSound(SoundType.Missile);
		} else {
			game.playSound(SoundType.Water);
		}

		if (lastMessage.wasFirstBlood())
			game.playSound(SoundType.FirstBlood);

		killingTurns = 0;
		if (lastMessage.getKills() > 0) {
			killingTurns++;
			messagesLockRead.lock();
			for (final Message m : messages) {
				if (m.getKills() > 0) {
					killingTurns++;
				} else {
					break;
				}
			}
			messagesLockRead.unlock();
		}

		/*
		 * Killing Spree (Three kills in a row) <br>
		 * Dominating ( Four kills in a row) <br>
		 * Mega Kill! ( Five kills in a row) <br>
		 * Unstoppable! ( Six kills in a row) <br>
		 * Wicked Sick( Seven kills in a row) <br>
		 * Monster Kill!!! ( Eight kills in a row) <br>
		 * Godlike! (Nine kills in a row) <br>
		 * Beyond Godlike! (Ten to infinite kills in a row)
		 */

		if (killingTurns == KillingSpreeTurns) {
			game.playSound(SoundType.KillingSpree);
		} else if (killingTurns == DominatingTurns) {
			game.playSound(SoundType.Dominating);
		} else if (killingTurns == MegaKillTurns) {
			game.playSound(SoundType.MegaKill);
		} else if (killingTurns == UnstoppableTurns) {
			game.playSound(SoundType.Unstoppable);
		} else if (killingTurns == MonsterKillTurns) {
			game.playSound(SoundType.MonsterKill);
		} else if (killingTurns == GodlikeTurns) {
			game.playSound(SoundType.GodLike);
		} else if (killingTurns >= HolyShitTurns) {
			game.playSound(SoundType.HolyShit);
		}

		switch (lastTurnKilledShips.size()) {
		case 0:
		case 1:
			break;
		case 2:
			game.playSound(SoundType.DoubleKill);
			break;
		case 3:
			game.playSound(SoundType.TripleKill);
			break;
		case 4:
			game.playSound(SoundType.UltraKill);
			break;
		default:
			game.playSound(SoundType.Rampage);
			break;
		}

		// if (totalKilledShips == game.getCurrentFleet().size())
		// game.playSound(AvaibleSounds.MonsterKill);
	}

	@Override
	public void chooseTarget() {
		chooseTarget(position);
	}

	public void chooseTarget(Coordinate coor) {
		if (game.getCurrentPlayer() != this || game.getTurnState() == TurnState.ChooseTargets) {
			turnTargetsLockWrite.lock();
			return_label: {

				for (int i = 0; i < turnTargets.size(); i++) {
					final Shot target = turnTargets.get(i);
					if (target.isPlaced && target.include(coor)) {
						target.isPlaced = false;
						nextTargetToPlace = i;

						break return_label;
					}
				}

				if (allShotsPlaced())
					break return_label;

				final Shot shot = turnTargets.get(nextTargetToPlace);
				shot.coordinate = coor;
				if (shot.validateShot()) {
					shot.isPlaced = true;
					selectNextTargetToPlace();
				}
			}
			turnTargetsLockWrite.unlock();
		}
	}

	@Override
	public Shot getNextTargetToPlace() {
		if (allShotsPlaced())
			return null;
		turnTargetsLockRead.lock();
		final Shot result = turnTargets.get(nextTargetToPlace);
		turnTargetsLockRead.unlock();
		return result;
	}

	private void selectNextTargetToPlace() {
		turnTargetsLockRead.lock();
		final int size = turnTargets.size();
		if (turnTargets.get(nextTargetToPlace).isPlaced) {
			final int n = nextTargetToPlace;
			nextTargetToPlace = (nextTargetToPlace + 1) % size;
			while (n != nextTargetToPlace) {
				if (!turnTargets.get(nextTargetToPlace).isPlaced) {
					turnTargetsLockRead.unlock();
					return;
				}
				nextTargetToPlace = (nextTargetToPlace + 1) % size;
			}
		}
		turnTargetsLockRead.unlock();
	}

	@Override
	public Player getEnemy() {
		return enemy;
	}

	@Override
	public int getKilledShips() {
		return totalKilledShips;
	}

	@Override
	public List<Message> getMessagesLock() {
		messagesLockRead.lock();
		return messages;
	}

	@Override
	public void getMessagesUnlock() {
		messagesLockRead.unlock();
	}

	@Override
	public Coordinate getPosition() {
		return position;
	}

	@Override
	public int getPositionX() {
		return position.x;
	}

	@Override
	public int getPositionY() {
		return position.y;
	}

	@Override
	public Ship getSelectedShip() {
		return selectedShip;
	}

	@Override
	public List<Shot> getTurnTargets() {
		turnTargetsLockRead.lock();
		return turnTargetsUnmodifiableList;
	}

	@Override
	public void unlockTurnTargets() {
		turnTargetsLockRead.unlock();
	}

	@Override
	public boolean isReady() {
		return ready;
	}

	@Override
	public Mark markAt() {
		return marks[position.x][position.y];
	}

	@Override
	public Mark markAt(int x, int y) {
		return marks[x][y];
	}

	@Override
	public Mark markAt(Coordinate coor) {
		return marks[coor.x][coor.y];
	}

	@Override
	public void movePosition(int x, int y) {
		movePositionAbsolute(position.x + x, position.y + y);
	}

	@Override
	public void movePositionAbsolute(int x, int y) {
		final int nX = x;
		final int nY = y;

		if (game.getGameState() == GameState.PlacingShips && selectedShip != null) {
			selectedShip.moveTo(nX, nY);

			if (!game.isInsideField(selectedShip)) {
				selectedShip.trimShip(maxX, maxY);
			} else {
				position = new Coordinate(nX, nY);
			}
		} else if (game.isInsideField(nX, nY)) {
			position = new Coordinate(nX, nY);
			Shot shot;
			if ((shot = getNextTargetToPlace()) != null)
				shot.coordinate = position;
		}
	}

	@Override
	public Message messageAt(Coordinate coor) {
		return messagesAt[coor.x][coor.y];
	}

	@Override
	public Message messageAt(int x, int y) {
		return messagesAt[x][y];
	}

	private void setMessageAt(Coordinate coor, Message message) {
		messagesAt[coor.x][coor.y] = message;
	}

	private boolean placeAllShipsRandom(boolean justSimpleRandom) {
		while (getSelectedShip() != null) {
			if (!setPositionToRandomLocation(justSimpleRandom)) {
				return false;
			}
			placeShip(false);
		}
		return true;
	}

	@Override
	public boolean placeAllShipsRandom(int retryTimes, boolean justSimpleRandom) {
		int howManyShipsLeft = shipsLeftToPlace.size();

		boolean allPlaced = false;
		for (int i = 0; i <= retryTimes && !allPlaced; i++) {
			allPlaced = placeAllShipsRandom(justSimpleRandom);
			if (!allPlaced) {
				for (int k = 0; k < howManyShipsLeft; k++)
					undoLastPlacedShip();
			}
		}
		return allPlaced;
	}

	@Override
	public boolean placeShip() {
		return placeShip(true);
	}

	private boolean placeShip(boolean needCheck) {
		final boolean result = !needCheck || canPlaceShip();
		if (result) {
			ships.add(new ShipClass(selectedShip));
			shipsLeftToPlace.remove(selectedShip);
			if (!shipsLeftToPlace.isEmpty()) {
				setSelectedShip(shipsLeftToPlace.get(0), true);
			} else {
				setSelectedShip(null, true);
			}
		}
		return result;
	}

	@Override
	public void placeShipNetwork(Ship ship) {
		ships.add(new ShipClass(ship));
	}

	@Override
	public void cancelSetReady() {
		ships.clear();
		ready = false;
	}

	@Override
	public Coordinate rotateShipClockwise() {
		if (selectedShip != null) {
			return selectedShip.rotateClockwise(maxX, maxY);
		} else {
			return new Coordinate(0, 0);
		}
	}

	@Override
	public void setEnemy(Player enemy) {
		this.enemy = enemy;
	}

	protected void setMarkAt(int x, int y, Mark mark) {
		if (game.isInsideField(x, y))
			marks[x][y] = mark;
	}

	protected void setMarkAt(Coordinate coor, Mark mark) {
		setMarkAt(coor.x, coor.y, mark);
	}

	@Override
	public void setMarkAt(Mark mark) {
		setMarkAt(position.x, position.y, mark);
	}

	/**
	 * @param numberOfTargets
	 *            Number of targets for this turn.
	 */
	@Override
	public void setNumberOfTargets(List<List<KindShot>> list) {
		turnTargetsLockWrite.lock();
		turnTargets.clear();

		List<KindShot> turn;
		if (game.getTurnType() == TurnTypes.Normal) {
			final double real = game.getRealTurnNumber();
			int number = game.getTurnNumber();
			if (real == number)
				number--;

			GameMode actualGameMode = mode.getActualGameMode(number + 1);
			// if (actualGameMode.isFullGameMode()) {
			list = actualGameMode.getShots();
			// } else { // TODO: Turn <Hits> not implemented yep
			// list = actualGameMode.getShots(); // pensar melhor
			// }
			turn = new ArrayList<KindShot>(list.get(number % list.size()));
			addExtraShipShots(turn, number);
		} else {
			turn = list.get(0);
		}

		for (final KindShot kindShot : turn) {
			turnTargets.add(new Shot(kindShot, this));
		}
		turnTargetsLockWrite.unlock();
	}

	private void getAliveAndDeadShips(int[] aliveShipKinds, int[] deadEnemyShipKinds) {
		for (final Ship ship : ships) {
			if (!ship.isSunk(enemy)) {
				aliveShipKinds[ship.getId()]++;
			}
		}
		for (final Ship ship : enemy.getShips()) {
			if (ship.isSunk(this)) {
				deadEnemyShipKinds[ship.getId()]++;
			}
		}
	}

	private void addExtraShipShots(List<KindShot> turnShots, int realTurnNumber) {
		realTurnNumber++;
		final int[] aliveShipKinds = new int[ShipClass.numberOfShips()];
		final int[] deadEnemyShipKinds = new int[ShipClass.numberOfShips()];
		getAliveAndDeadShips(aliveShipKinds, deadEnemyShipKinds);

		GameMode actualGameMode = mode.getActualGameMode(realTurnNumber);
		addExtraShipShots(turnShots, aliveShipKinds, deadEnemyShipKinds, actualGameMode.getShipsExtraInfo());
	}

	private void addExtraShipShots(List<KindShot> turnShots, int[] aliveShipKinds, int[] deadEnemyShipKinds,
			Map<Integer, ShipExtraInfo> extraInfoMap) {
		final Fleet fleet = game.getCurrentFleet();

		for (int i = 0; i < aliveShipKinds.length; i++) {
			if (aliveShipKinds[i] > 0) {
				final ShipExtraInfo extraInfo = extraInfoMap.get(i);
				if (extraInfo != null) {
					turnShots.addAll(extraInfo.getAllPlusShots());
					for (int k = 0; k < aliveShipKinds[i]; k++) {
						turnShots.addAll(extraInfo.getPlusShots());
					}
				}
			}
		}

		for (int i = 0; i < deadEnemyShipKinds.length; i++) {
			final ShipExtraInfo extraInfo = extraInfoMap.get(i);
			if (extraInfo != null) {
				if (fleet.getFleetNumbers().get(i) > 0 && fleet.getFleetNumbers().get(i) == deadEnemyShipKinds[i]) {
					turnShots.addAll(extraInfo.getAllDeadShots());
				}

				for (int k = 0; k < deadEnemyShipKinds[i]; k++) {
					turnShots.addAll(extraInfo.getDeadShots());
				}
			}
		}

		final ShipExtraInfo deafultExtraInfo = extraInfoMap.get(-1);
		if (deafultExtraInfo != null) {
			turnShots.addAll(deafultExtraInfo.getPlusShots());
		}
	}

	@Override
	public void setPosition(Coordinate newPosition) {
		position = newPosition;
	}

	@Override
	public boolean setPositionToRandomLocation(boolean justSimpleRandom) {
		final GameState gameState = game.getGameState();
		if (gameState == GameState.PlacingShips && selectedShip != null) {
			final int id = selectedShip.getId();
			if (justSimpleRandom) {
				int tries = 0;
				do {
					int rotation = GameClass.random.nextInt(ShipClass.getAllRotations(id).size());
					final int mX = maxX - ShipClass.sizeX(id, rotation) + 1;
					final int mY = maxY - ShipClass.sizeY(id, rotation) + 1;
					int x = GameClass.random.nextInt(mX);
					int y = GameClass.random.nextInt(mY);

					selectedShip.rotateTo(rotation, maxX, maxY);
					selectedShip.moveTo(x, y);

					if (canPlaceShip()) {
						setPosition(new Coordinate(x, y));
						return true;
					}
					tries++;
				} while (tries < 50);
			} else {
				final List<Ship> allPossiblePositions = ShipClass.createAllFieldPossibilities(id, maxX, maxY);
				Collections.shuffle(allPossiblePositions);
				for (final Ship possibleShip : allPossiblePositions) {
					selectedShip.rotateTo(possibleShip.getRotation(), maxX, maxY);
					final int x = possibleShip.minX();
					final int y = possibleShip.minY();
					selectedShip.moveTo(x, y);

					if (canPlaceShip()) {
						setPosition(new Coordinate(x, y));
						return true;
					}
				}
			}

		} else if (gameState == GameState.Playing) {
			final List<Coordinate> allPossiblePositions = new ArrayList<Coordinate>();

			for (int y = 0; y < maxY; y++) {
				for (int x = 0; x < maxX; x++) {
					final Coordinate c = new Coordinate(x, y);
					if (messageAt(x, y) == null && !markAt(x, y).isWater()) {
						boolean found = false;
						turnTargetsLockRead.lock();
						for (final Shot shot : turnTargets) {
							if (shot.isPlaced() && shot.include(c)) {
								found = true;
								break;
							}
						}
						turnTargetsLockRead.unlock();
						if (!found)
							allPossiblePositions.add(c);
					}
				}
			}

			if (!allPossiblePositions.isEmpty()) {
				final int index = GameClass.random.nextInt(allPossiblePositions.size());
				position = allPossiblePositions.get(index);
				return true;
			}
		}
		return false;
	}

	@Override
	public void tryToSetReady() {
		if (game.getPlayerVsSomething() == PlayingMode.PlayerVsPlayerNetwork) {
			if (imPlayer1) {
				if (shipsLeftToPlace.isEmpty()) {
					ready = true;
					((GameVsPlayer) game).getConnection().sendShipsPosition(ships);
					if (getEnemy().isReady()) {
						game.tryToStartGame();
					}
				}
			} else {
				ready = true;
				if (getEnemy().isReady()) {
					game.tryToStartGame();
				}
			}
		} else {
			if (ready) {
				ready = !ready;
			} else if (shipsLeftToPlace.isEmpty()) {
				ready = true;
				if (getEnemy().isReady()) {
					game.tryToStartGame();
				}
			}
		}
	}

	protected void setSelectedShip(Ship selectedShip, boolean moveToPosition) {
		this.selectedShip = selectedShip;
		if (this.selectedShip != null) {
			if (moveToPosition) {
				this.selectedShip.moveTo(position);
			}
			position = this.selectedShip.trimShip(maxX, maxY);
		}
	}

	@Override
	public void setShipFieldVisible(boolean visible) {
		shipFieldVisible = visible;
	}

	@Override
	public Ship shipAt(Coordinate coor) {
		return shipAt(coor.x, coor.y);
	}

	@Override
	public Ship shipAt(int x, int y) {
		if (optimize)
			return quickShips[x][y];

		for (final Ship ship : ships) {
			if (ship.pieceAt(x, y)) {
				return ship;
			}
		}
		return null;
	}

	@Override
	public boolean shipNear(int x, int y) {
		if (optimize)
			return nearShips[x][y];

		for (final Ship ship : ships) {
			if (ship.near(x, y)) {
				return true;
			}
		}
		return false;
	}

	private static class EffectCoordinate extends Coordinate {

		private final SpecialEffectTypes effect;

		EffectCoordinate(Coordinate coor, SpecialEffectTypes effect) {
			super(coor);
			this.effect = effect;
		}

		EffectCoordinate(int x, int y, SpecialEffectTypes effect) {
			super(x, y);
			this.effect = effect;
		}
	}

	@Override
	public ShotAllResults shotAll() {
		if (game.getCurrentPlayer() != this)
			return ShotAllResults.NotPlayerTurn;
		if (game.getTurnState() != TurnState.ChooseTargets)
			return ShotAllResults.NotInShootingTurnStatus;
		if (game.getTurnType() != TurnTypes.Explosion && !allShotsPlaced()
				&& getNumberShotsPlaced() < enemy.getRemainingSpotsLeft())
			return ShotAllResults.NotAllShotsPlaced;

		game.setTurnState(TurnState.Shooting);

		turnTargetsLockRead.lock();
		final List<Coordinate> newList = new ArrayList<Coordinate>(turnTargets.size());
		final List<Coordinate> placedList = new ArrayList<Coordinate>();
		targetList = new ArrayList<Coordinate>(turnTargets.size());

		for (final Shot shot : turnTargets) {
			newList.add(shot.getCoordinate());
			if (shot.isPlaced) {
				placedList.add(shot.getCoordinate());
				doShotSpecialEffect(shot);
			}
		}
		turnTargetsLockRead.unlock();

		List<Coordinate> counterList = new ArrayList<Coordinate>(0);
		if (game.getPlayerVsSomething() == PlayingMode.PlayerVsComputer || imPlayer1) {
			List<Double> values = getCounterAttackProbability(targetList);
			for (int i = 0; i < targetList.size(); i++) {
				EffectCoordinate shot = (EffectCoordinate) targetList.get(i);
				if (shot.effect != SpecialEffectTypes.Indestructible) {
					double value = values.get(i);
					final double nextDouble = GameClass.random.nextDouble();
					if (value > 0.0 && nextDouble < value) {
						counterList.add(shot);
					}
				}
			}
			targetList.removeAll(counterList);
		}

		if (game.getPlayerVsSomething() == PlayingMode.PlayerVsPlayerNetwork) {
			if (game.getTurnType() != TurnTypes.Explosion) {
				if (imPlayer1) {
					((GameVsPlayer) game).getConnection().sendShotsAndCounters(newList, counterList);
				} else {
					counterList.addAll(networkCounterList);
					targetList.removeAll(counterList);
				}
			}
		}

		lastTurnKilledShips.clear();

		// For special turns that don't have shots that can hit ships, (ex: camera shot turns)
		if (targetList.isEmpty() && counterList.isEmpty()) {
			game.finishTurn(true);
			return ShotAllResults.ShotsFired;
		}

		final List<MessageUnit> hitedShips = new ArrayList<MessageUnit>(targetList.size());

		for (final Coordinate shot : targetList) {
			setMessageAt(shot, new Message("", 0, null, null, null, false));

			final Ship ship = enemy.shipAt(shot);
			if (ship == null) {
				hitedShips.add(new MessageUnit(TypesMessageUnits.Water, ""));
			} else {
				final int shipId = ship.getId();
				final String shipName = ship.getName();
				if (ship.isSunk(this)) {
					hitedShips.add(new MessageUnit(String.format(SHIPDESTROYEDFORMAT, shipName), true, shipId));
					totalKilledShips++;
					lastTurnKilledShips.add(ship);
				} else {
					hitedShips.add(new MessageUnit(String.format(SHIPHITFORMAT, shipName), false, shipId));
				}
			}
		}
		for (Coordinate counter : counterList) {
			hitedShips.add(new MessageUnit(TypesMessageUnits.Counter, counter.toStringSmall()));
		}

		for (final Coordinate shot : targetList) {
			setMessageAt(shot, null);
		}

		Collections.sort(hitedShips);

		boolean wasFirstBloodPlay = totalKilledShips > 0 && totalKilledShips == lastTurnKilledShips.size()
				&& getEnemy().getKilledShips() == 0;

		switch (game.getTurnType()) {
		case Normal:
			lastMessage = new Message("", game.getTurnNumber(), hitedShips, targetList, counterList, wasFirstBloodPlay);
			break;
		case Extra:
			nextExtraTurn();
			lastMessage = new Message("E", extraTurns, hitedShips, targetList, counterList, wasFirstBloodPlay);
			break;
		case Explosion:
			nextExplosionTurn();
			lastMessage = new Message("X", explosionTurns, hitedShips, targetList, counterList, wasFirstBloodPlay);
			break;
		default:
			break;
		}

		if (game.getTurnType() == TurnTypes.Explosion || !GameClass.soundIsOn()) {
			game.finishTurn(true);
		} else {
			statistics.totalTimeTurns += game.getElapsedTurnTime();
			chooseSound();
		}

		return ShotAllResults.ShotsFired;
	}

	private void doShotSpecialEffect(Shot shot) {
		final List<Coordinate> list = shot.getShots();
		final SpecialEffectTypes specialEffect = shot.specialEffect();
		switch (specialEffect) {
		case None:
		case Indestructible:
			for (Coordinate coordinate : shot.getValidShots()) {
				targetList.add(new EffectCoordinate(coordinate, specialEffect));
			}
			break;
		case Camera:
			for (final Coordinate coordinate : list) {
				if (game.isInsideField(coordinate)) {
					this.setMarkAt(coordinate, enemy.shipAt(coordinate) == null ? Mark.Water100 : Mark.Ship100);
				}
			}
			break;
		//		case Random:
		// final List<Coordinate> rList = new ArrayList<Coordinate>(list);
		// Collections.shuffle(rList);
		// for (int i = 0; i < shot.getNTargets(); i++) {
		// targetList.add(rList.get(i));
		// }
		//			break;
		default:
			break;
		}
	}

	@Override
	public int getRemainingSpotsLeft() {
		int count = 0;

		for (int y = 0; y < maxY; y++) {
			for (int x = 0; x < maxX; x++) {
				if (messageAt(x, y) == null) {
					count++;
				}
			}
		}

		return count;
	}

	@Override
	public boolean showShipField() {
		return shipFieldVisible;
	}

	@Override
	public boolean showShots_AND_giveBonus() {
		if (game.getCurrentPlayer() != this || game.getTurnState() != TurnState.Shooting || lastMessage == null)
			return false;

		final int totalShots = targetList.size();
		statistics.totalShotsFired += totalShots;
		final int hits = lastMessage.getHits();
		statistics.totalShotsHitted += hits;
		statistics.totalSinkedShips += lastMessage.getKills();

		final TimeLimitType timeLimitType = mode.getTimeLimitType();
		if (game.getTurnType() == TurnTypes.Normal
				&& (timeLimitType == TimeLimitType.TotalTimeAndPerTurn || timeLimitType == TimeLimitType.ExtraFastMode)) {
			final int timeLeft = game.getRemainingTurnTime();
			if (timeLeft > 0)
				timeOfPlayer.increaseTime((int) Math.floor(1000 * timeLeft * mode.getTimeSavePercentage()));
		}

		addMessage(lastMessage);
		getBonusFromLastShot();

		if (hits == 0) {
			for (final Coordinate coor : targetList) {
				marks[coor.x][coor.y] = Mark.Water100;
			}
		} else if (hits == totalShots) {
			for (final Coordinate coor : targetList) {
				marks[coor.x][coor.y] = Mark.Ship100;
			}
		}

		for (final Coordinate coor : targetList) {
			setMessageAt(coor, lastMessage);
		}

		nextTargetToPlace = 0;
		turnTargetsLockWrite.lock();
		turnTargets.clear();
		turnTargetsLockWrite.unlock();
		targetList = null;
		lastMessage = null;

		return true;
	}

	@Override
	public void checkForExplosions() {
		for (final Ship ship : lastTurnKilledShips) {
			final int explosiveSize = getExplosionSize(ship.getId());
			if (explosiveSize > 0) {
				final List<Coordinate> explodeCoordinates = explode(ship, explosiveSize);
				bonusInNextTurn.add(new GameBonus.Explosion(explodeCoordinates));
			}
		}
	}

	private int getExplosionSize(int shipId) {
		int turnNumber = game.getTurnNumber();
		// System.out.println("CurrentPlayer:" + game.getCurrentPlayer() + ", ExplosionSize: " + turnNumber);

		GameMode actualGameMode = mode.getActualGameMode(turnNumber);
		ShipExtraInfo actualExtraInfo = actualGameMode.getShipsExtraInfo().get(shipId);
		if (actualExtraInfo != null) {
			// if (actualGameMode.isFullGameMode()) {
			return actualExtraInfo.getExplosiveSize();
			// } else {
			// ShipExtraInfo extraInfo = shipsExtraInfo.get(shipId);
			// if (extraInfo != null) {
			// return Math.max(extraInfo.getExplosiveSize(), actualExtraInfo.getExplosiveSize());
			// } else {
			// return actualExtraInfo.getExplosiveSize();
			// }
			// }
		}
		return 0;
	}

	private List<Coordinate> explode(Ship ship, int explosiveSize) {
		final List<Coordinate> result = new ArrayList<Coordinate>();

		// final Set<Coordinate> possiblePositions = new HashSet<Coordinate>();
		final List<Coordinate> possiblePositions = new ArrayList<Coordinate>();
		for (final Coordinate coordinate : ship.getListPieces()) {
			for (int j = -2; j <= 2; j++) {
				for (int i = -2; i <= 2; i++) {
					final Coordinate newCoor = new Coordinate(coordinate.x + i, coordinate.y + j);
					if (!possiblePositions.contains(newCoor))
						possiblePositions.add(newCoor);
				}
			}
		}
		for (final Coordinate coordinate : ship.getListPieces()) {
			for (int j = -1; j <= 1; j++) {
				for (int i = -1; i <= 1; i++) {
					final Coordinate newCoor = new Coordinate(coordinate.x + i, coordinate.y + j);
					possiblePositions.remove(newCoor);
				}
			}
		}
		final List<Coordinate> positions = new ArrayList<Coordinate>(possiblePositions);
		Collections.sort(positions);
		Collections.shuffle(positions, game.getRandomVar());

		final Iterator<Coordinate> iterator = positions.iterator();
		while (explosiveSize > 0 && iterator.hasNext()) {
			final Coordinate position = iterator.next();
			if (game.isInsideField(position)) {
				if (messageAt(position) == null) {
					result.add(position);
				}
				explosiveSize--;
			}
		}

		return result;
	}

	@Override
	public String toString() {
		return playerName;
	}

	@Override
	public synchronized boolean undoLastPlacedShip() {
		if (!ships.isEmpty()) {
			final Ship lastShip = ships.remove(ships.size() - 1);
			shipsLeftToPlace.add(0, lastShip);
			setSelectedShip(lastShip, false);

			if (ready) {
				ready = false;
				if (game.getPlayerVsSomething() == PlayingMode.PlayerVsPlayerNetwork && imPlayer1) {
					((GameVsPlayer) game).getConnection().sendCancelPlaceShips();
				}
			}
		}
		return !ships.isEmpty();
	}

	@Override
	public void fillWater() {
		if (markAt().isShip()) {
			final boolean[][] seen = new boolean[maxX][maxY];
			fill(position.x, position.y, seen, true);
		}
	}

	@Override
	public void fillWater(Ship ship) {
		if (markAt().isShip()) {
			final boolean[][] seen = new boolean[maxX][maxY];
			final Coordinate coor = ship.getListPieces().get(0);
			fill(coor.x, coor.y, seen, true);
		}
	}

	@Override
	public void clearWater() {
		if (markAt().isShip()) {
			final boolean[][] seen = new boolean[maxX][maxY];
			fill(position.x, position.y, seen, false);
		}
	}

	protected void fill(int x, int y, boolean[][] seen, boolean fillOrClear) {
		int mX, mY;
		for (int j = -1; j <= 1; j++) {
			mY = y + j;
			for (int i = -1; i <= 1; i++) {
				mX = x + i;
				if (game.isInsideField(mX, mY)) {
					if (fillOrClear && markAt(mX, mY) == Mark.None) {
						setMarkAt(mX, mY, Mark.Water);
					} else if (!fillOrClear && markAt(mX, mY) == Mark.Water) {
						setMarkAt(mX, mY, Mark.None);
					} else if (!seen[mX][mY] && markAt(mX, mY).isShip()) {
						seen[mX][mY] = true;
						fill(mX, mY, seen, fillOrClear);
					}
				}
			}
		}
	}

	protected void getBonusFromLastShot() {
		final Set<BonusPlay> bonus = mode.getActualGameMode(game.getTurnNumber()).getPossibleBonus();
		for (final BonusPlay b : bonus) {
			boolean gotBonus = false;

			switch (b.condition) {
			case FirstBlood:
				if (lastMessage.wasFirstBlood())
					gotBonus = true;
				break;
			case Kill:
				if (lastTurnKilledShips.size() == 1)
					gotBonus = true;
				break;
			case DoubleKill:
				if (lastTurnKilledShips.size() == 2)
					gotBonus = true;
				break;
			case TripleKill:
				if (lastTurnKilledShips.size() == 3)
					gotBonus = true;
				break;
			case UltraKill:
				if (lastTurnKilledShips.size() == 4)
					gotBonus = true;
				break;
			case Rampage:
				if (lastTurnKilledShips.size() >= 5)
					gotBonus = true;
				break;
			case KillingSpree:
				if (killingTurns == KillingSpreeTurns)
					gotBonus = true;
				break;
			case Dominating:
				if (killingTurns == DominatingTurns)
					gotBonus = true;
				break;
			case MegaKill:
				if (killingTurns == MegaKillTurns)
					gotBonus = true;
				break;
			case Unstoppable:
				if (killingTurns == UnstoppableTurns)
					gotBonus = true;
				break;
			case WickedSick:
				if (killingTurns == WickedSickTurns)
					gotBonus = true;
				break;
			case MonsterKill:
				if (killingTurns == MonsterKillTurns)
					gotBonus = true;
				break;
			case Godlike:
				if (killingTurns == GodlikeTurns)
					gotBonus = true;
				break;
			case HolyShit:
				if (killingTurns == HolyShitTurns)
					gotBonus = true;
				break;
			case NoWaterInARow:
				messagesLockRead.lock();
				if (!messages.isEmpty() && messages.get(0).getTurnType() == "") {
					int count = 0;

					for (final Message m : messages) {
						if (m.getTurnType() == "") {
							if (!m.isAllWater()) {
								count++;
							} else {
								break;
							}
						}
					}

					if (b.getParameter() == count)
						gotBonus = true;
				}
				messagesLockRead.unlock();
				break;
			}

			if (gotBonus) {
				// addPlayerBonus(b.getActions());
				for (final GameBonus gameBonus : b) {
					// TODO: Extra Turn Sound
					// if (gameBonus.getType() == BonusTypes.ExtraTurn)
					// game.playSound(AvaibleSounds.ExtraTurn);

					switch (gameBonus.getType()) {
					case ExtraTime:
						ExtraTime extraTime = (ExtraTime) gameBonus;
						timeOfPlayer.increaseTime(extraTime.getTime() * 1000);
						break;
					default:
						bonusInNextTurn.add(gameBonus);
						game.newConditionEvent(this, b);
						break;
					}
				}
			}
		}
	}

	@Override
	public long getWatchTime() {
		return timeOfPlayer.getElapsedTimeSecs();
	}

	@Override
	public void startWatch() {
		timeOfPlayer.start();
	}

	@Override
	public void stopWatch() {
		timeOfPlayer.stop();
	}

	@Override
	public List<GameBonus> getBonusInNextTurn() {
		return Collections.unmodifiableList(bonusInNextTurn);
	}

	@Override
	public void deleteUsedBonus(GameBonus gameBonus) {
		bonusInNextTurn.remove(gameBonus);
	}

	private int nextExtraTurn() {
		extraTurns++;
		return extraTurns;
	}

	private int nextExplosionTurn() {
		explosionTurns++;
		return explosionTurns;
	}

	@Override
	public int getExtraTurnsCount() {
		return extraTurns;
	}

	@Override
	public Statistics getStatistics() {
		return statistics;
	}

	@Override
	public boolean hasPlayedFirst() {
		return playingFirst;
	}

	@Override
	public List<Ship> getShips() {
		return unmodifiableShips;
	}

	/**
	 * @return Total number of shots already placed
	 */
	private int getNumberShotsPlaced() {
		int total = 0;
		turnTargetsLockRead.lock();
		for (final Shot shot : turnTargets) {
			final SpecialEffectTypes specialEffect = shot.specialEffect();
			if (specialEffect == SpecialEffectTypes.None || specialEffect == SpecialEffectTypes.Indestructible)
				if (shot.isPlaced)
					total++;
		}
		turnTargetsLockRead.unlock();
		return total;
	}

	@Override
	public boolean allShotsPlaced() {
		boolean result = true;
		turnTargetsLockRead.lock();
		for (final Shot shot : turnTargets) {
			if (!shot.isPlaced) {
				result = false;
				break;
			}
		}
		turnTargetsLockRead.unlock();
		return result;
	}

	@Override
	public List<Double> getCounterAttackProbability(List<Coordinate> list) {
		List<Double> results = new ArrayList<Double>(list.size());

		GameMode actualGameMode = mode.getActualGameMode(game.getTurnNumber());
		double fullShield = actualGameMode.getFullShield();

		for (int i = 0; i < list.size(); i++) {
			results.add(fullShield);
		}

		for (Ship ship : enemy.getShips()) {
			if (!ship.isSunk(this)) {
				ShipExtraInfo extraInfo = actualGameMode.getShipsExtraInfo().get(ship.getId());
				if (extraInfo != null) {
					List<Double> shield = extraInfo.getShield();
					if (!shield.isEmpty()) {
						for (int i = 0; i < list.size(); i++) {
							if (shield.get(0) < 0) {
								double value = results.get(i);
								results.set(i, value + (1 - value) * -shield.get(0));
							} else {
								int d = Integer.MAX_VALUE;
								for (Coordinate c : ship) {
									d = Math.min(d, c.dist(list.get(i)));
								}
								if (d < shield.size()) {
									double value = results.get(i);
									results.set(i, value + (1 - value) * shield.get(d));
								}
							}
						}
					}
				}
			}
		}
		return results;
	}

	@Override
	public void setNetworkCounterList(List<Coordinate> networkCounterList) {
		this.networkCounterList = networkCounterList;
	}

	@Override
	public void optimizeShips() {
		for (Ship ship : ships) {
			Set<Coordinate> allAround = Coordinate.allAround(ship);
			for (Coordinate c : allAround) {
				if (game.isInsideField(c))
					nearShips[c.x][c.y] = true;
			}

			for (Coordinate c : ship) {
				quickShips[c.x][c.y] = ship;
			}
		}

		optimize = true;
	}

	@Override
	public void removeAllShips() {
		while (undoLastPlacedShip()) {
		}
	}

	@Override
	public void pauseWatch() {
		timeOfPlayer.pause();
	}

	@Override
	public void continueWatch() {
		timeOfPlayer.continueWatch();
	}

	@Override
	public Game getGame() {
		return game;
	}

	// ############################################################################################
	// ################################## Static Classes ##########################################
	// ############################################################################################

	private static class StatisticsClass implements Statistics {

		private int totalShotsFired;
		private int totalShotsHitted;

		private double totalTimeTurns;
		private int totalSinkedShips;

		private final Game game;
		private final Player player;

		public StatisticsClass(Game game, Player player) {
			this.game = game;
			this.player = player;
			totalShotsFired = 0;
			totalShotsHitted = 0;
			totalTimeTurns = 0;
			totalSinkedShips = 0;
		}

		@Override
		public int getTotalShotsFired() {
			return totalShotsFired;
		}

		@Override
		public int getTotalShotsHitted() {
			return totalShotsHitted;
		}

		@Override
		public double getTotalTimeTurns() {
			return totalTimeTurns;
		}

		@Override
		public float getAimPercentage() {
			return totalShotsHitted == 0 ? 0 : totalShotsHitted / (float) totalShotsFired;
		}

		@Override
		public double getMedianTurnTime() {
			int turnNumber = game.getTurnNumber();
			final double totalTurnTime = totalTimeTurns;
			if (game.getCurrentPlayer() == player) {
				if (game.getTurnState() == TurnState.ChooseTargets) {
					turnNumber--;
				}
			} else if (player.hasPlayedFirst()) {
				turnNumber--;
			}
			return turnNumber == 0 ? 0 : totalTurnTime / turnNumber;
		}

		@Override
		public float getPercentageGameCleared() {
			final int totalPieces = game.getCurrentFleet().getTotalPieces();
			return totalShotsHitted / (float) totalPieces;
		}

		@Override
		public int getTotalShips() {
			return game.getCurrentFleet().size();
		}

		@Override
		public int getTotalSunkedShips() {
			return totalSinkedShips;
		}
	}

	public static class Shot {

		public enum SpecialEffectTypes {
			None, Camera, Indestructible
			//			Random,
			;
		}

		public enum KindShot {
			NormalShot(1, SpecialEffectTypes.None),
			//BigShot(5, SpecialEffectTypes.None), CrossKillRight(3, SpecialEffectTypes.None), CrossKillLeft(3, SpecialEffectTypes.None),

			IndestructibleShot(1, SpecialEffectTypes.Indestructible),

			//			MegaCameraShot(9, SpecialEffectTypes.Camera), CameraShot(4, SpecialEffectTypes.Camera), 
			CameraShot(1, SpecialEffectTypes.Camera),

			//			RandomShot(1, SpecialEffectTypes.Random),

			;

			private int nTargets;
			private SpecialEffectTypes specialEffect;

			private KindShot(int nTargets, SpecialEffectTypes specialEffect) {
				this.nTargets = nTargets;
				this.specialEffect = specialEffect;
			}

			public int getNTargets() {
				return nTargets;
			}

			// public boolean isSpecialEffect() {
			// return specialEffect;
			// }

			public SpecialEffectTypes specialEffect() {
				return specialEffect;
			}

			public static KindShot getKindShot(String name) {
				for (final KindShot kindShot : KindShot.values()) {
					if (name.equalsIgnoreCase(kindShot.toString()))
						return kindShot;
				}
				return null;
			}
		}

		private final KindShot kindShot;
		private Coordinate coordinate;
		private boolean isPlaced;
		private final PlayerClass player;

		public Shot(KindShot kindShot, PlayerClass player) {
			this(kindShot, null, player);
		}

		public int getNTargets() {
			return kindShot.nTargets;
		}

		public SpecialEffectTypes specialEffect() {
			return kindShot.specialEffect;
		}

		public Shot(KindShot kindShot, Coordinate coordinate, PlayerClass player) {
			super();
			this.kindShot = kindShot;
			this.coordinate = coordinate;
			this.isPlaced = false;
			this.player = player;
		}

		public boolean validateShot() {
			int atLeast1;
			switch (kindShot) {
			case NormalShot:
			case IndestructibleShot:
				return player.messageAt(coordinate) == null;
				//			case BigShot:
				//			case CrossKillRight: // Same as CrossKillLeft
				//			case CrossKillLeft:
				//				atLeast1 = 0;
				//				for (int j = -1; j <= 1; j++) {
				//					for (int i = -1; i <= 1; i++) {
				//						final Coordinate c = coordinate.translate(i, j);
				//						if (include(c) && player.game.isInsideField(c) && player.messageAt(c) == null)
				//							atLeast1++;
				//					}
				//				}
				//				return atLeast1 >= 1;
			case CameraShot:
				//			case CameraShot:
				//			case MegaCameraShot:
				atLeast1 = 0;
				for (int j = -1; j <= 1; j++) {
					for (int i = -1; i <= 1; i++) {
						final Coordinate c = coordinate.translate(i, j);
						if (include(c) && player.game.isInsideField(c))
							atLeast1++;
					}
				}
				return atLeast1 >= 1;
				//			case RandomShot:
				// TODO: random shot - como é que funciona??
			default:
				return false;
			}
		}

		public List<Coordinate> getShots() {
			return Shot.getShots(this.kindShot, this.coordinate);
		}

		public static List<Coordinate> getShots(KindShot kindShot, Coordinate center) {
			final List<Coordinate> list = new LinkedList<Coordinate>();

			switch (kindShot) {
			case NormalShot:
			case IndestructibleShot:
			case CameraShot:
				list.add(center);
				break;
			//			case BigShot: // All same
			//			case CrossKillRight: // All same
			//			case CrossKillLeft: // All same
			//			case CameraShot: // All same
			//			case MegaCameraShot: // All same
			//				for (int y = -1; y <= 1; y++) {
			//					for (int x = -1; x <= 1; x++) {
			//						final Coordinate target = center.translate(x, y);
			//						if (Shot.include(kindShot, center, target))
			//							list.add(target);
			//					}
			//				}
			//				break;
			default:
				break;
			}

			return list;
		}

		public List<Coordinate> getValidShots() {
			final ArrayList<Coordinate> list = new ArrayList<Coordinate>();
			for (final Coordinate coor : getShots()) {
				if (player.game.isInsideField(coor) && player.messageAt(coor) == null)
					list.add(coor);
			}
			return list;
		}

		// public static List<Coordinate> toCoordinateList(List<Shot> shotList) {
		// List<Coordinate> list = new ArrayList<Coordinate>(shotList.size());
		//
		// for (Shot shot : shotList) {
		// list.add(shot.coordinate);
		// }
		//
		// return list;
		// }

		public boolean include(Coordinate target) {
			return Shot.include(this.kindShot, this.coordinate, target);
		}

		// public boolean include(int x, int y) {
		// return include()
		// }

		public static boolean include(KindShot kindShot, Coordinate center, Coordinate target) {
			switch (kindShot) {
			case NormalShot:
			case IndestructibleShot:
			case CameraShot:
				return center.equals(target);
				//			case BigShot:
				//				return Math.abs(center.x - target.x) + Math.abs(center.y - target.y) <= 1;
				//			case CrossKillRight:
				//				return center.equals(target) || center.equals(target.translate(-1, -1))
				//						|| center.equals(target.translate(1, 1));
				//			case CrossKillLeft:
				//				return center.equals(target) || center.equals(target.translate(1, -1))
				//						|| center.equals(target.translate(-1, 1));
				//			case CameraShot:
				//				return center.equals(target) || center.equals(target.translate(0, 1))
				//				// || center.equals(target.translate(1, 0)) || center.equals(target.translate(1, 1));
				//						|| center.equals(target.translate(-1, 0)) || center.equals(target.translate(-1, 1));
				//			case MegaCameraShot:
				//				return Math.abs(center.x - target.x) <= 1 && Math.abs(center.y - target.y) <= 1;
			default:
				return false;
			}
		}

		public int sizeX() {
			switch (kindShot) {
			case NormalShot:
			case IndestructibleShot:
			case CameraShot:
				return 1;
				//			case CameraShot:
				//				return 2;
				//			case BigShot:
				//			case CrossKillRight:
				//			case CrossKillLeft:
				//			case MegaCameraShot:
				//				return 3;
			default:
				return 0;
			}
		}

		public int sizeY() {
			switch (kindShot) {
			case NormalShot:
			case IndestructibleShot:
			case CameraShot:
				return 1;
				//			case CameraShot:
				//				return 2;
				//			case BigShot:
				//			case CrossKillRight:
				//			case CrossKillLeft:
				//			case MegaCameraShot:
				//				return 3;
			default:
				return 0;
			}
		}

		public boolean isPlaced() {
			return isPlaced;
		}

		public KindShot getKindShot() {
			return kindShot;
		}

		public Coordinate getCoordinate() {
			return coordinate;
		}

		@Override
		public String toString() {
			return "Shot [coordinate=" + coordinate + ", isPlaced=" + isPlaced + ", kindShot=" + kindShot + "]";
		}

		public static List<List<Integer>> calculateFFListOfLists(List<List<KindShot>> shotsList) {
			List<List<Integer>> listOfLists = new ArrayList<List<Integer>>();

			for (List<KindShot> shots : shotsList) {
				List<Integer> list = calculateFFList(shots);
				listOfLists.add(list);
			}

			return listOfLists;
		}

		private static List<Integer> calculateFFList(List<KindShot> shots) {
			List<Integer> list = new ArrayList<Integer>();

			int count = 0;
			KindShot k = null;
			for (KindShot shot : shots) {
				if (k == null) {
					count = 1;
					k = shot;
				} else if (k != shot) {
					list.add(k.ordinal());
					list.add(count);
					count = 1;
					k = shot;
				} else {
					count++;
				}
			}
			if (k != null) {
				list.add(k.ordinal());
				list.add(count);
			}
			return list;
		}

		public static String calculateFFListString(List<KindShot> shots) {
			List<Integer> list = calculateFFList(shots);
			StringBuilder s = new StringBuilder();
			for (int i = 0; i < list.size(); i += 2) {
				s.append(" " + KindShot.values()[list.get(i)].name() + " " + list.get(i + 1));
			}
			s.deleteCharAt(0);
			return s.toString();
		}

		public static String calculateFFListOfListsOfString(List<List<KindShot>> listOfListsOfShots) {
			StringBuilder s = new StringBuilder();
			for (List<KindShot> list : listOfListsOfShots) {
				s.append(calculateFFListString(list) + ", ");
			}
			s.deleteCharAt(s.length() - 1);
			s.deleteCharAt(s.length() - 1);
			return s.toString();
		}
	}

}
