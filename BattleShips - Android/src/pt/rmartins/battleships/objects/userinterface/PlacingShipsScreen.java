package pt.rmartins.battleships.objects.userinterface;

import java.util.List;

import pt.rmartins.battleships.R;
import pt.rmartins.battleships.objects.Coordinate2;
import pt.rmartins.battleships.objects.Game;
import pt.rmartins.battleships.objects.Player;
import pt.rmartins.battleships.objects.Ship;
import pt.rmartins.battleships.objects.ai.ComputerAI;
import pt.rmartins.battleships.utilities.Draw;
import pt.rmartins.battleships.utilities.LanguageClass;
import pt.rmartins.battleships.utilities.Utils;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.MotionEvent;

public class PlacingShipsScreen extends UserInterfaceClass {

	private static final int COLOR_TEXT = Color.BLACK;

	private static final int COLOR_WATER = Color.CYAN;
	private static final int COLOR_WATER_NEAR_SHIP = Color.BLUE;
	private static final int COLOR_SHIP = Color.GRAY;
	private static final int COLOR_SHIP_GOOD = Color.GREEN;
	private static final int COLOR_SHIP_BAD = Color.RED;

	private static float TEXT_SIZE = 25f;

	private static final int UNDO_CODE = R.string.placing_ships_undo;
	private static final int CLEAR_CODE = R.string.placing_ships_clear;
	private static final int ROTATE_CODE = R.string.placing_ships_rotate;
	private static final int NEXT_CODE = R.string.placing_ships_next;
	private static final int READY_CODE = R.string.placing_ships_ready;
	private static final int WAITING_CODE = R.string.placing_ships_waiting;
	private static final int ENEMY_READY_CODE = R.string.placing_ships_enemy_ready;

	private static final int WAITING_TEXT_MAX_DOTS = 3;

	public enum State {
		WatchScreen, PlacingScreen, ReadyScreen, WaitingScreen;
	}

	private static Paint CENTER_TEXT_PAINT, LEFT_TEXT_PAINT, RIGHT_TEXT_PAINT;
	private static Paint FIELD_PAINT, WATER_PAINT, WATER_NEAR_SHIP_PAINT, SHIP_PAINT, SHIP_GOOD_PAINT, SHIP_BAD_PAINT;

	private RectF FIELD_AREA;
	private MyButton UNDO_BUTTON_AREA, CLEAR_BUTTON_AREA, ROTATE_BUTTON_AREA, NEXT_BUTTON_AREA, READY_BUTTON_AREA;

	private String UNDO_TEXT, CLEAR_TEXT, ROTATE_TEXT, NEXT_TEXT, READY_TEXT, WAITING_TEXT, ENEMY_READY_TEXT;

	private final Game game;
	private int maxX, maxY;
	private boolean portraitMode;
	private final Player playerGUI;

	private State state;
	private boolean movingShip;
	private float lastX, lastY;

	private float fieldInitX, fieldInitY;
	private int fieldSquareSize;

	private static final double PULSE_TICK_TIME = .4;
	private double pulseTime;
	private int pulse;
	private float TEXT_HEIGHT;

	public PlacingShipsScreen(int maxX, int maxY, Game game) {
		this.game = game;

		state = State.PlacingScreen;// State.WatchScreen;
		movingShip = false;
		if (!ComputerAI.DEBUG_AI_AUTO_PLACE)
			game.getPlayer1().setPositionToRandomLocation(false);

		pulseTime = 0f;
		pulse = 0;

		playerGUI = game.getPlayer1();

		initializeGUI(maxX, maxY);
	}

	public static void initializeScreenMultiplier(float SCREEN_SUPPORT_MULTIPLIER) {
		TEXT_SIZE *= SCREEN_SUPPORT_MULTIPLIER;

		Typeface typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL);

		CENTER_TEXT_PAINT = new Paint(Paint.ANTI_ALIAS_FLAG);
		CENTER_TEXT_PAINT.setStyle(Style.FILL);
		CENTER_TEXT_PAINT.setTextAlign(Paint.Align.CENTER);
		CENTER_TEXT_PAINT.setColor(COLOR_TEXT);
		CENTER_TEXT_PAINT.setTextSize(TEXT_SIZE);
		CENTER_TEXT_PAINT.setTypeface(typeface);

		LEFT_TEXT_PAINT = new Paint(CENTER_TEXT_PAINT);
		LEFT_TEXT_PAINT.setTextAlign(Paint.Align.LEFT);

		RIGHT_TEXT_PAINT = new Paint(CENTER_TEXT_PAINT);
		RIGHT_TEXT_PAINT.setTextAlign(Paint.Align.RIGHT);

