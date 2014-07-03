package pt.rmartins.battleships.objects;

import java.util.List;

import pt.rmartins.battleships.objects.Message.MessageUnit.TypesMessageUnits;

public class Message {

	//TODO: delete all string stuff?? or leave it for toString() debug?

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
			this.type = type; // TypesMessageUnits.Water;
			this.shipId = -1;
		}

		public MessageUnit(String text, boolean killShot, int shipId) {
			this.text = text;
			this.type = killShot ? TypesMessageUnits.AKillerShot : TypesMessageUnits.AShot;
			this.shipId = shipId;
		}

		// public String getText() {
		// return text;
		// }

		public String getShipName() {
			return ShipClass.getName(shipId);
		}

		// public TypesMessageUnits getType() {
		// return type;
		// }

		@Override
		public int compareTo(MessageUnit other) {
			final int ord = this.type.ordinal() - other.type.ordinal();
			return ord != 0 ? ord : this.shipId - other.shipId;
		}

		// public int getShipId() {
		// return shipId;
		// }
	}

	private String message;
	private final List<MessageUnit> parts;
	private final List<Coordinate> coors;
	private final List<Coordinate> counterCoors;

	private int totalHits;
	private final int[] hits;
	private final String turnType;
	private final int turnNumber;
	private boolean hasSomeWater;
	private boolean isAllWater;
	private int kills;
	private final boolean wasFirstBlood;

	public Message(String turnType, int turnNumber, List<MessageUnit> messageTokens, List<Coordinate> coors,
			List<Coordinate> counters, boolean wasFirstBlood) {
		this.turnType = turnType;
		this.turnNumber = turnNumber;
		this.parts = messageTokens;
		this.coors = coors;// == null ? null : coors; //Collections.unmodifiableList(coors);
		this.counterCoors = counters;// == null ? null : counters //;Collections.unmodifiableList(counters);
		this.message = null;
		this.hits = new int[ShipClass.numberOfShips()];
		this.wasFirstBlood = wasFirstBlood;
		if (turnNumber != 0)
			calculate();
	}

	// public Message(Message messageToCopy) {
	// this.turnType = messageToCopy.turnType;
	// this.turnNumber = messageToCopy.turnNumber;
	// this.parts = new ArrayList<MessageUnit>(messageToCopy.parts);
	// this.coors = Collections.unmodifiableList(new ArrayList<Coordinate>(messageToCopy.coors));
	// this.counterCoors = Collections.unmodifiableList(new ArrayList<Coordinate>(messageToCopy.counterCoors));
	// this.message = messageToCopy.message;
	// this.hits = new int[ShipClass.numberOfShips()];
	// calculate();
	// }

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Message) {
			final Message o = (Message) obj;
			return getMessage().equals(o.getMessage());
		}
		return false;
	}

	public int getHits() {
		return totalHits;
	}

	public int getHits(int shipId) {
		return hits[shipId];
	}

	public String getMessage() {
		return message;
	}

	public List<MessageUnit> getParts() {
		return parts; // Collections.unmodifiableList(parts);
	}

	private void calculate() {
		message = "";
		totalHits = 0;

		for (int i = 0; i < ShipClass.numberOfShips(); i++) {
			hits[i] = 0;
		}

		hasSomeWater = false;
		isAllWater = true;
		kills = 0;
		if (parts == null) {
			message = "";
		} else {
			for (final MessageUnit token : parts) {
				if (token.type == TypesMessageUnits.Water || token.type == TypesMessageUnits.Counter) {
					message += "; " + token;
				} else {
					message = token + "; " + message;
					totalHits++;
					hits[token.shipId]++;
				}
			}

			message = message.replace("; ;", ";").replace("  ", " ").trim();
			if (message.startsWith(";")) {
				message = message.substring(1).trim();
			}
			message = (message + ".").replace(";.", ".").replace("!.", "!");

			if (!counterCoors.isEmpty()) {
				message += "Counters: ";
				for (Coordinate coor : counterCoors) {
					message += " " + coor.toStringSmall();
				}
			}

			for (final MessageUnit token : parts) {
				if (token.type == TypesMessageUnits.Water) {
					hasSomeWater = true;
					break;
				}
			}

			for (final MessageUnit token : parts) {
				if (token.type != TypesMessageUnits.Water) {
					isAllWater = false;
					break;
				}
			}

			for (final MessageUnit token : parts)
				if (token.type == TypesMessageUnits.AKillerShot)
					kills++;
		}

	}

	@Override
	public String toString() {
		return getMessage();
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

	public List<Coordinate> getCoors() {
		return coors;
	}

	public List<Coordinate> getCounter() {
		return counterCoors;
	}

	public boolean wasFirstBlood() {
		return wasFirstBlood;
	}
}
