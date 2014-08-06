package pt.rmartins.battleships.objects.ai;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import pt.rmartins.battleships.objects.Coordinate2;
import pt.rmartins.battleships.objects.Message;
import pt.rmartins.battleships.objects.Message.MessageUnit.TypesMessageUnits;

public class MessageAI extends Message {

	//	private final List<ShipComputer> shipsWithMessage;

	public MessageAI(Message messageToCopy) {
		super(messageToCopy.getTurnType(), messageToCopy.getTurnNumber(), new ArrayList<MessageUnit>(
				messageToCopy.getParts()), new ArrayList<Coordinate2>(messageToCopy.getCoors()),
				new ArrayList<Coordinate2>(messageToCopy.getCounter()), messageToCopy.wasFirstBlood());
		//		shipsWithMessage = new ArrayList<ShipComputer>();
	}

	@Override
	public List<MessageUnit> getParts() {
		return messageTokens;
	}

	@Override
	public void updateStats() {
		super.updateStats();
	}

	public void removeWater() {
		for (Iterator<MessageUnit> iterator = messageTokens.iterator(); iterator.hasNext();) {
			MessageUnit type = iterator.next();
			if (type.type == TypesMessageUnits.Water) {
				iterator.remove();
				return;
			}
		}
	}

	public void removeShip(int shipId, boolean killerShot) {
		for (Iterator<MessageUnit> iterator = messageTokens.iterator(); iterator.hasNext();) {
			MessageUnit type = iterator.next();
			if (type.shipId == shipId) {
				if (killerShot && type.type == TypesMessageUnits.AKillerShot || !killerShot
						&& type.type == TypesMessageUnits.AShot) {
					iterator.remove();
					return;
				}
			}
		}
	}

	//	public void addShipWithMessage(ShipComputer ship) {
	//		shipsWithMessage.add(ship);
	//	}
	//
	//	public List<ShipComputer> getShipsWithMessage() {
	//		return shipsWithMessage;
	//	}

}
