package pt.rmartins.battleships.objects.userinterface;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import pt.rmartins.battleships.R;
import pt.rmartins.battleships.objects.Callback;
import pt.rmartins.battleships.objects.Coordinate;
import pt.rmartins.battleships.objects.Fleet;
import pt.rmartins.battleships.objects.Game;
import pt.rmartins.battleships.objects.Game.GameState;
import pt.rmartins.battleships.objects.Game.Mark;
import pt.rmartins.battleships.objects.Game.TurnState;
import pt.rmartins.battleships.objects.GameClass;
import pt.rmartins.battleships.objects.Message;
import pt.rmartins.battleships.objects.Message.MessageUnit;
import pt.rmartins.battleships.objects.Message.MessageUnit.TypesMessageUnits;
import pt.rmartins.battleships.objects.Player;
import pt.rmartins.battleships.objects.PlayerClass.Shot;
import pt.rmartins.battleships.objects.PlayerClass.Shot.KindShot;
import pt.rmartins.battleships.objects.Ship;
import pt.rmartins.battleships.objects.ShipClass;
import pt.rmartins.battleships.objects.ai.ComputerAI;
import pt.rmartins.battleships.objects.modes.GameBonus;
import pt.rmartins.battleships.objects.modes.GameBonus.BonusTypes;
import pt.rmartins.battleships.objects.modes.GameBonus.ExtraTurn;
import pt.rmartins.battleships.objects.modes.GameMode;
import pt.rmartins.battleships.objects.modes.GameMode.BonusPlay;
import pt.rmartins.battleships.objects.modes.GameMode.TimeLimitType;
import pt.rmartins.battleships.utilities.Draw;
import pt.rmartins.battleships.utilities.LanguageClass;
import pt.rmartins.battleships.utilities.StopWatch;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.Log;
import android.view.MotionEvent;

public class PlayingScreen extends UserInterfaceClass implements PlayInterface {

	private static final String TAG = PlayingScreen.class.getSimpleName();

	private static final int COLOR_BACKGROUND = Color.WHITE;
	private static final int COLOR_TEXT = Color.BLACK;

	private static final int COLOR_WHICH_MENU_TEXT = Color.RED;
	private static final int COLOR_WHICH_MENU_BULLET = Color.BLACK;
	private static final int COLOR_WHICH_MENU_SELECTED_BULLET = Color.RED;
	private static final int COLOR_LAST_PLAY_TEXT = Color.RED;

	private static final int COLOR_WATER = Color.CYAN;
	private static final int COLOR_WATER100 = Color.parseColor("#05B8CC");
	private static final int COLOR_SHIP = Color.parseColor("#FFC125");
	private static final int COLOR_SHIP100 = Color.parseColor("#FF7F00"); //Color.parseColor("#FFA812");
	private static final int COLOR_SHIP_GOOD = Color.GREEN;
	private static final int COLOR_SHIP_BAD = Color.RED;
	private static final int COLOR_DESTROYED_SHIP = Color.RED;

	private static class ConstantScaled {
		public float value;

		public ConstantScaled(float value) {
			this.value = value;
		}
	}

	private static ConstantScaled MINIMUM_PADDING, TOP_MENU_TEXT_SIZE, TOP_MENU_SMALL_TEXT_SIZE, WHICH_MENU_TEXT_SIZE,
			WHICH_MENU_BULLET_SIZE, WHICH_MENU_SELECTED_BULLET_SIZE, WHICH_MENU_BULLET_PADDING,
			PLACED_TARGETS_STROKE_WIDTH, POSITION_SELECTOR_STROKE_WIDTH, POSITION_SELECTOR_P2_STROKE_WIDTH,
			MIN_SQUARE_SIZE_MESSAGES, LAST_PLAY_STROKE_WIDTH, LAST_PLAY_DASH_EFFECT_SIZE, MISSILES_IMAGES_SIZE_X,
			MISSILES_IMAGES_SIZE_Y, MINI_MISSILES_IMAGES_SIZE_X, MINI_MISSILES_IMAGES_SIZE_Y, MINI_FIRE_IMAGES_SIZE_X,
			MINI_FIRE_IMAGES_SIZE_Y, MESSAGES_PLUS_X, DESTROYED_SHIP_STROKE_WIDTH, FIELD_PAINT_WIDTH,
			SWIPE_Y_THRESHOLD, POPUP_MARKS_MIN_SIZE, POPUP_MARKS_MAX_SIZE, POPUP_CLOSE_MAX_SIZE,
			TOP_MESSAGE_ARROW_SIZE;
	private static final int MINI_FIRE_IMAGES_FRAMES = 4;

	private static final List<ConstantScaled> CONSTANTS_LIST;
	static {
		CONSTANTS_LIST = new ArrayList<ConstantScaled>();
		CONSTANTS_LIST.add(MINIMUM_PADDING = new ConstantScaled(OUT_PADDING / 2));
		CONSTANTS_LIST.add(TOP_MENU_TEXT_SIZE = new ConstantScaled(25f));
		CONSTANTS_LIST.add(TOP_MENU_SMALL_TEXT_SIZE = new ConstantScaled(15f));
		CONSTANTS_LIST.add(WHICH_MENU_TEXT_SIZE = new ConstantScaled(15f));
		CONSTANTS_LIST.add(WHICH_MENU_BULLET_SIZE = new ConstantScaled(2f));
		CONSTANTS_LIST.add(WHICH_MENU_SELECTED_BULLET_SIZE = new ConstantScaled(4f));
		CONSTANTS_LIST.add(WHICH_MENU_BULLET_PADDING = new ConstantScaled(15f));
		CONSTANTS_LIST.add(PLACED_TARGETS_STROKE_WIDTH = new ConstantScaled(3f));
		CONSTANTS_LIST.add(POSITION_SELECTOR_STROKE_WIDTH = new ConstantScaled(5f));
		CONSTANTS_LIST.add(POSITION_SELECTOR_P2_STROKE_WIDTH = new ConstantScaled(3f));
		CONSTANTS_LIST.add(MIN_SQUARE_SIZE_MESSAGES = new ConstantScaled(5f));
		CONSTANTS_LIST.add(LAST_PLAY_STROKE_WIDTH = new ConstantScaled(3f));
		CONSTANTS_LIST.add(LAST_PLAY_DASH_EFFECT_SIZE = new ConstantScaled(6f));
		CONSTANTS_LIST.add(MISSILES_IMAGES_SIZE_X = new ConstantScaled(11f));
		CONSTANTS_LIST.add(MISSILES_IMAGES_SIZE_Y = new ConstantScaled(46f));
		CONSTANTS_LIST.add(MINI_MISSILES_IMAGES_SIZE_X = new ConstantScaled(11f));
		CONSTANTS_LIST.add(MINI_MISSILES_IMAGES_SIZE_Y = new ConstantScaled(33f));
		CONSTANTS_LIST.add(MINI_FIRE_IMAGES_SIZE_X = new ConstantScaled(11f));
		CONSTANTS_LIST.add(MINI_FIRE_IMAGES_SIZE_Y = new ConstantScaled(20f));
		CONSTANTS_LIST.add(MESSAGES_PLUS_X = new ConstantScaled(1f));
		CONSTANTS_LIST.add(DESTROYED_SHIP_STROKE_WIDTH = new ConstantScaled(2f));
		CONSTANTS_LIST.add(FIELD_PAINT_WIDTH = new ConstantScaled(1f));
		CONSTANTS_LIST.add(SWIPE_Y_THRESHOLD = new ConstantScaled(100f));
		CONSTANTS_LIST.add(POPUP_MARKS_MIN_SIZE = new ConstantScaled(40f));
		CONSTANTS_LIST.add(POPUP_MARKS_MAX_SIZE = new ConstantScaled(100f));
		CONSTANTS_LIST.add(POPUP_CLOSE_MAX_SIZE = new ConstantScaled(40f));
		CONSTANTS_LIST.add(TOP_MESSAGE_ARROW_SIZE = new ConstantScaled(40f));
	}

	private static final double PULSE_TICK_TIME = .7;

	private static class StringCode {
		private final int code;
		public String text;

		public StringCode(int code) {
			this.code = code;
		}

		public void updateText() {
			text = LanguageClass.getString(code);
		}

		@Override
		public String toString() {
			return text;
		}
	}

	private static final StringCode UNDO_TEXT = new StringCode(R.string.playing_undo);
	private static final StringCode TARGET_TEXT = new StringCode(R.string.playing_target);
	private static final StringCode LAUNCH_TEXT = new StringCode(R.string.playing_launch);
	private static final StringCode ENEMY_TURN_TEXT = new StringCode(R.string.playing_enemy_turn);
	private static final StringCode YOUR_TURN_TEXT = new StringCode(R.string.playing_your_turn);
	private static final StringCode YOUR_EXTRA_TURN_TEXT = new StringCode(R.string.playing_your_extra_turn);
	private static final StringCode ENEMY_EXTRA_TURN_TEXT = new StringCode(R.string.playing_enemy_extra_turn);
	private static final StringCode YOU_WON_TEXT = new StringCode(R.string.playing_you_won);
	private static final StringCode ENEMY_WON_TEXT = new StringCode(R.string.playing_enemy_won);
	private static final StringCode TOP_INFO_SPACE_NEEDED_TEXT = new StringCode(R.string.playing_max_space_needed);
	private static final StringCode REMATCH_TEXT = new StringCode(R.string.playing_rematch);
	private static final StringCode REMATCH_REQUEST_TEXT = new StringCode(R.string.playing_rematch_request);
	private static final StringCode REMATCH_ACCEPT_TEXT = new StringCode(R.string.playing_rematch_accept);
	private static final StringCode REMATCH_DECLINE_TEXT = new StringCode(R.string.playing_rematch_decline);
	private static final StringCode BACK_TO_MAINMENU_TEXT = new StringCode(R.string.playing_back_to_mainmenu);
	private static final StringCode GAME_PAUSED_TEXT = new StringCode(R.string.playing_game_paused);
	private static final StringCode WHICH_MENU_GAME_INFO_TEXT = new StringCode(R.string.playing_menu_game_info);
	private static final StringCode WHICH_MENU_MY_TEXT = new StringCode(R.string.playing_menu_my);
	private static final StringCode WHICH_MENU_ENEMY_TEXT = new StringCode(R.string.playing_menu_enemy);
	private static final StringCode EXIT_GAME_QUESTION_TEXT = new StringCode(R.string.playing_exit_game_question);
	private static final StringCode EXIT_GAME_YES_TEXT = new StringCode(R.string.playing_exit_game_yes);
	private static final StringCode EXIT_GAME_NO_TEXT = new StringCode(R.string.playing_exit_game_no);
	private static final StringCode MY_INFO_SHIPS_LOST_TEXT = new StringCode(R.string.playing_my_info_ships_lost);

	private static final StringCode[] stringCodeList = { UNDO_TEXT, TARGET_TEXT, LAUNCH_TEXT, ENEMY_TURN_TEXT,
			YOUR_TURN_TEXT, YOUR_EXTRA_TURN_TEXT, ENEMY_EXTRA_TURN_TEXT, YOU_WON_TEXT, ENEMY_WON_TEXT,
			TOP_INFO_SPACE_NEEDED_TEXT, REMATCH_TEXT, REMATCH_REQUEST_TEXT, REMATCH_ACCEPT_TEXT, REMATCH_DECLINE_TEXT,
			BACK_TO_MAINMENU_TEXT, GAME_PAUSED_TEXT, WHICH_MENU_GAME_INFO_TEXT, WHICH_MENU_MY_TEXT,
			WHICH_MENU_ENEMY_TEXT, EXIT_GAME_QUESTION_TEXT, EXIT_GAME_YES_TEXT, EXIT_GAME_NO_TEXT,
			MY_INFO_SHIPS_LOST_TEXT };

