package pt.rmartins.battleships.objects;

import java.util.List;

import pt.rmartins.battleships.objects.Game.Mark;
import pt.rmartins.battleships.objects.PlayerClass.Shot;
import pt.rmartins.battleships.objects.PlayerClass.Shot.KindShot;
import pt.rmartins.battleships.objects.modes.GameBonus;

public interface Player {

	/*
		 * Killing Spree (Three kills in a row) Dominating ( Four kills in a row) Mega Kill! ( Five kills in a row)
		 * Unstoppable! ( Six kills in a row) Wicked Sick( Seven kills in a row) Monster Kill!!! ( Eight kills in a row)
		 * Godlike! (Nine kills in a row) Beyond Godlike! (Ten to infinite kills in a row)
		 */

	public enum ShotAllResults {
		ShotsFired, NotPlayerTurn, NotInShootingTurnStatus, NotAllShotsPlaced
	}

	public static final int KillingSpreeTurns = 3;
	public static final int DominatingTurns = 4;
	public static final int MegaKillTurns = 5;
	public static final int UnstoppableTurns = 6;
	public static final int WickedSickTurns = 7;
	public static final int MonsterKillTurns = 8;
	public static final int GodlikeTurns = 9;
	public static final int HolyShitTurns = 10; // (Beyond Godlike!)

	public enum DisplayModes {
		ShowDestroyedShipsField, ShowStatistics
		// ShowPlayerBonus
	}

	public boolean canPlaceShip();

	public void chooseTarget();

	public Player getEnemy();

	public int getKilledShips();

	public List<Message> getMessagesLock();

	public void getMessagesUnlock();

	public Coordinate getPosition();

	public int getPositionX();

	public int getPositionY();

	public Ship getSelectedShip();

	/**
	 * This method will lock TurnTargets list in read mode, call {@link #unlockTurnTargets()} to unlock
	 * 
	 * @return Get all the targets of the current turn with their placed status
	 */
	public List<Shot> getTurnTargets();

	/**
	 * unlocks the read lock previously obtained after calling {@link #getTurnTargets()}
	 */
	public void unlockTurnTargets();

	public boolean isReady();

	/**
	 * @param x
	 * @param y
	 * @return The mark at the position (Always != null)
	 */
	public Mark markAt();

	/**
	 * @param x
	 * @param y
	 * @return The mark at the position (Always != null)
	 */
	public Mark markAt(int x, int y);

	/**
	 * @param x
	 * @param y
	 * @return The mark at the position (Always != null)
	 */
	public Mark markAt(Coordinate coor);

	public void movePosition(int x, int y);

	public void movePositionAbsolute(int x, int y);

	public Message messageAt(Coordinate coor);

	public Message messageAt(int x, int y);

	public boolean placeShip();

	public Coordinate rotateShipClockwise();

	public void setEnemy(Player enemy);

	public void setMarkAt(Mark mark);

	public void setNumberOfTargets(List<List<KindShot>> list);

	public void setPosition(Coordinate newPosition);

	public boolean setPositionToRandomLocation(boolean justSimpleRandom);

	public void tryToSetReady();

	public void cancelSetReady();

	public void setShipFieldVisible(boolean visible);

	public Ship shipAt(Coordinate coor);

	public Ship shipAt(int x, int y);

	public boolean shipNear(int x, int y);

	public ShotAllResults shotAll();

	public boolean showShipField();

	public boolean showShots_AND_giveBonus();

	/**
	 * @return If there are still ships placed
	 */
	public boolean undoLastPlacedShip();

	public void fillWater();

	public void fillWater(Ship ship);

	public void clearWater();

	// public void addPlayerBonus(GameBonus bonus, int quantity);

	public void startWatch();

	public long getWatchTime();

	public void stopWatch();

	public DisplayModes getExtraInfoDisplayMode();

	public void nextDisplayMode();

	public List<GameBonus> getBonusInNextTurn();

	public void deleteUsedBonus(GameBonus gameBonus);

	public int getExtraTurnsCount();

	public void placeShipNetwork(Ship ship);

	public Statistics getStatistics();

	public boolean hasPlayedFirst();

	public boolean placeAllShipsRandom(int retryTimes, boolean justSimpleRandom);

	public List<Ship> getShips();

	public Shot getNextTargetToPlace();

	public boolean allShotsPlaced();

	public List<Double> getCounterAttackProbability(List<Coordinate> list);

	public void setNetworkCounterList(List<Coordinate> networkCounterList);

	public void optimizeShips();

	public void removeAllShips();

	public void pauseWatch();

	public void continueWatch();

	/**
	 * @return Total number of spots in the field that are not fired at
	 */
	public int getRemainingSpotsLeft();

	// public void setNetworkExplosion(List<Coordinate> explodeCoordinates);

	public void checkForExplosions();

	// public int getExplosionCount();

}
