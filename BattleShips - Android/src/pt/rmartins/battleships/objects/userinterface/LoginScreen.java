package pt.rmartins.battleships.objects.userinterface;

import pt.rmartins.battleships.R;
import pt.rmartins.battleships.network.Connection;
import pt.rmartins.battleships.objects.Game;
import pt.rmartins.battleships.objects.Game.GameState;
import pt.rmartins.battleships.objects.GameClass;
import pt.rmartins.battleships.objects.Player;
import pt.rmartins.battleships.utilities.Draw;
import pt.rmartins.battleships.utilities.LanguageClass;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;

public class LoginScreen extends UserInterfaceClass implements KeyboardInterface {

	private static final int COLOR_TEXT = Color.BLACK;

	private static float BUTTON_TEXT_SIZE = 25f;

	private static Paint TEXT_PAINT, LEFT_TEXT_PAINT, RIGHT_TEXT_PAINT;

	private static final int NICKNAME_CODE = R.string.login_nickname;
	private static final int CONTINUE_CODE = R.string.login_continue;

	private String NICKNAME_TEXT, CONTINUE_TEXT;

	private int TEXT_HEIGHT;
	private MyButton CONTINUE_BUTTON, NICKNAME_CHANGE_AREA;
	private RectF NICKNAME_AREA;

	private int maxX, maxY;
	private final Game game;
	private final Activity activity;
	private final Connection conn;

	private final StringBuilder nickname;
	private final InputMethodManager inputMethodManager;

	public LoginScreen(int maxX, int maxY, Game game, Activity activity, Connection conn) {
		this.game = game;
		this.activity = activity;
		this.conn = conn;

		nickname = new StringBuilder(GameClass.getMultiplayerNickname());

		inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);

		initializeGUI(maxX, maxY);
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

	@Override
	public synchronized void initializeGUI(int maxX, int maxY) {
		this.maxX = maxX;
		this.maxY = maxY;
		NICKNAME_TEXT = LanguageClass.getString(NICKNAME_CODE);
		CONTINUE_TEXT = LanguageClass.getString(CONTINUE_CODE);

		TEXT_HEIGHT = Draw.getStrHeight(TEXT_PAINT);

		{
			int text_width = Draw.getStrWidth(TEXT_PAINT, NICKNAME_TEXT);
			NICKNAME_AREA = new RectF(maxX / 2 - text_width, OUT_PADDING, maxX / 2 + text_width, OUT_PADDING
					+ TEXT_HEIGHT);
		}
		{
			final String NICKNAME_MAX = Draw.repeatString("#", Player.NICKNAME_MAX_CHARACTERS);
			NICKNAME_CHANGE_AREA = UserInterfaceClass.CreateNewButton(NICKNAME_MAX, TEXT_PAINT, maxX / 2,
					NICKNAME_AREA.bottom + OUT_PADDING, ButtonAlignType.Top);
		}
		{
			CONTINUE_BUTTON = UserInterfaceClass.CreateNewButton(CONTINUE_TEXT, TEXT_PAINT, maxX / 2,
					NICKNAME_CHANGE_AREA.bottom + TEXT_HEIGHT, ButtonAlignType.Top);
		}

		/**
		 * When there are logins and ither stuff:
		 * 
		 * CONTINUE_BUTTON = UserInterfaceClass.CreateNewButton(CONTINUE_TEXT, TEXT_PAINT, maxX / 2, maxY - OUT_PADDING,
		 * ButtonAlignType.Bottom);
		 */
	}

	@Override
	public synchronized void draw(Canvas canvas) {
		canvas.drawText(NICKNAME_TEXT, NICKNAME_AREA.centerX(), NICKNAME_AREA.bottom, TEXT_PAINT);

		drawButton(canvas, CONTINUE_TEXT, CONTINUE_BUTTON, TEXT_PAINT);

		drawButton(canvas, nickname.toString(), NICKNAME_CHANGE_AREA, TEXT_PAINT);
	}

	@Override
	public synchronized void onTouchEvent(MotionEvent event) {
		final float x = event.getX();
		final float y = event.getY();
		final int action = event.getAction();

		if (buttonDownAndUp(action, x, y, CONTINUE_BUTTON)) {
			final String nicknameString = nickname.toString();
			GameClass.setMultiplayerNickname(nicknameString);
			conn.setNickname(nicknameString);
			game.setGameState(GameState.MultiplayerMenu);
		} else if (buttonDownAndUp(action, x, y, NICKNAME_CHANGE_AREA)) {
			if (inputMethodManager != null) {
				if (inputMethodManager.isActive())
					inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
				else
					inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
			}
		}
	}

	@Override
	public synchronized void update(double timeElapsed) {
	}

	@Override
	public synchronized boolean dispatchKeyEvent(KeyEvent event) {
		int keyAction = event.getAction();

		if (keyAction == KeyEvent.ACTION_DOWN) {
			int keyCode = event.getKeyCode();
			int keyUnicode = event.getUnicodeChar(event.getMetaState());
			char character = (char) keyUnicode;
			System.out.println("DEBUG MESSAGE KEY=" + character + " KEYCODE=" + keyCode);

			int length = nickname.toString().length();
			if (keyCode == KeyEvent.KEYCODE_DEL && length > 0)
				nickname.deleteCharAt(length - 1);
			else if (validCharacter(character)) {
				if (length < Player.NICKNAME_MAX_CHARACTERS) {
					if (length > 0 || keyCode != KeyEvent.KEYCODE_SPACE)
						nickname.append(character);
				}
			}

			return true;
		} else
			return false;
	}

	private boolean validCharacter(char c) {
		return Character.isLetterOrDigit(c) || c == '_' || c == ' ';
	}

	@Override
	public synchronized boolean backPressed() {
		GameClass.setMultiplayerNickname(nickname.toString());
		return super.backPressed();
	}
}
