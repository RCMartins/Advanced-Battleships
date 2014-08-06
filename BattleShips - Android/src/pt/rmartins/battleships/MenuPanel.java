package pt.rmartins.battleships;

import pt.rmartins.battleships.objects.Callback;
import pt.rmartins.battleships.objects.Game;
import pt.rmartins.battleships.objects.GameClass;
import pt.rmartins.battleships.objects.userinterface.MainMenu;
import pt.rmartins.battleships.objects.userinterface.UserInterface;
import android.app.Activity;
import android.graphics.Canvas;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

/**
 * This is the main surface that handles the ontouch events and draws the image to the screen.
 */
public class MenuPanel extends SurfaceView implements SurfaceHolder.Callback, MyPanel {

	private static final String TAG = MenuPanel.class.getSimpleName();

	private MainThread thread;

	private final Activity activity;
	private static UserInterface GUI;
	private static Game game;
	private static int width, height;

	private final NewGame NEW_GAME = new NewGame();
	private final Callback EXIT_GAME;

	public MenuPanel(final Activity activity) {
		super(activity);
		this.activity = activity;
		// adding the callback (this) to the surface holder to intercept events
		getHolder().addCallback(this);

		// make the GamePanel focusable so it can handle events
		setFocusable(true);

		lastGameTime = System.currentTimeMillis();

		setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				onTouchEvent(event);
				return true;
			}

		});

		GUI = null;
		game = null;
		width = 0;
		height = 0;

		EXIT_GAME = new Callback() {
			@Override
			public void callback() {
				if (game == null)
					activity.finish();
				else {
					game.closeResources();
					GUI = new MainMenu(width, height, activity, NEW_GAME, EXIT_GAME);
					game = null;
				}
			}
		};
	}

	public static class NewGame {
		public void setGame(Game newGame) {
			game = newGame;
			game.changeMAX(width, height);
			game.initialize();
			GUI = null;
		}

		public void setGUI(UserInterface newGUI) {
			GUI = newGUI;
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		MenuPanel.width = width;
		MenuPanel.height = height;
		GameClass.loadSettings(activity);
		if (game == null) {
			if (GUI == null)
				GUI = new MainMenu(width, height, activity, NEW_GAME, EXIT_GAME);
			else
				GUI.initializeGUI(width, height);
		} else
			game.changeMAX(width, height);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// create the game loop thread
		thread = new MainThread(getHolder(), this);

		// at this point the surface is created and
		// we can safely start the game loop
		thread.setRunning(true);
		thread.start();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d(TAG, "Surface is being destroyed");
		// tell the thread to shut down and wait for it to finish
		// this is a clean shutdown

		boolean retry = true;
		while (retry) {
			try {
				thread.setRunning(false);
				thread.join();
				retry = false;
			} catch (InterruptedException e) {
				// try again shutting down the thread
			}
		}
		Log.d(TAG, "Thread was shut down cleanly");
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		//		try {
		if (GUI != null)
			GUI.onTouchEvent(event);
		else if (game != null)
			game.onTouchEvent(event);
		//		} catch (Exception e) {
		//			Log.e(TAG, e.getMessage());
		//		}
		return true;
	}

	@Override
	public void render(Canvas canvas) {
		if (canvas != null) {
			if (GUI != null)
				GUI.draw(canvas);
			else if (game != null)
				game.draw(canvas);
		}
	}

	/**
	 * This is the game update method. It iterates through all the objects and calls their update method if they have
	 * one or calls specific engine's update method.
	 */
	private double lastGameTime;

	@Override
	public void update() {
		long gameTime = System.currentTimeMillis();
		double timeElapsed = (gameTime - lastGameTime) / 1000f;
		lastGameTime = gameTime;

		timeElapsed = Math.min(.1f, timeElapsed);

		if (GUI != null)
			GUI.update(timeElapsed);
		else if (game != null)
			game.update(timeElapsed);
	}

	@Override
	public void startTime() {
		lastGameTime = System.currentTimeMillis();
	}

	public void onBackPressed() {
		if (game != null) {
			game.onBackPressed();
		} else if (GUI instanceof MainMenu) {
			activity.finish();
		} else {
			GUI = new MainMenu(width, height, activity, NEW_GAME, EXIT_GAME);
		}
	}

	public void onDestroy() {
		if (game != null)
			game.closeResources();
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (game != null) {
			return game.dispatchKeyEvent(event);
		} else
			return super.dispatchKeyEvent(event);
	}

}
