package pt.rmartins.battleships.objects;

import java.util.List;

import pt.rmartins.battleships.objects.Message.MessageUnit.TypesMessageUnits;

public class Message {

	public static class MessageUnit implements Comparable<MessageUnit> {
		@Override
		public String toString() {
			return text;
		}

		public enum TypesMessageUnits {
			AShot, AKillerShot, Water, Counter;
		}

		public final String text;
		public final int shipId;
		public final TypesMessageUnits type;

		public MessageUnit(TypesMessageUnits type, String text) {
			this.text = text;
			this.type = type;
			this.shipId = -1;
		}

		public MessageUnit(String text, boolean killShot, int shipId) {
			this.text = text;
			this.type = killShot ? TypesMessageUnits.AKillerShot : TypesMessageUnits.AShot;
			this.shipId = shipId;
		}

		public String getShipName() {
			return ShipClass.getName(shipId);
		}

		@Override
		public int compareTo(MessageUnit other) {
			final int ord = this.type.ordinal() - other.type.ordinal();
			return ord != 0 ? ord : this.shipId - other.shipId;
		}
	}

	protected final List<MessageUnit> messageTokens;
	protected final List<Coordinate2> coors;
	protected final List<Coordinate2> counters;

	private int totalHits;
	private final int[] hits;
	protected final String turnType;
	protected final int turnNumber;
	private boolean hasSomeWater;
	private boolean isAllWater;
	private int kills;
	protected final boolean wasFirstBlood;

	public Message(String turnType, int turnNumber, List<MessageUnit> messageTokens, List<Coordinate2> coors,
			List<Coordinate2> counters, boolean wasFirstBlood) {
		this.turnType = turnType;
		this.turnNumber = turnNumber;
		this.messageTokens = messageTokens;
		this.coors = coors;
		this.counters = counters;
		this.hits = new int[ShipClass.numberOfShips()];
		this.wasFirstBlood = wasFirstBlood;
		if (turnNumber != 0)
			updateStats();
	}

	public int getHits() {
		return totalHits;
	}

	public int getHits(int shipId) {
		return hits[shipId];
	}

	public List<MessageUnit> getParts() {
		return messageTokens;
	}

	protected void updateStats() {
		totalHits = 0;

		for (int i = 0; i < ShipClass.numberOfShips(); i++) {
			hits[i] = 0;
		}

		hasSomeWater = false;
		isAllWater = true;
		kills = 0;
		if (messageTokens != null) {
			for (final MessageUnit token : messageTokens) {
				if (token.type != TypesMessageUnits.Water && token.type != TypesMessageUnits.Counter) {
					totalHits++;
					hits[token.shipId]++;
				}
			}

			for (final MessageUnit token : messageTokens) {
				if (token.type == TypesMessageUnits.AKillerShot)
					kills++;

				if (token.type == TypesMessageUnits.Water)
					hasSomeWater = true;
				else
					isAllWater = false;
			}
		}
	}

	@Override
	public String toString() {
		return messageTokens + "" + counters;
	}

	public boolean hasSomeWater() {
		return hasSomeWater;
	}

	public boolean isAllWater() {
		return isAllWater;
	}

	public String getTurnType() {
		return turnType;
	}

	public int getTurnNumber() {
		return turnNumber;
	}

	public String getTurnId() {
		return turnType + turnNumber;
	}

	public int getKills() {
		return kills;
	}

	public int getMisses() {
		return coors.size() - getHits();
	}

	public List<Coordinate2> getCoors() {
		return coors;
	}

	public List<Coordinate2> getCounter() {
		return counters;
	}

	public boolean wasFirstBlood() {
		return wasFirstBlood;
	}
}
