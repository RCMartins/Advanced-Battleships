package pt.rmartins.battleships.utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.StringTokenizer;

import pt.rmartins.battleships.R;
import pt.rmartins.battleships.objects.Coordinate;
import pt.rmartins.battleships.objects.Fleet;
import pt.rmartins.battleships.objects.Game;
import pt.rmartins.battleships.objects.GameClass;
import pt.rmartins.battleships.objects.Message;
import pt.rmartins.battleships.objects.Player;
import pt.rmartins.battleships.objects.Ship;
import pt.rmartins.battleships.objects.ShipClass;
import pt.rmartins.battleships.objects.modes.GameMode;
import pt.rmartins.battleships.parser.gamemodes.ParserGameModes;
import pt.rmartins.battleships.parser.ships.ParserShips;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;

public final class DataLoader {

	@SuppressWarnings("unused")
	private static final String TAG = DataLoader.class.getSimpleName();

	public static final int VERSION = 1;

	private static Activity activity;

	public static void initializeDataLoader(Activity activity) {
		DataLoader.activity = activity;
	}

	private static StringTokenizer tokenize(String s) {
		return new StringTokenizer(s);
	}

	public static void loadGameFleets(List<Fleet> avaiableFleets) {
		final List<String> input = DataLoader.getClearText(R.raw.data__fleettypes);
		if (input != null) {
			for (final String s : input) {
				final String[] tokens = s.split(";");
				// String name = tokens[0].trim();
				final String nShips = tokens[1].trim();
				final Scanner inNShips = new Scanner(nShips);

				final int maxX = inNShips.nextInt();
				final int maxY = inNShips.nextInt();

				final List<Integer> fleet = new ArrayList<Integer>();

				int n = -1;
				final int numberOfShips = ShipClass.numberOfShips();
				while (inNShips.hasNext() && ++n < numberOfShips) {
					fleet.add(inNShips.nextInt());
				}

				for (int i = fleet.size(); i < numberOfShips; i++) {
					fleet.add(0);
				}

				avaiableFleets.add(new Fleet(maxX, maxY, fleet));
			}
		}
	}

	/**
	 * Interpreter of the Battle Ships game.
	 * 
	 * @param gameModes
	 */
	public static void loadGameModes(List<GameMode> gameModes) {
		final List<String> input = DataLoader.getClearText(R.raw.data__gamemodes_new);
		if (input == null || input.isEmpty())
			return;
		gameModes.addAll(ParserGameModes.parseGameModes(listToString(input)));
	}

	//	private static int loadGameMode(GameMode mode, List<AdvancedToken> tokenList, int index) throws Exception {
	//			} else if (token.value.equals("ships")) {
	//				index += 2;
	//				while (index < tokenList.size()) {
	//					if (tokenList.get(index).value.equals("}")) {
	//						index++;
	//						break;
	//					} else if (tokenList.get(index + 1).value.equals(":")) {
	//						if (tokenList.get(index).value.equals("all")) {
	//							index += 2;
	//							index = addAllExtraInfoToShip(-1, mode, tokenList, index);
	//						} else {
	//							int shipId = ShipClass.getId(tokenList.get(index).value);
	//							index += 2;
	//							index = addAllExtraInfoToShip(shipId, mode, tokenList, index);
	//						}
	//					} else {
	//						throw new Exception("Token not implemented (ships zone): " + token.value);
	//					}
	//				}
	//			} else if (token.value.equals("fullturn") || token.value.equals("turn")) {
	//				index++;
	//				int init = tokenList.get(index).value.equals("*") ? 1 : Math.max(1,
	//						Integer.parseInt(tokenList.get(index).value));
	//				index += 2;
	//				int end = tokenList.get(index).value.equals("*") ? Integer.MAX_VALUE : Integer.parseInt(tokenList
	//						.get(index).value);
	//				index += 2;
	//
	//				if (token.value.equals("fullturn")) {
	//					GameMode turnMode = new GameMode();
	//					mode.addTurnMode(init, end, turnMode);
	//					index = loadGameMode(turnMode, tokenList, index);
	//				} else {
	//					GameMode turnMode = new GameMode(mode);
	//					turnMode.setPartialGameMode();
	//					mode.addTurnMode(init, end, turnMode);
	//					index = loadGameMode(turnMode, tokenList, index);
	//					// while (index < tokenList.size()) {
	//					// if (tokenList.get(index).value.equals("}")) {
	//					// index++;
	//					// break;
	//					// } else if (tokenList.get(index + 1).value.equals(":")) {
	//					// if (tokenList.get(index).value.equals("all")) {
	//					// index += 2;
	//					// index = addAllExtraInfoToShip(-1, turnMode, tokenList, index);
	//					// } else {
	//					// int shipId = ShipClass.getId(tokenList.get(index).value);
	//					// index += 2;
	//					// index = addAllExtraInfoToShip(shipId, turnMode, tokenList, index);
	//					// }
	//					// } else {
	//					// throw new Exception("Token not implemented (Turns zone): " + token.value);
	//					// }
	//					// }