	private static final double WAITING_POPUP_TIME = 0.1;
	private static final double POPUP_TIME_YOUR_TURN = 1.5;
	private static final double POPUP_TIME_EXTRA_TURN = 2.5;
	private static final double POPUP_TIME_EXTERNAL_MESSAGE = 4.0;
	private static final double POPUP_TIME_FINISHED_GAME = Double.MAX_VALUE;

	private static Matrix UPSIDE_DOWN_MISSILE_MATRIX;

	public enum Menu {
		GameInfo(R.string.playing_menu_game_info), Enemy(R.string.playing_menu_enemy), My(R.string.playing_menu_my),

		DEBUG_EnemyMarks(R.string.playing_menu_my),

		;

		private final int code;
		private String text;

		private Menu(int code) {
			this.code = code;
		}

		private void updateText() {
			text = LanguageClass.getString(code);
		}
	}

	private static final Menu[] activeMenus;

	static {
		if (ComputerAI.DEBUG_AI_SHOW_SCREEN)
			activeMenus = new Menu[] { Menu.GameInfo, Menu.DEBUG_EnemyMarks, Menu.Enemy, Menu.My };
		else
			activeMenus = new Menu[] { Menu.GameInfo, Menu.Enemy, Menu.My };
	}

	public enum PopupType {
		EasyClose, Normal, Wait, Finish, Back;
	}

	private static class Popup {
		public PopupType type;
		public double remainingTime;
		public final List<String> text;
		public boolean showMissiles;
		public final List<KindShot> missilesList;
		public RectF missleRect;
		public MyButton rect, closeRect;

		public Popup() {
			text = new ArrayList<String>();
			missilesList = new ArrayList<KindShot>();
		}
	}

	private static class ShootingAnimation {
		public double x, y, dx, dy;
		public final double ax, ay;
		public final KindShot kindShot;
		private final boolean yourTurn;

		public ShootingAnimation(double x, double y, double ax, double ay, boolean yourTurn, KindShot kindShot) {
			this.x = x;
			this.y = y;
			this.dx = dy = 0;
			this.ax = ax;
			this.ay = ay;
			this.yourTurn = yourTurn;
			this.kindShot = kindShot;
		}
	}

	private static final List<Coordinate> waterPiecesList = new ArrayList<Coordinate>(1);
	private static final Mark[][] MINI_MARKS_FILL, MINI_MARKS_CLEAR;
	static {
		waterPiecesList.add(new Coordinate(0, 0));

		final Mark s = Mark.Ship;
		final Mark w = Mark.Water;
		final Mark n = Mark.None;
		MINI_MARKS_FILL = new Mark[][] { { w, w, w, w, w }, { w, s, s, s, w }, { n, w, s, w, n }, { n, w, s, w, n },
				{ n, w, w, w, n } };
		MINI_MARKS_CLEAR = new Mark[][] { { n, n, n, n, n }, { n, s, s, s, n }, { n, n, s, n, n }, { n, n, s, n, n },
				{ n, n, n, n, n } };
	}

	private static Paint CENTER_TEXT_PAINT, LEFT_TEXT_PAINT, RIGHT_TEXT_PAINT, RIGHT_TEXT_SMALL_PAINT,
			WHICH_MENU_PAINT, WHICH_MENU_BULLET_PAINT, WHICH_MENU_SELECTED_BULLET_PAINT;
	private static Paint FIELD_PEN, BLACK_PEN;
	private static Paint WATER_PAINT, WATER100_PAINT, SHIP_PAINT, SHIP100_PAINT, SHIP_GOOD_PAINT, SHIP_BAD_PAINT,
			TRANSPARENT_PAINT, BACKGROUND_PAINT, DESTROYED_SHIP_PAINT;
	private static Paint PLACED_TARGETS_PAINT, LAST_PLAY_PAINT, LAST_PLAY_TEXT_CENTER_PAINT, LAST_PLAY_TEXT_LEFT_PAINT,
			POSITION_SELECTOR_PAINT, POSITION_SELECTOR_P2_PAINT, IMAGE_PAINT, IMAGE_50_PAINT;

	private static Bitmap MISSILE_IMAGE, MISSILE_INDESTRUCTIBLE_IMAGE, MINI_MISSILE_IMAGE,
			MINI_MISSILE_INDESTRUCTIBLE_IMAGE, SHIELD_IMAGE;
	private static Bitmap FIRE_IMAGE, UPSIDE_DOWN_FIRE_IMAGE, POPUP_CLOSE_IMAGE, ARROW_TOP_IMAGE, ARROW_TOP_RED_IMAGE;

	private static Rect[] MINI_FIRE_SOURCE;
	private static RectF MINI_FIRE_AREA;

	private RectF TIME_TURN_AREA, WHICH_MENU_AREA, FIELD_AREA, FIELD_SQUARE_AREA, MESSAGES_AREA, MY_INFO_AREA;
	private RectF SIDE_MISSILES_AREA, POPUP_SMALL_MISSILES_AREA, POPUP_MEDIUM_MISSILES_AREA, MARKS_SQUARE_POPUP_AREA,
			GAME_INFO_AREA, SHIELD_AREA;

	private MyButton POPUP_SMALL_AREA, POPUP_MEDIUM_AREA;
	private MyButton TARGET_BUTTON, LAUNCH_BUTTON, POPUP_SMALL_CLOSE_BUTTON, POPUP_MEDIUM_CLOSE_BUTTON, REMATCH_BUTTON,
			BACK_TO_MAINMENU_BUTTON, EXIT_GAME_YES_BUTTON, EXIT_GAME_NO_BUTTON, TOP_MESSAGE_BUTTON,
			MARKS_POPUP_BUTTONS;

	private static final long TIME_SWIPE_THRESHOLD = 600;
	private final float SWIPE_X_THRESHOLD;

	private static final long TIME_MARKS_POPUP = 200;
	private static final double SHOOTING_ANIMATION_TIME = 3.4;
	private static final double SHOOTING_PULSE_TICK_TIME = 0.1;
	private static double shootingPulseTime;
	private static long shootingPulse;

	private float NORMAL_TEXT_HEIGHT, SMALL_TEXT_HEIGHT;

	private final float maxX;
	private final float maxY;
	private final Player GUIplayer;
	private final Game game;
	private GameState currentState;
	private TurnState turnState;
	private final Callback finishGameCallBack;

	private Menu menu;
	private boolean swipeTouchDown;
	private float firstX, firstY, lastX, lastY;
	private int lastGX, lastGY;
	private long lastTime;
	private int fleetMAXX, fleetMAXY, fleetSS;
	private float MESSAGES_TEXT_WIDTH, MESSAGES_TEXT_HEIGHT, MESSAGES_PLUS_Y;
	private float messagesYOffset;
	private int lastMessagesSize;
	private boolean touchDownMessages, newMessage;

	private float fieldInitX, fieldInitY;
	private int fieldSquareSize;
	private double pulseTime;
	private int pulse;
	private final Queue<Popup> pocketPopups;
	private final Queue<Popup> activePopups;
	private boolean marksPopupDown, showMarksPopup;
	private float MARKS_POPUP_SIZE, MINI_MARKS_POPUP_SIZE, marksPopupTargetX, marksPopupTargetY, marksPopupLineX,
			marksPopupLineY, marksPopupLineX2, marksPopupLineY2;
	private boolean yourTurn;

	private boolean showSAnimation;
	private double sAnimationTime;
	private final List<ShootingAnimation> sAnimationList;

	public PlayingScreen(int maxX, int maxY, Player GUIplayer, Game game, Callback finishGameCallBack) {
		this.maxX = maxX;
		this.maxY = maxY;
		this.GUIplayer = GUIplayer;
		this.game = game;
		this.currentState = this.game.getGameState();
		this.turnState = this.game.getTurnState();
		this.yourTurn = this.game.getCurrentPlayer() == this.GUIplayer;
		this.finishGameCallBack = finishGameCallBack;

		menu = Menu.Enemy;

		swipeTouchDown = false;
		firstX = firstY = lastX = lastY = lastTime = 0;
		lastGX = lastGY = 0;

		messagesYOffset = 0;
		touchDownMessages = false;
		newMessage = false;

		pulseTime = 0f;
		pulse = 0;

		activePopups = new LinkedList<Popup>();
		pocketPopups = new LinkedList<Popup>();

		SWIPE_X_THRESHOLD = maxX / 3;

		showMarksPopup = false;

		sAnimationList = new ArrayList<ShootingAnimation>();
		shootingPulse = 0;

		initializeAreas();
	}

	public static void initializeScreenMultiplier(Context context, float SCREEN_SUPPORT_MULTIPLIER) {
		for (ConstantScaled constant : CONSTANTS_LIST) {
			constant.value *= SCREEN_SUPPORT_MULTIPLIER;
		}

		Typeface typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL);

		CENTER_TEXT_PAINT = new Paint(Paint.ANTI_ALIAS_FLAG);
		CENTER_TEXT_PAINT.setStyle(Style.FILL);
		CENTER_TEXT_PAINT.setTextAlign(Paint.Align.CENTER);
		CENTER_TEXT_PAINT.setColor(COLOR_TEXT);
		CENTER_TEXT_PAINT.setTextSize(TOP_MENU_TEXT_SIZE.value);
		CENTER_TEXT_PAINT.setTypeface(typeface);

		LEFT_TEXT_PAINT = new Paint(CENTER_TEXT_PAINT);
		LEFT_TEXT_PAINT.setTextAlign(Paint.Align.LEFT);

		RIGHT_TEXT_PAINT = new Paint(CENTER_TEXT_PAINT);
		RIGHT_TEXT_PAINT.setTextAlign(Paint.Align.RIGHT);

		RIGHT_TEXT_SMALL_PAINT = new Paint(CENTER_TEXT_PAINT);
		RIGHT_TEXT_SMALL_PAINT.setTextAlign(Paint.Align.RIGHT);
		RIGHT_TEXT_SMALL_PAINT.setTextSize(TOP_MENU_SMALL_TEXT_SIZE.value);

		WHICH_MENU_PAINT = new Paint(CENTER_TEXT_PAINT);
		WHICH_MENU_PAINT.setColor(COLOR_WHICH_MENU_TEXT);
		WHICH_MENU_PAINT.setTextSize(WHICH_MENU_TEXT_SIZE.value);

		FIELD_PEN = new Paint();
		FIELD_PEN.setStyle(Style.STROKE);
		FIELD_PEN.setStrokeWidth(FIELD_PAINT_WIDTH.value);
		FIELD_PEN.setColor(Color.BLACK);

		BLACK_PEN = new Paint(FIELD_PEN);

		WATER_PAINT = new Paint();
		WATER_PAINT.setStyle(Style.FILL);
		WATER_PAINT.setColor(COLOR_WATER);

		WATER100_PAINT = new Paint();
		WATER100_PAINT.setStyle(Style.FILL);
		WATER100_PAINT.setColor(COLOR_WATER100);

		SHIP_PAINT = new Paint();
		SHIP_PAINT.setStyle(Style.FILL);
		SHIP_PAINT.setColor(COLOR_SHIP);

		SHIP100_PAINT = new Paint();
		SHIP100_PAINT.setStyle(Style.FILL);
		SHIP100_PAINT.setColor(COLOR_SHIP100);

