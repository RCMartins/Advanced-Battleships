package pt.rmartins.battleships.network;

import java.util.List;

import pt.rmartins.battleships.objects.Coordinate;
import pt.rmartins.battleships.objects.Ship;

public interface Connection {

	/* #BEGIN Game Calls */

	public void sendInitializingInformation(boolean hostPlayingFirst, long randomSeed);

	public void sendReadyToStart();

	public void sendShipsPosition(List<Ship> ships);

	public void sendCancelPlaceShips();

	public void sendShotsAndCounters(List<Coordinate> shotsList, List<Coordinate> counterList);

	public void sendPause();

	public void sendUnpause();

	public void sendRematchSignal();

	public void sendRematchAnswerSignal(boolean accept);

	/* #END Game Calls */

	/* #BEGIN Connection */

	public void addConnectionCallBack(ConnectionCallback callback);

	public void addPlayCallBack(PlayCallback callback);

	public void removeConnectionCallBack(ConnectionCallback callback);

	public void removePlayCallBack(PlayCallback callback);

	boolean isConnected();

	public void closeConnection();

	public String getJoinedGameId();

	public void close();

	/* #END Connection */

	/* #BEGIN Nuggeta Logic */

	public void createGame(String gameModeStr, List<Integer> fleet, int maxX, int maxY);

	public void refreshGames();

	public void joinGame(String gameId);

	public void unJoinGame(String gameId);

	public void unHostGame(String gameId);

	public boolean isHost();

	public void setNickname(String nickname);

	/* #END Nuggeta Logic */

}
