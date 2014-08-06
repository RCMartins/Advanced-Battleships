package pt.rmartins.battleships.utilities;

import android.graphics.Paint;
import android.graphics.Rect;

public final class Draw {

	private static Rect bounds = new Rect();

	//	public static void drawRect(Canvas canvas, int startX, int startY, Rect rect, Paint paint) {
	//		canvas.drawRect(rect.left + startX, rect.top + startY, rect.right + startX, rect.bottom
	//				+ startY, paint);
	//	}
	//
	//	public static void clipPath(Canvas canvas, int startX, int startY, Path path) {
	//		path.offset(startX, startY);
	//		canvas.clipPath(path);
	//		path.offset(-startX, -startY);
	//	}
	//
	//	public static void drawPath(Canvas canvas, int startX, int startY, Path path, Paint paint) {
	//		path.offset(startX, startY);
	//		canvas.drawPath(path, paint);
	//		path.offset(-startX, -startY);
	//	}

	public static int getStrHeight(Paint paint) {
		final String str = "09#abcABCMij";
		paint.getTextBounds(str, 0, str.length(), bounds);
		return bounds.height();
	}

	public static int getStrHeight(Paint paint, String str) {
		paint.getTextBounds(str, 0, str.length(), bounds);
		return bounds.height();
	}

	public static int getStrWidth(Paint paint, String str) {
		paint.getTextBounds(str, 0, str.length(), bounds);
		return bounds.width();
	}

	public static String repeatString(String str, int repetitions) {
		StringBuilder sb = new StringBuilder(str.length() * repetitions);
		for (int i = 0; i < repetitions; i++)
			sb.append(str);
		return sb.toString();
	}

	public static void setPaintFitWidth(float maxWidth, Paint paint, String... strings) {
		for (String str : strings) {
			setPaintFitStringAux(maxWidth, Float.MAX_VALUE, paint, str);
		}
	}

	public static void setPaintFit(float maxWidth, float maxHeight, Paint paint, String... strings) {
		for (String str : strings) {
			setPaintFitStringAux(maxWidth, maxHeight, paint, str);
		}
	}

	private static void setPaintFitStringAux(float maxWidth, float maxHeight, Paint paint, String str) {
		while (getStrWidth(paint, str) > maxWidth || getStrHeight(paint, str) > maxHeight) {
			paint.setTextSize(paint.getTextSize() - 1);
		}
	}
}