		SHIP_GOOD_PAINT = new Paint(SHIP_PAINT);
		SHIP_GOOD_PAINT.setColor(COLOR_SHIP_GOOD);

		SHIP_BAD_PAINT = new Paint(SHIP_PAINT);
		SHIP_BAD_PAINT.setColor(COLOR_SHIP_BAD);

		TRANSPARENT_PAINT = new Paint();
		TRANSPARENT_PAINT.setStyle(Style.FILL);
		TRANSPARENT_PAINT.setAlpha(0);

		BACKGROUND_PAINT = new Paint(SHIP_PAINT);
		BACKGROUND_PAINT.setColor(COLOR_BACKGROUND);

		DESTROYED_SHIP_PAINT = new Paint();
		DESTROYED_SHIP_PAINT.setStyle(Style.STROKE);
		DESTROYED_SHIP_PAINT.setStrokeWidth(DESTROYED_SHIP_STROKE_WIDTH.value);
		DESTROYED_SHIP_PAINT.setColor(COLOR_DESTROYED_SHIP);

		PLACED_TARGETS_PAINT = new Paint(Paint.ANTI_ALIAS_FLAG);
		PLACED_TARGETS_PAINT.setStyle(Style.STROKE);
		PLACED_TARGETS_PAINT.setStrokeWidth(PLACED_TARGETS_STROKE_WIDTH.value);
		PLACED_TARGETS_PAINT.setColor(Color.BLACK);

		LAST_PLAY_PAINT = new Paint(Paint.ANTI_ALIAS_FLAG);
		LAST_PLAY_PAINT.setStyle(Style.STROKE);
		LAST_PLAY_PAINT.setStrokeWidth(LAST_PLAY_STROKE_WIDTH.value);
		LAST_PLAY_PAINT.setPathEffect(new DashPathEffect(new float[] { LAST_PLAY_DASH_EFFECT_SIZE.value * 2,
				LAST_PLAY_DASH_EFFECT_SIZE.value }, 0));
		LAST_PLAY_PAINT.setColor(COLOR_LAST_PLAY_TEXT);

		LAST_PLAY_TEXT_CENTER_PAINT = new Paint(CENTER_TEXT_PAINT);
		LAST_PLAY_TEXT_CENTER_PAINT.setColor(COLOR_LAST_PLAY_TEXT);

		LAST_PLAY_TEXT_LEFT_PAINT = new Paint(LEFT_TEXT_PAINT);
		LAST_PLAY_TEXT_LEFT_PAINT.setColor(COLOR_LAST_PLAY_TEXT);

		POSITION_SELECTOR_PAINT = new Paint(Paint.ANTI_ALIAS_FLAG);
		POSITION_SELECTOR_PAINT.setStyle(Style.STROKE);
		POSITION_SELECTOR_PAINT.setStrokeWidth(POSITION_SELECTOR_STROKE_WIDTH.value);
		POSITION_SELECTOR_PAINT.setColor(Color.BLACK);

		POSITION_SELECTOR_P2_PAINT = new Paint(POSITION_SELECTOR_PAINT);
		POSITION_SELECTOR_P2_PAINT.setStrokeWidth(POSITION_SELECTOR_P2_STROKE_WIDTH.value);

		WHICH_MENU_BULLET_PAINT = new Paint(Paint.ANTI_ALIAS_FLAG);
		WHICH_MENU_BULLET_PAINT.setStyle(Style.FILL);
		WHICH_MENU_BULLET_PAINT.setStrokeWidth(WHICH_MENU_BULLET_SIZE.value);
		WHICH_MENU_BULLET_PAINT.setColor(COLOR_WHICH_MENU_BULLET);

		WHICH_MENU_SELECTED_BULLET_PAINT = new Paint(Paint.ANTI_ALIAS_FLAG);
		WHICH_MENU_SELECTED_BULLET_PAINT.setStyle(Style.FILL);
		WHICH_MENU_SELECTED_BULLET_PAINT.setStrokeWidth(WHICH_MENU_SELECTED_BULLET_SIZE.value);
		WHICH_MENU_SELECTED_BULLET_PAINT.setColor(COLOR_WHICH_MENU_SELECTED_BULLET);

		IMAGE_PAINT = new Paint(Paint.ANTI_ALIAS_FLAG);
		IMAGE_PAINT.setStyle(Style.FILL);

		IMAGE_50_PAINT = new Paint(IMAGE_PAINT);
		IMAGE_50_PAINT.setAlpha((int) (255 * .5f));

		final Resources res = context.getResources();
		Bitmap missileImage = BitmapFactory.decodeResource(res, R.drawable.missile_image);
		MISSILE_IMAGE = Bitmap.createScaledBitmap(missileImage, (int) MISSILES_IMAGES_SIZE_X.value,
				(int) MISSILES_IMAGES_SIZE_Y.value, false);
		MINI_MISSILE_IMAGE = Bitmap.createScaledBitmap(missileImage, (int) MINI_MISSILES_IMAGES_SIZE_X.value,
				(int) MINI_MISSILES_IMAGES_SIZE_Y.value, false);

		Bitmap indestructibleMissileImage = BitmapFactory.decodeResource(res, R.drawable.missile_image_indestructible);
		MISSILE_INDESTRUCTIBLE_IMAGE = Bitmap.createScaledBitmap(indestructibleMissileImage,
				(int) MISSILES_IMAGES_SIZE_X.value, (int) MISSILES_IMAGES_SIZE_Y.value, false);
		MINI_MISSILE_INDESTRUCTIBLE_IMAGE = Bitmap.createScaledBitmap(indestructibleMissileImage,
				(int) MINI_MISSILES_IMAGES_SIZE_X.value, (int) MINI_MISSILES_IMAGES_SIZE_Y.value, false);

		UPSIDE_DOWN_MISSILE_MATRIX = new Matrix();
		UPSIDE_DOWN_MISSILE_MATRIX.setRotate(180, MINI_MISSILES_IMAGES_SIZE_X.value / 2,
				MINI_MISSILES_IMAGES_SIZE_Y.value / 2);

		Bitmap fireImage = BitmapFactory.decodeResource(res, R.drawable.missile_fire);
		FIRE_IMAGE = Bitmap.createScaledBitmap(fireImage,
				(int) MINI_FIRE_IMAGES_SIZE_X.value * MINI_FIRE_IMAGES_FRAMES, (int) MINI_FIRE_IMAGES_SIZE_Y.value,
				false);
		MINI_FIRE_SOURCE = new Rect[MINI_FIRE_IMAGES_FRAMES];
		for (int i = 0; i < MINI_FIRE_IMAGES_FRAMES; i++) {
			final int left = (int) MINI_FIRE_IMAGES_SIZE_X.value * i;
			final int top = 0;
			final int right = (int) MINI_FIRE_IMAGES_SIZE_X.value * (i + 1);
			final int bottom = (int) MINI_FIRE_IMAGES_SIZE_Y.value;
			MINI_FIRE_SOURCE[i] = new Rect(left, top, right, bottom);
		}
		MINI_FIRE_AREA = new RectF(0, 0, MINI_FIRE_IMAGES_SIZE_X.value, MINI_FIRE_IMAGES_SIZE_Y.value);

		Bitmap fireImageUpsideDown = BitmapFactory.decodeResource(res, R.drawable.missile_fire_upsidedown);
		UPSIDE_DOWN_FIRE_IMAGE = Bitmap.createScaledBitmap(fireImageUpsideDown, (int) MINI_FIRE_IMAGES_SIZE_X.value
				* MINI_FIRE_IMAGES_FRAMES, (int) MINI_FIRE_IMAGES_SIZE_Y.value, false);

		SHIELD_IMAGE = BitmapFactory.decodeResource(res, R.drawable.shield);

		POPUP_CLOSE_IMAGE = BitmapFactory.decodeResource(res, R.drawable.close_popup_4);

		Bitmap arrowImageBlack = BitmapFactory.decodeResource(res, R.drawable.arrow_top_black);
		ARROW_TOP_IMAGE = Bitmap.createScaledBitmap(arrowImageBlack, (int) TOP_MESSAGE_ARROW_SIZE.value,
				(int) TOP_MESSAGE_ARROW_SIZE.value, false);
		Bitmap arrowImageRed = BitmapFactory.decodeResource(res, R.drawable.arrow_top_red);
		ARROW_TOP_RED_IMAGE = Bitmap.createScaledBitmap(arrowImageRed, (int) TOP_MESSAGE_ARROW_SIZE.value,
				(int) TOP_MESSAGE_ARROW_SIZE.value, false);

