package pt.rmartins.battleships.objects.userinterface;

import pt.rmartins.battleships.utilities.Draw;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.view.MotionEvent;

public abstract class UserInterfaceClass implements UserInterface {

	protected static final int COLOR_DOWN_BUTTON = Color.GRAY;
	protected static final int COLOR_NORMAL_BUTTON = Color.LTGRAY;
	protected static final int COLOR_BORDER_BUTTON = Color.DKGRAY;
	protected static final int COLOR_TEXT = Color.BLACK;

	protected static float OUT_PADDING = 5f;
	protected static float IN_PADDING = 10f;
	protected static float BUTTON_STROKE_WIDTH = 4f;

	protected static Paint BORDER_BUTTON_PAINT, DOWN_BUTTON_PAINT, NORMAL_BUTTON_PAINT;

	public static void initializeScreenMultiplier(float SCREEN_SUPPORT_MULTIPLIER) {
		OUT_PADDING *= SCREEN_SUPPORT_MULTIPLIER;
		IN_PADDING *= SCREEN_SUPPORT_MULTIPLIER;
		BUTTON_STROKE_WIDTH *= SCREEN_SUPPORT_MULTIPLIER;

		DOWN_BUTTON_PAINT = new Paint(Paint.ANTI_ALIAS_FLAG);
		DOWN_BUTTON_PAINT.setStyle(Style.FILL);
		DOWN_BUTTON_PAINT.setColor(COLOR_DOWN_BUTTON);

		NORMAL_BUTTON_PAINT = new Paint(Paint.ANTI_ALIAS_FLAG);
		NORMAL_BUTTON_PAINT.setStyle(Style.FILL);
		NORMAL_BUTTON_PAINT.setColor(COLOR_NORMAL_BUTTON);

		BORDER_BUTTON_PAINT = new Paint(Paint.ANTI_ALIAS_FLAG);
		BORDER_BUTTON_PAINT.setStyle(Style.STROKE);
		BORDER_BUTTON_PAINT.setStrokeWidth(BUTTON_STROKE_WIDTH);
		BORDER_BUTTON_PAINT.setColor(COLOR_BORDER_BUTTON);
	}

	private static int BACKUP_COLOR;

	protected void drawButton(Canvas canvas, String text, RectF rect, Paint textPaint) {
		Paint buttonBackPaint = NORMAL_BUTTON_PAINT;
		if (rect instanceof MyButton) {
			final MyButton myButton = (MyButton) rect;
			if (myButton.isButtonDown()) {
				buttonBackPaint = DOWN_BUTTON_PAINT;
			}
			if (!myButton.isEnabled()) {
				BACKUP_COLOR = textPaint.getColor();
				textPaint.setColor(Color.GRAY);
			}
		}
		canvas.drawRect(rect, buttonBackPaint);
		canvas.drawRect(rect, BORDER_BUTTON_PAINT);

		final float textHeight = Draw.getStrHeight(textPaint, text);
		if (text.contains("\n")) {
			String[] split = text.split("\n");
			String str1 = split[0];
			String str2 = split[1];
			canvas.drawText(str1, rect.centerX(), rect.centerY() - IN_PADDING / 2, textPaint);
			canvas.drawText(str2, rect.centerX(), rect.centerY() + IN_PADDING / 2 + textHeight, textPaint);
		} else
			canvas.drawText(text, rect.centerX(), rect.centerY() + textHeight / 2, textPaint);

		if (rect instanceof MyButton) {
			final MyButton myButton = (MyButton) rect;
			if (!myButton.isEnabled()) {
				textPaint.setColor(BACKUP_COLOR);
			}
		}
	}

	protected boolean buttonDownAndUp(int action, float x, float y, MyButton rect) {
		if (action == MotionEvent.ACTION_DOWN) {
			if (rect.contains(x, y))
				rect.setButtonDown(true);
		} else if (action == MotionEvent.ACTION_MOVE) {
			if (rect.isButtonDown())
				rect.setButtonDown(rect.contains(x, y));
		} else if (action == MotionEvent.ACTION_UP) {
			if (rect.containsAndDown(x, y)) {
				rect.setButtonDown(false);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean backPressed() {
		return false;
	}

	public static MyButton CreateNewButton(String text, Paint textPaint, float x, float y, ButtonAlignType alignType) {
		final float text_height = Draw.getStrHeight(textPaint);
		final float text_width;
		final float button_height;
		if (text.contains("\n")) {
			String[] split = text.split("\n");
			String str1 = split[0];
			String str2 = split[1];
			float text_width1 = Draw.getStrWidth(textPaint, str1);
			float text_width2 = Draw.getStrWidth(textPaint, str2);
			text_width = Math.max(text_width1, text_width2);
			button_height = text_height * 2 + IN_PADDING * 3;
		} else {
			text_width = Draw.getStrWidth(textPaint, text);
			button_height = text_height + IN_PADDING * 2;
		}

		final MyButton button;
		if (alignType == ButtonAlignType.Top) {
			button = new MyButton(x - text_width / 2 - IN_PADDING, y, x + text_width / 2 + IN_PADDING, y
					+ button_height);
		} else if (alignType == ButtonAlignType.Bottom) {
			button = new MyButton(x - text_width / 2 - IN_PADDING, y - button_height, x + text_width / 2 + IN_PADDING,
					y);
		} else {
			button = null;
		}
		return button;
	}

	@Override
	public void clean() {
	}
}