	// private static int addSpecialConditions(GameMode mode, String specialConditionName, List<AdvancedToken> tokens,
	// int index) {
	// while (index < tokens.size()) {
	// if (tokens.get(index).value.matches("wingame")) {
	// mode.addSpecialCondition(specialConditionName, "wingame");
	// index++;
	// }
	// if (tokens.get(index).value.equals(";")) {
	// break;
	// } else {
	// index++;
	// }
	// }
	// return index + 1;
	// }

	public static void loadShips() {
		final List<String> input = DataLoader.getClearText(R.raw.data__shipsdata);
		if (input == null || input.isEmpty())
			return;
		ParserShips.parseShips(listToString(input));

		//		List<Locale> langs;
		//		{
		//			final StringTokenizer tokenizer = tokenize(input.get(0));
		//			langs = new ArrayList<Locale>();
		//			while (tokenizer.hasMoreElements()) {
		//				String s = tokenizer.nextToken();
		//				langs.add(new Locale(s.trim()));
		//				s = tokenizer.nextToken();
		//				if (s.equals(";"))
		//					break;
		//				else if (s.equals(","))
		//					continue;
		//				else
		//					Log.e(TAG, "LoadShips format error!");
		//			}
		//		}
		//
		//		for (int index = 1; index < input.size(); index++) {
		//			final String s = input.get(index);
		//			final String[] tokens = s.split(";");
		//			if (tokens.length < langs.size() + 1)
		//				continue;
		//			final String coor = tokens[tokens.length - 1].trim().replace(",", " ");
		//			final Scanner inCoor = new Scanner(coor);
		//
		//			final List<Coordinate> parts = new ArrayList<Coordinate>();
		//			try {
		//				while (inCoor.hasNext()) {
		//					parts.add(new Coordinate(inCoor.nextInt(), inCoor.nextInt()));
		//				}
		//			} catch (final Exception e) {
		//				continue;
		//			}
		//
		//			Map<Locale, String> names = new TreeMap<Locale, String>();
		//			for (int i = 0; i < tokens.length - 1; i++) {
		//				names.put(langs.get(i), tokens[i].trim());
		//			}
		//
		//			if (parts.size() > 0) {
		//				ShipClass.createNewShip(names, parts);
		//			}
		//		}
	}

	public static void loadDefaultSettings(Context context, Resources res) {
		Locale locale = Locale.ENGLISH;
		boolean soundIsOn = true;
		String nickname = "Player";

		final List<String> input = DataLoader.getClearText(R.raw.data__default_settings);
		if (input != null) {
			for (String line : input) {
				String[] tokens = line.split(":");
				if (tokens.length == 2) {
					String settingName = tokens[0].trim();
					String setting = tokens[1].trim();

					if (settingName.equalsIgnoreCase("Language")) {
						locale = LanguageClass.getLocale(setting);
					} else if (settingName.equalsIgnoreCase("Sound") && setting.matches("on|off")) {
						soundIsOn = setting.equals("on");
					} else if (settingName.equalsIgnoreCase("Nickname")) {
						nickname = setting;
					}
				}
			}
		}

		LanguageClass.initialize(context, locale, res);
		LanguageClass.setLanguage(locale);
		GameClass.setSoundIsOn(soundIsOn);
		GameClass.setMultiplayerNickname(nickname);
	}

