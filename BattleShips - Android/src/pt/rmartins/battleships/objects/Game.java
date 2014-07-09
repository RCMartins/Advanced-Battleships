package pt.rmartins.battleships.objects;

import java.util.Random;

import pt.rmartins.battleships.objects.modes.GameMode;
import pt.rmartins.battleships.objects.modes.GameMode.BonusPlay;
import pt.rmartins.battleships.sound.SoundType;
import android.graphics.Canvas;
import android.view.KeyEvent;
import android.view.MotionEvent;

public interface Game {

	public static final boolean DEVELOPER_MODE = false;
	public static final boolean DEBUG = false;
	public static final boolean DEBUG2 = false;

	public static final String GAMEVERSION = "0.5c";
	public static final String PREFERENCES_SETTINGS_FILE_NAME = "settings";
	public static final String PREFERENCES_INITIALIZED_KEY = "settings_initialized";
	public static final String PREFERENCES_LOCALE_KEY = "locale";
	public static final String PREFERENCES_SOUND_KEY = "sound";
	public static final String PREFERENCES_NICKNAME_KEY = "nickname";

	public enum Mark {
		None, Water, Water100, Ship, Ship100;

		public boolean isWater() {
			return this == Water || this == Water100;
		}

		public boolean isShip() {
			return this == Ship || this == Ship100;
		}
	}

	public enum TurnState {
		ChooseTargets, Shooting;
	}

	public enum PlayingMode {
		PlayerVsPlayerNetwork,

		PlayerVsComputer,
	}

	public enum TurnTypes {
		Normal, Extra, Explosion;
	}

	public static enum GameState {
		LoginMenu, MultiplayerMenu, LobbyMenu, SendInitializingInformation, WaitMaster,

		Idle,

		ChoosingMode, FinishedChoosingMode,

		PlacingShips, Playing, PlayingInPause, FinishedGame

		;
	}

	public Fleet getCurrentFleet();

	public GameMode getCurrentGameMode();

	public Player getCurrentPlayer();

	public int getTurnNumber();

	public double getRealTurnNumber();

	public GameState getGameState();

	/**
	 * @return game time in seconds
	 */
	public long getGameTime();

	public int getMaxX();

	public int getMaxY();

	public Player getPlayer1();

	public Player getPlayer2();

	public TurnState getTurnState();

	public boolean isInsideField(Iterable<Coordinate> coordinates);

	public boolean isInsideField(Coordinate coordinate);

	public boolean isInsideField(int x, int y);

	public void playSound(SoundType sound);

	public void setTurnState(TurnState newTurnState);

	public void tryToStartGame();

	/**
	 * @return Turn time in seconds
	 */
	public long getElapsedTurnTime();

	public TurnTypes getTurnType();

	public PlayingMode getPlayerVsSomething();

	public void setMasterPlayingFirstFirst(boolean masterPlayingFirst);

	public void pauseUnpauseGame(boolean fromNetwork);

	public void finishTurn(boolean checkExplosions);

	public void initializePlacingShips(boolean fromNetwork);

	public int getLastTurnTime();

	public int getRemainingTurnTime();

	public void setGameState(GameState gameState);

	public Player getWinningPlayer();

	public void setGameMode(GameMode gameMode);

	public void setFleet(Fleet fleet);

	public void setRandomVar(long newSeed);

	public Random getRandomVar();

	public void changeMAX(int width, int height);

	public void onTouchEvent(MotionEvent event);

	public void draw(Canvas canvas);

	public void update(double timeElapsed);

	public void newConditionEvent(Player player, BonusPlay bonusPlay);

	public boolean onBackPressed();

	public void initialize();

	public void closeResources();

	public void requestRemake(Player playerRequesting);

	public boolean dispatchKeyEvent(KeyEvent event);

}
