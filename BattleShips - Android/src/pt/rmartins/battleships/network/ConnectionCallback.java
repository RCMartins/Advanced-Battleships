package pt.rmartins.battleships.network;

import java.util.List;

import pt.rmartins.battleships.objects.modes.GameMode;
import pt.rmartins.battleships.parser.gamemodes.ParserGameModes;

public interface ConnectionCallback {

	public static class GameDefinition {
		public final String gameId;
		public final List<Integer> fleet;
		public final String gameModeStr;
		public final int maxX, maxY;
		private GameMode gameMode;

		public GameDefinition(String gameId, List<Integer> fleet, String gameModeStr, int maxX, int maxY) {
			this.gameId = gameId;
			this.fleet = fleet;
			this.gameModeStr = gameModeStr;
			this.maxX = maxX;
			this.maxY = maxY;
			gameMode = null;
		}

		public GameMode getGameMode() {
			if (gameMode == null)
				gameMode = ParserGameModes.parseGameModes(gameModeStr).get(0);
			return gameMode;
		}
	}

	public static final int MAX_CONNECTION_RETRIES = 5;

	public void connected();

	public void errorConnecting(int retriesLeft);

	public void gameStarted(boolean master);

	public void refreshGames(List<GameDefinition> existingGames);

	public void hostedGame(String gameId);

	public void joinedGame(String gameId, String playerNickname);

	public void unjoinedGame();

	public void oponentDisconnected();

}
