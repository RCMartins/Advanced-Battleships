package pt.rmartins.battleships.objects.userinterface;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import pt.rmartins.battleships.R;
import pt.rmartins.battleships.objects.Coordinate2;
import pt.rmartins.battleships.objects.Fleet;
import pt.rmartins.battleships.objects.Game;
import pt.rmartins.battleships.objects.Game.GameState;
import pt.rmartins.battleships.objects.Game.PlayingMode;
import pt.rmartins.battleships.objects.GameClass;
import pt.rmartins.battleships.objects.ShipClass;
import pt.rmartins.battleships.objects.ShipClass.ShipData;
import pt.rmartins.battleships.objects.modes.GameMode;
import pt.rmartins.battleships.utilities.Draw;
import pt.rmartins.battleships.utilities.LanguageClass;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.MotionEvent;

public class ChooseScreen extends UserInterfaceClass {

	private static final String TAG = ChooseScreen.class.getSimpleName();

	private static float SQUARE_SIZE_INIT = 30f;
	private static float FIELD_PAINT_WIDTH = 1f;
	private static float BUTTON_TEXT_SIZE = 25f;
	private static float GAME_MODE_TEXT_SIZE = 20f;
	private static float SWIPE_Y_THRESHOLD = 100f;

	private static final int COLOR_SHIP = Color.GRAY;

	private enum State {
		Fleet, GameMode, FieldSize;
	}

	private static final int GAMEINFO_FIELD_SIZE_CODE = R.string.gameinfo_field_size;

	private static final int CHOOSE_FLEET_CODE = R.string.choose_screen_choose_fleet;
	private static final int CHOOSE_GAME_MODE_CODE = R.string.choose_screen_choose_gamemode;
	private static final int CHOOSE_FIELD_SIZE_CODE = R.string.choose_screen_choose_fieldsize;

	private static Paint TEXT_PAINT, GAME_MODE_TEXT_PAINT, FIELD_PEN, SHIP_PAINT;

	private MyButton CHOOSE_FLEET_BUTTON, CHOOSE_GAMEMODE_BUTTON;
	private RectF FLEET_AREA, GAME_INFO_AREA;

	private String CHOOSE_FLEET_TEXT, CHOOSE_GAME_MODE_TEXT, CHOOSE_FIELD_SIZE_TEXT;
	private float TEXT_HEIGHT;

	private final Game game;
	private final PlayingMode playingVersus;
	private int maxX, maxY;
	private State state;

	private Fleet currentFleet;
	private int currentFleetIndex;
	private GameMode currentGameMode;
	private int currentGameModeIndex;

	private final List<ShowFleetData> showFleet;

	private final float SWIPE_X_THRESHOLD;

	private float firstX, firstY, lastX, lastY;
	private boolean swipeTouchDown;

	public ChooseScreen(int maxX, int maxY, Game game, PlayingMode playingVersus) {
		this.game = game;
		this.playingVersus = playingVersus;

		state = State.Fleet;

		currentFleetIndex = 0;
		final List<Fleet> avaiableFleets = GameClass.getAvaiableFleets();
		currentFleet = avaiableFleets.get(currentFleetIndex);
		if (!currentFleet.isAIready()) {
			currentFleetIndex = nextFleetIndex(1);
			currentFleet = avaiableFleets.get(currentFleetIndex);
		}
		currentGameModeIndex = 0;
		currentGameMode = GameClass.getGameModes().get(currentGameModeIndex);

		SWIPE_X_THRESHOLD = maxX / 3;

		initializeGUI(maxX, maxY);

		final int numberOfFleets = avaiableFleets.size();
		showFleet = new ArrayList<ShowFleetData>(numberOfFleets);
		for (int i = 0; i < numberOfFleets; i++) {
			showFleet.add(new ShowFleetData(FLEET_AREA, TEXT_HEIGHT, avaiableFleets.get(i)));
		}
	}

	public static void initializeScreenMultiplier(float SCREEN_SUPPORT_MULTIPLIER) {
		SQUARE_SIZE_INIT *= SCREEN_SUPPORT_MULTIPLIER;
		FIELD_PAINT_WIDTH *= SCREEN_SUPPORT_MULTIPLIER;
		BUTTON_TEXT_SIZE *= SCREEN_SUPPORT_MULTIPLIER;
		GAME_MODE_TEXT_SIZE *= SCREEN_SUPPORT_MULTIPLIER;
		SWIPE_Y_THRESHOLD *= SCREEN_SUPPORT_MULTIPLIER;

		Typeface typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL);

