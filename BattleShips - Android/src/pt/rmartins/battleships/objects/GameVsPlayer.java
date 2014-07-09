package pt.rmartins.battleships.objects;

import java.util.ArrayList;
import java.util.List;

import pt.rmartins.battleships.R;
import pt.rmartins.battleships.network.Connection;
import pt.rmartins.battleships.network.ConnectionCallback.GameDefinition;
import pt.rmartins.battleships.network.ConnectionNugetta;
import pt.rmartins.battleships.network.PlayCallback;
import pt.rmartins.battleships.objects.PlayerClass.Shot.KindShot;
import pt.rmartins.battleships.objects.modes.GameBonus;
import pt.rmartins.battleships.objects.modes.GameBonus.BonusTypes;
import pt.rmartins.battleships.objects.modes.GameBonus.Explosion;
import pt.rmartins.battleships.objects.modes.GameBonus.ExtraTurn;
import pt.rmartins.battleships.objects.modes.GameMode.TimeLimitType;
import pt.rmartins.battleships.objects.userinterface.ChooseScreen;
import pt.rmartins.battleships.objects.userinterface.KeyboardInterface;
import pt.rmartins.battleships.objects.userinterface.LobbyScreen;
import pt.rmartins.battleships.objects.userinterface.LoginScreen;
import pt.rmartins.battleships.objects.userinterface.MultiplayerScreen;
import pt.rmartins.battleships.objects.userinterface.PlacingShipsScreen;
import pt.rmartins.battleships.objects.userinterface.PlayInterface;
import pt.rmartins.battleships.objects.userinterface.PlayingScreen;
import pt.rmartins.battleships.objects.userinterface.UserInterface;
import pt.rmartins.battleships.utilities.LanguageClass;
import android.app.Activity;
import android.view.KeyEvent;

public class GameVsPlayer extends GameClass implements PlayCallback {

	@SuppressWarnings("unused")
	private static final String TAG = GameVsPlayer.class.getSimpleName();

	public static final PlayingMode PLAYING_VERSUS = PlayingMode.PlayerVsPlayerNetwork;
	private final Activity activity;
	private Connection conn;

	public GameVsPlayer(Activity activity, Callback finishGameCallBack) {
		super(finishGameCallBack);
		this.activity = activity;

		conn = new ConnectionNugetta();
		conn.addPlayCallBack(this);
	}

	@Override
	public void initialize() {
		setGameState(GameState.LoginMenu);
	}

	@Override
	public PlayingMode getPlayerVsSomething() {
		return PLAYING_VERSUS;
	}

	public Connection getConnection() {
		return conn;
	}

	@Override
	public synchronized void pauseUnpauseGame(boolean fromNetwork) {
		if (gameState == GameState.Playing) {
			time.pause();
			turnTime.pause();
			player1.pauseWatch();
			player2.pauseWatch();
			setGameState(GameState.PlayingInPause);
			if (!fromNetwork)
				conn.sendPause();
		} else if (gameState == GameState.PlayingInPause) {
			time.continueWatch();
			turnTime.continueWatch();
			player1.continueWatch();
			player2.continueWatch();
			setGameState(GameState.Playing);
			if (!fromNetwork)
				conn.sendUnpause();
		}
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
	}

	@Override
	public void initializePlacingShips(boolean fromNetwork) {
		if (!fromNetwork) {
			isHostPlayingFirst = random.nextBoolean();
			long newSeed = random.nextLong();
			setRandomVar(newSeed);
			conn.sendInitializingInformation(isHostPlayingFirst, newSeed);
		}

		maxX = currentFleet.maxX;
		maxY = currentFleet.maxY;
		final List<Ship> shipsToPlace = currentFleet.getFleet();

		winningPlayer = null;

		player1 = new PlayerClass("player1", shipsToPlace, currentGameMode, this, true, isHostPlayingFirst);
		player2 = new PlayerClass("player2", shipsToPlace, currentGameMode, this, false, !isHostPlayingFirst);
		player1.setEnemy(player2);
		player2.setEnemy(player1);
	}