		FIELD_PAINT = new Paint();
		FIELD_PAINT.setStyle(Style.STROKE);
		FIELD_PAINT.setStrokeWidth(1);
		FIELD_PAINT.setColor(Color.BLACK);

		WATER_PAINT = new Paint();
		WATER_PAINT.setStyle(Style.FILL);
		WATER_PAINT.setColor(COLOR_WATER);

		WATER_NEAR_SHIP_PAINT = new Paint();
		WATER_NEAR_SHIP_PAINT.setStyle(Style.FILL);
		WATER_NEAR_SHIP_PAINT.setColor(COLOR_WATER_NEAR_SHIP);

		SHIP_PAINT = new Paint();
		SHIP_PAINT.setStyle(Style.FILL);
		SHIP_PAINT.setColor(COLOR_SHIP);

		SHIP_GOOD_PAINT = new Paint(SHIP_PAINT);
		SHIP_GOOD_PAINT.setColor(COLOR_SHIP_GOOD);

		SHIP_BAD_PAINT = new Paint(SHIP_PAINT);
		SHIP_BAD_PAINT.setColor(COLOR_SHIP_BAD);
	}

	@Override
	public synchronized void initializeGUI(int maxX, int maxY) {
		this.maxX = maxX;
		this.maxY = maxY;
		this.portraitMode = maxY > maxX;
		UNDO_TEXT = LanguageClass.getString(UNDO_CODE);
		CLEAR_TEXT = LanguageClass.getString(CLEAR_CODE);
		ROTATE_TEXT = LanguageClass.getString(ROTATE_CODE);
		NEXT_TEXT = LanguageClass.getString(NEXT_CODE);
		READY_TEXT = LanguageClass.getString(READY_CODE);
		WAITING_TEXT = LanguageClass.getString(WAITING_CODE);
		ENEMY_READY_TEXT = LanguageClass.getString(ENEMY_READY_CODE);

		TEXT_HEIGHT = Draw.getStrHeight(CENTER_TEXT_PAINT, UNDO_TEXT);

		final int fieldX = game.getMaxX();
		final int fieldY = game.getMaxY();

		if (portraitMode) {
			{
				final float padding = OUT_PADDING + IN_PADDING * 2;

				UNDO_BUTTON_AREA = new MyButton(OUT_PADDING, OUT_PADDING, maxX / 3, TEXT_HEIGHT + padding);
				CLEAR_BUTTON_AREA = new MyButton(maxX / 2, OUT_PADDING, maxX - OUT_PADDING, TEXT_HEIGHT + padding);
			}
			{
				fieldSquareSize = (int) Math.floor(Math.min((maxX - OUT_PADDING) / fieldX, maxY * 2 / 3 / fieldY));
				fieldInitX = maxX / 2 - fieldX * fieldSquareSize / 2f;
				fieldInitY = UNDO_BUTTON_AREA.bottom + OUT_PADDING;

				FIELD_AREA = new RectF(fieldInitX, fieldInitY, fieldInitX + fieldSquareSize * fieldX, fieldInitY
						+ fieldSquareSize * fieldY);
			}
			{
				final float y = (maxY - FIELD_AREA.bottom) / 2 - TEXT_HEIGHT / 2 + FIELD_AREA.bottom;
				final float bPadding = OUT_PADDING + IN_PADDING;

				ROTATE_BUTTON_AREA = new MyButton(OUT_PADDING, y - bPadding, maxX / 3, y + TEXT_HEIGHT + bPadding);
				NEXT_BUTTON_AREA = new MyButton(maxX * 2 / 3, y - bPadding, maxX - OUT_PADDING, y + TEXT_HEIGHT
						+ bPadding);
				READY_BUTTON_AREA = new MyButton(maxX / 4, y - bPadding, maxX / 4 * 3, y + TEXT_HEIGHT + bPadding);
			}
		} else {
			final int maxField = Math.max(fieldX, fieldY);
			fieldSquareSize = (int) ((maxY - OUT_PADDING * 2) / maxField);
			fieldInitX = OUT_PADDING;
			fieldInitY = OUT_PADDING;
			FIELD_AREA = new RectF(fieldInitX, fieldInitY, fieldInitX + fieldSquareSize * fieldX, fieldInitY
					+ fieldSquareSize * fieldY);

			{
				final float[] text_widths = { Draw.getStrWidth(CENTER_TEXT_PAINT, UNDO_TEXT),
						Draw.getStrWidth(CENTER_TEXT_PAINT, CLEAR_TEXT),
						Draw.getStrWidth(CENTER_TEXT_PAINT, ROTATE_TEXT),
						Draw.getStrWidth(CENTER_TEXT_PAINT, NEXT_TEXT) };
				final float maxTextWidth = Utils.max(text_widths) + IN_PADDING * 2;
				final int text_height = Draw.getStrHeight(CENTER_TEXT_PAINT, NEXT_TEXT);

				float width = maxX - OUT_PADDING * 2 - FIELD_AREA.right;
				float my = maxY / 10f;

				if (maxTextWidth < width || true) {
					float mx = FIELD_AREA.right + OUT_PADDING + width / 2;
					float bHeight = Math.max(my, text_height + IN_PADDING * 2);
					final float left = mx - maxTextWidth / 2;
					final float right = mx + maxTextWidth / 2;
					float top = my * 1.5f - bHeight / 2;
					CLEAR_BUTTON_AREA = new MyButton(left, top, right, top + bHeight);
					top += my * 2f;
					UNDO_BUTTON_AREA = new MyButton(left, top, right, top + bHeight);
					top += my * 3f;
					ROTATE_BUTTON_AREA = new MyButton(left, top, right, top + bHeight);
					top += my * 1f;
					READY_BUTTON_AREA = new MyButton(left, top, right, top + bHeight);
					top += my * 1f;
					NEXT_BUTTON_AREA = new MyButton(left, top, right, top + bHeight);
				}
			}
		}
	}

	@Override
	public synchronized void draw(Canvas canvas) {
		if (state == State.WatchScreen) {

		} else {
			drawButton(canvas, UNDO_TEXT, UNDO_BUTTON_AREA, CENTER_TEXT_PAINT);
			drawButton(canvas, CLEAR_TEXT, CLEAR_BUTTON_AREA, CENTER_TEXT_PAINT);

			final int fieldX = game.getMaxX();
			final int fieldY = game.getMaxY();

			drawField(canvas, fieldSquareSize, fieldX, fieldY);

			if (state == State.PlacingScreen) {
				drawButton(canvas, ROTATE_TEXT, ROTATE_BUTTON_AREA, CENTER_TEXT_PAINT);
				drawButton(canvas, NEXT_TEXT, NEXT_BUTTON_AREA, CENTER_TEXT_PAINT);

				//				final Player player = game.getPlayer1();
				//				final Ship selectedShip = player.getSelectedShip();
				//				final List<Coordinate> listPieces = ShipClass.getShipParts(selectedShip.getId(), 0);
				//
				//				final float endX = ROTATE_BUTTON_AREA.right + BUTTON_OUT_PADDING;
				//				final float endY = fieldInitY + fieldSquareSize * fieldY + BUTTON_OUT_PADDING;
				//
				//				drawShip(canvas, endX, endY, fieldSquareSize, listPieces, SHIP_PAINT);
			} else if (state == State.ReadyScreen) {
				drawButton(canvas, READY_TEXT, READY_BUTTON_AREA, CENTER_TEXT_PAINT);
			} else if (state == State.WaitingScreen) {
				final String str = WAITING_TEXT + Draw.repeatString(".", pulse % (WAITING_TEXT_MAX_DOTS + 1));
				final float x = READY_BUTTON_AREA.centerX() - Draw.getStrWidth(LEFT_TEXT_PAINT, WAITING_TEXT) / 2;
				canvas.drawText(str, x, READY_BUTTON_AREA.centerY() + TEXT_HEIGHT / 2, LEFT_TEXT_PAINT);
			}
		}
		{
			canvas.drawText(Coordinate2.COORDINATE_COUNTER + "", maxX - 10, maxY - 10, RIGHT_TEXT_PAINT);
		}
	}

	private void drawField(Canvas canvas, int ss, int fieldX, int fieldY) {
		final Player player = game.getPlayer1();

		canvas.drawRect(fieldInitX, fieldInitY, fieldInitX + ss * fieldX, fieldInitY + ss * fieldY, WATER_PAINT);

		for (int x = 0; x < fieldX; x++) {
			for (int y = 0; y < fieldY; y++) {
				final float x1 = fieldInitX + x * ss;
				final float y1 = fieldInitY + y * ss;
				final float x2 = fieldInitX + (x + 1) * ss;
				final float y2 = fieldInitY + (y + 1) * ss;
				if (player.shipAt(x, y) != null) {
					canvas.drawRect(x1, y1, x2, y2, SHIP_PAINT);
				} else if (player.shipNear(x, y)) {
					canvas.drawRect(x1, y1, x2, y2, WATER_NEAR_SHIP_PAINT);
				}
			}
		}

		final Ship selectedShip = player.getSelectedShip();
		if (selectedShip != null) {
			Paint selectedShipPaint = player.canPlaceShip() ? SHIP_GOOD_PAINT : SHIP_BAD_PAINT;
			drawShip(canvas, fieldInitX, fieldInitY, ss, selectedShip.getListPieces(), selectedShipPaint);
		}

		for (int i = 0; i <= fieldX; i++) {
			canvas.drawLine(fieldInitX + ss * i, fieldInitY, fieldInitX + ss * i, fieldInitY + ss * fieldY, FIELD_PAINT);
		}

		for (int i = 0; i <= fieldY; i++) {
			canvas.drawLine(fieldInitX, fieldInitY + ss * i, fieldInitX + ss * fieldX, fieldInitY + ss * i, FIELD_PAINT);
		}
	}

	private void drawShip(Canvas canvas, float initX, float initY, int ss, List<Coordinate2> listPieces, Paint shipPaint) {
		for (Coordinate2 coor : listPieces) {
			final float x1 = initX + coor.x * ss;
			final float y1 = initY + coor.y * ss;
			final float x2 = initX + (coor.x + 1) * ss;
			final float y2 = initY + (coor.y + 1) * ss;
			canvas.drawRect(x1, y1, x2, y2, shipPaint);
			canvas.drawRect(x1, y1, x2, y2, FIELD_PAINT);
		}
	}

	private int convertScreenToGame(float screenInit, int squareSize, float screenCoor) {
		return (int) Math.floor((screenCoor - screenInit) / squareSize);
	}

	@Override
	public synchronized void onTouchEvent(MotionEvent event) {
		final float x = event.getX();
		final float y = event.getY();
		final int action = event.getActionMasked();

		//		Log.i("TAG YO", event.toString());

		final int gameX = convertScreenToGame(fieldInitX, fieldSquareSize, x);
		final int gameY = convertScreenToGame(fieldInitY, fieldSquareSize, y);

		if (state != State.WatchScreen) {
			if (buttonDownAndUp(action, x, y, UNDO_BUTTON_AREA)) {
				playerGUI.undoLastPlacedShip();
				state = State.PlacingScreen;
			} else if (buttonDownAndUp(action, x, y, CLEAR_BUTTON_AREA)) {
				while (playerGUI.undoLastPlacedShip())
					;
				state = State.PlacingScreen;
			} else if (state == State.PlacingScreen) {
				if (buttonDownAndUp(action, x, y, ROTATE_BUTTON_AREA)) {
					playerGUI.rotateShipClockwise();
				} else if (buttonDownAndUp(action, x, y, NEXT_BUTTON_AREA)) {
					playerGUI.placeShip();
					if (playerGUI.getSelectedShip() == null)
						state = State.ReadyScreen;
					else
						playerGUI.setPositionToRandomLocation(false);
				}
			} else if (state == State.ReadyScreen) {
				if (buttonDownAndUp(action, x, y, READY_BUTTON_AREA)) {
					playerGUI.tryToSetReady();
					if (game.getPlayer1().isReady())
						state = State.WaitingScreen;
				}
			}
		}

		if (action == MotionEvent.ACTION_DOWN) {
			if (state == State.PlacingScreen && game.isInsideField(gameX, gameY)) {
				if (playerGUI.getSelectedShip().near(gameX, gameY, 2)) {
					lastX = x;
					lastY = y;
					movingShip = true;
				}
			}
		} else if (action == MotionEvent.ACTION_MOVE) {
			if (movingShip) {
				if (Math.abs(lastX - x) >= fieldSquareSize) {
					int dx = -Math.round((lastX - x) / fieldSquareSize);
					lastX += dx * fieldSquareSize;
					playerGUI.movePosition(dx, 0);
				}
				if (Math.abs(lastY - y) >= fieldSquareSize) {
					int dy = -Math.round((lastY - y) / fieldSquareSize);
					lastY += dy * fieldSquareSize;
					playerGUI.movePosition(0, dy);
				}
			}
		} else if (action == MotionEvent.ACTION_UP) {
			movingShip = false;
		} else if (action == MotionEvent.ACTION_POINTER_DOWN) {
			if (state == State.PlacingScreen && movingShip) {
				playerGUI.rotateShipClockwise();
			}
		}
	}

	@Override
	public synchronized void update(double timeElapsed) {
		pulseTime += timeElapsed;
		if (pulseTime >= PULSE_TICK_TIME) {
			pulseTime -= PULSE_TICK_TIME;
			pulse++;

			if (ComputerAI.DEBUG_AI_AUTO_PLACE && state == State.PlacingScreen) {
				//				playerGUI.placeShip();
				//				if (playerGUI.getSelectedShip() == null)
				//					state = State.ReadyScreen;
				//				else
				//					playerGUI.setPositionToRandomLocation(false);
				playerGUI.placeAllShipsRandom(10, true);
				if (playerGUI.getSelectedShip() == null) {
					state = State.ReadyScreen;
					playerGUI.tryToSetReady();
					if (game.getPlayer1().isReady())
						state = State.WaitingScreen;
				}
			}
		}
	}
}