		TEXT_PAINT = new Paint(Paint.ANTI_ALIAS_FLAG);
		TEXT_PAINT.setStyle(Style.FILL);
		TEXT_PAINT.setTextAlign(Paint.Align.CENTER);
		TEXT_PAINT.setColor(COLOR_TEXT);
		TEXT_PAINT.setTextSize(BUTTON_TEXT_SIZE);
		TEXT_PAINT.setTypeface(typeface);

		GAME_MODE_TEXT_PAINT = new Paint(TEXT_PAINT);
		GAME_MODE_TEXT_PAINT.setTextAlign(Paint.Align.LEFT);
		GAME_MODE_TEXT_PAINT.setTextSize(GAME_MODE_TEXT_SIZE);

		FIELD_PEN = new Paint();
		FIELD_PEN.setStyle(Style.STROKE);
		FIELD_PEN.setStrokeWidth(FIELD_PAINT_WIDTH);
		FIELD_PEN.setColor(Color.BLACK);

		SHIP_PAINT = new Paint();
		SHIP_PAINT.setStyle(Style.FILL);
		SHIP_PAINT.setColor(COLOR_SHIP);

	}

	@Override
	public synchronized void initializeGUI(int maxX, int maxY) {
		this.maxX = maxX;
		this.maxY = maxY;
		CHOOSE_FLEET_TEXT = LanguageClass.getString(CHOOSE_FLEET_CODE);
		CHOOSE_GAME_MODE_TEXT = LanguageClass.getString(CHOOSE_GAME_MODE_CODE);

		//		GAMEINFO_FIELD_SIZE_TEXT = LanguageClass.getString(GAMEINFO_FIELD_SIZE_CODE);

		TEXT_HEIGHT = Draw.getStrHeight(TEXT_PAINT);

		CHOOSE_FLEET_BUTTON = UserInterfaceClass.CreateNewButton(CHOOSE_FLEET_TEXT, TEXT_PAINT, maxX / 2, maxY
				- OUT_PADDING, ButtonAlignType.Bottom);

		CHOOSE_GAMEMODE_BUTTON = UserInterfaceClass.CreateNewButton(CHOOSE_GAME_MODE_TEXT, TEXT_PAINT, maxX / 2, maxY
				- OUT_PADDING, ButtonAlignType.Bottom);
		{
			FLEET_AREA = new MyButton(OUT_PADDING, OUT_PADDING, maxX - OUT_PADDING, CHOOSE_FLEET_BUTTON.top
					- OUT_PADDING);
		}
		{
			GAME_INFO_AREA = new MyButton(OUT_PADDING, OUT_PADDING, maxX - OUT_PADDING, CHOOSE_FLEET_BUTTON.top
					- OUT_PADDING);
		}
	}

	@Override
	public synchronized void draw(Canvas canvas) {
		if (swipeTouchDown) {
			if (Math.abs(firstY - lastY) <= SWIPE_Y_THRESHOLD) {
				int index = 0;
				final float diffX = firstX - lastX;
				if (diffX > 0) {
					index = 1;
				} else if (diffX < 0) {
					index = -1;
				}
				if (index != 0) {
					if (index == 1) {
						canvas.translate(-diffX, 0);
						drawState(canvas, currentFleetIndex, currentGameMode);
						canvas.translate(maxX, 0);
					} else if (index == -1) {
						canvas.translate(-diffX, 0);
						drawState(canvas, currentFleetIndex, currentGameMode);
						canvas.translate(-maxX, 0);
					}

					if (state == State.Fleet) {
						int nextIndex = nextFleetIndex(index);
						drawState(canvas, nextIndex, currentGameMode);
					} else if (state == State.GameMode) {
						final List<GameMode> gameModes = GameClass.getGameModes();
						int nextGameModeIndex = (gameModes.size() + currentGameModeIndex + index) % gameModes.size();
						GameMode nextGameMode = gameModes.get(nextGameModeIndex);
						drawState(canvas, currentFleetIndex, nextGameMode);
					}
					return;
				}
			}
		}
		drawState(canvas, currentFleetIndex, currentGameMode);
	}

	private void drawState(Canvas canvas, int drawFleetIndex, GameMode drawGameMode) {
		if (state == State.Fleet) {
			showFleet.get(drawFleetIndex).draw(canvas);
			drawButton(canvas, CHOOSE_FLEET_TEXT, CHOOSE_FLEET_BUTTON, TEXT_PAINT);
		} else if (state == State.GameMode) {
			ScreenUtils.drawGameInfo(canvas, game, drawGameMode, GAME_INFO_AREA, GAME_MODE_TEXT_PAINT, TEXT_PAINT,
					false);
			drawButton(canvas, CHOOSE_GAME_MODE_TEXT, CHOOSE_GAMEMODE_BUTTON, TEXT_PAINT);
		} else if (state == State.FieldSize) {

		}
	}

	private int nextFleetIndex(int index) {
		final List<Fleet> avaiableFleets = GameClass.getAvaiableFleets();
		int count = 0;
		int fleetIndex = currentFleetIndex;
		do {
			fleetIndex = (avaiableFleets.size() + fleetIndex + index) % avaiableFleets.size();
			count++;
			if (count == avaiableFleets.size())
				break;
		} while (!avaiableFleets.get(fleetIndex).isAIready());
		return fleetIndex;
	}

	@Override
	public synchronized void onTouchEvent(MotionEvent event) {
		final float x = event.getX();
		final float y = event.getY();
		final int action = event.getAction();

		if (state == State.Fleet && buttonDownAndUp(action, x, y, CHOOSE_FLEET_BUTTON)) {
			game.setFleet(currentFleet);
			state = State.GameMode;
		} else if (state == State.GameMode && buttonDownAndUp(action, x, y, CHOOSE_GAMEMODE_BUTTON)) {
			game.setGameMode(currentGameMode);
			game.setGameState(GameState.FinishedChoosingMode);
		} else if (action == MotionEvent.ACTION_DOWN) {
			firstX = lastX = x;
			firstY = lastY = y;
			if (!(state == State.Fleet && CHOOSE_FLEET_BUTTON.contains(x, y) || state == State.GameMode
					&& CHOOSE_GAMEMODE_BUTTON.contains(x, y)))
				swipeTouchDown = true;
		} else if (action == MotionEvent.ACTION_MOVE) {
			lastX = x;
			lastY = y;
		} else if (action == MotionEvent.ACTION_UP) {
			if (swipeTouchDown) {
				if (Math.abs(firstY - lastY) <= SWIPE_Y_THRESHOLD) {
					int index = 0;
					if (firstX - lastX >= SWIPE_X_THRESHOLD) {
						index = 1;
					} else if (firstX - lastX <= -SWIPE_X_THRESHOLD) {
						index = -1;
					}
					if (index != 0) {
						if (state == State.Fleet) {
							final List<Fleet> avaiableFleets = GameClass.getAvaiableFleets();
							currentFleetIndex = nextFleetIndex(index);
							currentFleet = avaiableFleets.get(currentFleetIndex);
						} else if (state == State.GameMode) {
							final List<GameMode> gameModes = GameClass.getGameModes();
							currentGameModeIndex = (gameModes.size() + currentGameModeIndex + index) % gameModes.size();
							currentGameMode = gameModes.get(currentGameModeIndex);
						}
					}
				}
			}

			swipeTouchDown = false;
		}
	}

	@Override
	public synchronized void update(double timeElapsed) {
	}

	@Override
	public boolean backPressed() {
		if (state == State.GameMode) {
			state = State.Fleet;
			return true;
		} else
			return super.backPressed();
	}

	public static class ShowFleetData {

		private static class FleetShip {
			private final int id, sizeX, sizeY, rotation;

			public FleetShip(int id, int sizeX, int sizeY, int rotation) {
				this.id = id;
				this.sizeX = sizeX;
				this.sizeY = sizeY;
				this.rotation = rotation;
			}
		}

		private final RectF fleetArea;
		private final float textHeight;
		private final Fleet drawFleet;
		private final List<FleetShip> drawFleetShips;
		private boolean initialized;
		private float squareSize;

		public ShowFleetData(RectF fleetArea, float textHeight, Fleet drawFleet) {
			this.fleetArea = fleetArea;
			this.textHeight = textHeight;
			this.drawFleet = drawFleet;

			final int numberOfShips = ShipClass.numberOfShips();
			drawFleetShips = new ArrayList<FleetShip>(numberOfShips);
			initialized = false;
			squareSize = 0;
		}

		private float calcFleetScreenSize(float squareSize) {
			float x = fleetArea.left;
			float y = fleetArea.top;
			y += textHeight;
			y += OUT_PADDING + squareSize * .5f;

			final float distX = 0.5f;
			final float distY = 0.5f;

			final List<Integer> fleet = drawFleet.getFleetNumbers();
			drawFleetShips.clear();

			for (int id = 0; id < fleet.size(); id++) {
				final int fleetN = fleet.get(id);
				if (fleetN > 0) {
					int sX = -1;
					int r = -1;
					int minY = Integer.MAX_VALUE;
					final List<ShipData> allRotations = ShipClass.getAllRotations(id);
					for (int rotation = 0; rotation < allRotations.size(); rotation++) {
						ShipData shipData = allRotations.get(rotation);
						if (shipData.sizeY < minY) {
							minY = shipData.sizeY;
							sX = shipData.sizeX;
							r = rotation;
						}
					}
					FleetShip ship = new FleetShip(id, sX, minY, r);
					drawFleetShips.add(ship);
				}
			}

			Collections.sort(drawFleetShips, new Comparator<FleetShip>() {
				@Override
				public int compare(FleetShip lhs, FleetShip rhs) {
					return lhs.sizeY - rhs.sizeY;
				}
			});

			for (int i = 0; i < drawFleetShips.size(); i++) {
				final FleetShip fleetShip = drawFleetShips.get(i);
				final int fleetN = fleet.get(fleetShip.id);

				if (i > 0) {
					if (drawFleetShips.get(i - 1).sizeY == fleetShip.sizeY) {
						x += squareSize * 1.0f;
					} else {
						x = fleetArea.left;
						y += squareSize * (drawFleetShips.get(i - 1).sizeY + distY);
					}
				}

				for (int j = 0; j < fleetN; j++) {
					if (x + squareSize * fleetShip.sizeX > fleetArea.right) {
						x = fleetArea.left;
						y += squareSize * (fleetShip.sizeY + distY);
					}
					x += squareSize * (fleetShip.sizeX + distX);
				}
			}

			y += squareSize * drawFleetShips.get(drawFleetShips.size() - 1).sizeY;
			if (y > fleetArea.bottom) {
				return calcFleetScreenSize(squareSize * .9f);
			} else {
				return squareSize;
			}
		}

		public void draw(Canvas canvas) {
			if (!initialized) {
				squareSize = calcFleetScreenSize(SQUARE_SIZE_INIT);
				initialized = true;
			}

			float x = fleetArea.left;
			float y = fleetArea.top;

			y += textHeight;

			final int fieldMaxX = drawFleet.maxX;
			final int fieldMaxY = drawFleet.maxY;
			canvas.drawText(LanguageClass.getString(GAMEINFO_FIELD_SIZE_CODE, fieldMaxX, fieldMaxY),
					fleetArea.centerX(), y, TEXT_PAINT);
			y += OUT_PADDING + squareSize * .5f;

			final float distX = 0.5f;
			final float distY = 0.5f;

			final List<Integer> fleet = drawFleet.getFleetNumbers();

			for (int i = 0; i < drawFleetShips.size(); i++) {
				final FleetShip fleetShip = drawFleetShips.get(i);
				final int fleetN = fleet.get(fleetShip.id);

				if (i > 0) {
					if (drawFleetShips.get(i - 1).sizeY == fleetShip.sizeY) {
						x += squareSize * 1.0f;
					} else {
						x = fleetArea.left;
						y += squareSize * (drawFleetShips.get(i - 1).sizeY + distY);
					}
				}

				for (int j = 0; j < fleetN; j++) {
					if (x + squareSize * fleetShip.sizeX > fleetArea.right) {
						x = fleetArea.left;
						y += squareSize * (fleetShip.sizeY + distY);
					}
					drawShip(canvas, x, y, squareSize, ShipClass.getShipParts(fleetShip.id, fleetShip.rotation),
							SHIP_PAINT);
					x += squareSize * (fleetShip.sizeX + distX);
				}
			}
		}

		private void drawShip(Canvas canvas, float initX, float initY, float ss, List<Coordinate2> listPieces,
				Paint shipPaint) {
			canvas.translate(initX, initY);
			for (Coordinate2 coor : listPieces) {
				final float x1 = coor.x * ss;
				final float y1 = coor.y * ss;
				final float x2 = (coor.x + 1) * ss;
				final float y2 = (coor.y + 1) * ss;
				canvas.drawRect(x1, y1, x2, y2, shipPaint);
				canvas.drawRect(x1, y1, x2, y2, FIELD_PEN);
			}
			canvas.translate(-initX, -initY);
		}

	}
}
