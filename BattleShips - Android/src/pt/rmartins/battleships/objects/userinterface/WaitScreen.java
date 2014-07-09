//package pt.rmartins.battleships.objects.userinterface;
//
//import android.graphics.Canvas;
//import android.graphics.Color;
//import android.graphics.Paint;
//import android.graphics.Paint.Style;
//import android.graphics.Typeface;
//import android.view.MotionEvent;
//
//public class WaitScreen extends UserInterfaceClass {
//
//	private static final int COLOR_TEXT = Color.BLACK;
//
//	private static float BUTTON_TEXT_SIZE = 30f;
//
//	private static Paint TEXT_PAINT;
//
//	private final float maxX;
//	private final float maxY;
//
//	public WaitScreen(int maxX, int maxY) {
//		this.maxX = maxX;
//		this.maxY = maxY;
//
//		initialize();
//	}
//
//	public static void initializeScreenMultiplier(float SCREEN_SUPPORT_MULTIPLIER) {
//		BUTTON_TEXT_SIZE *= SCREEN_SUPPORT_MULTIPLIER;
//
//		Typeface typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL);
//
//		TEXT_PAINT = new Paint(Paint.ANTI_ALIAS_FLAG);
//		TEXT_PAINT.setStyle(Style.FILL);
//		TEXT_PAINT.setTextAlign(Paint.Align.CENTER);
//		TEXT_PAINT.setColor(COLOR_TEXT);
//		TEXT_PAINT.setTextSize(BUTTON_TEXT_SIZE);
//		TEXT_PAINT.setTypeface(typeface);
//	}
//
//	private synchronized void initialize() {
//	}
//
//	@Override
//	public synchronized void draw(Canvas canvas) {
//		canvas.drawColor(Color.WHITE);
//		canvas.drawText("Loading...", maxX / 2, maxY / 2, TEXT_PAINT);
//	}
//
//	@Override
//	public synchronized void onTouchEvent(MotionEvent event) {
//		final float x = event.getX();
//		final float y = event.getY();
//		final int action = event.getAction();
//	}
//
//	@Override
//	public synchronized void update(double timeElapsed) {
//		// TODO Auto-generated method stub
//	}
//}
