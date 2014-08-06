package pt.rmartins.battleships.objects.userinterface;

import java.util.List;

import pt.rmartins.battleships.R;
import pt.rmartins.battleships.network.Connection;
import pt.rmartins.battleships.network.ConnectionCallback;
import pt.rmartins.battleships.objects.Game;
import pt.rmartins.battleships.objects.Game.GameState;
import pt.rmartins.battleships.objects.GameClass;
import pt.rmartins.battleships.objects.Player;
import pt.rmartins.battleships.utilities.Draw;
import pt.rmartins.battleships.utilities.LanguageClass;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.MotionEvent;

public class MultiplayerScreen extends UserInterfaceClass implements ConnectionCallback {

	private static final int COLOR_TEXT = Color.BLACK;

	private static float TEXT_SIZE = 23f;
	private static float SMALL_TEXT_SIZE = 18f;
	private static float REFRESH_AREA_SIZE = 50f;

	private static final int CREATE_GAME_CODE = R.string.multiplayer_host_game;
	private static final int JOIN_GAME_CODE = R.string.multiplayer_join_game;
	private static final int CONNECTED_CODE = R.string.multiplayer_connected;
	private static final int NOT_CONNECTED_CODE = R.string.multiplayer_not_connected;

	private static String CREATE_GAME_TEXT, JOIN_GAME_TEXT, NOT_CONNECTED_TEXT;

	private static Paint TEXT_PAINT, SMALL_LEFT_TEXT_PAINT, SMALL_TEXT_PAINT, LEFT_TEXT_PAINT, RIGHT_TEXT_PAINT,
			IMAGE_PAINT, GAME_AREA_PEN;
	private static float TEXT_HEIGHT, SMALL_TEXT_HEIGHT;

	private static Bitmap REFRESH_IMAGE, REFRESH_DISABLED_IMAGE;

	private RectF WAITING_AREA, GAME_AREA, JOIN_GAME_AREA, CONNECTED_AREA, REFRESH_IMAGE_AREA;
	private MyButton CREATE_GAME_AREA, REFRESH_BUTTON;
	private static float GAMES_DIST_Y;

	private final Game game;
	private int maxX, maxY;
	private final Connection conn;

	private int retriesLeft;
	private boolean connected;

	private List<GameDefinition> existingGames;
	private final String nickname;

	private static final double DISABLE_REFRESH_TIME = 2.0;
	private static final double TIME_TO_AUTO_REFRESH = 10.0 + DISABLE_REFRESH_TIME;
	private static final float REFRESH_ANIMATION_MULTIPLIER = 2.0f;
	private static final double AFK_MAX_TIME = 60.0;
	private double timeToAutoRefresh, timeToEnableRefresh, afkTime;

	public static void initializeScreenMultiplier(Context context, float SCREEN_SUPPORT_MULTIPLIER) {
		TEXT_SIZE *= SCREEN_SUPPORT_MULTIPLIER;
		SMALL_TEXT_SIZE *= SCREEN_SUPPORT_MULTIPLIER;
		REFRESH_AREA_SIZE *= SCREEN_SUPPORT_MULTIPLIER;

		Typeface typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL);

		TEXT_PAINT = new Paint(Paint.ANTI_ALIAS_FLAG);
		TEXT_PAINT.setStyle(Style.FILL);
		TEXT_PAINT.setTextAlign(Paint.Align.CENTER);
		TEXT_PAINT.setColor(COLOR_TEXT);
		TEXT_PAINT.setTextSize(TEXT_SIZE);
		TEXT_PAINT.setTypeface(typeface);

		SMALL_LEFT_TEXT_PAINT = new Paint(TEXT_PAINT);
		SMALL_LEFT_TEXT_PAINT.setTextAlign(Paint.Align.LEFT);
		SMALL_LEFT_TEXT_PAINT.setTextSize(SMALL_TEXT_SIZE);

