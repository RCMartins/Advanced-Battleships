package pt.rmartins.battleships.objects.modes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import pt.rmartins.battleships.R;
import pt.rmartins.battleships.objects.PlayerClass.Shot;
import pt.rmartins.battleships.objects.PlayerClass.Shot.KindShot;
import pt.rmartins.battleships.objects.ShipClass;
import pt.rmartins.battleships.utilities.DataLoader;
import pt.rmartins.battleships.utilities.LanguageClass;

public class GameMode {

	public static class BonusPlay implements Comparable<BonusPlay>, Iterable<GameBonus> {

		public enum ConditionType {
			FirstBlood("First Blood", R.string.condition_type_firstblood_detail),

			Kill("Kill", R.string.condition_type_kill_detail),

			DoubleKill("Double Kill", R.string.condition_type_doublekill_detail),

			TripleKill("Triple Kill", R.string.condition_type_triplekill_detail),

			UltraKill("Ultra Kill", R.string.condition_type_ultrakill_detail),

			Rampage("Rampage", R.string.condition_type_rampage_detail),

			NoWaterInARow("No water [X] turns in a row", R.string.condition_type_nowaterinarow_detail),

			KillingSpree("Killing Spree", R.string.condition_type_killingspree_detail),

			Dominating("Dominating", R.string.condition_type_dominating_detail),

			MegaKill("Mega Kill", R.string.condition_type_megakill_detail),

			Unstoppable("Unstoppable", R.string.condition_type_unstoppable_detail),

			WickedSick("Wicked Sick", R.string.condition_type_wickedsick_detail),

			MonsterKill("Monster Kill", R.string.condition_type_monsterkill_detail),

			Godlike("Godlike", R.string.condition_type_godlike_detail),

			HolyShit("Holy Shit", R.string.condition_type_holyshit_detail),

			;

			public final String name;
			public final int detailCode;

			private ConditionType(String name, int detailCode) {
				this.name = name;
				this.detailCode = detailCode;
			}

			public String getDetail(Object... params) {
				return LanguageClass.getString(detailCode, params);
			}
		}

		public ConditionType condition;
		public final List<GameBonus> actions;
		public final int conditionParameter;

		public BonusPlay(ConditionType condition) {
			this(condition, 0, null);
		}

		public BonusPlay(ConditionType condition, int conditionParameter) {
			this(condition, conditionParameter, null);
		}

		public BonusPlay(ConditionType condition, int conditionParameter, List<GameBonus> actions) {
			this.condition = condition;
			this.conditionParameter = conditionParameter;
			if (actions == null) {
				this.actions = new LinkedList<GameBonus>();
			} else {
				this.actions = new LinkedList<GameBonus>(actions);
			}
		}

		public void addAction(GameBonus action) {
			actions.add(action);
		}

		@Override
		public Iterator<GameBonus> iterator() {
			return actions.iterator();
		}

		public int getParameter() {
			return conditionParameter;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof BonusPlay) {
				final BonusPlay o = (BonusPlay) obj;
				return this.condition == o.condition && this.conditionParameter == o.conditionParameter;
			}
			return false;
		}

		@Override
		public String toString() {
			return ConditionType.values()[condition.ordinal()].name.replace("[X]", "" + conditionParameter);
		}

		public String getConditionDetails() {
			return ConditionType.values()[condition.ordinal()].getDetail(conditionParameter);
		}

