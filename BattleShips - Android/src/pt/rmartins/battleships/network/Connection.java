package pt.rmartins.battleships.network;

import java.util.List;

import pt.rmartins.battleships.objects.Coordinate2;
import pt.rmartins.battleships.objects.Ship;

public interface Connection {

	/* #BEGIN Game Calls */

	public void sendInitializingInformation(boolean hostPlayingFirst, long randomSeed);

	public void sendShipsPosition(List<Ship> ships);

	public void sendReadyToStart();

	public void sendCancelReadyToStart();

	public void sendShotsAndCounters(List<Coordinate2> shotsList, List<Coordinate2> counterList);

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

	public void close();

	/* #END Connection */

	/* #BEGIN Nuggeta Logic */

	public void hostGame(String gameModeStr, List<Integer> fleet, int maxX, int maxY);

	public void unHostGame(String gameId);

	public void refreshGames();

	public void joinGame(String gameId);

	public void unJoinGame(String gameId);

	public String getJoinedGameId();

	public boolean isHost();

	public void setNickname(String nickname);

	/* #END Nuggeta Logic */

}
