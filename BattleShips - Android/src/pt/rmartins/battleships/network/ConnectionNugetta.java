package pt.rmartins.battleships.network;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pt.rmartins.battleships.network.ConnectionCallback.GameDefinition;
import pt.rmartins.battleships.objects.Coordinate;
import pt.rmartins.battleships.objects.Ship;
import pt.rmartins.battleships.objects.ShipClass;
import pt_rmartins_battleships.ngdl.nobjects.NuggCoordinate;
import pt_rmartins_battleships.ngdl.nobjects.NuggGameInitialization;
import pt_rmartins_battleships.ngdl.nobjects.NuggGameMode;
import pt_rmartins_battleships.ngdl.nobjects.NuggShip;
import pt_rmartins_battleships.ngdl.nobjects.NuggShipsPosition;
import pt_rmartins_battleships.ngdl.nobjects.NuggSingleMessage;
import pt_rmartins_battleships.ngdl.nobjects.NuggSingleObject;
import pt_rmartins_battleships.ngdl.nobjects.NuggTurn;
import android.util.Log;

import com.nuggeta.NuggetaPlug;
import com.nuggeta.network.Message;
import com.nuggeta.ngdl.nobjects.GameRunningState;
import com.nuggeta.ngdl.nobjects.GameStateChange;
import com.nuggeta.ngdl.nobjects.GetGamesResponse;
import com.nuggeta.ngdl.nobjects.JoinGameResponse;
import com.nuggeta.ngdl.nobjects.JoinGameStatus;
import com.nuggeta.ngdl.nobjects.MatchMakingType;
import com.nuggeta.ngdl.nobjects.NGame;
import com.nuggeta.ngdl.nobjects.NMatchMakingConditions;
import com.nuggeta.ngdl.nobjects.NPlayer;
import com.nuggeta.ngdl.nobjects.NuggetaQuery;
import com.nuggeta.ngdl.nobjects.PlayerEnterGame;
import com.nuggeta.ngdl.nobjects.PlayerUnjoinGame;
import com.nuggeta.ngdl.nobjects.SearchImmediateGameResponse;
import com.nuggeta.ngdl.nobjects.SearchImmediateGameStatus;
import com.nuggeta.ngdl.nobjects.StartResponse;
import com.nuggeta.ngdl.nobjects.StartStatus;
import com.nuggeta.ngdl.nobjects.UnjoinGameResponse;
import com.nuggeta.ngdl.nobjects.UnjoinGameStatus;

public class ConnectionNugetta implements Connection {

	private static final String TAG = NuggetaPlug.class.getSimpleName();

	private NuggetaPlug nuggetaPlug;
	private String myGameID;

	private boolean connected;
	private GameRunningState gameState;
	private final ConnectionNuggetaCallBacks callbackHelper;

	private boolean host;
	private int tryNumber;

	public ConnectionNugetta() {
		// start the NuggetaPlug
		nuggetaPlug = new NuggetaPlug("nuggeta://pt_rmartins_battleships_46e37c39-4062-43d3-9606-84882002aa94");
		nuggetaPlug.start();

		tryNumber = 1;

		connected = false;
		host = false;

		callbackHelper = new ConnectionNuggetaCallBacks();

		gameState = GameRunningState.FINISHED;

		// start the GameLoop Thread which handle received Messages
		Thread gameLoopThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						// pump incoming messages
						List<Message> messages = nuggetaPlug.pump();

						for (Message message : messages) {
							handleMessage(message);
						}

						Thread.sleep(10);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
		gameLoopThread.start();

	}

