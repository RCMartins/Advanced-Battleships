package pt.rmartins.battleships.objects.userinterface;

import java.util.List;

import pt.rmartins.battleships.R;
import pt.rmartins.battleships.network.Connection;
import pt.rmartins.battleships.network.ConnectionCallback;
import pt.rmartins.battleships.objects.Game;
import pt.rmartins.battleships.objects.Game.GameState;
import pt.rmartins.battleships.objects.GameClass;
import pt.rmartins.battleships.objects.Player;
import pt.rmartins.battleships.objects.modes.GameMode;
import pt.rmartins.battleships.objects.userinterface.ChooseScreen.ShowFleetData;
import pt.rmartins.battleships.utilities.Draw;
import pt.rmartins.battleships.utilities.LanguageClass;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.MotionEvent;

public class LobbyScreen extends UserInterfaceClass implements ConnectionCallback {

	private static final int COLOR_TEXT = Color.BLACK;

	private static float BUTTON_TEXT_SIZE = 25f;

	private static Paint TEXT_PAINT, LEFT_TEXT_PAINT, RIGHT_TEXT_PAINT;

	private static final int START_GAME_CODE = R.string.lobby_start_game;
	private static final int VS_CODE = R.string.lobby_versus;

	private enum InterfaceType {
		Type1, Type2;
	}

	private String START_GAME_TEXT;
	private String VS_TEXT;

	private int TEXT_HEIGHT;
	private MyButton START_GAME_BUTTON;
	private RectF MY_NICKNAME_AREA, VS_AREA, ENEMY_NICKNAME_AREA, FLEET_AREA, GAME_MODE_AREA;

	private final int maxX, maxY;
	private final Game game;
	private final Connection conn;
	private final String lobbyId;
	private final boolean host;
	private InterfaceType interfaceType;
	private final String myNickname;
	private String enemyNickname;
	private final ShowFleetData showFleet;

	public LobbyScreen(int maxX, int maxY, Game game, Connection conn) {
		this.maxX = maxX;
		this.maxY = maxY;
		this.game = game;
		this.conn = conn;

		conn.addConnectionCallBack(this);
		this.lobbyId = conn.getJoinedGameId();
		this.host = conn.isHost();

		myNickname = GameClass.getMultiplayerNickname();
		enemyNickname = "";

		initialize();
		showFleet = new ShowFleetData(FLEET_AREA, TEXT_HEIGHT, game.getCurrentFleet());
	}

	@Override
	public synchronized void clean() {
		super.clean();
		conn.removeConnectionCallBack(this);
	}

	public static void initializeScreenMultiplier(Context context, float SCREEN_SUPPORT_MULTIPLIER) {
		BUTTON_TEXT_SIZE *= SCREEN_SUPPORT_MULTIPLIER;

		Typeface typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL);

		TEXT_PAINT = new Paint(Paint.ANTI_ALIAS_FLAG);
		TEXT_PAINT.setStyle(Style.FILL);
		TEXT_PAINT.setTextAlign(Paint.Align.CENTER);
		TEXT_PAINT.setColor(COLOR_TEXT);
		TEXT_PAINT.setTextSize(BUTTON_TEXT_SIZE);
		TEXT_PAINT.setTypeface(typeface);

		LEFT_TEXT_PAINT = new Paint(TEXT_PAINT);
		LEFT_TEXT_PAINT.setTextAlign(Paint.Align.LEFT);