	@Override
	public void setGameState(GameState newGameState) {
		this.gameState = newGameState;

		switch (this.gameState) {
		case LoginMenu:
			setNewGUI(new LoginScreen(SCREENX, SCREENY, this, activity, conn));
			break;
		case MultiplayerMenu:
			setNewGUI(new MultiplayerScreen(SCREENX, SCREENY, this, conn));
			break;
		case LobbyMenu:
		case WaitMaster:
			setNewGUI(new LobbyScreen(SCREENX, SCREENY, this, conn));
			break;
		case ChoosingMode:
			setNewGUI(new ChooseScreen(SCREENX, SCREENY, this));
			break;
		case SendInitializingInformation:
			setNewGUI(null);
			initializePlacingShips(false);
			break;
		case PlacingShips:
			setNewGUI(new PlacingShipsScreen(SCREENX, SCREENY, this));
			break;
		case Playing:
			setNewGUI(new PlayingScreen(SCREENX, SCREENY, player1, this, finishGameCallBack));
			break;
		case FinishedGame:
			if (time != null) {
				time.stop();
				turnTime.stop();
			}
			break;
		default:
			break;
		}

		if (GUI != null && GUI instanceof PlayInterface)
			((PlayInterface) GUI).changedStateEvent(this.gameState);

		if (this.gameState == GameState.FinishedChoosingMode) {
			conn.createGame(currentGameMode.toFileLanguage(), currentFleet.getFleetNumbers(), currentFleet.maxX,
					currentFleet.maxY);
			setGameState(GameState.MultiplayerMenu);
		}
	}

	private void setNewGUI(UserInterface newGUI) {
		if (GUI != null)
			GUI.clean();
		GUI = newGUI;
	}

	@Override
	public void closeResources() {
		super.closeResources();
		//TODO: só chamar isto se o utilizador fechar a aplicação!!!!! (ou sair do multiplayer)
		if (conn != null) {
			conn.closeConnection();
			conn = null;
		}
	}

	@Override
	public void receiveGameModeInformation(GameDefinition gameDefinition) {
		setGameMode(gameDefinition.getGameMode());
		setFleet(new Fleet(gameDefinition.maxX, gameDefinition.maxY, gameDefinition.fleet));
	}

	@Override
	public void receiveInitializingInformation(boolean hostPlayingFirst, long randomSeed) {
		setMasterPlayingFirstFirst(!hostPlayingFirst);
		setRandomVar(randomSeed);
		initializePlacingShips(true);
		conn.sendReadyToStart();
		setGameState(GameState.PlacingShips);
	}

	@Override
	public void receiveReadyToStart() {
		setGameState(GameState.PlacingShips);
	}

	@Override
	public void receiveShipsPosition(List<Ship> ships) {
		Player enemy = player2;
		for (Ship ship : ships) {
			enemy.placeShipNetwork(ship);
		}
		enemy.tryToSetReady();
	}

	@Override
	public void receiveCancelPlaceShips() {
		Player enemy = player2;
		enemy.cancelSetReady();
	}

	@Override
	public void receiveShotsAndCounters(List<Coordinate> shotsList, List<Coordinate> counterList) {
		Player enemy = player2;
		for (Coordinate coor : shotsList) {
			enemy.setPosition(coor);
			enemy.chooseTarget();
		}
		enemy.setNetworkCounterList(counterList);
		enemy.shotAll();
	}

	@Override
	public synchronized void receivePause() {
		if (gameState == GameState.Playing)
			pauseUnpauseGame(true);
	}

	@Override
	public synchronized void receiveUnpause() {
		if (gameState == GameState.PlayingInPause)
			pauseUnpauseGame(true);
	}

	@Override
	public boolean onBackPressed() {
		if (super.onBackPressed())
			return true;
		if (gameState == GameState.MultiplayerMenu || gameState == GameState.PlacingShips
				|| gameState == GameState.Playing || gameState == GameState.PlayingInPause
				|| gameState == GameState.FinishedGame) {
			if (conn != null)
				conn.close();
		}
		return false;
	}

	@Override
	public void requestRemake(Player playerRequesting) {
		if (playerRequesting == player1)
			conn.sendRematchSignal();
	}

	@Override
	public void receiveRematchAnswer(boolean accept) {
		if (accept) {
			initializePlacingShips(true);
			setGameState(GameState.PlacingShips);
		}
	}

	@Override
	public void receiveOponentDisconnected() {
		if (GUI != null && GUI instanceof PlayInterface)
			((PlayInterface) GUI).externalMessage(LanguageClass.getString(R.string.multiplayer_enemy_disconnected));
		if (gameState == GameState.Playing || gameState == GameState.PlayingInPause) {
			finishGame(player1);
		}
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (GUI != null && GUI instanceof KeyboardInterface)
			return ((KeyboardInterface) GUI).dispatchKeyEvent(event);
		else
			return false;
	}
}
