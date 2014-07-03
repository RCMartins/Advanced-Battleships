package pt.rmartins.battleships;

import pt.rmartins.battleships.objects.Game;
import pt.rmartins.battleships.objects.GameClass;
import pt.rmartins.battleships.objects.userinterface.ChooseScreen;
import pt.rmartins.battleships.objects.userinterface.MainMenu;
import pt.rmartins.battleships.objects.userinterface.MultiplayerScreen;
import pt.rmartins.battleships.objects.userinterface.OptionsScreen;
import pt.rmartins.battleships.objects.userinterface.PlacingShipsScreen;
import pt.rmartins.battleships.objects.userinterface.PlayingScreen;
import pt.rmartins.battleships.objects.userinterface.ScreenUtils;
import pt.rmartins.battleships.objects.userinterface.UserInterfaceClass;
import pt.rmartins.battleships.objects.userinterface.WaitScreen;
import pt.rmartins.battleships.utilities.DataLoader;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.nuggeta.NuggetaContext;

public class ControlActivity extends Activity {

	protected static final String TAG = ControlActivity.class.getSimpleName();

	private static boolean initialized = false;

	private static class InitializeTask extends AsyncTask<Activity, Void, Activity> {
		@Override
		protected Activity doInBackground(Activity... activityList) {
			Activity activity = activityList[0];

			if (!initialized) {
				float SCREEN_SUPPORT_MULTIPLIER;
				try {
					DisplayMetrics dm = new DisplayMetrics();
					activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
					SCREEN_SUPPORT_MULTIPLIER = dm.density;

					int dim = dm.widthPixels * dm.heightPixels;
					if (dim >= 960 * 720)
						SCREEN_SUPPORT_MULTIPLIER = Math.max(SCREEN_SUPPORT_MULTIPLIER, 2.0f);
					else if (dim >= 640 * 480)
						SCREEN_SUPPORT_MULTIPLIER = Math.max(SCREEN_SUPPORT_MULTIPLIER, 1.5f);
					else if (dim >= 470 * 320)
						SCREEN_SUPPORT_MULTIPLIER = Math.max(SCREEN_SUPPORT_MULTIPLIER, 1.0f);
					else
						SCREEN_SUPPORT_MULTIPLIER = Math.max(SCREEN_SUPPORT_MULTIPLIER, 0.75f);
				} catch (Exception e) {
					System.out.println("dm error");
					SCREEN_SUPPORT_MULTIPLIER = 1.0f;
				}

				UserInterfaceClass.initializeScreenMultiplier(SCREEN_SUPPORT_MULTIPLIER);
				ScreenUtils.initializeScreenMultiplier(activity, SCREEN_SUPPORT_MULTIPLIER);
				MainMenu.initializeScreenMultiplier(SCREEN_SUPPORT_MULTIPLIER);
				ChooseScreen.initializeScreenMultiplier(SCREEN_SUPPORT_MULTIPLIER);
				PlacingShipsScreen.initializeScreenMultiplier(SCREEN_SUPPORT_MULTIPLIER);
				PlayingScreen.initializeScreenMultiplier(activity, SCREEN_SUPPORT_MULTIPLIER);
				MultiplayerScreen.initializeScreenMultiplier(activity, SCREEN_SUPPORT_MULTIPLIER);
				WaitScreen.initializeScreenMultiplier(SCREEN_SUPPORT_MULTIPLIER);
				OptionsScreen.initializeScreenMultiplier(activity, SCREEN_SUPPORT_MULTIPLIER);

				GameClass.initializeGameClass(activity);
			}

			return activity;
		}

		@Override
		protected void onPostExecute(Activity activity) {
			menuPanel = new MenuPanel(activity);
			activity.setContentView(menuPanel);

			initialized = true;
		}
	}

	private static MenuPanel menuPanel;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		if (Game.DEVELOPER_MODE) {
			StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites()
					.detectNetwork() // or .detectAll() for all detectable problems
					.penaltyLog().build());
			StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().penaltyLog()
					.penaltyDeath().build());
		}

		super.onCreate(savedInstanceState);

		// requesting to turn the title OFF
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// making it full screen
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_loading);

		DataLoader.initializeDataLoader(this);

		new InitializeTask().execute(this);

		// register main activity on NuggetaContext
		NuggetaContext.register(ControlActivity.this);
	}

	@Override
	protected void onStart() {
		super.onStart();
		//		Control.unPause();
	}

	@Override
	protected void onPause() {
		super.onPause();
		//		if (MenuControl.getMenuState() == MenuState.Game) {
		//			Control.pause();
		//		}
		//		Statistics.saveStatistics();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		menuPanel.onDestroy();
	}

	@Override
	public void onBackPressed() {
		if (initialized)
			menuPanel.onBackPressed();
	}

	public void button_test(View view) {
		//		final String resultId = nuggetaPlug.createGame();
		//		final String resultId = nuggetaPlug.searchImmediateGame();
		//		Log.i(TAG, resultId);
	}
}
