package pt.rmartins.battleships.network;

import java.util.List;

import pt.rmartins.battleships.network.ConnectionCallback.GameDefinition;
import pt.rmartins.battleships.objects.Coordinate;
import pt.rmartins.battleships.objects.Ship;

public interface PlayCallback {

	public void receiveGameModeInformation(GameDefinition gameDefinition);

	public void receiveInitializingInformation(boolean masterPlaysFirst, long randomSeed);

	public void receiveReadyToStart();

	public void receiveShipsPosition(List<Ship> ships);

	public void receiveCancelPlaceShips();

	public void receiveShotsAndCounters(List<Coordinate> shotsList, List<Coordinate> counterList);

	public void receivePause();

	public void receiveUnpause();

	public void receiveRematchAnswer(boolean accept);

	public void receiveOponentDisconnected();

}
