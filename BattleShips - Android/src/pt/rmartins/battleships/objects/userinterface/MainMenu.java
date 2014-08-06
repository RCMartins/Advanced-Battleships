package pt.rmartins.battleships.objects.userinterface;

import pt.rmartins.battleships.MenuPanel.NewGame;
import pt.rmartins.battleships.R;
import pt.rmartins.battleships.objects.Callback;
import pt.rmartins.battleships.objects.Game;
import pt.rmartins.battleships.objects.GameVsComputer;
import pt.rmartins.battleships.objects.GameVsPlayer;
import pt.rmartins.battleships.objects.userinterface.campaign.CampaignScreen;
import pt.rmartins.battleships.utilities.Draw;
import pt.rmartins.battleships.utilities.LanguageClass;
import pt.rmartins.battleships.utilities.Utils;
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

	private int maxX, maxY;
	private final Activity activity;
	private final NewGame newGame;
	private final Callback exitGame;

	public MainMenu(int maxX, int maxY, Activity activity, NewGame newGame, Callback exitGame) {
		this.activity = activity;
		this.newGame = newGame;
		this.exitGame = exitGame;

		initializeGUI(maxX, maxY);
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

	@Override
	public synchronized void initializeGUI(int maxX, int maxY) {
		this.maxX = maxX;
		this.maxY = maxY;
		CAMPAIGN_TEXT = LanguageClass.getString(CAMPAIGN_CODE);
		ONE_PLAYER_TEXT = LanguageClass.getString(ONE_PLAYER_CODE);
		TWO_PLAYERS_TEXT = LanguageClass.getString(TWO_PLAYERS_CODE);
		OPTIONS_TEXT = LanguageClass.getString(OPTIONS_CODE);

		final float[] text_widths = { Draw.getStrWidth(TEXT_PAINT, ONE_PLAYER_TEXT),
				Draw.getStrWidth(TEXT_PAINT, TWO_PLAYERS_TEXT), Draw.getStrWidth(TEXT_PAINT, TWO_PLAYERS_TEXT),
				Draw.getStrWidth(TEXT_PAINT, TWO_PLAYERS_TEXT) };
		final int text_height = Draw.getStrHeight(TEXT_PAINT, ONE_PLAYER_TEXT);

		final float width = Utils.max(text_widths) + IN_PADDING * 2;
		final float left = maxX / 2 - width / 2;
		final float right = maxX / 2 + width / 2;

		VERSION_AREA = new RectF(0, maxY - text_height - OUT_PADDING, maxX, maxY - OUT_PADDING);

		final float sy = VERSION_AREA.top / 9;
		final float height = Math.max(sy, Draw.getStrHeight(TEXT_PAINT, ONE_PLAYER_TEXT) + IN_PADDING * 2);

		float top = sy * 1.5f - height / 2;
		float bottom = sy * 1.5f + height / 2;
		CAMPAIGN_BUTTON = new MyButton(left, top, right, bottom);
		top += sy * 2;
		bottom += sy * 2;
		ONE_PLAYER_BUTTON = new MyButton(left, top, right, bottom);
		top += sy * 2;
		bottom += sy * 2;
		TWO_PLAYERS_BUTTON = new MyButton(left, top, right, bottom);
		top += sy * 2;
		bottom += sy * 2;
		OPTIONS_BUTTON = new MyButton(left, top, right, bottom);

	}

	@Override
	public synchronized void draw(Canvas canvas) {
		canvas.drawColor(Color.WHITE);
		drawButton(canvas, CAMPAIGN_TEXT, CAMPAIGN_BUTTON, TEXT_PAINT);
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

		if (buttonDownAndUp(action, x, y, CAMPAIGN_BUTTON)) {
			newGame.setGUI(new CampaignScreen(maxX, maxY, activity));
		} else if (buttonDownAndUp(action, x, y, ONE_PLAYER_BUTTON)) {
			newGame.setGame(new GameVsComputer(activity, exitGame));
		} else if (buttonDownAndUp(action, x, y, TWO_PLAYERS_BUTTON)) {
			newGame.setGame(new GameVsPlayer(activity, exitGame));
		} else if (buttonDownAndUp(action, x, y, OPTIONS_BUTTON)) {
			newGame.setGUI(new OptionsScreen(maxX, maxY, activity));
		}
	}

	@Override
	public synchronized void update(double timeElapsed) {
		// TODO Auto-generated method stub
	}
}