		missileImage.recycle();
		indestructibleMissileImage.recycle();
		fireImage.recycle();
		fireImageUpsideDown.recycle();
	}

	private void initializeAreas() {
		for (StringCode stringCode : stringCodeList) {
			stringCode.updateText();
		}

		for (Menu menu : activeMenus) {
			menu.updateText();
		}

		NORMAL_TEXT_HEIGHT = Draw.getStrHeight(CENTER_TEXT_PAINT, "A");
		SMALL_TEXT_HEIGHT = Draw.getStrHeight(RIGHT_TEXT_SMALL_PAINT, "A");

		{
			final float height = Math.max(NORMAL_TEXT_HEIGHT, SMALL_TEXT_HEIGHT * 2 + MINIMUM_PADDING.value);
			TIME_TURN_AREA = new RectF(OUT_PADDING, OUT_PADDING, maxX - OUT_PADDING, OUT_PADDING + height);
		}

		WHICH_MENU_AREA = TIME_TURN_AREA; //new RectF(OUT_PADDING, OUT_PADDING, maxX - OUT_PADDING, OUT_PADDING + NORMAL_TEXT_HEIGHT);

		final int fieldX = game.getCurrentFleet().maxX;
		final int fieldY = game.getCurrentFleet().maxY;

		fieldSquareSize = (int) Math.floor(Math.min((maxX - MINIMUM_PADDING.value * 3 - MISSILES_IMAGES_SIZE_X.value)
				/ fieldX, maxY * 2 / 3 / fieldY));
		fieldInitX = (maxX - MINIMUM_PADDING.value - MISSILES_IMAGES_SIZE_X.value) / 2 - fieldX * fieldSquareSize / 2f;
		fieldInitY = WHICH_MENU_AREA.bottom + OUT_PADDING;

		FIELD_AREA = new RectF(fieldInitX, fieldInitY, fieldInitX + fieldSquareSize * fieldX, fieldInitY
				+ fieldSquareSize * fieldY);

		FIELD_SQUARE_AREA = new RectF(0, 0, fieldSquareSize, fieldSquareSize);

		{
			final float y = (maxY - FIELD_AREA.bottom) / 2 - NORMAL_TEXT_HEIGHT / 2 + FIELD_AREA.bottom;
			TARGET_BUTTON = new MyButton(maxX * 2 / 3, y - OUT_PADDING - IN_PADDING, maxX - OUT_PADDING, y
					+ NORMAL_TEXT_HEIGHT + OUT_PADDING + IN_PADDING);
			LAUNCH_BUTTON = new MyButton(TARGET_BUTTON);
			LAUNCH_BUTTON.offset(0, LAUNCH_BUTTON.height() / 2 + OUT_PADDING / 2);
			TARGET_BUTTON.offset(0, -TARGET_BUTTON.height() / 2 - OUT_PADDING / 2);
		}

		MESSAGES_AREA = new RectF(OUT_PADDING, FIELD_AREA.bottom + OUT_PADDING, TARGET_BUTTON.left - OUT_PADDING, maxY
				- OUT_PADDING);

		MY_INFO_AREA = new RectF(OUT_PADDING, FIELD_AREA.bottom + OUT_PADDING, maxX - OUT_PADDING, maxY - OUT_PADDING);

		{
			{
				final float sizeY = NORMAL_TEXT_HEIGHT + IN_PADDING * 3 + MISSILES_IMAGES_SIZE_Y.value;
				final float y = maxY / 2 - sizeY / 2;
				POPUP_SMALL_AREA = new MyButton(OUT_PADDING, y, maxX - OUT_PADDING, y + sizeY);

				float mx = POPUP_SMALL_AREA.left + IN_PADDING;
				float my = POPUP_SMALL_AREA.bottom - IN_PADDING - MISSILES_IMAGES_SIZE_Y.value;
				POPUP_SMALL_MISSILES_AREA = new RectF(0, 0, MISSILES_IMAGES_SIZE_X.value, MISSILES_IMAGES_SIZE_Y.value);
				POPUP_SMALL_MISSILES_AREA.offset(mx, my);

				final float closeSize = POPUP_CLOSE_MAX_SIZE.value;
				POPUP_SMALL_CLOSE_BUTTON = new MyButton(0, 0, closeSize, closeSize);
				POPUP_SMALL_CLOSE_BUTTON.offset(POPUP_SMALL_AREA.right - POPUP_SMALL_CLOSE_BUTTON.width()
						- BUTTON_STROKE_WIDTH, POPUP_SMALL_AREA.top + BUTTON_STROKE_WIDTH);
			}
			{
				final float sizeY = NORMAL_TEXT_HEIGHT * 2 + IN_PADDING * 4 + MISSILES_IMAGES_SIZE_Y.value;
				final float y = maxY / 2 - sizeY / 2;
				POPUP_MEDIUM_AREA = new MyButton(OUT_PADDING, y, maxX - OUT_PADDING, y + sizeY);

				float mx = POPUP_MEDIUM_AREA.left + IN_PADDING;
				float my = POPUP_MEDIUM_AREA.bottom - IN_PADDING - MISSILES_IMAGES_SIZE_Y.value;
				POPUP_MEDIUM_MISSILES_AREA = new RectF(0, 0, MISSILES_IMAGES_SIZE_X.value, MISSILES_IMAGES_SIZE_Y.value);
				POPUP_MEDIUM_MISSILES_AREA.offset(mx, my);

				final float closeSize = POPUP_CLOSE_MAX_SIZE.value;
				POPUP_MEDIUM_CLOSE_BUTTON = new MyButton(0, 0, closeSize, closeSize);
				POPUP_MEDIUM_CLOSE_BUTTON.offset(POPUP_MEDIUM_AREA.right - POPUP_MEDIUM_CLOSE_BUTTON.width()
						- BUTTON_STROKE_WIDTH, POPUP_MEDIUM_AREA.top + BUTTON_STROKE_WIDTH);
			}
		}

		{
			final float x = POPUP_MEDIUM_AREA.left + OUT_PADDING;
			final float y = POPUP_MEDIUM_AREA.bottom - OUT_PADDING - IN_PADDING * 3 - NORMAL_TEXT_HEIGHT * 2;
			REMATCH_BUTTON = new MyButton(x, y, POPUP_MEDIUM_AREA.centerX() - OUT_PADDING, POPUP_MEDIUM_AREA.bottom
					- OUT_PADDING);
		}
		{
			final float x = POPUP_MEDIUM_AREA.centerX() + OUT_PADDING;
			final float y = POPUP_MEDIUM_AREA.bottom - OUT_PADDING - IN_PADDING * 3 - NORMAL_TEXT_HEIGHT * 2;
			BACK_TO_MAINMENU_BUTTON = new MyButton(x, y, POPUP_MEDIUM_AREA.right - OUT_PADDING,
					POPUP_MEDIUM_AREA.bottom - OUT_PADDING);
		}
		{
			final float x = POPUP_MEDIUM_AREA.left + OUT_PADDING;
			final float y = POPUP_MEDIUM_AREA.bottom - OUT_PADDING - IN_PADDING * 3 - NORMAL_TEXT_HEIGHT * 2;
			EXIT_GAME_YES_BUTTON = new MyButton(x, y, POPUP_MEDIUM_AREA.centerX() - OUT_PADDING,
					POPUP_MEDIUM_AREA.bottom - OUT_PADDING);
		}
		{
			final float x = POPUP_MEDIUM_AREA.centerX() + OUT_PADDING;
			final float y = POPUP_MEDIUM_AREA.bottom - OUT_PADDING - IN_PADDING * 3 - NORMAL_TEXT_HEIGHT * 2;
			EXIT_GAME_NO_BUTTON = new MyButton(x, y, POPUP_MEDIUM_AREA.right - OUT_PADDING, POPUP_MEDIUM_AREA.bottom
					- OUT_PADDING);
		}

		{
			float x = FIELD_AREA.right + MINIMUM_PADDING.value;
			float y = FIELD_AREA.top + MINIMUM_PADDING.value;
			SIDE_MISSILES_AREA = new RectF(x, y, x + MISSILES_IMAGES_SIZE_X.value, y + MISSILES_IMAGES_SIZE_Y.value);
		}

		{
			MARKS_POPUP_SIZE = Math.max(POPUP_MARKS_MIN_SIZE.value,
					Math.min(POPUP_MARKS_MAX_SIZE.value, fieldSquareSize));
			MARKS_SQUARE_POPUP_AREA = new RectF(0, 0, MARKS_POPUP_SIZE, MARKS_POPUP_SIZE);

			MINI_MARKS_POPUP_SIZE = MARKS_POPUP_SIZE / 5;

			MARKS_POPUP_BUTTONS = new MyButton(0, 0, MARKS_POPUP_SIZE, MARKS_POPUP_SIZE * 5);
		}
		{
			float x = OUT_PADDING;
			float y = TIME_TURN_AREA.bottom + OUT_PADDING;
			GAME_INFO_AREA = new RectF(x, y, maxX - OUT_PADDING, maxY - OUT_PADDING);
		}

		fleetMAXX = 0;
		fleetMAXY = 0;
		final Fleet currentFleet = game.getCurrentFleet();
		final List<Integer> fleetNumbers = currentFleet.getFleetNumbers();
		for (int i = 0; i < fleetNumbers.size(); i++) {
			if (fleetNumbers.get(i) > 0) {
				fleetMAXX = Math.max(fleetMAXX, ShipClass.sizeX(i, 0));
				fleetMAXY = Math.max(fleetMAXY, ShipClass.sizeY(i, 0));
			}
		}

		{
			int mY = fieldSquareSize / fleetMAXY;
			int mX = (int) ((MESSAGES_AREA.width() - MESSAGES_TEXT_WIDTH) / ((4 + 3 * MESSAGES_PLUS_X.value) * fleetMAXX));
			fleetSS = Math.min(mX, mY);
			fleetSS = Math.min(fleetSS, fieldSquareSize);
			fleetSS = Math.max(fleetSS, (int) MIN_SQUARE_SIZE_MESSAGES.value);
		}

		MESSAGES_TEXT_WIDTH = Draw.getStrWidth(CENTER_TEXT_PAINT, "E99 ");
		MESSAGES_TEXT_HEIGHT = Draw.getStrHeight(CENTER_TEXT_PAINT, "E99 ");
		MESSAGES_PLUS_Y = Math.max(MESSAGES_TEXT_HEIGHT, fleetMAXY * fleetSS) + .5f * fleetSS;

		{
			final int max = Math.min(fleetMAXX, fleetMAXY);
			SHIELD_AREA = new RectF(0, 0, fleetSS * max, fleetSS * max);
		}
		{
			TOP_MESSAGE_BUTTON = new MyButton(0, 0, TOP_MESSAGE_ARROW_SIZE.value, TOP_MESSAGE_ARROW_SIZE.value);
			TOP_MESSAGE_BUTTON.offset(MESSAGES_AREA.right - OUT_PADDING - TOP_MESSAGE_BUTTON.width(), FIELD_AREA.bottom
					+ OUT_PADDING);
		}
	}

	@Override
	public synchronized void draw(Canvas canvas) {
		final GameMode gameMode = game.getCurrentGameMode();
		{
			String timeStr = getTimeStr(gameMode, GUIplayer);
			canvas.drawText(timeStr, TIME_TURN_AREA.left, TIME_TURN_AREA.centerY() + NORMAL_TEXT_HEIGHT / 2,
					LEFT_TEXT_PAINT);

			Turn_Text_Label: {
				String turnStr;
				if (currentState == GameState.FinishedGame) {
					if (yourTurn)
						turnStr = YOU_WON_TEXT.text;
					else
						turnStr = ENEMY_WON_TEXT.text;
				} else if (currentState == GameState.PlayingInPause) {
					turnStr = GAME_PAUSED_TEXT.text;
				} else {
					turnStr = LanguageClass.format("(%d)", game.getTurnNumber());
					if (yourTurn)
						turnStr = YOUR_TURN_TEXT + " " + turnStr;
					else {
						turnStr = ENEMY_TURN_TEXT + " " + turnStr;

						if (gameMode.getTimeLimitType() != TimeLimitType.NoTimeLimit) {
							String enemyTimeStr = getTimeStr(gameMode, GUIplayer.getEnemy());

							canvas.drawText(turnStr, TIME_TURN_AREA.right, TIME_TURN_AREA.top + SMALL_TEXT_HEIGHT,
									RIGHT_TEXT_SMALL_PAINT);
							canvas.drawText(enemyTimeStr, TIME_TURN_AREA.right, TIME_TURN_AREA.bottom,
									RIGHT_TEXT_SMALL_PAINT);

							break Turn_Text_Label;
						}
					}
				}
				canvas.drawText(turnStr, TIME_TURN_AREA.right, TIME_TURN_AREA.centerY() + SMALL_TEXT_HEIGHT / 2,
						RIGHT_TEXT_SMALL_PAINT);
			}

			canvas.drawText(menu.text, WHICH_MENU_AREA.centerX(), WHICH_MENU_AREA.bottom, WHICH_MENU_PAINT);

			final int nBullets = activeMenus.length;
			float bulletsX = WHICH_MENU_AREA.centerX() - (nBullets - 1) * WHICH_MENU_BULLET_PADDING.value / 2f;
			for (Menu m : activeMenus) {
				float size = menu == m ? WHICH_MENU_SELECTED_BULLET_SIZE.value : WHICH_MENU_BULLET_SIZE.value;
				Paint paint = menu == m ? WHICH_MENU_SELECTED_BULLET_PAINT : WHICH_MENU_BULLET_PAINT;
				canvas.drawCircle(bulletsX, WHICH_MENU_AREA.top, size, paint);
				bulletsX += WHICH_MENU_BULLET_PADDING.value;
			}
		}

		if (menu == Menu.GameInfo) {
			ScreenUtils.drawGameInfo(canvas, game, gameMode, GAME_INFO_AREA, LEFT_TEXT_PAINT, CENTER_TEXT_PAINT, true);
		} else {
			final int fieldX = game.getCurrentFleet().maxX;
			final int fieldY = game.getCurrentFleet().maxY;

			drawField(canvas, fieldSquareSize, fieldX, fieldY);
		}

		drawPopups(canvas);
	}

	private String getTimeStr(final GameMode gameMode, Player player) {
		String timeStr;
		final TimeLimitType timeLimitType = gameMode.getTimeLimitType();
		if (timeLimitType == TimeLimitType.NoTimeLimit) {
			timeStr = StopWatch.format(game.getGameTime());
		} else if (timeLimitType == TimeLimitType.TotalTime) {
			int timeLimit = gameMode.getTimeLimit();
			timeStr = StopWatch.format(timeLimit - player.getWatchTime());
		} else if (timeLimitType == TimeLimitType.TotalTimeAndPerTurn) {
			int timeLimit = gameMode.getTimeLimit();
			timeStr = StopWatch.format(timeLimit - player.getWatchTime());

			int turnLimit;
			if (yourTurn && player == GUIplayer || !yourTurn && player != GUIplayer)
				turnLimit = game.getRemainingTurnTime();
			else
				turnLimit = gameMode.getTimePerTurn();
			timeStr += "+" + StopWatch.formatOnlySecs(turnLimit < 0 ? 0 : turnLimit);
		} else {
			timeStr = "<unknown>";
		}
		return timeStr;
	}

	private void drawPopups(Canvas canvas) {
		if (activePopups.isEmpty())
			return;
		Popup popup = activePopups.peek();
		if (popup.type == PopupType.Wait)
			return;

		final RectF rect = popup.rect;
		final int textLines = popup.text.size();

		canvas.drawRect(rect, NORMAL_BUTTON_PAINT);
		canvas.drawRect(rect, BORDER_BUTTON_PAINT);

		if (textLines > 0) {
			float y = rect.top + IN_PADDING + NORMAL_TEXT_HEIGHT;
			canvas.drawText(popup.text.get(0), rect.centerX(), y, CENTER_TEXT_PAINT);
			if (textLines > 1) {
				y += NORMAL_TEXT_HEIGHT + IN_PADDING;
				canvas.drawText(popup.text.get(1), rect.centerX(), y, CENTER_TEXT_PAINT);
			}
		}

		if (popup.showMissiles) {
			final RectF missileRect = popup.missleRect;
			canvas.translate(missileRect.left, missileRect.top);
			for (int i = 0; i < popup.missilesList.size(); i++) {
				canvas.drawBitmap(getMissileImage(popup.missilesList.get(i)), MISSILES_IMAGES_SIZE_X.value * i * 2, 0,
						IMAGE_PAINT);
			}
			canvas.translate(-missileRect.left, -missileRect.top);
		}
		if (popup.type == PopupType.Finish) {
			drawButton(canvas, REMATCH_TEXT.text, REMATCH_BUTTON, CENTER_TEXT_PAINT);
			drawButton(canvas, BACK_TO_MAINMENU_TEXT.text, BACK_TO_MAINMENU_BUTTON, CENTER_TEXT_PAINT);
		} else if (popup.type == PopupType.Back) {
			drawButton(canvas, EXIT_GAME_YES_TEXT.text, EXIT_GAME_YES_BUTTON, CENTER_TEXT_PAINT);
			drawButton(canvas, EXIT_GAME_NO_TEXT.text, EXIT_GAME_NO_BUTTON, CENTER_TEXT_PAINT);
		}

		{
			canvas.drawBitmap(POPUP_CLOSE_IMAGE, null, popup.closeRect, IMAGE_PAINT);
		}
	}

	private void drawField(Canvas canvas, int ss, int fieldX, int fieldY) {
		final Player player = game.getPlayer1();
		final Player enemy = player.getEnemy();

		if (menu == Menu.My) {
			canvas.drawRect(fieldInitX, fieldInitY, fieldInitX + ss * fieldX, fieldInitY + ss * fieldY, WATER_PAINT);

			final List<Message> messages = enemy.getMessagesLock();
			final Message lastMessage = messages.isEmpty() ? null : messages.get(0);
			enemy.getMessagesUnlock();

			if (lastMessage != null) {
				final List<Coordinate> counters = lastMessage.getCounter();
				for (Coordinate coor : counters) {
					final float x1 = fieldInitX + coor.x * ss;
					final float y1 = fieldInitY + coor.y * ss;
					canvas.translate(x1, y1);
					canvas.drawBitmap(SHIELD_IMAGE, null, FIELD_SQUARE_AREA, IMAGE_50_PAINT);
					canvas.translate(-x1, -y1);
				}
			}

			for (int x = 0; x < fieldX; x++) {
				for (int y = 0; y < fieldY; y++) {
					final float x1 = fieldInitX + x * ss;
					final float y1 = fieldInitY + y * ss;
					final float x2 = x1 + ss;
					final float y2 = y1 + ss;

					if (player.shipAt(x, y) != null) {
						canvas.drawRect(x1, y1, x2, y2, SHIP_PAINT);
					}
					final Message messageAt = enemy.messageAt(x, y);
					if (messageAt != null) {
						final boolean lastPlay;
						lastPlay = messageAt == lastMessage;

						final String turnId = messageAt.getTurnId();
						final float fx = x1 + .5f * ss;
						final float fy = y2 - OUT_PADDING;
						canvas.drawText(turnId, fx, fy, lastPlay ? LAST_PLAY_TEXT_CENTER_PAINT : CENTER_TEXT_PAINT);

						if (lastPlay) {
							canvas.drawRect(x1, y1, x2, y2, LAST_PLAY_PAINT);
						}
					}
				}
			}

			int nShipsLost = enemy.getStatistics().getTotalSunkedShips();
			int totalShips = player.getStatistics().getTotalShips();
			final String destroyedStr = LanguageClass.format(MY_INFO_SHIPS_LOST_TEXT.text, nShipsLost, totalShips);
			canvas.drawText(destroyedStr, MY_INFO_AREA.centerX(), MY_INFO_AREA.centerY() - MESSAGES_TEXT_HEIGHT / 2,
					CENTER_TEXT_PAINT);
		} else if (menu == Menu.Enemy) {
			final List<Message> messages = player.getMessagesLock();
			final Message lastMessage = messages.isEmpty() ? null : messages.get(0);

			if (lastMessage != null) {
				final List<Coordinate> counters = lastMessage.getCounter();
				for (Coordinate coor : counters) {
					final float x1 = fieldInitX + coor.x * ss;
					final float y1 = fieldInitY + coor.y * ss;
					canvas.translate(x1, y1);
					canvas.drawBitmap(SHIELD_IMAGE, null, FIELD_SQUARE_AREA, IMAGE_50_PAINT);
					canvas.translate(-x1, -y1);
				}
			}

			for (int x = 0; x < fieldX; x++) {
				for (int y = 0; y < fieldY; y++) {
					final float x1 = fieldInitX + x * ss;
					final float y1 = fieldInitY + y * ss;
					final float x2 = x1 + ss;
					final float y2 = y1 + ss;

					final Mark markAt = player.markAt(x, y);
					if (markAt != Mark.None) {
						final Paint markPaint;
						if (markAt.isWater())
							markPaint = markAt == Mark.Water ? WATER_PAINT : WATER100_PAINT;
						else
							markPaint = markAt == Mark.Ship ? SHIP_PAINT : SHIP100_PAINT;

						canvas.drawRect(x1, y1, x2, y2, markPaint);
					}
					final Message messageAt = player.messageAt(x, y);
					if (messageAt != null) {
						final boolean lastPlay = messageAt == lastMessage;

						final String turnId = messageAt.getTurnId();
						final float fx = x1 + .5f * ss;
						final float fy = y2 - OUT_PADDING;
						canvas.drawText(turnId, fx, fy, lastPlay ? LAST_PLAY_TEXT_CENTER_PAINT : CENTER_TEXT_PAINT);

						if (lastPlay) {
							canvas.drawRect(x1, y1, x2, y2, LAST_PLAY_PAINT);
						}
					}
				}
			}

			boolean shotAtPosition = false;
			{
				final List<Shot> turnTargets = player.getTurnTargets();
				for (int i = 0; i < turnTargets.size(); i++) {
					Shot shot = turnTargets.get(i);
					final boolean isPlaced = shot.isPlaced();
					if (isPlaced) {
						if (shot.getCoordinate().equals(player.getPosition()))
							shotAtPosition = true;

						for (final Coordinate coor : shot.getValidShots()) {
							final float x1 = fieldInitX + coor.x * ss;
							final float y1 = fieldInitY + coor.y * ss;
							final float x2 = x1 + ss;
							final float y2 = y1 + ss;
							canvas.drawRect(x1, y1, x2, y2, PLACED_TARGETS_PAINT);
							canvas.drawLine(x1, y1, x2, y2, PLACED_TARGETS_PAINT);
							canvas.drawLine(x2, y1, x1, y2, PLACED_TARGETS_PAINT);
						}
					}

					Paint paint = isPlaced ? IMAGE_50_PAINT : IMAGE_PAINT;
					float y = (MISSILES_IMAGES_SIZE_Y.value + MINIMUM_PADDING.value) * i;
					canvas.drawBitmap(getMissileImage(shot.getKindShot()), SIDE_MISSILES_AREA.left, y
							+ SIDE_MISSILES_AREA.top, paint);
				}
				player.unlockTurnTargets();
			}

			// Draw current 'position' marker
			{
				final float x1 = fieldInitX + player.getPositionX() * ss;
				final float y1 = fieldInitY + player.getPositionY() * ss;
				final float x2 = x1 + ss;
				final float y2 = y1 + ss;
				canvas.drawRect(x1, y1, x2, y2, pulse % 2 == 0 ? POSITION_SELECTOR_PAINT : POSITION_SELECTOR_P2_PAINT);
			}

			// Draw Messages
			{
				canvas.save();
				canvas.clipRect(MESSAGES_AREA);

				float msgX = MESSAGES_AREA.left;
				float msgY = MESSAGES_AREA.top + MESSAGES_PLUS_Y / 2 - messagesYOffset;
				for (Message message : messages) {

					final boolean lastPlay = lastMessage == message;

					canvas.drawText(message.getTurnId() + "    ", msgX, msgY + MESSAGES_TEXT_HEIGHT / 2,
							lastPlay ? LAST_PLAY_TEXT_LEFT_PAINT : LEFT_TEXT_PAINT);

					float msgUnitX = msgX + MESSAGES_TEXT_WIDTH;
					float msgUnitY = msgY;
					for (MessageUnit messageUnit : message.getParts()) {
						int sx;
						if (messageUnit.type == TypesMessageUnits.Water) {
							sx = 1;
							drawShip(canvas, msgUnitX, msgUnitY - fleetSS / 2, fleetSS, waterPiecesList, WATER_PAINT);
						} else if (messageUnit.type == TypesMessageUnits.Counter) {
							sx = 3;
							canvas.translate(msgUnitX, msgUnitY - SHIELD_AREA.height() / 2);
							canvas.drawBitmap(SHIELD_IMAGE, null, SHIELD_AREA, IMAGE_50_PAINT);
							canvas.translate(-msgUnitX, -(msgUnitY - SHIELD_AREA.height() / 2));
						} else {
							sx = ShipClass.sizeX(messageUnit.shipId, 0);
							int sy = ShipClass.sizeY(messageUnit.shipId, 0);
							float x1 = msgUnitX;
							float y1 = msgUnitY - (sy * fleetSS) / 2;
							final List<Coordinate> listPieces = ShipClass.getShipParts(messageUnit.shipId, 0);
							drawShip(canvas, msgUnitX, y1, fleetSS, listPieces, SHIP_PAINT);

							if (messageUnit.type == TypesMessageUnits.AKillerShot) {
								float x2 = x1 + sx * fleetSS;
								float y2 = y1 + sy * fleetSS;
								canvas.drawLine(x1, y1, x2, y2, DESTROYED_SHIP_PAINT);
								canvas.drawLine(x2, y1, x1, y2, DESTROYED_SHIP_PAINT);
							}
						}
						msgUnitX += (sx + MESSAGES_PLUS_X.value) * fleetSS;
					}

					msgY += MESSAGES_PLUS_Y;
				}
				canvas.restore();
				if (messagesYOffset > 0) {
					Bitmap image = newMessage ? ARROW_TOP_RED_IMAGE : ARROW_TOP_IMAGE;
					canvas.drawBitmap(image, TOP_MESSAGE_BUTTON.left, TOP_MESSAGE_BUTTON.top, IMAGE_PAINT);
					canvas.drawRect(TOP_MESSAGE_BUTTON, BLACK_PEN);
				}

				// Draw Target & Launch buttons
				if (shotAtPosition) {
					drawButton(canvas, UNDO_TEXT.text, TARGET_BUTTON, CENTER_TEXT_PAINT);
				} else if (!player.allShotsPlaced()) {
					drawButton(canvas, TARGET_TEXT.text, TARGET_BUTTON, CENTER_TEXT_PAINT);
				}
				if (player.allShotsPlaced() && yourTurn && currentState == GameState.Playing
						&& turnState == TurnState.ChooseTargets) {
					drawButton(canvas, LAUNCH_TEXT.text, LAUNCH_BUTTON, CENTER_TEXT_PAINT);
				}
			}
			player.getMessagesUnlock();
		} else if (menu == Menu.DEBUG_EnemyMarks) {
			final List<Message> messages = enemy.getMessagesLock();
			final Message lastMessage = messages.isEmpty() ? null : messages.get(0);

			if (lastMessage != null) {
				final List<Coordinate> counters = lastMessage.getCounter();
				for (Coordinate coor : counters) {
					final float x1 = fieldInitX + coor.x * ss;
					final float y1 = fieldInitY + coor.y * ss;
					canvas.translate(x1, y1);
					canvas.drawBitmap(SHIELD_IMAGE, null, FIELD_SQUARE_AREA, IMAGE_50_PAINT);
					canvas.translate(-x1, -y1);
				}
			}

			for (int x = 0; x < fieldX; x++) {
				for (int y = 0; y < fieldY; y++) {
					final float x1 = fieldInitX + x * ss;
					final float y1 = fieldInitY + y * ss;
					final float x2 = x1 + ss;
					final float y2 = y1 + ss;

					final Mark markAt = enemy.markAt(x, y);
					if (markAt != Mark.None) {
						final Paint markPaint;
						if (markAt.isWater())
							markPaint = markAt == Mark.Water ? WATER_PAINT : WATER100_PAINT;
						else
							markPaint = markAt == Mark.Ship ? SHIP_PAINT : SHIP100_PAINT;
						canvas.drawRect(x1, y1, x2, y2, markPaint);
					}
					final Message messageAt = enemy.messageAt(x, y);
					if (messageAt != null) {
						final boolean lastPlay = messageAt == lastMessage;

						final String turnId = messageAt.getTurnId();
						final float fx = x1 + .5f * ss;
						final float fy = y2 - OUT_PADDING;
						canvas.drawText(turnId, fx, fy, lastPlay ? LAST_PLAY_TEXT_CENTER_PAINT : CENTER_TEXT_PAINT);

						if (lastPlay) {
							canvas.drawRect(x1, y1, x2, y2, LAST_PLAY_PAINT);
						}
					}
				}
			}

			{
				final List<Shot> turnTargets = enemy.getTurnTargets();
				for (int i = 0; i < turnTargets.size(); i++) {
					Shot shot = turnTargets.get(i);
					final boolean isPlaced = shot.isPlaced();
					if (isPlaced) {
						for (final Coordinate coor : shot.getValidShots()) {
							final float x1 = fieldInitX + coor.x * ss;
							final float y1 = fieldInitY + coor.y * ss;
							final float x2 = x1 + ss;
							final float y2 = y1 + ss;
							canvas.drawRect(x1, y1, x2, y2, PLACED_TARGETS_PAINT);
							canvas.drawLine(x1, y1, x2, y2, PLACED_TARGETS_PAINT);
							canvas.drawLine(x2, y1, x1, y2, PLACED_TARGETS_PAINT);
						}
					}

					Paint paint = isPlaced ? IMAGE_50_PAINT : IMAGE_PAINT;
					float y = (MISSILES_IMAGES_SIZE_Y.value + MINIMUM_PADDING.value) * i;
					canvas.drawBitmap(getMissileImage(shot.getKindShot()), SIDE_MISSILES_AREA.left, y
							+ SIDE_MISSILES_AREA.top, paint);
				}
				enemy.unlockTurnTargets();
			}

			// Draw Messages
			{
				canvas.save();
				canvas.clipRect(MESSAGES_AREA);

				float msgX = MESSAGES_AREA.left;
				float msgY = MESSAGES_AREA.top + MESSAGES_PLUS_Y / 2;
				for (Message message : messages) {
					//					if (msgY < MESSAGES_AREA.top - MESSAGES_PLUS_Y)
					//						continue;
					//					if (msgY > MESSAGES_AREA.bottom + MESSAGES_PLUS_Y)
					//						break;

					final boolean lastPlay = lastMessage == message;

					canvas.drawText(message.getTurnId() + "    ", msgX, msgY + MESSAGES_TEXT_HEIGHT / 2,
							lastPlay ? LAST_PLAY_TEXT_LEFT_PAINT : LEFT_TEXT_PAINT);

					float msgUnitX = msgX + MESSAGES_TEXT_WIDTH;
					float msgUnitY = msgY;
					for (MessageUnit messageUnit : message.getParts()) {
						int sx;
						if (messageUnit.type == TypesMessageUnits.Water) {
							sx = 1;
							drawShip(canvas, msgUnitX, msgUnitY - fleetSS / 2, fleetSS, waterPiecesList, WATER_PAINT);
						} else if (messageUnit.type == TypesMessageUnits.Counter) {
							sx = 3;
							canvas.translate(msgUnitX, msgUnitY - SHIELD_AREA.height() / 2);
							canvas.drawBitmap(SHIELD_IMAGE, null, SHIELD_AREA, IMAGE_50_PAINT);
							canvas.translate(-msgUnitX, -(msgUnitY - SHIELD_AREA.height() / 2));
						} else {
							sx = ShipClass.sizeX(messageUnit.shipId, 0);
							int sy = ShipClass.sizeY(messageUnit.shipId, 0);
							float x1 = msgUnitX;
							float y1 = msgUnitY - (sy * fleetSS) / 2;
							final List<Coordinate> listPieces = ShipClass.getShipParts(messageUnit.shipId, 0);
							drawShip(canvas, msgUnitX, y1, fleetSS, listPieces, SHIP_PAINT);

							if (messageUnit.type == TypesMessageUnits.AKillerShot) {
								float x2 = x1 + sx * fleetSS;
								float y2 = y1 + sy * fleetSS;
								canvas.drawLine(x1, y1, x2, y2, DESTROYED_SHIP_PAINT);
								canvas.drawLine(x2, y1, x1, y2, DESTROYED_SHIP_PAINT);
							}
						}
						msgUnitX += (sx + MESSAGES_PLUS_X.value) * fleetSS;
					}

					msgY += MESSAGES_PLUS_Y;
				}
				canvas.restore();
			}
			enemy.getMessagesUnlock();
		}

		for (int i = 0; i <= fieldX; i++)
			canvas.drawLine(fieldInitX + ss * i, fieldInitY, fieldInitX + ss * i, fieldInitY + ss * fieldY, FIELD_PEN);
		for (int i = 0; i <= fieldY; i++)
			canvas.drawLine(fieldInitX, fieldInitY + ss * i, fieldInitX + ss * fieldX, fieldInitY + ss * i, FIELD_PEN);

		if (menu == Menu.Enemy) {
			if (showMarksPopup) {
				//Paint paint = pulse % 2 == 1 ? POSITION_SELECTOR_PAINT : POSITION_SELECTOR_P2_PAINT;//BUTTON_PAINT;
				Paint paint = POSITION_SELECTOR_PAINT;

				canvas.translate(MARKS_POPUP_BUTTONS.left, MARKS_POPUP_BUTTONS.top);

				canvas.drawRect(MARKS_SQUARE_POPUP_AREA, SHIP_PAINT);
				canvas.drawRect(MARKS_SQUARE_POPUP_AREA, paint);

				canvas.translate(0, MARKS_POPUP_SIZE);
				canvas.drawRect(MARKS_SQUARE_POPUP_AREA, WATER_PAINT);
				canvas.drawRect(MARKS_SQUARE_POPUP_AREA, paint);

				canvas.translate(0, MARKS_POPUP_SIZE);
				canvas.drawRect(MARKS_SQUARE_POPUP_AREA, BACKGROUND_PAINT);
				canvas.drawRect(MARKS_SQUARE_POPUP_AREA, paint);

				canvas.translate(0, MARKS_POPUP_SIZE);
				drawMiniField(canvas, 0f, 0f, MINI_MARKS_POPUP_SIZE, MINI_MARKS_FILL);
				//canvas.drawRect(MARKS_POPUP_AREA, WATER_PAINT);
				canvas.drawRect(MARKS_SQUARE_POPUP_AREA, paint);

				canvas.translate(0, MARKS_POPUP_SIZE);
				drawMiniField(canvas, 0f, 0f, MINI_MARKS_POPUP_SIZE, MINI_MARKS_CLEAR);
				//canvas.drawRect(MARKS_POPUP_AREA, BACKGROUND_PAINT);
				canvas.drawRect(MARKS_SQUARE_POPUP_AREA, paint);

				canvas.translate(0, -MARKS_POPUP_SIZE * 4);
				canvas.translate(-MARKS_POPUP_BUTTONS.left, -MARKS_POPUP_BUTTONS.top);

				canvas.drawLine(marksPopupLineX, marksPopupLineY, marksPopupLineX2, marksPopupLineY2, FIELD_PEN);
				canvas.drawLine(marksPopupLineX, marksPopupLineY + fieldSquareSize, marksPopupLineX2, marksPopupLineY2
						+ MARKS_POPUP_SIZE * 5, FIELD_PEN);
			}

		}
		if (showSAnimation) {
			canvas.translate(-MINI_MISSILES_IMAGES_SIZE_X.value / 2, -MISSILES_IMAGES_SIZE_Y.value / 2);
			for (ShootingAnimation sAnimation : sAnimationList) {
				final float x = (float) sAnimation.x;
				final float y = (float) sAnimation.y;
				final boolean yourT = sAnimation.yourTurn;
				final boolean showGoingUp = y + MISSILES_IMAGES_SIZE_Y.value > 0;
				final boolean showGoingDown = y - MISSILES_IMAGES_SIZE_Y.value < 0;
				final int index;
				final double abs_dy = Math.abs(sAnimation.dy);
				final double abs_ay = Math.abs(sAnimation.ay);
				if (abs_dy > SHOOTING_ANIMATION_TIME * 2 / 3 * abs_ay)
					index = (shootingPulse % 2) == 0 ? 2 : 3;
				else if (abs_dy > SHOOTING_ANIMATION_TIME * 1 / 3 * abs_ay)
					index = (shootingPulse % 2) == 0 ? 2 : 1;
				else
					index = (shootingPulse % 2) == 0 ? 0 : 1;

				if (showGoingUp && (yourT && menu == Menu.My || !yourT && menu == Menu.Enemy)) {
					canvas.translate(x, y);
					canvas.drawBitmap(getMiniMissileImage(sAnimation.kindShot), 0, 0, IMAGE_PAINT);
					canvas.translate(0, MINI_MISSILES_IMAGES_SIZE_Y.value);
					canvas.drawBitmap(FIRE_IMAGE, MINI_FIRE_SOURCE[index], MINI_FIRE_AREA, IMAGE_PAINT);
					canvas.translate(0, -MINI_MISSILES_IMAGES_SIZE_Y.value);
					canvas.translate(-x, -y);
				}

				if (showGoingDown && (yourT && menu == Menu.Enemy || !yourT && menu == Menu.My)) {
					canvas.translate(x, -y);
					canvas.drawBitmap(getMiniMissileImage(sAnimation.kindShot), UPSIDE_DOWN_MISSILE_MATRIX, IMAGE_PAINT);
					canvas.translate(0, -MINI_FIRE_IMAGES_SIZE_Y.value);
					canvas.drawBitmap(UPSIDE_DOWN_FIRE_IMAGE, MINI_FIRE_SOURCE[index], MINI_FIRE_AREA, IMAGE_PAINT);
					canvas.translate(0, MINI_FIRE_IMAGES_SIZE_Y.value);
					canvas.translate(-x, y);
				}
			}
			canvas.translate(MINI_MISSILES_IMAGES_SIZE_X.value / 2, MISSILES_IMAGES_SIZE_Y.value / 2);
		}
	}

	private Bitmap getMissileImage(KindShot kindShot) {
		if (kindShot == KindShot.NormalShot)
			return MISSILE_IMAGE;
		else if (kindShot == KindShot.IndestructibleShot)
			return MISSILE_INDESTRUCTIBLE_IMAGE;
		else
			return MISSILE_IMAGE; //TODO: camera shot images
	}

	private Bitmap getMiniMissileImage(KindShot kindShot) {
		if (kindShot == KindShot.NormalShot)
			return MINI_MISSILE_IMAGE;
		else if (kindShot == KindShot.IndestructibleShot)
			return MINI_MISSILE_INDESTRUCTIBLE_IMAGE;
		else
			return MINI_MISSILE_IMAGE; //TODO: camera shot images
	}

	private void drawShip(Canvas canvas, float initX, float initY, float ss, List<Coordinate> listPieces,
			Paint shipPaint) {
		for (Coordinate coor : listPieces) {
			final float x1 = initX + coor.x * ss;
			final float y1 = initY + coor.y * ss;
			final float x2 = initX + (coor.x + 1) * ss;
			final float y2 = initY + (coor.y + 1) * ss;
			canvas.drawRect(x1, y1, x2, y2, shipPaint);
			canvas.drawRect(x1, y1, x2, y2, FIELD_PEN);
		}
	}

	private void drawMiniField(Canvas canvas, float initX, float initY, float ss, Mark[][] marks) {
		for (int y = 0; y < marks.length; y++) {
			final Mark[] line = marks[y];
			for (int x = 0; x < line.length; x++) {
				final float x1 = initX + x * ss;
				final float y1 = initY + y * ss;
				final float x2 = initX + (x + 1) * ss;
				final float y2 = initY + (y + 1) * ss;
				Paint paint = line[x] == Mark.Ship ? SHIP_PAINT : (line[x] == Mark.Water ? WATER_PAINT
						: BACKGROUND_PAINT);
				canvas.drawRect(x1, y1, x2, y2, paint);
				canvas.drawRect(x1, y1, x2, y2, FIELD_PEN);
			}
		}
	}

	private int convertScreenToGame(float screenInit, int squareSize, float screenCoor) {
		return (int) Math.floor((screenCoor - screenInit) / squareSize);
	}

	private float convertGameToScreen(float screenInit, int squareSize, float gameCoor) {
		return screenInit + gameCoor * squareSize;
	}

	@Override
	public synchronized void onTouchEvent(MotionEvent event) {
		final float x = event.getX();
		final float y = event.getY();
		final int action = event.getAction();

		final int gameX = convertScreenToGame(fieldInitX, fieldSquareSize, x);
		final int gameY = convertScreenToGame(fieldInitY, fieldSquareSize, y);

		final Player player = game.getPlayer1();

		if (action == MotionEvent.ACTION_DOWN) {
			firstX = lastX = x;
			firstY = lastY = y;
			lastGX = gameX;
			lastGY = gameY;
			swipeTouchDown = true;
			lastTime = System.currentTimeMillis();

			if (menu == Menu.Enemy && showMarksPopup && MARKS_POPUP_BUTTONS.contains(x, y)) {
				MARKS_POPUP_BUTTONS.setButtonDown(true);
			} else {
				showMarksPopup = false;
				popup_label: {
					if (!activePopups.isEmpty()) {
						Popup popup = activePopups.peek();
						if (popup.type != PopupType.Wait && popup.rect.contains(x, y)) {
							if (popup.closeRect.contains(x, y)) {
								popup.closeRect.setButtonDown(true);
								return;
							} else if (popup.type == PopupType.EasyClose) {
								buttonDownAndUp(action, x, y, popup.rect);
							} else if (popup.type == PopupType.Finish) {
								buttonDownAndUp(action, x, y, REMATCH_BUTTON);
								buttonDownAndUp(action, x, y, BACK_TO_MAINMENU_BUTTON);
							} else if (popup.type == PopupType.Back) {
								buttonDownAndUp(action, x, y, EXIT_GAME_YES_BUTTON);
								buttonDownAndUp(action, x, y, EXIT_GAME_NO_BUTTON);
							}
							break popup_label;
						}
					}
					if (menu == Menu.Enemy || menu == Menu.DEBUG_EnemyMarks) {
						if (currentState == GameState.Playing) {
							buttonDownAndUp(action, x, y, TARGET_BUTTON);
							buttonDownAndUp(action, x, y, LAUNCH_BUTTON);
						}
						if (messagesYOffset > 0)
							buttonDownAndUp(action, x, y, TOP_MESSAGE_BUTTON);
						if (game.isInsideField(gameX, gameY))
							player.movePositionAbsolute(gameX, gameY);
						if (!TARGET_BUTTON.isButtonDown() && !LAUNCH_BUTTON.isButtonDown()
								&& !TOP_MESSAGE_BUTTON.isButtonDown() && MESSAGES_AREA.contains(x, y))
							touchDownMessages = true;
						if (game.isInsideField(gameX, gameY))
							marksPopupDown = true;
					}
				}
			}
		} else if (action == MotionEvent.ACTION_MOVE) {
			if (menu == Menu.Enemy) {
				boolean curr = currentState == GameState.Playing || currentState == GameState.PlayingInPause
						|| currentState == GameState.FinishedGame;
				if (curr && !touchDownMessages && !MARKS_POPUP_BUTTONS.isButtonDown()) {
					boolean canMove = true;
					if (!activePopups.isEmpty()) {
						Popup popup = activePopups.peek();
						if (popup.type != PopupType.Wait && popup.rect.contains(x, y))
							canMove = false;
					}

					if (canMove && game.isInsideField(gameX, gameY)) {
						player.movePositionAbsolute(gameX, gameY);
						if (showMarksPopup && (lastGX != gameX || lastGY != gameY)) {
							lastGX = gameX;
							lastGY = gameY;
							showMarksPopup(gameX, gameY);
						}
					}
				}
				if (touchDownMessages) {
					final List<Message> messages = player.getMessagesLock();
					float totalMessagesSizeY = MESSAGES_PLUS_Y * messages.size();
					float max = totalMessagesSizeY - MESSAGES_AREA.height();
					player.getMessagesUnlock();

					float diffY = lastY - y;
					float messagesYOffsetBefore = messagesYOffset;
					messagesYOffset = Math.max(0, Math.min(max, messagesYOffset + diffY));
					if (messagesYOffsetBefore == 0 || messagesYOffset == 0)
						newMessage = false;
					lastY = y;
				}
			} else {
				marksPopupDown = false;
			}
		} else if (action == MotionEvent.ACTION_UP) {
			if (swipeTouchDown) {
				final long curTime = System.currentTimeMillis();
				Log.d(TAG, "Swipe Time: " + (curTime - lastTime) + "     Swipe Y: " + (firstY - y) + "    Swipe X: "
						+ (firstX - x));
				if (curTime - lastTime < TIME_SWIPE_THRESHOLD && Math.abs(firstY - y) <= SWIPE_Y_THRESHOLD.value) {
					if (firstX - x >= SWIPE_X_THRESHOLD) {
						int index = getMenuIndex(menu);
						menu = activeMenus[(index + 1) % activeMenus.length];
					} else if (firstX - x <= -SWIPE_X_THRESHOLD) {
						int index = getMenuIndex(menu);
						menu = activeMenus[(activeMenus.length + index - 1) % activeMenus.length];
					}
				}
			}
			if (currentState == GameState.Playing) {
				if (menu == Menu.Enemy || menu == Menu.DEBUG_EnemyMarks) {
					if (buttonDownAndUp(action, x, y, TARGET_BUTTON)) {
						if (ComputerAI.DEBUG_AI_AUTO_SHOOT) {
							while (!player.allShotsPlaced()) {
								player.setPositionToRandomLocation(false);
								player.chooseTarget();
							}
							player.shotAll();
						} else
							player.chooseTarget();
					} else if (buttonDownAndUp(action, x, y, LAUNCH_BUTTON)) {
						player.shotAll();
					}
				}
			}
			if (menu == Menu.Enemy) {
				if (showMarksPopup && buttonDownAndUp(action, x, y, MARKS_POPUP_BUTTONS)) {
					final int n = (int) Math.floor(((y - MARKS_POPUP_BUTTONS.top) / MARKS_POPUP_SIZE));
					if (n == 0) {
						if (player.markAt() != Mark.Water100 && player.markAt() != Mark.Ship100)
							player.setMarkAt(Mark.Ship);
					} else if (n == 1) {
						if (player.markAt() != Mark.Water100 && player.markAt() != Mark.Ship100)
							player.setMarkAt(Mark.Water);
					} else if (n == 2) {
						if (player.markAt() != Mark.Water100 && player.markAt() != Mark.Ship100)
							player.setMarkAt(Mark.None);
					} else if (n == 3)
						player.fillWater();
					else if (n == 4)
						player.clearWater();
					showMarksPopup = false;
				}
				if (messagesYOffset > 0 && buttonDownAndUp(action, x, y, TOP_MESSAGE_BUTTON)) {
					messagesYOffset = 0;
					newMessage = false;
				}
			}

			if (!activePopups.isEmpty()) {
				Popup popup = activePopups.peek();
				if (popup.type != PopupType.Wait) {
					final MyButton closeRect = popup.closeRect;
					if (closeRect.containsAndDown(x, y)) {
						activePopups.remove();
						pocketPopups.add(popup);
					} else if (popup.type == PopupType.EasyClose && buttonDownAndUp(action, x, y, popup.rect)) {
						activePopups.remove();
						pocketPopups.add(popup);
					} else if (popup.type == PopupType.Finish) {
						if (buttonDownAndUp(action, x, y, REMATCH_BUTTON)) {
							game.requestRemake(GUIplayer);
						} else if (buttonDownAndUp(action, x, y, BACK_TO_MAINMENU_BUTTON)) {
							finishGameCallBack.callback();
						}
						REMATCH_BUTTON.setButtonDown(false);
						BACK_TO_MAINMENU_BUTTON.setButtonDown(false);
					} else if (popup.type == PopupType.Back) {
						if (buttonDownAndUp(action, x, y, EXIT_GAME_YES_BUTTON)) {
							finishGameCallBack.callback();
						} else if (buttonDownAndUp(action, x, y, EXIT_GAME_NO_BUTTON)) {
							activePopups.remove();
							pocketPopups.add(popup);
						}
						EXIT_GAME_YES_BUTTON.setButtonDown(false);
						EXIT_GAME_NO_BUTTON.setButtonDown(false);
					}
					closeRect.setButtonDown(false);
				}
			}

			swipeTouchDown = false;
			touchDownMessages = false;
			marksPopupDown = false;

			TARGET_BUTTON.setButtonDown(false);
			LAUNCH_BUTTON.setButtonDown(false);
		}
	}

	private int getMenuIndex(Menu searchMenu) {
		for (int i = 0; i < activeMenus.length; i++) {
			Menu menu = activeMenus[i];
			if (menu == searchMenu)
				return i;
		}
		return -1;
	}

	@Override
	public synchronized void update(double timeElapsed) {
		pulseTime += timeElapsed;
		if (pulseTime >= PULSE_TICK_TIME) {
			pulseTime -= PULSE_TICK_TIME;
			pulse++;
		}
		shootingPulseTime += timeElapsed;
		if (shootingPulseTime >= SHOOTING_PULSE_TICK_TIME) {
			shootingPulseTime -= SHOOTING_PULSE_TICK_TIME;
			shootingPulse++;
		}

		if (!activePopups.isEmpty()) {
			final Popup popup = activePopups.peek();
			popup.remainingTime -= timeElapsed;
			if (popup.remainingTime <= 0) {
				activePopups.remove();
				pocketPopups.add(popup);
			}
		}

		final long curTime = System.currentTimeMillis();
		if (marksPopupDown && !showMarksPopup && (curTime - lastTime) > TIME_MARKS_POPUP) {
			showMarksPopup(lastGX, lastGY);
		}

		if (showSAnimation) {
			sAnimationTime -= timeElapsed;
			if (sAnimationTime <= 0)
				showSAnimation = false;
			else {
				for (ShootingAnimation sAnimation : sAnimationList) {
					sAnimation.dx += sAnimation.ax * timeElapsed;
					sAnimation.dy += sAnimation.ay * timeElapsed;

					sAnimation.x += sAnimation.dx * timeElapsed;
					sAnimation.y += sAnimation.dy * timeElapsed;
				}
			}
		}

		final List<Message> messages = GUIplayer.getMessagesLock();
		final int size = messages.size();
		GUIplayer.getMessagesUnlock();
		if (size > lastMessagesSize) {
			lastMessagesSize = size;
			newMessage = true;
		}
	}

	private void showMarksPopup(int popupGameX, int popupGameY) {
		final int fieldX = game.getCurrentFleet().maxX;

		final float x = convertGameToScreen(fieldInitX, fieldSquareSize, popupGameX);
		final float y = convertGameToScreen(fieldInitY, fieldSquareSize, popupGameY);
		float marksPopupX, marksPopupY;
		if ((x + fieldSquareSize * 1.5f + MARKS_POPUP_SIZE) > fieldInitX + fieldSquareSize * fieldX) {
			marksPopupX = x - fieldSquareSize * .5f - MARKS_POPUP_SIZE;
			marksPopupLineX = x;
			marksPopupLineX2 = marksPopupX + MARKS_POPUP_SIZE;
		} else {
			marksPopupX = x + fieldSquareSize * 1.5f;
			marksPopupLineX = x + fieldSquareSize;
			marksPopupLineX2 = marksPopupX;
		}
		marksPopupLineY = y;

		marksPopupY = Math.max(0,
				Math.min(maxY - MARKS_POPUP_SIZE * 5, (y + fieldSquareSize * .5f) - MARKS_POPUP_SIZE * 2.5f));
		marksPopupLineY2 = marksPopupY;

		MARKS_POPUP_BUTTONS.offsetTo(marksPopupX, marksPopupY);

		marksPopupTargetX = lastGX;
		marksPopupTargetY = lastGY;

		showMarksPopup = true;
	}

	@Override
	public synchronized void changedStateEvent(GameState changedState) {
		currentState = changedState;

		if (currentState == GameState.FinishedGame) {
			Popup popup = getFreePopup();
			popup.text.clear();
			Player winner = game.getWinningPlayer();
			String winnerStr;
			if (winner == GUIplayer)
				winnerStr = YOU_WON_TEXT.text;
			else
				winnerStr = ENEMY_WON_TEXT.text;
			popup.text.add(winnerStr);
			popup.remainingTime = POPUP_TIME_FINISHED_GAME;
			popup.showMissiles = false;
			popup.rect = POPUP_MEDIUM_AREA;
			popup.closeRect = POPUP_MEDIUM_CLOSE_BUTTON;
			popup.type = PopupType.Finish;
			activePopups.add(popup);
		}
	}

	@Override
	public synchronized void newConditionEvent(Player extraTurnPlayer, BonusPlay bonusPlay) {
		Popup popup = getFreePopup();
		popup.text.clear();
		popup.text.add(bonusPlay.condition.name);
		if (extraTurnPlayer == GUIplayer) {
			popup.text.add(YOUR_EXTRA_TURN_TEXT.text);
		} else {
			popup.text.add(ENEMY_EXTRA_TURN_TEXT.text);
		}
		popup.remainingTime = (ComputerAI.DEBUG_AI ? 0.2 : 1) * POPUP_TIME_EXTRA_TURN;

		popup.showMissiles = false;
		for (GameBonus bonus : bonusPlay) {
			if (bonus.getType() == BonusTypes.ExtraTurn) {
				ExtraTurn extraTurn = (GameBonus.ExtraTurn) bonus;
				popup.missilesList.clear();
				popup.missilesList.addAll(extraTurn.getTurnShots());
				popup.showMissiles = true;
				popup.missleRect = POPUP_MEDIUM_MISSILES_AREA;
			}
		}
		popup.rect = POPUP_MEDIUM_AREA;
		popup.closeRect = POPUP_MEDIUM_CLOSE_BUTTON;
		popup.type = PopupType.Normal;
		activePopups.add(popup);
		addPopupWait();
	}

	@Override
	public synchronized void newTurnEvent(Player newTurnPlayer) {
		//		Log.i(TAG, "New Turn: " + newTurnPlayer.toString());

		Popup popup = getFreePopup();
		popup.text.clear();
		if (newTurnPlayer == GUIplayer) {
			popup.text.add(YOUR_TURN_TEXT.text);
			yourTurn = true;
		} else {
			popup.text.add(ENEMY_TURN_TEXT.text);
			yourTurn = false;
		}
		popup.remainingTime = (ComputerAI.DEBUG_AI ? 0.2 : 1) * POPUP_TIME_YOUR_TURN;
		popup.showMissiles = true;
		popup.missilesList.clear();
		final List<Shot> turnTargets = newTurnPlayer.getTurnTargets();
		for (Shot shot : turnTargets) {
			popup.missilesList.add(shot.getKindShot());
		}
		newTurnPlayer.unlockTurnTargets();
		popup.rect = POPUP_SMALL_AREA;
		popup.missleRect = POPUP_SMALL_MISSILES_AREA;
		popup.closeRect = POPUP_SMALL_CLOSE_BUTTON;
		popup.type = PopupType.EasyClose;
		activePopups.add(popup);
		addPopupWait();
	}

	@Override
	public synchronized void changedTurnState(TurnState changedTurnState) {
		turnState = changedTurnState;

		if (turnState == TurnState.Shooting) {
			showSAnimation = true;
			sAnimationTime = SHOOTING_ANIMATION_TIME;
			final Player player = yourTurn ? GUIplayer : GUIplayer.getEnemy();
			final List<Shot> turnTargets = player.getTurnTargets();
			final List<Ship> playerShips = player.getShips();
			sAnimationList.clear();
			for (Shot shot : turnTargets) {
				final Coordinate shotCoordinate = shot.getCoordinate();

				final double x0, x1, y0, y1;
				if (yourTurn) {
					final int randomIndex1 = GameClass.random.nextInt(playerShips.size());
					final List<Coordinate> listPieces = playerShips.get(randomIndex1).getListPieces();
					final int randomIndex2 = GameClass.random.nextInt(listPieces.size());
					final Coordinate randomShipCoordinate = listPieces.get(randomIndex2);

					x0 = convertGameToScreen(fieldInitX, fieldSquareSize, randomShipCoordinate.x + .5f);
					y0 = convertGameToScreen(fieldInitY, fieldSquareSize, randomShipCoordinate.y + .5f);
					x1 = convertGameToScreen(fieldInitX, fieldSquareSize, shotCoordinate.x + .5f);
					y1 = convertGameToScreen(fieldInitY, fieldSquareSize, shotCoordinate.y + .5f);
				} else {
					final int fieldX = game.getCurrentFleet().maxX;
					final int fieldY = game.getCurrentFleet().maxY;

					final int rx = GameClass.random.nextInt(fieldX);
					final int ry = GameClass.random.nextInt(fieldY);

					x0 = convertGameToScreen(fieldInitX, fieldSquareSize, rx + .5f);
					y0 = convertGameToScreen(fieldInitY, fieldSquareSize, ry + .5f);

					x1 = convertGameToScreen(fieldInitX, fieldSquareSize, shotCoordinate.x + .5f);
					y1 = convertGameToScreen(fieldInitY, fieldSquareSize, shotCoordinate.y + .5f);
				}
				final ShootingAnimation newSAnimation;
				//				{
				//					final double dx = (x1 - x0) / SHOOTING_ANIMATION_TIME;
				//					final double dy = (-y1 - y0) / SHOOTING_ANIMATION_TIME;
				//					newSAnimation = new ShootingAnimation(x0, y0, dx, dy, yourTurn, shot.getKindShot());
				//				}
				{
					final double ax = 2 * (x1 - x0) / (SHOOTING_ANIMATION_TIME * SHOOTING_ANIMATION_TIME);
					final double ay = 2 * (-y1 - y0) / (SHOOTING_ANIMATION_TIME * SHOOTING_ANIMATION_TIME);
					newSAnimation = new ShootingAnimation(x0, y0, ax, ay, yourTurn, shot.getKindShot());
				}
				sAnimationList.add(newSAnimation);
			}
			player.unlockTurnTargets();
		}
	}

	private Popup getFreePopup() {
		if (pocketPopups.isEmpty())
			return new Popup();
		else
			return pocketPopups.remove();
	}

	private void addPopupWait() {
		Popup popup = getFreePopup();
		popup.type = PopupType.Wait;
		popup.remainingTime = (ComputerAI.DEBUG_AI ? 0.2 : 1) * WAITING_POPUP_TIME;
		activePopups.add(popup);
	}

	@Override
	public synchronized boolean backPressed() {
		if (!activePopups.isEmpty()) {
			Popup popup = activePopups.poll();
			pocketPopups.add(popup);
		} else if (currentState == GameState.FinishedGame) {
			changedStateEvent(GameState.FinishedGame);
		} else {
			Popup popup = getFreePopup();
			popup.text.clear();
			popup.text.add(EXIT_GAME_QUESTION_TEXT.text);
			popup.remainingTime = POPUP_TIME_FINISHED_GAME;
			popup.showMissiles = false;
			popup.rect = POPUP_MEDIUM_AREA;
			popup.closeRect = POPUP_MEDIUM_CLOSE_BUTTON;
			popup.type = PopupType.Back;
			activePopups.add(popup);
		}
		return true;
	}

	@Override
	public synchronized void externalMessage(String messageStr) {
		Popup popup = getFreePopup();
		popup.text.clear();
		popup.text.add(messageStr);
		popup.remainingTime = (ComputerAI.DEBUG_AI ? 0.2 : 1) * POPUP_TIME_EXTERNAL_MESSAGE;
		popup.showMissiles = false;
		popup.rect = POPUP_SMALL_AREA;
		popup.closeRect = POPUP_SMALL_CLOSE_BUTTON;
		popup.type = PopupType.Normal;
		activePopups.add(popup);
		addPopupWait();
	}
}
