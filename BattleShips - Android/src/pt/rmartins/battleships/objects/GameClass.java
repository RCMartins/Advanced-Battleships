package pt.rmartins.battleships.objects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.Random;

import pt.rmartins.battleships.objects.PlayerClass.Shot.KindShot;
import pt.rmartins.battleships.objects.ai.ComputerAI;
import pt.rmartins.battleships.objects.modes.GameMode;
import pt.rmartins.battleships.objects.modes.GameMode.BonusPlay;
import pt.rmartins.battleships.objects.modes.GameMode.TimeLimitType;
import pt.rmartins.battleships.objects.userinterface.PlayInterface;
import pt.rmartins.battleships.objects.userinterface.UserInterface;
import pt.rmartins.battleships.sound.SoundType;
import pt.rmartins.battleships.utilities.DataLoader;
import pt.rmartins.battleships.utilities.LanguageClass;
import pt.rmartins.battleships.utilities.StopWatch;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.MotionEvent;

public abstract class GameClass implements Game {

	@SuppressWarnings("unused")
	private static final String TAG = GameClass.class.getSimpleName();

	public static final Random random;

	static {
		if (ComputerAI.DEBUG_AI)
			random = new Random(1);
		else
			random = new Random();
	}

	private static Context context;
	private static Resources res;
	private static Editor editor;

	private static boolean soundIsOn;
	private static String multiplayerNickname;
	protected static List<Fleet> avaiableFleets;
	protected static List<GameMode> gameModes;

	public static boolean soundIsOn() {
		return soundIsOn;
	}

	public static void setSoundIsOn(boolean value) {
		editor.putBoolean(PREFERENCES_SOUND_KEY, value);
		editor.apply();
		soundIsOn = value;
	}

	public static Context getContext() {
		return GameClass.context;
	}

	public static List<Fleet> getAvaiableFleets() {
		return avaiableFleets;
	}

	public static List<GameMode> getGameModes() {
		return gameModes;
	}

	public static String getMultiplayerNickname() {
		return multiplayerNickname;
	}

	public static void setMultiplayerNickname(String nickname) {
		if (isNicknameValid(nickname)) {
			nickname = nickname.trim();
			editor.putString(PREFERENCES_NICKNAME_KEY, nickname);
			editor.apply();
			GameClass.multiplayerNickname = nickname;
		}
	}

	private static boolean isNicknameValid(String nickname) {
		return nickname.trim().length() > 0;
	}

	public static void initializeGameClass(Activity activity) {
		loadSettings(activity);

		if (Game.DEBUG)
			soundIsOn = false;

		ShipClass.initializeClass();
		DataLoader.loadShips();

		avaiableFleets = new ArrayList<Fleet>();
		DataLoader.loadGameFleets(avaiableFleets);
		avaiableFleets = Collections.unmodifiableList(avaiableFleets);

		gameModes = new ArrayList<GameMode>();
		DataLoader.loadGameModes(gameModes);
		gameModes = Collections.unmodifiableList(gameModes);
	}

	public static void loadSettings(Activity activity) {
		GameClass.context = activity;
		GameClass.res = activity.getResources();

		SharedPreferences settings = context.getSharedPreferences(Game.PREFERENCES_SETTINGS_FILE_NAME, 0);
		editor = settings.edit();

		if (settings.getBoolean(PREFERENCES_INITIALIZED_KEY, false)) {
			final String localeStr = settings.getString(PREFERENCES_LOCALE_KEY, "en-US");
			final Locale locale = LanguageClass.getLocale(localeStr);
			LanguageClass.initialize(activity, locale, res);
			soundIsOn = settings.getBoolean(PREFERENCES_SOUND_KEY, true);
			multiplayerNickname = settings.getString(PREFERENCES_NICKNAME_KEY, "Player");
		} else {
			DataLoader.loadDefaultSettings(activity, res);
			editor.putBoolean(PREFERENCES_INITIALIZED_KEY, true);
			editor.apply();
		}
	}

	protected int SCREENX, SCREENY;
	protected int maxX, maxY;
	protected Player player1, player2;
	protected GameState gameState;
	protected Player winningPlayer;
	private TurnState turnState;

	protected TurnTypes turnType;
	protected double currentTurn;
	protected Player currentPlayer;
	protected Fleet currentFleet;
	protected GameMode currentGameMode;
	protected NextTurn defaultNextTurn;
	protected StopWatch time;

	protected StopWatch turnTime;
	protected int lastTurnTime;
	private final Queue<SoundType> soundsToPlay;

	protected boolean isHostPlayingFirst;

	protected UserInterface GUI;
	protected final Random randomVar;

	protected final Callback finishGameCallBack;

