package pt.rmartins.battleships.objects.userinterface;

import pt.rmartins.battleships.objects.Game.GameState;
import pt.rmartins.battleships.objects.Game.TurnState;
import pt.rmartins.battleships.objects.Player;
import pt.rmartins.battleships.objects.modes.GameMode.BonusPlay;

public interface PlayInterface {

	public void changedStateEvent(GameState changedState);

	public void changedTurnState(TurnState changedTurnStatus);

	public void newConditionEvent(Player extraTurnPlayer, BonusPlay bonusPlay);

	public void newTurnEvent(Player player);

	public void externalMessage(String messageStr);

}
