package pt.rmartins.battleships;

import android.graphics.Canvas;

public interface MyPanel {

	//	public enum RotationType {
	//		Portrait, Landscape;
	//	}

	void startTime();

	void update();

	void render(Canvas canvas);

}