	public GameClass(Callback finishGameCallBack) {
		this.finishGameCallBack = finishGameCallBack;

		this.gameState = Game.GameState.Idle;

		randomVar = new Random();

		soundsToPlay = new LinkedList<SoundType>();
		//		imPlayer1 = true;

		currentFleet = avaiableFleets.isEmpty() ? null : avaiableFleets.get(0);

		if (gameModes.isEmpty())
			gameModes.add(new GameMode());
		currentGameMode = gameModes.get(0);
	}

	@Override
	public synchronized void finishTurn(boolean checkExplosions) {
		if (gameState == GameState.Playing) {
			boolean result = currentPlayer.showShots_AND_giveBonus();
			if (result) {
				currentPlayer.checkForExplosions();
			}

			this.updateGlobalConditions();
			if (soundsToPlay.isEmpty() || !soundIsOn) {
				if (currentPlayer.getStatistics().getTotalSunkedShips() == getCurrentFleet().size()) {
					finishGame(currentPlayer);
				} else {
					goNextTurn();
				}
			} else {
				playNextSound();
			}
		}
	}

	protected void finishGame(Player winningPlayer) {
		this.winningPlayer = winningPlayer;
		setGameState(GameState.FinishedGame);
		this.updateGlobalConditions();

		if (winningPlayer == player1)
			playSound(SoundType.WonGame);
		else
			playSound(SoundType.LostGame);
	}

	@Override
	public synchronized Fleet getCurrentFleet() {
		return currentFleet;
	}

	@Override
	public synchronized GameMode getCurrentGameMode() {
		return currentGameMode;
	}

	@Override
	public synchronized Player getCurrentPlayer() {
		return currentPlayer;
	}

	@Override
	public synchronized double getRealTurnNumber() {
		return currentTurn;
	}

	@Override
	public synchronized int getTurnNumber() {
		return (int) Math.floor(currentTurn);
	}

	@Override
	public synchronized GameState getGameState() {
		return gameState;
	}

	@Override
	public synchronized long getGameTime() {
		return time.getElapsedTimeSecs();
	}

	@Override
	public int getMaxX() {
		return maxX;
	}

	@Override
	public int getMaxY() {
		return maxY;
	}

	@Override
	public Player getPlayer1() {
		return player1;
	}

	@Override
	public Player getPlayer2() {
		return player2;
	}

	@Override
	public synchronized TurnState getTurnState() {
		return turnState;
	}

	protected abstract void goNextTurn();

