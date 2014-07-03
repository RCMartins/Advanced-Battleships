package pt.rmartins.battleships.objects.userinterface;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import pt.rmartins.battleships.R;
import pt.rmartins.battleships.objects.Coordinate;
import pt.rmartins.battleships.objects.Fleet;
import pt.rmartins.battleships.objects.Game;
import pt.rmartins.battleships.objects.Game.GameState;
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

	private static class FleetShip {
		private final int id, sizeX, sizeY, rotation;

		public FleetShip(int id, int sizeX, int sizeY, int rotation) {
			this.id = id;
			this.sizeX = sizeX;
			this.sizeY = sizeY;
			this.rotation = rotation;
		}
	}

	private static final int GAMEINFO_FIELD_SIZE_CODE = R.string.gameinfo_field_size;

	private static final int CHOOSE_FLEET_CODE = R.string.choose_screen_choose_fleet;
	private static final int CHOOSE_GAME_MODE_CODE = R.string.choose_screen_choose_gamemode;
	private static final int CHOOSE_FIELD_SIZE_CODE = R.string.choose_screen_choose_fieldsize;

	private static Paint TEXT_PAINT, GAME_MODE_TEXT_PAINT, FIELD_PEN, SHIP_PAINT;

	private final Game game;
	private final float maxX;
	private final float maxY;
	private State state;

	private Fleet currentFleet;
	private int currentFleetIndex;
	private GameMode currentGameMode;
	private int currentGameModeIndex;

	private final List<List<FleetShip>> fleetShips;
	private final float[] squareSizeList;

	private MyButton FLEET_AREA, GAME_INFO_AREA, CHOOSE_FLEET_BUTTON, CHOOSE_GAMEMODE_BUTTON;

	private String CHOOSE_FLEET_TEXT, CHOOSE_GAME_MODE_TEXT, CHOOSE_FIELD_SIZE_TEXT;
	private float TEXT_HEIGHT;

	private final float SWIPE_X_THRESHOLD;

	private final float scrollX, scrollY;
	private float firstX, firstY, lastX, lastY;
	private boolean swipeTouchDown;

	//	private double lastTime;

	public ChooseScreen(int maxX, int maxY, Game game) {
		this.maxX = maxX;
		this.maxY = maxY;
		this.game = game;

		state = State.Fleet;

		currentFleetIndex = 0;
		currentFleet = GameClass.getAvaiableFleets().get(currentFleetIndex);
		currentGameModeIndex = 0;
		currentGameMode = GameClass.getGameModes().get(currentGameModeIndex);

		scrollX = 0f;
		scrollY = 0f;

		SWIPE_X_THRESHOLD = maxX / 3;

		final int numberOfFleets = GameClass.getAvaiableFleets().size();
		final int numberOfShips = ShipClass.numberOfShips();

		fleetShips = new ArrayList<List<FleetShip>>(numberOfFleets);
		squareSizeList = new float[numberOfFleets];
		for (int i = 0; i < numberOfFleets; i++) {
			fleetShips.add(new ArrayList<FleetShip>(numberOfShips));
			squareSizeList[i] = 0;
		}

		initialize();
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

	private void initialize() {
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

	private synchronized float calcFleetScreenSize(int fleetIndex) {
		if (squareSizeList[fleetIndex] == 0) {
			final Fleet fleet = GameClass.getAvaiableFleets().get(fleetIndex);
			squareSizeList[fleetIndex] = calcFleetScreenSizeAux(SQUARE_SIZE_INIT, fleet, fleetShips.get(fleetIndex));
		}
		return squareSizeList[fleetIndex];
	}

	private float calcFleetScreenSizeAux(float squareSize, Fleet calcFleet, List<FleetShip> calcFleetShips) {
		float x = FLEET_AREA.left;
		float y = FLEET_AREA.top;
		y += TEXT_HEIGHT;
		y += OUT_PADDING + squareSize * .5f;

		final float distX = 0.5f;
		final float distY = 0.5f;

		final List<Integer> fleet = calcFleet.getFleetNumbers();
		calcFleetShips.clear();

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
				calcFleetShips.add(ship);
			}
		}

		Collections.sort(calcFleetShips, new Comparator<FleetShip>() {
			@Override
			public int compare(FleetShip lhs, FleetShip rhs) {
				return lhs.sizeY - rhs.sizeY;
			}
		});

		for (int i = 0; i < calcFleetShips.size(); i++) {
			final FleetShip fleetShip = calcFleetShips.get(i);
			final int fleetN = fleet.get(fleetShip.id);

			if (i > 0) {
				if (calcFleetShips.get(i - 1).sizeY == fleetShip.sizeY) {
					x += squareSize * 1.0f;
				} else {
					x = FLEET_AREA.left;
					y += squareSize * (calcFleetShips.get(i - 1).sizeY + distY);
				}
			}

			for (int j = 0; j < fleetN; j++) {
				if (x + squareSize * fleetShip.sizeX > FLEET_AREA.right) {
					x = FLEET_AREA.left;
					y += squareSize * (fleetShip.sizeY + distY);
				}
				x += squareSize * (fleetShip.sizeX + distX);
			}
		}

		y += squareSize * calcFleetShips.get(calcFleetShips.size() - 1).sizeY;
		if (y > FLEET_AREA.bottom) {
			return calcFleetScreenSizeAux(squareSize * .9f, calcFleet, calcFleetShips);
		} else {
			return squareSize;
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
						drawState(canvas, currentFleet, currentFleetIndex, currentGameMode);
						canvas.translate(maxX, 0);
					} else if (index == -1) {
						canvas.translate(-diffX, 0);
						drawState(canvas, currentFleet, currentFleetIndex, currentGameMode);
						canvas.translate(-maxX, 0);
					}

					if (state == State.Fleet) {
						final List<Fleet> avaiableFleets = GameClass.getAvaiableFleets();
						int nextIndex = (avaiableFleets.size() + currentFleetIndex + index) % avaiableFleets.size();
						Fleet nextFleet = avaiableFleets.get(nextIndex);
						drawState(canvas, nextFleet, nextIndex, currentGameMode);
					} else if (state == State.GameMode) {
						final List<GameMode> gameModes = GameClass.getGameModes();
						int nextGameModeIndex = (gameModes.size() + currentGameModeIndex + index) % gameModes.size();
						GameMode nextGameMode = gameModes.get(nextGameModeIndex);
						drawState(canvas, currentFleet, currentFleetIndex, nextGameMode);
					}
					return;
				}
			}
		}
		drawState(canvas, currentFleet, currentFleetIndex, currentGameMode);
	}

	private void drawState(Canvas canvas, Fleet drawFleet, int drawFleetIndex, GameMode drawGameMode) {
		if (state == State.Fleet) {
			float squareSize = calcFleetScreenSize(drawFleetIndex);

			canvas.save();
			canvas.clipRect(FLEET_AREA);

			float x = FLEET_AREA.left;
			float y = FLEET_AREA.top;

			y += TEXT_HEIGHT;

			final int fieldMaxX = drawFleet.maxX;
			final int fieldMaxY = drawFleet.maxY;
			canvas.drawText(LanguageClass.getString(GAMEINFO_FIELD_SIZE_CODE, fieldMaxX, fieldMaxY),
					FLEET_AREA.centerX(), y, TEXT_PAINT);
			y += OUT_PADDING + squareSize * .5f;

			final float distX = 0.5f;
			final float distY = 0.5f;

			final List<Integer> fleet = drawFleet.getFleetNumbers();
			final List<FleetShip> drawFleetShips = fleetShips.get(drawFleetIndex);

			for (int i = 0; i < drawFleetShips.size(); i++) {
				final FleetShip fleetShip = drawFleetShips.get(i);
				final int fleetN = fleet.get(fleetShip.id);

				if (i > 0) {
					if (drawFleetShips.get(i - 1).sizeY == fleetShip.sizeY) {
						x += squareSize * 1.0f;
					} else {
						x = FLEET_AREA.left;
						y += squareSize * (drawFleetShips.get(i - 1).sizeY + distY);
					}
				}

				for (int j = 0; j < fleetN; j++) {
					if (x + squareSize * fleetShip.sizeX > FLEET_AREA.right) {
						x = FLEET_AREA.left;
						y += squareSize * (fleetShip.sizeY + distY);
					}
					drawShip(canvas, x, y, squareSize, ShipClass.getShipParts(fleetShip.id, fleetShip.rotation),
							SHIP_PAINT);
					x += squareSize * (fleetShip.sizeX + distX);
				}
			}

			canvas.restore();
			drawButton(canvas, CHOOSE_FLEET_TEXT, CHOOSE_FLEET_BUTTON, TEXT_PAINT);
		} else if (state == State.GameMode) {
			ScreenUtils.drawGameInfo(canvas, game, drawGameMode, GAME_INFO_AREA, GAME_MODE_TEXT_PAINT, TEXT_PAINT,
					false);
			drawButton(canvas, CHOOSE_GAME_MODE_TEXT, CHOOSE_GAMEMODE_BUTTON, TEXT_PAINT);
		} else if (state == State.FieldSize) {

		}
	}

	private void drawShip(Canvas canvas, float initX, float initY, float ss, List<Coordinate> listPieces,
			Paint shipPaint) {
		canvas.translate(initX, initY);
		for (Coordinate coor : listPieces) {
			final float x1 = coor.x * ss;
			final float y1 = coor.y * ss;
			final float x2 = (coor.x + 1) * ss;
			final float y2 = (coor.y + 1) * ss;
			canvas.drawRect(x1, y1, x2, y2, shipPaint);
			canvas.drawRect(x1, y1, x2, y2, FIELD_PEN);
		}
		canvas.translate(-initX, -initY);
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
							currentFleetIndex = (avaiableFleets.size() + currentFleetIndex + index)
									% avaiableFleets.size();
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
		// TODO Auto-generated method stub

	}

	@Override
	public boolean backPressed() {
		if (state == State.GameMode) {
			state = State.Fleet;
			return true;
		} else
			return super.backPressed();
	}
}