		SMALL_TEXT_PAINT = new Paint(SMALL_LEFT_TEXT_PAINT);
		SMALL_TEXT_PAINT.setTextAlign(Paint.Align.CENTER);

		TEXT_HEIGHT = Draw.getStrHeight(TEXT_PAINT);
		SMALL_TEXT_HEIGHT = Draw.getStrHeight(SMALL_LEFT_TEXT_PAINT);

		LEFT_TEXT_PAINT = new Paint(TEXT_PAINT);
		LEFT_TEXT_PAINT.setTextAlign(Paint.Align.LEFT);

		RIGHT_TEXT_PAINT = new Paint(TEXT_PAINT);
		RIGHT_TEXT_PAINT.setTextAlign(Paint.Align.RIGHT);

		IMAGE_PAINT = new Paint(Paint.ANTI_ALIAS_FLAG);
		IMAGE_PAINT.setStyle(Style.FILL);

		GAME_AREA_PEN = new Paint(Paint.ANTI_ALIAS_FLAG);
		GAME_AREA_PEN.setStyle(Style.STROKE);
		GAME_AREA_PEN.setStrokeWidth(SCREEN_SUPPORT_MULTIPLIER);

		final Resources res = context.getResources();
		{
			final int image_size = (int) (REFRESH_AREA_SIZE - BUTTON_STROKE_WIDTH * 2);
			REFRESH_IMAGE = BitmapFactory.decodeResource(res, R.drawable.refresh_green);
			REFRESH_IMAGE = Bitmap.createScaledBitmap(REFRESH_IMAGE, image_size, image_size, false);

			REFRESH_DISABLED_IMAGE = BitmapFactory.decodeResource(res, R.drawable.refresh_black);
			REFRESH_DISABLED_IMAGE = Bitmap.createScaledBitmap(REFRESH_DISABLED_IMAGE, image_size, image_size, false);
		}
	}

	public MultiplayerScreen(int maxX, int maxY, Game game, Connection conn) {
		this.game = game;
		this.conn = conn;

		afkTime = 0.0;
		timeToEnableRefresh = 0.0;

		existingGames = null;
		initializeGUI(maxX, maxY);

		retriesLeft = ConnectionCallback.MAX_CONNECTION_RETRIES;

		conn.addConnectionCallBack(this);
		connected = conn.isConnected();
		nickname = GameClass.getMultiplayerNickname();
		if (connected)
			initialize();
	}

	@Override
	public synchronized void clean() {
		super.clean();
		conn.removeConnectionCallBack(this);
	}

	@Override
	public synchronized void initializeGUI(int maxX, int maxY) {
		this.maxX = maxX;
		this.maxY = maxY;
		CREATE_GAME_TEXT = LanguageClass.getString(CREATE_GAME_CODE);
		JOIN_GAME_TEXT = LanguageClass.getString(JOIN_GAME_CODE);
		NOT_CONNECTED_TEXT = LanguageClass.getString(NOT_CONNECTED_CODE);
		{
			final String maxConnectedText = LanguageClass.getString(CONNECTED_CODE,
					Draw.repeatString("#", Player.NICKNAME_MAX_CHARACTERS));
			final int text_width1 = Draw.getStrWidth(TEXT_PAINT, maxConnectedText);
			final int text_width2 = Draw.getStrWidth(TEXT_PAINT, NOT_CONNECTED_TEXT);
			final float width = Math.max(text_width1, text_width2);
			//			CONNECTED_AREA = new RectF(0, 0, width, TEXT_HEIGHT);
			//			CONNECTED_AREA.offset(maxX / 2 - width / 2, maxY - TEXT_HEIGHT - OUT_PADDING);
			CONNECTED_AREA = new RectF(OUT_PADDING, OUT_PADDING, maxX - OUT_PADDING, OUT_PADDING + TEXT_HEIGHT);
		}
		{
			final int text_width = Draw.getStrWidth(TEXT_PAINT, CREATE_GAME_TEXT);
			CREATE_GAME_AREA = new MyButton(0, 0, text_width + IN_PADDING * 2, TEXT_HEIGHT + IN_PADDING * 2);
			CREATE_GAME_AREA.offset(OUT_PADDING, maxY - OUT_PADDING - CREATE_GAME_AREA.height());
		}
		{
			REFRESH_BUTTON = new MyButton(0, 0, REFRESH_AREA_SIZE, REFRESH_AREA_SIZE);
			REFRESH_BUTTON.offset(maxX - OUT_PADDING - REFRESH_BUTTON.width(),
					maxY - OUT_PADDING - REFRESH_BUTTON.height());
			REFRESH_IMAGE_AREA = new RectF(REFRESH_BUTTON);
			REFRESH_IMAGE_AREA.left += BUTTON_STROKE_WIDTH;
			REFRESH_IMAGE_AREA.top += BUTTON_STROKE_WIDTH;
			REFRESH_IMAGE_AREA.right -= BUTTON_STROKE_WIDTH;
			REFRESH_IMAGE_AREA.bottom -= BUTTON_STROKE_WIDTH;
		}
		{
			final float height = SMALL_TEXT_HEIGHT;
			GAME_AREA = new RectF(OUT_PADDING, 0, maxX - OUT_PADDING, height + IN_PADDING * 2);
			GAME_AREA.offset(0, CONNECTED_AREA.bottom + OUT_PADDING * 2);

			GAMES_DIST_Y = GAME_AREA.height();
		}
		{
			final float width = Draw.getStrWidth(SMALL_LEFT_TEXT_PAINT, JOIN_GAME_TEXT) + IN_PADDING * 2;
			final float height = SMALL_TEXT_HEIGHT + IN_PADDING * 2;
			JOIN_GAME_AREA = new MyButton(0, 0, width, height);
			JOIN_GAME_AREA.offset(GAME_AREA.right - JOIN_GAME_AREA.width(),
					GAME_AREA.centerY() - JOIN_GAME_AREA.height() / 2);

		}
	}

	private void initialize() {
		refreshGames();
	}

	private void refreshGames() {
		if (connected && afkTime < AFK_MAX_TIME) {
			conn.refreshGames();
			timeToAutoRefresh = TIME_TO_AUTO_REFRESH;
			REFRESH_BUTTON.setEnabled(false);
			timeToEnableRefresh = DISABLE_REFRESH_TIME;
		}
	}

	@Override
	public synchronized void draw(Canvas canvas) {
		{
			String str;
			if (connected) {
				str = LanguageClass.getString(CONNECTED_CODE, nickname);
			} else {
				if (retriesLeft == 0)
					str = "error connecting";
				else
					str = NOT_CONNECTED_TEXT + " (" + retriesLeft + " retries left)";
			}
			canvas.drawText(str, CONNECTED_AREA.left, CONNECTED_AREA.bottom, LEFT_TEXT_PAINT);
		}

		if (connected) {
			drawButton(canvas, CREATE_GAME_TEXT, CREATE_GAME_AREA, TEXT_PAINT);
			drawButton(canvas, "", REFRESH_BUTTON, TEXT_PAINT);
			{
				if (REFRESH_BUTTON.isEnabled())
					canvas.drawBitmap(REFRESH_IMAGE, REFRESH_IMAGE_AREA.left, REFRESH_IMAGE_AREA.top, IMAGE_PAINT);
				else {
					canvas.save();
					final float degrees = ((float) (timeToEnableRefresh / DISABLE_REFRESH_TIME)) * 360f
							* REFRESH_ANIMATION_MULTIPLIER;
					canvas.rotate(degrees, REFRESH_IMAGE_AREA.centerX(), REFRESH_IMAGE_AREA.centerY());
					canvas.drawBitmap(REFRESH_DISABLED_IMAGE, REFRESH_IMAGE_AREA.left, REFRESH_IMAGE_AREA.top,
							IMAGE_PAINT);
					canvas.restore();
				}
			}
			if (existingGames != null) {
				if (existingGames.isEmpty()) {
					canvas.drawText("No games...", GAME_AREA.centerX(), GAME_AREA.centerY() + TEXT_HEIGHT / 2,
							TEXT_PAINT);
				} else {
					for (int i = 0; i < existingGames.size(); i++) {
						final GameDefinition gameDefinition = existingGames.get(i);

						String text = gameDefinition.maxX + "x" + gameDefinition.maxY + " - "
								+ gameDefinition.getGameMode().getName();

						canvas.translate(0, GAMES_DIST_Y * i);
						canvas.drawText(text, GAME_AREA.left + IN_PADDING, GAME_AREA.centerY() + SMALL_TEXT_HEIGHT / 2,
								SMALL_LEFT_TEXT_PAINT);

						String joinStr = JOIN_GAME_TEXT;
						drawButton(canvas, joinStr, JOIN_GAME_AREA, SMALL_TEXT_PAINT);
						canvas.drawRect(GAME_AREA, GAME_AREA_PEN);
						canvas.translate(0, -GAMES_DIST_Y * i);
					}
				}
			}
		} else if (retriesLeft == 0) {
			//TODO: draw a giant button to retry connection :D
			// make it with 5 tries again
		}
	}

	@Override
	public synchronized void onTouchEvent(MotionEvent event) {
		final float x = event.getX();
		final float y = event.getY();
		final int action = event.getAction();

		afkTime = 0.0;

		if (buttonDownAndUp(action, x, y, CREATE_GAME_AREA)) {
			game.setGameState(GameState.ChoosingMode);
		} else if (buttonDownAndUp(action, x, y, REFRESH_BUTTON)) {
			refreshGames();
		}

		if (action == MotionEvent.ACTION_DOWN) {
			if (existingGames != null) {
				if (x >= JOIN_GAME_AREA.left && x <= JOIN_GAME_AREA.right) {
					for (int i = 0; i < existingGames.size(); i++) {
						float by = JOIN_GAME_AREA.top + GAMES_DIST_Y * i;
						if (y >= by && y <= by + JOIN_GAME_AREA.height()) {
							final String gameId = existingGames.get(i).gameId;
							conn.joinGame(gameId);
						}
					}
				}
			}
		} else if (action == MotionEvent.ACTION_MOVE) {
		} else if (action == MotionEvent.ACTION_UP) {
		}
	}

	@Override
	public synchronized void update(double timeElapsed) {
		afkTime += timeElapsed;

		timeToAutoRefresh -= timeElapsed;
		if (timeToAutoRefresh < 0) {
			refreshGames();
		}

		if (timeToEnableRefresh > 0.0) {
			timeToEnableRefresh -= timeElapsed;
			if (timeToEnableRefresh <= 0.0) {
				REFRESH_BUTTON.setEnabled(true);
			}
		}
	}

	@Override
	public synchronized void connected() {
		connected = true;
		initialize();
	}

	@Override
	public void gameStarted(boolean master) {
	}

	@Override
	public synchronized void refreshGames(List<GameDefinition> existingGames) {
		this.existingGames = existingGames;
	}

	@Override
	public synchronized void hostedGame(String gameId) {
		game.setGameState(GameState.LobbyMenu);
	}

	@Override
	public void joinedGame(String gameId, String playerNickname) {
		game.setGameState(GameState.LobbyMenu);
	}

	@Override
	public synchronized void unjoinedGame() {
	}

	@Override
	public void errorConnecting(int retriesLeft) {
		this.retriesLeft = retriesLeft;
	}

	@Override
	public void oponentDisconnected() {
	}

	//	@Override
	//	public void setEnemyNickname(String nickname) {
	//	}
}