	@Override
	public boolean isInsideField(Iterable<Coordinate> coordenadas) {
		for (final Coordinate coor : coordenadas) {
			if (!isInsideField(coor.x, coor.y)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isInsideField(int x, int y) {
		return x >= 0 && x < maxX && y >= 0 && y < maxY;
	}

	@Override
	public boolean isInsideField(Coordinate coor) {
		return coor.x >= 0 && coor.x < maxX && coor.y >= 0 && coor.y < maxY;
	}

	private static class onCompletionRunnableClass implements Runnable {

		private final Game game;

		public onCompletionRunnableClass(Game game) {
			this.game = game;
		}

		@Override
		public void run() {
			game.finishTurn(true);
		}
	}

	private final Runnable onCompletionRunnable = new onCompletionRunnableClass(this);

	private boolean playNextSound() {
		if (soundsToPlay.isEmpty()) {
			return false;
		} else {
			final SoundType sound = soundsToPlay.remove();

			if (soundIsOn) {
				sound.playSound(this, onCompletionRunnable);
				return true;
			} else {
				onCompletionRunnable.run();
				return false;
			}
		}
	}

	@Override
	public synchronized void playSound(SoundType sound) {
		if (SoundType.soundIsPlaying() || !soundsToPlay.isEmpty()) {
			soundsToPlay.add(sound);
		} else {
			soundsToPlay.add(sound);
			playNextSound();
		}
	}

	@Override
	public synchronized void setTurnState(TurnState turnState) {
		this.turnState = turnState;

		switch (this.turnState) {
		case Shooting:
			// time.stop(); //Tempo global não se pára?
			turnTime.stop();
			currentPlayer.stopWatch();
			break;
		default:
			break;
		}

		if (GUI != null && GUI instanceof PlayInterface)
			((PlayInterface) GUI).changedTurnState(this.turnState);
	}

	@Override
	public synchronized void tryToStartGame() {
		if (gameState == GameState.PlacingShips && player1.isReady() && player2.isReady()) {
			player1.optimizeShips();
			player2.optimizeShips();

			final GameMode gameMode = getCurrentGameMode();

			currentTurn = 0.5;
			defaultNextTurn = new NextTurn();
			currentPlayer = isHostPlayingFirst ? player1 : player2;
			defaultNextTurn.nextPlayer = null;
			defaultNextTurn.turnType = TurnTypes.Normal;
			defaultNextTurn.nextTurnPlus = 0.5;
			defaultNextTurn.targets = gameMode.getShots();

			time = new StopWatch();
			turnTime = new StopWatch();

			setGameState(GameState.Playing);
			goNextTurn();

			time.start();
		}
	}

	protected static class NextTurn {
		public Player nextPlayer;
		public TurnTypes turnType;
		public double nextTurnPlus;
		public List<List<KindShot>> targets;

		public NextTurn() {
		}

		public NextTurn(NextTurn otherTurn) {
			copy(otherTurn);
		}

		public void copy(NextTurn otherTurn) {
			nextPlayer = otherTurn.nextPlayer;
			turnType = otherTurn.turnType;
			nextTurnPlus = otherTurn.nextTurnPlus;
			targets = otherTurn.targets;
		}
	}

	private void updateGlobalConditions() {
		if (gameState == GameState.Playing) {
			final GameMode mode = getCurrentGameMode();
			final TimeLimitType timeLimitType = mode.getTimeLimitType();
			switch (timeLimitType) {
			case TotalTimeAndPerTurn:
			case ExtraFastMode:
				if (turnTime.isRunning() && this.getRemainingTurnTime() <= 0) {
					currentPlayer.startWatch();
				}
				break;
			default:
				break;
			}

			int timeLimit;
			switch (timeLimitType) {
			case TotalTime:
			case TotalTimeAndPerTurn:
			case ExtraFastMode:
				timeLimit = mode.getTimeLimit();

				if (player1.getWatchTime() > timeLimit)
					finishGame(player1.getEnemy());

				if (player2.getWatchTime() > timeLimit)
					finishGame(player2.getEnemy());

				break;
			default:
				break;
			}
		}
	}

	@Override
	public synchronized long getElapsedTurnTime() {
		return turnTime.getElapsedTimeSecs();
	}

	@Override
	public synchronized TurnTypes getTurnType() {
		return turnType;
	}

	@Override
	public synchronized void setMasterPlayingFirstFirst(boolean masterPlayingFirst) {
		isHostPlayingFirst = masterPlayingFirst;
	}

	@Override
	public synchronized int getLastTurnTime() {
		return lastTurnTime;
	}

	@Override
	public synchronized int getRemainingTurnTime() {
		final GameMode mode = getCurrentGameMode();
		int timeLimitPerTurn;
		switch (mode.getTimeLimitType()) {
		case TotalTimeAndPerTurn:
			timeLimitPerTurn = mode.getTimePerTurn();
			break;
		case ExtraFastMode:
			timeLimitPerTurn = lastTurnTime + mode.getTimeExtraPerTurn();
			break;
		case NoTimeLimit:
		case TotalTime:
		default:
			return 0;
		}

		return timeLimitPerTurn - (int) turnTime.getElapsedTimeSecs();
	}

	@Override
	public synchronized Player getWinningPlayer() {
		return winningPlayer;
	}

	@Override
	public synchronized void setGameMode(GameMode gameMode) {
		currentGameMode = gameMode;
	}

	@Override
	public synchronized void setFleet(Fleet fleet) {
		currentFleet = fleet;
	}

	@Override
	public synchronized void setRandomVar(long newSeed) {
		randomVar.setSeed(newSeed);
	}

	@Override
	public synchronized Random getRandomVar() {
		return randomVar;
	}

	@Override
	public synchronized void changeMAX(int width, int height) {
		SCREENX = width;
		SCREENY = height;
	}

	@Override
	public synchronized void draw(Canvas canvas) {
		canvas.drawColor(Color.WHITE);
		if (GUI != null) {
			GUI.draw(canvas);
		}
	}

	@Override
	public synchronized void onTouchEvent(MotionEvent event) {
		if (GUI != null) {
			GUI.onTouchEvent(event);
		}
	}

	@Override
	public synchronized void update(double timeElapsed) {
		updateGlobalConditions();
		if (GUI != null) {
			GUI.update(timeElapsed);
		}
	}

	@Override
	public synchronized void newConditionEvent(Player player, BonusPlay bonusPlay) {
		if (GUI != null && GUI instanceof PlayInterface) {
			((PlayInterface) GUI).newConditionEvent(player, bonusPlay);
		}
	}

	@Override
	public synchronized boolean onBackPressed() {
		if (GUI != null) {
			if (GUI.backPressed())
				return true;
			SoundType.stopAllSounds();
			finishGameCallBack.callback();
		}
		return false;
	}

	@Override
	public synchronized void closeResources() {
	}

}
