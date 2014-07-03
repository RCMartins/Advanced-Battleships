package pt.rmartins.battleships.objects.modes;

import java.util.Collections;
import java.util.List;

import pt.rmartins.battleships.R;
import pt.rmartins.battleships.objects.Coordinate;
import pt.rmartins.battleships.objects.PlayerClass.Shot;
import pt.rmartins.battleships.objects.PlayerClass.Shot.KindShot;
import pt.rmartins.battleships.utilities.LanguageClass;

public abstract class GameBonus implements Comparable<GameBonus> {

	public enum BonusTypes {
		ExtraTurn(R.string.bonus_types_extraturn), Explosion(R.string.bonus_types_explosion), ExtraTime(
				R.string.bonus_types_extratime);

		private final int resourceCode;

		private BonusTypes(int resourceCode) {
			this.resourceCode = resourceCode;
		}

		@Override
		public String toString() {
			return LanguageClass.getString(resourceCode);
		}
	}

	public static class ExtraTurn extends GameBonus {

		private final List<KindShot> turnShots;

		public ExtraTurn(List<KindShot> turnShots) {
			super(BonusTypes.ExtraTurn);
			this.turnShots = turnShots;
		}

		public List<KindShot> getTurnShots() {
			return Collections.unmodifiableList(turnShots);
		}

		@Override
		public String toString() {
			return "ExtraTurn(" + Shot.calculateFFListString(turnShots) + ")";
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}

			final ExtraTurn other = (ExtraTurn) obj;
			if (turnShots == null) {
				if (other.turnShots != null) {
					return false;
				}
			} else if (!turnShots.equals(other.turnShots)) {
				return false;
			}

			return super.equals(obj);
		}

	}

	public static class Explosion extends GameBonus {

		private final List<Coordinate> positions;

		public Explosion(List<Coordinate> positions) {
			super(BonusTypes.Explosion);
			this.positions = positions;
		}

		public List<Coordinate> getPositions() {
			return positions;
		}

	}

	public static class ExtraTime extends GameBonus {

		private final int time;

		/**
		 * @param time
		 *            in seconds
		 */
		public ExtraTime(int time) {
			super(BonusTypes.ExtraTime);
			this.time = time;
		}

		public int getTime() {
			return time;
		}

		@Override
		public String toString() {
			return "Time(" + time + ")";
		}

	}

	private final BonusTypes type;

	private GameBonus(BonusTypes type) {
		this.type = type;
	}

	@Override
	public int compareTo(GameBonus o) {
		return type.ordinal() - o.type.ordinal();
	}

	public BonusTypes getType() {
		return type;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final GameBonus other = (GameBonus) obj;
		if (type == null) {
			if (other.type != null) {
				return false;
			}
		} else if (!type.equals(other.type)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return type.toString();
	}
}
