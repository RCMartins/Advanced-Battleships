package pt.rmartins.battleships.objects.userinterface;

import android.graphics.Canvas;
import android.view.MotionEvent;

public interface UserInterface {

	public enum ButtonAlignType {
		Top, Bottom, Left, Right;
	}

	public void draw(Canvas canvas);

	public void onTouchEvent(MotionEvent event);

	public void update(double timeElapsed);

	/**
	 * @return true is consumed, else default back
	 */
	public boolean backPressed();

	public void clean();

	public void initializeGUI(int maxX, int maxY);
}
