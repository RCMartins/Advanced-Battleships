package pt.rmartins.battleships.network;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import pt.rmartins.battleships.objects.Coordinate2;
import pt.rmartins.battleships.objects.Ship;

public class ConnectionNuggetaCallBacks implements ConnectionCallback, PlayCallback {

	public static abstract class ForAllConnection {
		public abstract void run(ConnectionCallback callback);
	}

	public static abstract class ForAllPlay {
		public abstract void run(PlayCallback callback);
	}

	private final ConcurrentLinkedQueue<ConnectionCallback> connectionCallbacks;
	private final ConcurrentLinkedQueue<PlayCallback> playCallbacks;

	public ConnectionNuggetaCallBacks() {
		connectionCallbacks = new ConcurrentLinkedQueue<ConnectionCallback>();
		playCallbacks = new ConcurrentLinkedQueue<PlayCallback>();
	}

	public synchronized void addConnectionCallBack(ConnectionCallback callback) {
		connectionCallbacks.add(callback);
	}

	public synchronized void addPlayCallBack(PlayCallback callback) {
		playCallbacks.add(callback);
	}

	public synchronized void removeConnectionCallBack(ConnectionCallback callback) {
		connectionCallbacks.remove(callback);
	}

	public synchronized void removePlayCallBack(PlayCallback callback) {
		playCallbacks.remove(callback);
	}

	public synchronized void sendConnectionCallBack(ForAllConnection forAll) {
		for (ConnectionCallback callback : connectionCallbacks) {
			forAll.run(callback);
		}
	}

	public synchronized void sendPlayCallBack(ForAllPlay forAll) {
		for (PlayCallback callback : playCallbacks) {
			forAll.run(callback);
		}
	}

	@Override
	public synchronized void receiveGameModeInformation(GameDefinition gameDefinition) {
		for (PlayCallback callback : playCallbacks) {
			callback.receiveGameModeInformation(gameDefinition);
		}
	}

	@Override
	public synchronized void receiveInitializingInformation(boolean masterPlaysFirst, long randomSeed) {
		for (PlayCallback callback : playCallbacks) {
			callback.receiveInitializingInformation(masterPlaysFirst, randomSeed);
		}
	}

	@Override
	public synchronized void receiveReadyToStart() {
		for (PlayCallback callback : playCallbacks) {
			callback.receiveReadyToStart();
		}
	}

	@Override
	public synchronized void receiveShipsPosition(List<Ship> ships) {
		for (PlayCallback callback : playCallbacks) {
			callback.receiveShipsPosition(ships);
		}
	}

	@Override
	public synchronized void receiveCancelPlaceShips() {
		for (PlayCallback callback : playCallbacks) {
			callback.receiveCancelPlaceShips();
		}
	}

	@Override
	public synchronized void receiveShotsAndCounters(List<Coordinate2> shotsList, List<Coordinate2> counterList) {
		for (PlayCallback callback : playCallbacks) {
			callback.receiveShotsAndCounters(shotsList, counterList);
		}
	}

	@Override
	public synchronized void receivePause() {
		for (PlayCallback callback : playCallbacks) {
			callback.receivePause();
		}
	}

	@Override
	public synchronized void receiveUnpause() {
		for (PlayCallback callback : playCallbacks) {
			callback.receiveUnpause();
		}
	}

	@Override
	public synchronized void receiveRematchAnswer(boolean accept) {
		for (PlayCallback callback : playCallbacks) {
			callback.receiveRematchAnswer(accept);
		}
	}

	@Override
	public synchronized void receiveOponentDisconnected() {
		for (PlayCallback callback : playCallbacks) {
			callback.receiveOponentDisconnected();
		}
	}

	@Override
	public synchronized void connected() {
		for (ConnectionCallback callback : connectionCallbacks) {
			callback.connected();
		}
	}

	@Override
	public synchronized void errorConnecting(int retriesLeft) {
		for (ConnectionCallback callback : connectionCallbacks) {
			callback.errorConnecting(retriesLeft);
		}
	}

	@Override
	public synchronized void gameStarted(boolean master) {
		for (ConnectionCallback callback : connectionCallbacks) {
			callback.gameStarted(master);
		}
	}

	@Override
	public synchronized void refreshGames(List<GameDefinition> existingGames) {
		for (ConnectionCallback callback : connectionCallbacks) {
			callback.refreshGames(existingGames);
		}
	}

	@Override
	public synchronized void hostedGame(String gameId) {
		for (ConnectionCallback callback : connectionCallbacks) {
			callback.hostedGame(gameId);
		}
	}

	@Override
	public synchronized void joinedGame(String gameId, String playerNickname) {
		for (ConnectionCallback callback : connectionCallbacks) {
			callback.joinedGame(gameId, playerNickname);
		}
	}

	@Override
	public synchronized void unjoinedGame() {
		for (ConnectionCallback callback : connectionCallbacks) {
			callback.unjoinedGame();
		}
	}

	@Override
	public synchronized void oponentDisconnected() {
		for (ConnectionCallback callback : connectionCallbacks) {
			callback.oponentDisconnected();
		}
	}

}
