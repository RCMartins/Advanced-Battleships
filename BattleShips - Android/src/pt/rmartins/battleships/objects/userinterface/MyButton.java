package pt.rmartins.battleships.objects.userinterface;

import android.graphics.RectF;

public class MyButton extends RectF {

	private boolean buttonDown;
	private boolean enabled;

	public MyButton(RectF other) {
		super(other);
		this.buttonDown = false;
		if (other instanceof MyButton)
			enabled = ((MyButton) other).enabled;
		else
			enabled = true;
	}

	public MyButton(float left, float top, float right, float bottom) {
		super(left, top, right, bottom);
		this.buttonDown = false;
		enabled = true;
	}

	public boolean isButtonDown() {
		return buttonDown;
	}

	public void setButtonDown(boolean buttonDown) {
		this.buttonDown = enabled && buttonDown;
	}

	public boolean containsAndDown(float x, float y) {
		return enabled && buttonDown && contains(x, y);
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

}