		RIGHT_TEXT_PAINT = new Paint(TEXT_PAINT);
		RIGHT_TEXT_PAINT.setTextAlign(Paint.Align.RIGHT);
	}

	private void initialize() {
		START_GAME_TEXT = LanguageClass.getString(START_GAME_CODE);
		VS_TEXT = LanguageClass.getString(VS_CODE);

		TEXT_HEIGHT = Draw.getStrHeight(TEXT_PAINT);

		{
			START_GAME_BUTTON = UserInterfaceClass.CreateNewButton(START_GAME_TEXT, TEXT_PAINT, maxX / 2, maxY
					- OUT_PADDING, ButtonAlignType.Bottom);
			START_GAME_BUTTON.setEnabled(false);
		}
		{
			final float vs_text_width = Draw.getStrWidth(TEXT_PAINT, VS_TEXT) + IN_PADDING * 2;
			final String NICKNAME_MAX = Draw.repeatString("#", Player.NICKNAME_MAX_CHARACTERS);
			final float max_nickname_width = Draw.getStrWidth(TEXT_PAINT, NICKNAME_MAX);
			final float maxPossibleWidth = vs_text_width + max_nickname_width * 2;

			if (maxPossibleWidth > maxX) {
				float y = TEXT_HEIGHT;
				MY_NICKNAME_AREA = new RectF(maxX / 2 - max_nickname_width / 2, y, maxX / 2 + max_nickname_width / 2, y
						+ TEXT_HEIGHT);
				y += TEXT_HEIGHT + OUT_PADDING;
				VS_AREA = new RectF(maxX / 2 - vs_text_width / 2, y, maxX / 2 + vs_text_width / 2, y + TEXT_HEIGHT);
				y += TEXT_HEIGHT + OUT_PADDING;
				ENEMY_NICKNAME_AREA = new RectF(maxX / 2 - max_nickname_width / 2, y,
						maxX / 2 + max_nickname_width / 2, y + TEXT_HEIGHT);
				interfaceType = InterfaceType.Type2;
			} else {
				final float vsY = TEXT_HEIGHT;
				VS_AREA = new RectF(maxX / 2 - vs_text_width / 2, vsY, maxX / 2 + vs_text_width / 2, vsY + TEXT_HEIGHT);
				MY_NICKNAME_AREA = new RectF(VS_AREA.left - max_nickname_width, vsY, VS_AREA.left, vsY + TEXT_HEIGHT);
				ENEMY_NICKNAME_AREA = new RectF(VS_AREA.right, vsY, VS_AREA.right + max_nickname_width, vsY
						+ TEXT_HEIGHT);
				interfaceType = InterfaceType.Type1;
			}
		}
		{
			RectF remainingArea = new RectF(OUT_PADDING, ENEMY_NICKNAME_AREA.bottom + OUT_PADDING, maxX - OUT_PADDING,
					START_GAME_BUTTON.top - OUT_PADDING);
			FLEET_AREA = new RectF(remainingArea);
			FLEET_AREA.bottom = remainingArea.centerY();
			GAME_MODE_AREA = new RectF(remainingArea);
			GAME_MODE_AREA.top = remainingArea.centerY() - 1;
		}
	}

	@Override
	public synchronized void draw(Canvas canvas) {
		if (interfaceType == InterfaceType.Type1) {
			canvas.drawText(VS_TEXT, VS_AREA.centerX(), VS_AREA.bottom, TEXT_PAINT);
			canvas.drawText(myNickname, MY_NICKNAME_AREA.left, MY_NICKNAME_AREA.bottom, LEFT_TEXT_PAINT);
			canvas.drawText(enemyNickname, ENEMY_NICKNAME_AREA.right, ENEMY_NICKNAME_AREA.bottom, RIGHT_TEXT_PAINT);
		} else if (interfaceType == InterfaceType.Type2) {
			canvas.drawText(VS_TEXT, VS_AREA.centerX(), VS_AREA.bottom, TEXT_PAINT);
			canvas.drawText(myNickname, MY_NICKNAME_AREA.centerX(), MY_NICKNAME_AREA.bottom, TEXT_PAINT);
			canvas.drawText(enemyNickname, ENEMY_NICKNAME_AREA.centerX(), ENEMY_NICKNAME_AREA.bottom, TEXT_PAINT);
		}
		if (host)
			drawButton(canvas, START_GAME_TEXT, START_GAME_BUTTON, TEXT_PAINT);

		showFleet.draw(canvas);
		final GameMode gameMode = game.getCurrentGameMode();
		ScreenUtils.drawGameInfo(canvas, game, gameMode, GAME_MODE_AREA, LEFT_TEXT_PAINT, TEXT_PAINT, false);
	}

	@Override
	public synchronized void onTouchEvent(MotionEvent event) {
		final float x = event.getX();
		final float y = event.getY();
		final int action = event.getAction();

		if (host) {
			if (buttonDownAndUp(action, x, y, START_GAME_BUTTON)) {
				game.setGameState(GameState.SendInitializingInformation);
			}
		}
	}

	@Override
	public synchronized void update(double timeElapsed) {

	}

	@Override
	public synchronized void connected() {
	}

	@Override
	public synchronized void errorConnecting(int retriesLeft) {
	}

	@Override
	public synchronized void refreshGames(List<GameDefinition> existingGames) {
	}

	@Override
	public synchronized void hostedGame(String gameId) {
	}

	@Override
	public synchronized void joinedGame(String gameId, String playerNickname) {
		if (gameId.equals(lobbyId)) {
			enemyNickname = playerNickname;
			if (host) {
				START_GAME_BUTTON.setEnabled(true);
			}
		}
	}

	@Override
	public synchronized void unjoinedGame() {
		// TODO Auto-generated method stub
	}

	@Override
	public synchronized void oponentDisconnected() {
		enemyNickname = "";
		START_GAME_BUTTON.setEnabled(false);
		//TODO add a message that oponent has disconnected.
	}

	@Override
	public synchronized void gameStarted(boolean master) {
		if (master)
			game.setGameState(GameState.SendInitializingInformation);
		else
			game.setGameState(GameState.WaitMaster);
	}

	@Override
	public synchronized boolean backPressed() {
		if (host)
			conn.unHostGame(lobbyId);
		else
			conn.unJoinGame(lobbyId);
		game.setGameState(GameState.MultiplayerMenu);
		return true;
	}
}
