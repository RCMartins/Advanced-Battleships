package pt.rmartins.battleships.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import pt.rmartins.battleships.objects.PlayerClass.Shot.KindShot;
import pt.rmartins.battleships.objects.ai.ComputerAI;
import pt.rmartins.battleships.objects.modes.GameBonus;
import pt.rmartins.battleships.objects.modes.GameBonus.BonusTypes;
import pt.rmartins.battleships.objects.modes.GameBonus.Explosion;
import pt.rmartins.battleships.objects.modes.GameBonus.ExtraTurn;
import pt.rmartins.battleships.objects.modes.GameMode.TimeLimitType;
import pt.rmartins.battleships.objects.userinterface.ChooseScreen;
import pt.rmartins.battleships.objects.userinterface.PlacingShipsScreen;
import pt.rmartins.battleships.objects.userinterface.PlayInterface;
import pt.rmartins.battleships.objects.userinterface.PlayingScreen;
import android.app.Activity;

public class GameVsComputer extends GameClass {

	@SuppressWarnings("unused")
	private static final String TAG = GameVsComputer.class.getSimpleName();

	public static final PlayingMode PLAYING_VERSUS = PlayingMode.PlayerVsComputer;

	private final ExecutorService service;
	private final ScheduledExecutorService canceller;

	private Thread initializeThread;

	public GameVsComputer(Activity activity, Callback finishGameCallBack) {
		super(finishGameCallBack);

		service = Executors.newFixedThreadPool(1);
		canceller = Executors.newScheduledThreadPool(1);
	}

	@Override
	public void initialize() {
		setGameState(GameState.ChoosingMode);
	}

	@Override
	public PlayingMode getPlayerVsSomething() {
		return PLAYING_VERSUS;
	}

	@Override
	public void initializePlacingShips(boolean _fromNetwork) {
		isHostPlayingFirst = random.nextBoolean();
		setRandomVar(random.nextLong());

		maxX = currentFleet.maxX;
		maxY = currentFleet.maxY;
		final List<Ship> shipsToPlace = currentFleet.getFleet();

		winningPlayer = null;

		player1 = new PlayerClass("player1", shipsToPlace, currentGameMode, this, true, isHostPlayingFirst);
		player2 = new ComputerAI("player2", shipsToPlace, currentGameMode, this, false, !isHostPlayingFirst);

		player1.setEnemy(player2);
		player2.setEnemy(player1);

		//		initializeThread = new Thread(new Runnable() {
		//			@Override
		//			public void run() {
		((ComputerAI) player2).initialize(shipsToPlace);
		((ComputerAI) player2).computerPlaceYourShips(false);
		//			}
		//		});
		//		initializeThread.start();
	}