	private static boolean inCommentBlock;

	private static String clearComents(String s) {
		if (s.contains("//")) {
			s = s.substring(0, s.indexOf("//"));
		}
		if (s.contains("/*") && !inCommentBlock) {
			inCommentBlock = true;
			final int index = s.indexOf("/*");
			String result = s.substring(0, index);
			if (s.length() >= index + 3) {
				s = s.substring(index + 2);
				result += DataLoader.clearComents(s);
			}
			s = result;
		} else if (s.contains("*/")) {
			inCommentBlock = false;
			final int index = s.indexOf("*/");
			if (s.length() >= index + 3) {
				s = s.substring(index + 2);
				s = DataLoader.clearComents(s);
			} else {
				s = "";
			}
		} else if (inCommentBlock) {
			s = "";
		}

		return s;
	}

	private static List<String> getClearText(int resourceID) {
		final List<String> list = new ArrayList<String>();
		try {
			final InputStream input = activity.getResources().openRawResource(resourceID);
			final BufferedReader bi = new BufferedReader(new InputStreamReader(input));

			String s;
			inCommentBlock = false;
			while ((s = bi.readLine()) != null) {
				s = DataLoader.clearComents(s).replace("\\s", " ").replace("  ", " ").trim();
				if (!s.isEmpty())
					list.add(s);
			}
		} catch (final IOException e) {
			return null;
		}
		return list;
	}

	private static String listToString(List<String> list) {
		final StringBuilder text = new StringBuilder();
		for (final String string : list) {
			text.append(string);
		}
		return text.toString();
	}

	public static void saveGame(Game game, String file) {
		StringBuilder s = new StringBuilder();

		Fleet fleet = game.getCurrentFleet();
		s.append("fleet:" + fleet.getFleetNumbers().toString().replaceAll("[\\[|\\]|,]", "") + "\n");
		s.append("gamemode:" + game.getCurrentGameMode().toFileLanguage() + "\n");
		s.append("gametime:" + game.getGameTime() + "\n");

		Player player1 = game.getPlayer1();
		s.append("Player1:\n");
		addPlayerData(s, game, player1);

		Player player2 = game.getPlayer2();
		s.append("Player2:\n");
		addPlayerData(s, game, player2);

		System.out.println(s);
	}

	private static void addPlayerData(StringBuilder s, Game game, Player player) {
		s.append("shipsposition:");
		for (final Ship ship : player.getShips()) {
			final byte[] shipData = ShipClass.getShipBytes(ship);
			for (int i = 0; i < shipData.length; i++) {
				s.append(" " + shipData[i]);
			}
		}
		s.append("\n");
		s.append("shotsequence:\n");
		final List<Message> messages = player.getMessagesLock();
		for (Message m : messages) {
			for (Coordinate coor : m.getCoors()) {
				s.append(coor.toStringOnlyNumbers() + " ");
			}
			s.append("-1 ");
			for (Coordinate coor : m.getCounter()) {
				s.append(coor.toStringOnlyNumbers());
			}
			s.append("\n");
		}
		player.getMessagesUnlock();

		s.append("marks:\n");
		for (int y = 0; y < game.getMaxY(); y++) {
			for (int x = 0; x < game.getMaxX(); x++) {
				s.append(player.markAt(x, y).ordinal() + " ");
			}
			s.append("\n");
		}

		s.append("time:");
		s.append(player.getWatchTime());
	}

	public static void loadGame(Game game, String file) {

	}
}