	private void handleMessage(final Message message) {
		if (message instanceof StartResponse) {
			StartResponse startResponse = (StartResponse) message;
			if (startResponse.getStartStatus() == StartStatus.READY) {
				connected = true;
				Log.i(TAG, "Connection Ready with Nuggeta");
				callbackHelper.connected();
			} else {
				if (startResponse.getStartStatus() == StartStatus.WARNED) {
					Log.i(TAG, "Connection warned with Nuggeta");
				} else if (startResponse.getStartStatus() == StartStatus.REFUSED) {
					Log.i(TAG, "Connection refused with Nuggeta");
				} else if (startResponse.getStartStatus() == StartStatus.FAILED) {
					Log.i(TAG, "Connection refused with Nuggeta");
				}

				callbackHelper.errorConnecting(ConnectionCallback.MAX_CONNECTION_RETRIES - tryNumber);
				tryNumber++;
				if (tryNumber <= ConnectionCallback.MAX_CONNECTION_RETRIES) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
					nuggetaPlug = new NuggetaPlug(
							"nuggeta://pt_rmartins_battleships_46e37c39-4062-43d3-9606-84882002aa94");
					nuggetaPlug.start();
				}
			}
		} else if (message instanceof GetGamesResponse) {
			GetGamesResponse getGamesResponse = (GetGamesResponse) message;
			final List<NGame> games = getGamesResponse.getGames();
			final List<GameDefinition> gameDefinitions = new ArrayList<GameDefinition>(games.size());
			for (NGame nGame : games) {
				final List<NPlayer> players = nGame.getPlayers();
				if (players.isEmpty()) {
					nuggetaPlug.stopGame(nGame.getId());
				} else {
					final NuggGameMode nuggGameDef = nGame.getMatchMakingConditions().getGameDefinition();
					GameDefinition gameDef = new GameDefinition(nGame.getId(), nuggGameDef.getFleet(),
							nuggGameDef.getGameModeStr(), nuggGameDef.getMaxX(), nuggGameDef.getMaxY());
					gameDefinitions.add(gameDef);
				}
			}
			List<GameDefinition> unmodifiableList = Collections.unmodifiableList(gameDefinitions);
			callbackHelper.refreshGames(unmodifiableList);

			Log.i(TAG, "GetGames: " + gameDefinitions.size() + " avaiable");
		} else if (message instanceof JoinGameResponse) {
			JoinGameResponse joinGameResponse = (JoinGameResponse) message;
			if (joinGameResponse.getJoinGameStatus() == JoinGameStatus.ACCEPTED) {
				final NGame game = joinGameResponse.getGame();
				myGameID = game.getId();
				host = false;
				Log.i(TAG, "Joined Game: " + myGameID);
				callbackHelper.joinedGame(myGameID);
				{
					final NuggGameMode nuggGameDef = game.getMatchMakingConditions().getGameDefinition();
					GameDefinition gameDefinition = new GameDefinition(myGameID, nuggGameDef.getFleet(),
							nuggGameDef.getGameModeStr(), nuggGameDef.getMaxX(), nuggGameDef.getMaxY());
					callbackHelper.receiveGameModeInformation(gameDefinition);
				}
			}
		} else if (message instanceof UnjoinGameResponse) {
			UnjoinGameResponse unjoinGameResponse = (UnjoinGameResponse) message;
			if (unjoinGameResponse.getUnjoinGameStatus() == UnjoinGameStatus.SUCCESS) {
				if (myGameID.equals(unjoinGameResponse.getGameId())) {
					myGameID = null;
				}
				Log.i(TAG, "Unjoined Game: " + unjoinGameResponse.getGameId());
				callbackHelper.unjoinedGame();
			}
			//		} else if (message instanceof PlayerUnjoinGame) {
			//			PlayerUnjoinGame playerUnjoinGame = (PlayerUnjoinGame) message;
			//			Log.i(TAG, "Player Unjoined Game: " + playerUnjoinGame.getGameId());
		} else if (message instanceof GameStateChange) {
			GameStateChange gameStateChange = (GameStateChange) message;
			gameState = gameStateChange.getGameRunningState();
			if (gameStateChange.getGameRunningState() == GameRunningState.RUNNING) {
				callbackHelper.gameStarted(host);
			}
			Log.i(TAG, "GameStateChange: " + gameState);
		} else if (message instanceof PlayerEnterGame) {
			PlayerEnterGame playerEnterGame = (PlayerEnterGame) message;
			final NPlayer player = playerEnterGame.getPlayer();
			Log.i(TAG, "PlayerEnterGame: id: " + player.getID() + " name: " + player.getName());
		} else if (message instanceof NuggGameInitialization) {
			NuggGameInitialization nuggGameInitialization = (NuggGameInitialization) message;
			final boolean hostPlayingFirst = nuggGameInitialization.isHostPlayingFirst();
			final long randomSeed = Long.parseLong(nuggGameInitialization.getRandomSeed());
			callbackHelper.receiveInitializingInformation(hostPlayingFirst, randomSeed);
		} else if (message instanceof NuggSingleMessage) {
			NuggSingleMessage nuggSingleMessage = (NuggSingleMessage) message;
			final NuggSingleObject singleMessage = nuggSingleMessage.getMessage();
			if (singleMessage == NuggSingleObject.READY_TO_START) {
				callbackHelper.receiveReadyToStart();
			} else if (singleMessage == NuggSingleObject.CANCEL_PLACED_SHIPS) {
				callbackHelper.receiveCancelPlaceShips();
			} else if (singleMessage == NuggSingleObject.PAUSE) {
				callbackHelper.receivePause();
			} else if (singleMessage == NuggSingleObject.UNPAUSE) {
				callbackHelper.receiveUnpause();
			} else if (singleMessage == NuggSingleObject.REMAKE_REQUEST) { // TODO: completar isto
				sendRematchAnswerSignal(true);
				callbackHelper.receiveRematchAnswer(true);
			} else if (singleMessage == NuggSingleObject.REMAKE_ACCEPT) {
				callbackHelper.receiveRematchAnswer(true);
			} else if (singleMessage == NuggSingleObject.REMAKE_DECLINE) {
				callbackHelper.receiveRematchAnswer(false);
			}
		} else if (message instanceof NuggShipsPosition) {
			NuggShipsPosition nuggShipsPosition = (NuggShipsPosition) message;
			callbackHelper.receiveShipsPosition(convertListNuggToShip(nuggShipsPosition.getPosition()));
		} else if (message instanceof NuggTurn) {
			final NuggTurn nuggTurn = (NuggTurn) message;
			{
				final List<Coordinate> shots = Collections.unmodifiableList(convertListNuggToCoordinate(nuggTurn
						.getShots()));
				final List<Coordinate> counters = Collections.unmodifiableList(convertListNuggToCoordinate(nuggTurn
						.getCounters()));
				callbackHelper.receiveShotsAndCounters(shots, counters);
			}
		} else if (message instanceof SearchImmediateGameResponse) {
			SearchImmediateGameResponse gameResponse = (SearchImmediateGameResponse) message;
			final SearchImmediateGameStatus gameStatus = gameResponse.getSearchImmediateGameStatus();
			if (gameStatus == SearchImmediateGameStatus.ACCEPTED) {
				myGameID = gameResponse.getGame().getId();
				Log.i(TAG, "Created/Joined Game: " + myGameID);
				host = true;
				callbackHelper.joinedGame(myGameID);
			}
		} else if (message instanceof PlayerUnjoinGame) {
			callbackHelper.oponentDisconnected();
			callbackHelper.receiveOponentDisconnected();
		} else {
			Log.i(TAG, "Received unhandled message : " + message);
		}
	}

	@Override
	public void addConnectionCallBack(ConnectionCallback callback) {
		callbackHelper.addConnectionCallBack(callback);
	}

	@Override
	public void addPlayCallBack(PlayCallback callback) {
		callbackHelper.addPlayCallBack(callback);
	}

	@Override
	public void sendInitializingInformation(boolean hostPlayingFirst, long randomSeed) {
		NuggGameInitialization nuggGameInitialization = new NuggGameInitialization();
		nuggGameInitialization.setHostPlayingFirst(hostPlayingFirst);
		nuggGameInitialization.setRandomSeed("" + randomSeed);
		nuggetaPlug.sendGameMessage(nuggGameInitialization, myGameID);
	}

	@Override
	public void sendReadyToStart() {
		NuggSingleMessage nuggSingleMessage = new NuggSingleMessage();
		nuggSingleMessage.setMessage(NuggSingleObject.READY_TO_START);
		nuggetaPlug.sendGameMessage(nuggSingleMessage, myGameID);
	}

	@Override
	public void sendShipsPosition(List<Ship> ships) {
		NuggShipsPosition nuggShipsPosition = new NuggShipsPosition();
		List<NuggShip> list = new ArrayList<NuggShip>(ships.size());
		for (Ship ship : ships) {
			list.add(convertShipToNugg(ship));
		}
		nuggShipsPosition.setPosition(list);
		nuggetaPlug.sendGameMessage(nuggShipsPosition, myGameID);
	}

	private NuggShip convertShipToNugg(Ship ship) {
		NuggShip nuggShip = new NuggShip();
		nuggShip.setId(ship.getId());
		nuggShip.setX(ship.minX());
		nuggShip.setY(ship.minY());
		nuggShip.setRotation(ship.getRotation());
		return nuggShip;
	}

	private List<Ship> convertListNuggToShip(List<NuggShip> nuggList) {
		List<Ship> list = new ArrayList<Ship>(nuggList.size());
		for (NuggShip nuggShip : nuggList) {
			list.add(convertNuggToShip(nuggShip));
		}
		return list;
	}

	private Ship convertNuggToShip(NuggShip nuggShip) {
		return new ShipClass(nuggShip.getId(), nuggShip.getRotation(), nuggShip.getX(), nuggShip.getY());
	}

	@Override
	public void sendCancelPlaceShips() {
		NuggSingleMessage nuggSingleMessage = new NuggSingleMessage();
		nuggSingleMessage.setMessage(NuggSingleObject.CANCEL_PLACED_SHIPS);
		nuggetaPlug.sendGameMessage(nuggSingleMessage, myGameID);
	}

	@Override
	public void sendShotsAndCounters(List<Coordinate> shotsList, List<Coordinate> counterList) {
		NuggTurn nuggTurn = new NuggTurn();
		nuggTurn.setShots(convertListCoordinateToNugg(shotsList));
		nuggTurn.setCounters(convertListCoordinateToNugg(counterList));
		nuggetaPlug.sendGameMessage(nuggTurn, myGameID);
	}

	private List<NuggCoordinate> convertListCoordinateToNugg(List<Coordinate> coorList) {
		List<NuggCoordinate> list = new ArrayList<NuggCoordinate>(coorList.size());
		for (Coordinate coor : coorList) {
			list.add(convertCoordinateToNugg(coor));
		}
		return list;
	}

	private NuggCoordinate convertCoordinateToNugg(Coordinate coor) {
		NuggCoordinate nuggCoordinate = new NuggCoordinate();
		nuggCoordinate.setX(coor.x);
		nuggCoordinate.setY(coor.y);

		return nuggCoordinate;
	}

	private List<Coordinate> convertListNuggToCoordinate(List<NuggCoordinate> nuggList) {
		List<Coordinate> list = new ArrayList<Coordinate>(nuggList.size());
		for (NuggCoordinate nuggCoor : nuggList) {
			list.add(convertNuggToCoordinate(nuggCoor));
		}
		return list;
	}

	private Coordinate convertNuggToCoordinate(NuggCoordinate nuggCoor) {
		return new Coordinate(nuggCoor.getX(), nuggCoor.getY());
	}

	@Override
	public void sendPause() {
		NuggSingleMessage nuggSingleMessage = new NuggSingleMessage();
		nuggSingleMessage.setMessage(NuggSingleObject.PAUSE);
		nuggetaPlug.sendGameMessage(nuggSingleMessage, myGameID);
	}

	@Override
	public void sendUnpause() {
		NuggSingleMessage nuggSingleMessage = new NuggSingleMessage();
		nuggSingleMessage.setMessage(NuggSingleObject.UNPAUSE);
		nuggetaPlug.sendGameMessage(nuggSingleMessage, myGameID);
	}

	@Override
	public void sendRematchSignal() {
		NuggSingleMessage nuggSingleMessage = new NuggSingleMessage();
		nuggSingleMessage.setMessage(NuggSingleObject.REMAKE_REQUEST);
		nuggetaPlug.sendGameMessage(nuggSingleMessage, myGameID);
	}

	@Override
	public void sendRematchAnswerSignal(boolean accept) {
		NuggSingleMessage nuggSingleMessage = new NuggSingleMessage();
		nuggSingleMessage.setMessage(accept ? NuggSingleObject.REMAKE_ACCEPT : NuggSingleObject.REMAKE_DECLINE);
		nuggetaPlug.sendGameMessage(nuggSingleMessage, myGameID);
	}

	@Override
	public boolean isConnected() {
		return connected;
	}

	@Override
	public void closeConnection() {
		exitCurrentGame();
		nuggetaPlug.stop();
	}

	@Override
	public void createGame(String gameModeStr, List<Integer> fleet, int maxX, int maxY) {
		if (connected) {
			//nuggetaPlug.createGame();

			NMatchMakingConditions matchMakingConditions = new NMatchMakingConditions();
			matchMakingConditions.setMatchMakingType(MatchMakingType.SEARCH_GAME);
			NuggGameMode gameDefinition = new NuggGameMode();
			gameDefinition.setGameModeStr(gameModeStr);
			gameDefinition.setFleet(fleet);
			gameDefinition.setMaxX(maxX);
			gameDefinition.setMaxY(maxY);
			matchMakingConditions.setGameDefinition(gameDefinition);

			NuggetaQuery dbQuery = new NuggetaQuery();
			dbQuery.setQuery("$WHERE Players.size > 0");

			nuggetaPlug.searchImmediateGameByConditions(dbQuery, matchMakingConditions);
		}
	}

	//	private void joinCreatedGame(String gameId) {
	//		if (connected) {
	//			exitCurrentGame();
	//			nuggetaPlug.joinGame(gameId);
	//			master = true;
	//		}
	//	}

	@Override
	public void joinExistingGame(String gameId) {
		if (connected) {
			exitCurrentGame();
			nuggetaPlug.joinGame(gameId);
		}
	}

	@Override
	public void unjoinGame(String gameId) {
		if (connected) {
			nuggetaPlug.unjoinGame(gameId);
		}
	}

	@Override
	public void refreshGames() {
		if (connected) {
			NuggetaQuery dbQuery = new NuggetaQuery();
			dbQuery.setQuery("$WHERE Players.size >= 0");
			nuggetaPlug.getGames(dbQuery);

			//			nuggetaPlug.getPlayerProfile();
			//			nuggetaPlug.getPlayerWallet();
		}
	}

	@Override
	public void exitCurrentGame() {
		if (connected) {
			if (myGameID != null)
				nuggetaPlug.unjoinGame(myGameID);
		}
	}

	@Override
	public String getJoinedGameId() {
		return myGameID;
	}
}
