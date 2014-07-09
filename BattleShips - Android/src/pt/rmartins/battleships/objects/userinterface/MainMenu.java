package pt.rmartins.battleships.objects.userinterface;

import pt.rmartins.battleships.MenuPanel.NewGame;
import pt.rmartins.battleships.R;
import pt.rmartins.battleships.objects.Callback;
import pt.rmartins.battleships.objects.Game;
import pt.rmartins.battleships.objects.GameVsComputer;
import pt.rmartins.battleships.objects.GameVsPlayer;
import pt.rmartins.battleships.utilities.Draw;
import pt.rmartins.battleships.utilities.LanguageClass;
import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.MotionEvent;

public class MainMenu extends UserInterfaceClass {

	private static final int CAMPAIGN_CODE = R.string.mainmenu_campaign;
	private static final int ONE_PLAYER_CODE = R.string.mainmenu_1player;
	private static final int TWO_PLAYERS_CODE = R.string.mainmenu_2players;
	private static final int OPTIONS_CODE = R.string.mainmenu_options;
	private static final int VERSION_CODE = R.string.mainmenu_version;

	private static float BUTTON_TEXT_SIZE = 30f;

	private String CAMPAIGN_TEXT, ONE_PLAYER_TEXT, TWO_PLAYERS_TEXT, OPTIONS_TEXT;

	private static Paint TEXT_PAINT;
	private MyButton CAMPAIGN_BUTTON, ONE_PLAYER_BUTTON, TWO_PLAYERS_BUTTON, OPTIONS_BUTTON;
	private RectF VERSION_AREA;

	private final float maxX;
	private final float maxY;
	private final Activity activity;
	private final NewGame newGame;
	private final Callback exitGame;

	public MainMenu(int maxX, int maxY, Activity activity, NewGame newGame, Callback exitGame) {
		this.maxX = maxX;
		this.maxY = maxY;
		this.activity = activity;
		this.newGame = newGame;
		this.exitGame = exitGame;

		initialize();
	}

	public static void initializeScreenMultiplier(float SCREEN_SUPPORT_MULTIPLIER) {
		BUTTON_TEXT_SIZE *= SCREEN_SUPPORT_MULTIPLIER;

		Typeface typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL);

		TEXT_PAINT = new Paint(Paint.ANTI_ALIAS_FLAG);
		TEXT_PAINT.setStyle(Style.FILL);
		TEXT_PAINT.setTextAlign(Paint.Align.CENTER);
		TEXT_PAINT.setColor(COLOR_TEXT);
		TEXT_PAINT.setTextSize(BUTTON_TEXT_SIZE);
		TEXT_PAINT.setTypeface(typeface);
	}

	private synchronized void initialize() {
		CAMPAIGN_TEXT = LanguageClass.getString(CAMPAIGN_CODE);
		ONE_PLAYER_TEXT = LanguageClass.getString(ONE_PLAYER_CODE);
		TWO_PLAYERS_TEXT = LanguageClass.getString(TWO_PLAYERS_CODE);
		OPTIONS_TEXT = LanguageClass.getString(OPTIONS_CODE);

		final int text_width1 = Draw.getStrWidth(TEXT_PAINT, ONE_PLAYER_TEXT);
		final int text_width2 = Draw.getStrWidth(TEXT_PAINT, TWO_PLAYERS_TEXT);
		final int text_height = Draw.getStrHeight(TEXT_PAINT, ONE_PLAYER_TEXT);

		final float width = Math.max(maxX / 2, Math.max(text_width1, text_width2) + IN_PADDING * 2);
		final float sx = maxX / 2 - width / 2;
		final float sy = maxY / 7;

		ONE_PLAYER_BUTTON = new MyButton(sx, sy, sx + width, sy * 2);
		TWO_PLAYERS_BUTTON = new MyButton(sx, sy * 3, sx + width, sy * 4);
		OPTIONS_BUTTON = new MyButton(sx, sy * 5, sx + width, sy * 6);

		VERSION_AREA = new RectF(sx, maxY - text_height - OUT_PADDING, sx + width, maxY - OUT_PADDING);
	}

	@Override
	public synchronized void draw(Canvas canvas) {
		canvas.drawColor(Color.WHITE);
		drawButton(canvas, ONE_PLAYER_TEXT, ONE_PLAYER_BUTTON, TEXT_PAINT);
		drawButton(canvas, TWO_PLAYERS_TEXT, TWO_PLAYERS_BUTTON, TEXT_PAINT);
		drawButton(canvas, OPTIONS_TEXT, OPTIONS_BUTTON, TEXT_PAINT);

		final String versionStr = LanguageClass.getString(VERSION_CODE, Game.GAMEVERSION);
		canvas.drawText(versionStr, VERSION_AREA.centerX(), VERSION_AREA.bottom, TEXT_PAINT);
	}

	@Override
	public synchronized void onTouchEvent(MotionEvent event) {
		final float x = event.getX();
		final float y = event.getY();
		final int action = event.getAction();

		if (buttonDownAndUp(action, x, y, ONE_PLAYER_BUTTON)) {
			newGame.setGame(new GameVsComputer(activity, exitGame));
		} else if (buttonDownAndUp(action, x, y, TWO_PLAYERS_BUTTON)) {
			newGame.setGame(new GameVsPlayer(activity, exitGame));
		} else if (buttonDownAndUp(action, x, y, OPTIONS_BUTTON)) {
			newGame.setGUI(new OptionsScreen((int) maxX, (int) maxY, activity, exitGame));
		}
	}

	@Override
	public synchronized void update(double timeElapsed) {
		// TODO Auto-generated method stub
	}
}