	@Override
	protected void goNextTurn() {
		final NextTurn nextTurn = new NextTurn(defaultNextTurn);

		boolean extraTurnUsed = false;
		List<GameBonus> newList = new ArrayList<GameBonus>(currentPlayer.getBonusInNextTurn());
		for (final GameBonus gBonus : newList) {
			final BonusTypes type = gBonus.getType();
			if (type == BonusTypes.Explosion) { // TODO: este metodo está um bocado desfigurado :/
				if (!extraTurnUsed) {
					currentPlayer.deleteUsedBonus(gBonus);

					turnType = TurnTypes.Explosion;
					setTurnState(TurnState.ChooseTargets);

					final Explosion bonus = (Explosion) gBonus;

					nextTurn.targets = new ArrayList<List<KindShot>>(1);
					final ArrayList<KindShot> list = new ArrayList<KindShot>();
					for (int i = 0; i < bonus.getPositions().size(); i++) {
						list.add(KindShot.IndestructibleShot);
					}
					nextTurn.targets.add(list);
					currentPlayer.setNumberOfTargets(nextTurn.targets);

					for (final Coordinate coor : bonus.getPositions()) {
						currentPlayer.setPosition(coor);
						currentPlayer.chooseTarget();
					}
					currentPlayer.shotAll();

					extraTurnUsed = true;
					return;
				}
			}
		}

		GameBonus usedGameBonus = null;

		newList = new ArrayList<GameBonus>(currentPlayer.getBonusInNextTurn());
		for (final GameBonus gBonus : newList) {
			final BonusTypes type = gBonus.getType();
			if (type == BonusTypes.ExtraTurn) {
				if (!extraTurnUsed) {
					usedGameBonus = gBonus;

					extraTurnUsed = true;
					nextTurn.turnType = TurnTypes.Extra;
					nextTurn.nextTurnPlus = 0;
					nextTurn.nextPlayer = currentPlayer;
					final ExtraTurn bonus = (ExtraTurn) gBonus;
					nextTurn.targets = new ArrayList<List<KindShot>>(1);
					nextTurn.targets.add(new ArrayList<KindShot>(bonus.getTurnShots()));
					currentPlayer.deleteUsedBonus(gBonus);
				}
			}
		}

		turnType = nextTurn.turnType;
		currentTurn += nextTurn.nextTurnPlus;
		currentPlayer.setNumberOfTargets(nextTurn.targets);

		if (turnType == TurnTypes.Normal && currentTurn == 1.0) {
			currentPlayer.getEnemy().setNumberOfTargets(nextTurn.targets);
		}

		if (currentTurn > 1.0 && nextTurn.nextPlayer == null) {
			currentPlayer = currentPlayer.getEnemy();
		}

		setTurnState(TurnState.ChooseTargets);

		if (getCurrentGameMode().getTimeLimitType() == TimeLimitType.TotalTime) {
			currentPlayer.startWatch();
		}

		if (turnType == TurnTypes.Normal) { // is the next turn is a normal turn? then update lastTurnTime
			if (currentTurn == 1.0) {
				lastTurnTime = getCurrentGameMode().getTimePerTurn() - getCurrentGameMode().getTimeExtraPerTurn();
			} else {
				lastTurnTime = (int) turnTime.getElapsedTimeSecs();
			}
		}
		turnTime.resetAndStop();
		turnTime.start();

		if (GUI != null) {
			if (turnType == TurnTypes.Normal) {
				if (GUI instanceof PlayInterface)
					((PlayInterface) GUI).newTurnEvent(currentPlayer);
			} else if (turnType == TurnTypes.Extra && usedGameBonus != null) {
			}
		}

		class PlayTurn implements Callable<Void> {
			@Override
			public Void call() throws Exception {
				((ComputerAI) player2).playYourTurn();
				return null;
			}
		}

		//		class Update implements Callable<Void> {
		//			@Override
		//			public Void call() throws Exception {
		//				((ComputerAI) player2).DEBUG_updateGameStatus();
		//				return null;
		//			}
		//		}
		//
		//		class Wait implements Callable<Void> {
		//
		//			private final int time;
		//
		//			public Wait(int time) {
		//				this.time = time;
		//			}
		//
		//			@Override
		//			public Void call() throws Exception {
		//				Thread.sleep(time * 1000);
		//				return null;
		//			}
		//		}

		if (currentPlayer == player2) {
			service.submit(new PlayTurn());

			//			if (ComputerAI.DEBUG_AI) {
			//				//new Scheduler().execute(new Update(), 10)./*execute(new Wait(3), 10).*/execute(new PlayTurn(), 10);
			//							execute(new PlayTurn());
			//			} else {
			//				execute(new PlayTurn(), 10);
			//			}

			//			new Thread(new Runnable() {
			//				@Override
			//				public void run() {
			//					((ComputerAI) player2).playYourTurn();
			//				}
			//			}).start();
		}
	}

	//	private void execute(Callable<?> c, int time) {
	//		final Future<?> future = service.submit(c);
	//		canceller.schedule(new Runnable() {
	//			@Override
	//			public void run() {
	//				//				if (future.cancel(true)) {
	//				//					Log.i(TAG, "Task cancelled!!");
	//				//					((ComputerAI) player2).playYourTurnRandom();
	//				//				}
	//			}
	//		}, time, TimeUnit.SECONDS);
	//	}

	@Override
	public void pauseUnpauseGame(boolean _fromNetwork) {
		if (gameState == GameState.Playing) {
			time.pause();
			turnTime.pause();
			player1.pauseWatch();
			player2.pauseWatch();
			setGameState(GameState.PlayingInPause);
		} else if (gameState == GameState.PlayingInPause) {
			time.continueWatch();
			turnTime.continueWatch();
			player1.continueWatch();
			player2.continueWatch();
			setGameState(GameState.Playing);
		}
	}

	@Override
	public void setGameState(GameState newGameState) {
		//		if (newGameState == GameState.Playing) {
		//			try {
		//				initializeThread.join();
		//			} catch (InterruptedException e) {
		//			}
		//		}
		this.gameState = newGameState;

		switch (this.gameState) {
		case ChoosingMode:
			GUI = new ChooseScreen(SCREENX, SCREENY, this);
			break;
		case PlacingShips:
			initializePlacingShips(false);
			GUI = new PlacingShipsScreen(SCREENX, SCREENY, this);
			break;
		case Playing:
			GUI = new PlayingScreen(SCREENX, SCREENY, player1, this, finishGameCallBack);
			break;
		case FinishedGame:
			time.stop();
			turnTime.stop();
			break;
		default:
			break;
		}

		if (GUI != null && GUI instanceof PlayInterface)
			((PlayInterface) GUI).changedStateEvent(this.gameState);

		if (this.gameState == GameState.FinishedChoosingMode)
			setGameState(GameState.PlacingShips);
	}

	@Override
	public void requestRemake(Player _playerRequesting) {
		setGameState(GameState.PlacingShips);
	}
}
