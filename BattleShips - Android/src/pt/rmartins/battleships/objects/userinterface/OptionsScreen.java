package pt.rmartins.battleships.objects.userinterface;

import pt.rmartins.battleships.R;
import pt.rmartins.battleships.objects.Callback;
import pt.rmartins.battleships.objects.GameClass;
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
import android.view.MotionEvent;

public class OptionsScreen extends UserInterfaceClass {

	private static final int COLOR_TEXT = Color.BLACK;

	private static float BUTTON_TEXT_SIZE = 25f;

	private static Paint TEXT_PAINT, LEFT_TEXT_PAINT, RIGHT_TEXT_PAINT;
	private RectF TITLE_AREA, LANGUAGE_TEXT_AREA, SOUND_TEXT_AREA;
	private MyButton LANGUAGE_CHANGE_BUTTON, SOUND_CHANGE_BUTTON;

	private static final int TITLE_CODE = R.string.options_title;
	private static final int LANGUAGE_CODE = R.string.options_language_change;
	private static final int SOUND_CODE = R.string.options_sound;
	private static final int SOUND_ON_CODE = R.string.options_sound_on;
	private static final int SOUND_OFF_CODE = R.string.options_sound_off;

	private static final String MAX_LANGUAGE_SIZE = "pt-PT";

	private String TITLE_TEXT, LANGUAGE_TEXT, SOUND_TEXT, SOUND_ON_TEXT, SOUND_OFF_TEXT;

	private final int maxX, maxY;
	private final Activity activity;
	private final Callback backToMainMenu;

	private float TEXT_HEIGHT;

	public OptionsScreen(int maxX, int maxY, Activity activity, Callback backToMainMenu) {
		this.maxX = maxX;
		this.maxY = maxY;
		this.activity = activity;
		this.backToMainMenu = backToMainMenu;

		initialize();
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
		TEXT_HEIGHT = Draw.getStrHeight(TEXT_PAINT);

		{
			TITLE_TEXT = LanguageClass.getString(TITLE_CODE);
			TITLE_AREA = new RectF(OUT_PADDING, OUT_PADDING, maxX - OUT_PADDING, OUT_PADDING + TEXT_HEIGHT);
		}
		{
			//final float total_width  = Draw.getStrWidth(TEXT_PAINT, LANGUAGE_TEXT)  + Draw.getStrWidth(TEXT_PAINT, MAX_LANGUAGE_SIZE) + OUT_PADDING ;
			LANGUAGE_TEXT = LanguageClass.getString(LANGUAGE_CODE);
			final float text_width1 = Draw.getStrWidth(TEXT_PAINT, LANGUAGE_TEXT);
			final float y = TITLE_AREA.bottom + OUT_PADDING + TEXT_HEIGHT;
			LANGUAGE_TEXT_AREA = new RectF(OUT_PADDING, y + IN_PADDING, OUT_PADDING + text_width1, y + TEXT_HEIGHT
					+ IN_PADDING);
			final float text_width2 = Draw.getStrWidth(TEXT_PAINT, MAX_LANGUAGE_SIZE);
			LANGUAGE_CHANGE_BUTTON = new MyButton(LANGUAGE_TEXT_AREA.right + OUT_PADDING, y, LANGUAGE_TEXT_AREA.right
					+ OUT_PADDING + text_width2 + IN_PADDING * 2, y + IN_PADDING * 2 + TEXT_HEIGHT);
		}
		{
			SOUND_TEXT = LanguageClass.getString(SOUND_CODE);
			SOUND_ON_TEXT = LanguageClass.getString(SOUND_ON_CODE);
			SOUND_OFF_TEXT = LanguageClass.getString(SOUND_OFF_CODE);

			final float text_width1 = Draw.getStrWidth(TEXT_PAINT, SOUND_TEXT);
			final float y = LANGUAGE_TEXT_AREA.bottom + OUT_PADDING + TEXT_HEIGHT;
			SOUND_TEXT_AREA = new RectF(OUT_PADDING, y + IN_PADDING, OUT_PADDING + text_width1, y + TEXT_HEIGHT
					+ IN_PADDING);

			final float text_width_on = Draw.getStrWidth(TEXT_PAINT, SOUND_ON_TEXT);
			final float text_width_off = Draw.getStrWidth(TEXT_PAINT, SOUND_OFF_TEXT);
			final float text_width2 = Math.max(text_width_on, text_width_off);
			SOUND_CHANGE_BUTTON = new MyButton(SOUND_TEXT_AREA.right + OUT_PADDING, y, SOUND_TEXT_AREA.right
					+ OUT_PADDING + text_width2 + IN_PADDING * 2, y + IN_PADDING * 2 + TEXT_HEIGHT);
		}
	}

	@Override
	public synchronized void draw(Canvas canvas) {
		canvas.drawColor(Color.WHITE);
		canvas.drawText(TITLE_TEXT, TITLE_AREA.centerX(), TITLE_AREA.bottom, TEXT_PAINT);

		canvas.drawText(LANGUAGE_TEXT, LANGUAGE_TEXT_AREA.left, LANGUAGE_TEXT_AREA.bottom, LEFT_TEXT_PAINT);

		String currentLanguage = LanguageClass.getCurrentLanguage().getLanguage();
		drawButton(canvas, currentLanguage, LANGUAGE_CHANGE_BUTTON, TEXT_PAINT);

		canvas.drawText(SOUND_TEXT, SOUND_TEXT_AREA.left, SOUND_TEXT_AREA.bottom, LEFT_TEXT_PAINT);
		String soundText = GameClass.soundIsOn() ? SOUND_ON_TEXT : SOUND_OFF_TEXT;
		drawButton(canvas, soundText, SOUND_CHANGE_BUTTON, TEXT_PAINT);
	}

	@Override
	public synchronized void onTouchEvent(MotionEvent event) {
		final float x = event.getX();
		final float y = event.getY();
		final int action = event.getAction();

		if (buttonDownAndUp(action, x, y, LANGUAGE_CHANGE_BUTTON)) {
			LanguageClass.setNextLanguage();
			initialize();
		} else if (buttonDownAndUp(action, x, y, SOUND_CHANGE_BUTTON)) {
			GameClass.setSoundIsOn(!GameClass.soundIsOn());
		}
	}

	@Override
	public synchronized void update(double timeElapsed) {
		// TODO Auto-generated method stub
	}
}