		@Override
		public int compareTo(BonusPlay o) {
			final int n = this.condition.ordinal() - o.condition.ordinal();
			return n != 0 ? n : this.conditionParameter - o.conditionParameter;
		}

	}

	public static class ShipExtraInfo {
		/**
		 * tiros que se têm, por cada barco, de um determinado tipo, que estiver vivo.
		 */
		private final List<KindShot> eachAliveShots;
		/**
		 * tiros que se têm, enquanto se tiver, pelo menos um dos barcos de um determinado tipo, vivo.
		 */
		private final List<KindShot> anyAliveShots;
		/**
		 * tiros que se ganham quando se destroi cada barco de um determinado tipo
		 */
		private final List<KindShot> eachDeadShots;
		/**
		 * tiros que se ganham quando se destroi todos os barcos que um determinado tipo
		 */
		private final List<KindShot> allDeadShots;
		private int explosiveSize;
		private final List<Double> shield;

		public ShipExtraInfo(List<KindShot> eachAliveShots, List<KindShot> anyAliveShots, List<KindShot> eachDeadShots,
				List<KindShot> allDeadShots, List<Double> shield) {
			this.eachAliveShots = new ArrayList<KindShot>(eachAliveShots);
			this.anyAliveShots = new ArrayList<KindShot>(anyAliveShots);
			this.eachDeadShots = new ArrayList<KindShot>(eachDeadShots);
			this.allDeadShots = new ArrayList<KindShot>(allDeadShots);
			this.explosiveSize = 0;
			this.shield = shield;
		}

		public List<KindShot> getPlusShots() {
			return eachAliveShots;
		}

		public List<KindShot> getAllPlusShots() {
			return anyAliveShots;
		}

		public List<KindShot> getDeadShots() {
			return eachDeadShots;
		}

		public List<KindShot> getAllDeadShots() {
			return allDeadShots;
		}

		public int getExplosiveSize() {
			return explosiveSize;
		}

		public List<Double> getShield() {
			return shield;
		}
	}

	public enum MessagesMode {
		NORMAL(R.string.messages_mode_normal), NDELAY(R.string.messages_mode_ndelay);

		private final int resourceCode;

		private MessagesMode(int resourceCode) {
			this.resourceCode = resourceCode;
		}

		public String getName() {
			return LanguageClass.getString(resourceCode);
		}
	}

	public enum TimeLimitType {
		// sem limite
		NoTimeLimit,
		// só limite total
		TotalTime,
		// limite total + tempo por turno
		TotalTimeAndPerTurn,
		// limite total + o tempo que se demora num turno (+ uma constante) é o tempo do adversário no turno seguinte.
		ExtraFastMode
	}

	private String name;
	private TimeLimitType timeLimitType;
	private int timeLimit;
	private int timePerTurn;
	private int timeExtraPerTurn;
	private List<List<KindShot>> totalShots;
	private MessagesMode messagesMode;
	private int messagesModeParameter;
	private Set<BonusPlay> possibleBonus;
	private Map<Integer, ShipExtraInfo> shipsExtraInfo;
	private double timeSavePercentage;
	private List<Turn> turns, readOnlyTurns;
	private float fullShield;
	private boolean fullGameMode;

	public GameMode() {
		final List<List<KindShot>> list = new ArrayList<List<KindShot>>(1);
		final List<KindShot> play = new ArrayList<KindShot>(3);
		play.add(KindShot.NormalShot);
		play.add(KindShot.NormalShot);
		play.add(KindShot.NormalShot);
		list.add(play);
		initialize("BattleShip Normal Mode", list, MessagesMode.NORMAL);
	}

	public GameMode(String name, List<List<KindShot>> totalShoots, MessagesMode messagesMode) {
		initialize(name, totalShoots, messagesMode);
	}

	public GameMode(GameMode other) {
		this.name = other.name;
		this.totalShots = new ArrayList<List<KindShot>>(other.totalShots);
		this.messagesMode = other.messagesMode;
		this.messagesModeParameter = other.messagesModeParameter;
		this.shipsExtraInfo = new TreeMap<Integer, ShipExtraInfo>(other.shipsExtraInfo);
		this.possibleBonus = new TreeSet<BonusPlay>(other.possibleBonus);
		this.turns = new ArrayList<Turn>(other.turns);
		this.readOnlyTurns = Collections.unmodifiableList(turns);
		this.timeSavePercentage = other.timeSavePercentage;
		this.timeLimitType = other.timeLimitType;
		this.timeLimit = other.timeLimit;
		this.timePerTurn = other.timePerTurn;
		this.timeExtraPerTurn = other.timeExtraPerTurn;
		this.fullShield = other.fullShield;
		this.fullGameMode = other.fullGameMode;
	}

	private void initialize(String name, List<List<KindShot>> totalShots, MessagesMode messagesMode) {
		this.name = name;
		this.totalShots = totalShots;
		this.messagesMode = messagesMode;
		this.messagesModeParameter = 0;
		this.shipsExtraInfo = new TreeMap<Integer, ShipExtraInfo>();
		this.possibleBonus = new TreeSet<BonusPlay>();
		this.turns = new ArrayList<Turn>();
		this.readOnlyTurns = Collections.unmodifiableList(turns);
		this.timeSavePercentage = 0.0;
		this.setTimeLimit(0);
		this.fullShield = 0f;
		this.fullGameMode = true;
	}

	public void addPossibleBonus(BonusPlay b) {
		if (possibleBonus.contains(b))
			possibleBonus.remove(b);
		possibleBonus.add(b);
	}

	public String getName() {
		return name;
	}

	public Set<BonusPlay> getPossibleBonus() {
		return possibleBonus;
	}

	public MessagesMode getMessagesMode() {
		return messagesMode;
	}

	public int getMessagesModeParameter() {
		return messagesModeParameter;
	}

	public TimeLimitType getTimeLimitType() {
		return timeLimitType;
	}

	/**
	 * @return Time limit in seconds
	 */
	public int getTimeLimit() {
		return timeLimit;
	}

	public int getTimePerTurn() {
		return timePerTurn;
	}

	public int getTimeExtraPerTurn() {
		return timeExtraPerTurn;
	}

	public List<List<KindShot>> getShots() {
		return totalShots;
	}

	public Map<Integer, ShipExtraInfo> getShipsExtraInfo() {
		return shipsExtraInfo;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setShowKind(MessagesMode showKind) {
		this.messagesMode = showKind;
	}

	public void setShowKindParameters(int value) {
		this.messagesModeParameter = value;
	}

	public void setTimeLimit(int timeLimit) {
		setTimeLimitPerTurnExtra(timeLimit, 0, -1);
	}

	public void setTimeLimitAndPerTurn(int timeLimit, int timePerTurn) {
		setTimeLimitPerTurnExtra(timeLimit, timePerTurn, -1);
	}

	public void setTimeLimitPerTurnExtra(int timeLimit, int timePerTurn, int timeExtraPerTurn) {
		this.timeLimit = timeLimit;
		this.timePerTurn = timePerTurn;
		this.timeExtraPerTurn = timeExtraPerTurn;
		if (this.timePerTurn == 0) {
			timeLimitType = this.timeLimit == 0 ? TimeLimitType.NoTimeLimit : TimeLimitType.TotalTime;
		} else if (this.timeExtraPerTurn < 0) {
			timeLimitType = TimeLimitType.TotalTimeAndPerTurn;
		} else {
			timeLimitType = TimeLimitType.ExtraFastMode;
		}
	}

	public void setTotalShoots(List<List<KindShot>> totalShots) {
		this.totalShots = totalShots;
	}

	public void addShipExtraInfo(int shipId, List<KindShot> plusShots, List<KindShot> allPlusShots,
			List<KindShot> deadShots, List<KindShot> allDeadShots, int explosive, List<Double> shield) {

		final ShipExtraInfo extraInfo = new ShipExtraInfo(plusShots, allPlusShots, deadShots, allDeadShots, shield);
		extraInfo.explosiveSize = explosive;
		shipsExtraInfo.put(shipId, extraInfo);
	}

	public void setTimeSave(double timeSavePercentage) {
		this.timeSavePercentage = timeSavePercentage;
	}

	public double getTimeSavePercentage() {
		return timeSavePercentage;
	}

	public void setPartialGameMode() {
		fullGameMode = false;
	}

	public void setFullShield(float value) {
		fullShield = value;
	}

	public float getFullShield() {
		return fullShield;
	}

	public boolean isFullGameMode() {
		return fullGameMode;
	}

	private static void toFFListOfListOfShots(List<List<Integer>> listOfLists, StringBuilder s) {
		for (List<Integer> list : listOfLists) {
			for (int i = 0; i < list.size(); i += 2) {
				s.append(KindShot.values()[list.get(i)].name() + " " + list.get(i + 1));
			}
			s.append(",");
		}
		s.replace(s.length() - 1, s.length(), ";");
	}

	public String toFileLanguage() {
		return toFileLanguage(this, false);
	}

	private static String toFileLanguage(GameMode mode, boolean turnMode) {
		StringBuilder s = new StringBuilder();

		if (!turnMode) {
			s.append("Version:" + DataLoader.VERSION + ";");
			s.append(mode.name + "{");
		}
		s.append("Hits:");
		toFFListOfListOfShots(Shot.calculateFFListOfLists(mode.totalShots), s);
		if (mode.messagesMode != MessagesMode.NORMAL) {
			if (mode.messagesModeParameter > 0)
				s.append("MessagesMode:" + mode.messagesMode.toString() + " " + mode.messagesModeParameter + ";");
			else
				s.append("MessagesMode:" + mode.messagesMode.toString() + ";");
		}
		if (mode.timeSavePercentage > 0.0)
			s.append("TimeSave:" + mode.timeSavePercentage + ";");
		s.append("TimeLimit:" + mode.timeLimit);
		if (mode.timePerTurn > 0)
			s.append("," + mode.timePerTurn);
		if (mode.timeExtraPerTurn >= 0)
			s.append("," + mode.timeExtraPerTurn);
		s.append(";");
		if (mode.fullShield > 0.0)
			s.append("FullShield:" + mode.fullShield + ";");

		{
			StringBuilder s2 = new StringBuilder();
			for (BonusPlay bonus : mode.possibleBonus) {
				if (bonus.conditionParameter == 0)
					s2.append(bonus.condition.name() + ":");
				else
					s2.append(bonus.condition.name() + " " + bonus.conditionParameter + ":");

				for (GameBonus gameBonus : bonus) {
					s2.append(gameBonus.toString() + ",");
				}
				s2.replace(s2.length() - 1, s2.length(), ";");
			}
			if (s2.length() > 0) {
				s.append("BonusPlay{");
				s.append(s2);
				s.append("}");
			}
		}

		{
			StringBuilder s2 = new StringBuilder();
			for (Entry<Integer, ShipExtraInfo> entry : mode.shipsExtraInfo.entrySet()) {
				s2.append(ShipClass.getName(entry.getKey()) + ":");
				ShipExtraInfo shipExtraInfo = entry.getValue();

				if (!shipExtraInfo.eachAliveShots.isEmpty())
					s2.append("EachAliveShots(" + Shot.calculateFFListString(shipExtraInfo.eachAliveShots) + "),");
				if (!shipExtraInfo.anyAliveShots.isEmpty())
					s2.append("AnyAliveShots(" + Shot.calculateFFListString(shipExtraInfo.anyAliveShots) + "),");
				if (!shipExtraInfo.eachDeadShots.isEmpty())
					s2.append("EachDeadShots(" + Shot.calculateFFListString(shipExtraInfo.eachDeadShots) + "),");
				if (!shipExtraInfo.allDeadShots.isEmpty())
					s2.append("AllDeadShots(" + Shot.calculateFFListString(shipExtraInfo.allDeadShots) + "),");

				if (shipExtraInfo.explosiveSize > 0)
					s2.append("Explosive(" + shipExtraInfo.explosiveSize + "),");

				if (!shipExtraInfo.shield.isEmpty()) {
					if (shipExtraInfo.shield.size() == 1 && shipExtraInfo.shield.get(0) < 0) {
						s2.append("FullShield(" + (-shipExtraInfo.shield.get(0)) + "),");
					} else {
						String t = shipExtraInfo.shield.toString();
						s2.append("Shield(" + t.substring(1, t.length() - 1) + "),");
					}
				}

				s2.replace(s2.length() - 1, s2.length(), ";");
			}
			if (s2.length() > 0) {
				s.append("Ships{");
				s.append(s2);
				s.append("}");
			}
		}

		for (Turn turn : mode.turns) {
			if (turn.gameMode.fullGameMode) {
				String s2 = toFileLanguage(turn.gameMode, true);
				if (s2.length() > 0) {
					s.append("FullTurn " + turn.begin + "-" + (turn.end == Integer.MAX_VALUE ? "*" : turn.end) + "{");
					s.append(s2);
					s.append("}");
				}
			} else { // TODO: Turn Not Implemented Yet - tem de ser ter os dois modos para comparar as diferenças
				String s2 = toFileLanguage(turn.gameMode, true);
				if (s2.length() > 0) {
					s.append("Turn " + turn.begin + "-" + (turn.end == Integer.MAX_VALUE ? "*" : turn.end) + "{");
					s.append(s2);
					s.append("}");
				}
			}
		}

		if (!turnMode)
			s.append("}");

		return s.toString();
	}

	public static class Turn {
		public final int begin, end;
		public final GameMode gameMode;

		public Turn(int begin, int end, GameMode gameMode) {
			super();
			this.begin = begin;
			this.end = end;
			this.gameMode = gameMode;
		}

		public List<Turn> interceptTurns(Turn newTurn) {
			List<Turn> result = new ArrayList<Turn>();

			if (newTurn.end < begin) { // No Intersection1
				result.add(newTurn);
				result.add(this);
			} else if (newTurn.begin > end) { // No Intersection2
				result.add(this);
				result.add(newTurn);
			} else { // Intersection
				if (newTurn.begin <= begin) { // 1*
					if (newTurn.end >= end) { // 1
						result.add(newTurn);
					} else { // 12
						result.add(newTurn);
						result.add(new Turn(newTurn.end + 1, end, gameMode));
					}
				} else if (newTurn.begin > begin) { // 2*
					if (newTurn.end >= end) { // 21
						result.add(new Turn(begin, newTurn.begin - 1, gameMode));
						result.add(newTurn);
					} else { // 212
						result.add(new Turn(begin, newTurn.begin - 1, gameMode));
						result.add(newTurn);
						result.add(new Turn(newTurn.end + 1, end, gameMode));
					}
				}
			}
			return result;
		}
	}

	public void addTurnMode(int begin, int end, GameMode turnMode) {
		Turn newTurn = new Turn(begin, end, turnMode);
		if (turns.isEmpty()) {
			turns.add(newTurn);
		} else {
			List<Turn> newTurnList = new ArrayList<Turn>();
			for (int i = 0; i < turns.size(); i++) {
				List<Turn> list = turns.get(i).interceptTurns(newTurn);

				if (list.get(list.size() - 1) == newTurn) {
					newTurnList.add(list.get(0));
				} else {
					newTurnList.addAll(list);
					newTurnList.addAll(turns.subList(i + 1, turns.size()));
					break;
				}
			}
			turns = newTurnList;
		}
	}

	public GameMode getActualGameMode(int turnNumber) {
		for (Turn turn : turns) {
			if (turnNumber >= turn.begin && turnNumber <= turn.end)
				return turn.gameMode;
		}
		return this;
	}

	public List<Turn> getTurnGameModes() {
		return readOnlyTurns;
	}

}
