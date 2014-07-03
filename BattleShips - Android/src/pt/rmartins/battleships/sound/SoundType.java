package pt.rmartins.battleships.sound;

import pt.rmartins.battleships.R;
import pt.rmartins.battleships.objects.Game;
import pt.rmartins.battleships.objects.GameClass;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;

public enum SoundType {
	MonsterKill(R.raw.monsterkill), Ownage(R.raw.ownage), Rampage(R.raw.rampage), DoubleKill(R.raw.doublekill), TripleKill(
			R.raw.triplekill), UltraKill(R.raw.ultrakill), Unstoppable(R.raw.unstoppable), MegaKill(R.raw.megakill), KillingSpree(
			R.raw.killingspree), HolyShit(R.raw.holyshit), GodLike(R.raw.godlike), FirstBlood(R.raw.firstblood), Dominating(
			R.raw.dominating), Combowhore(R.raw.combowhore), Butcher(R.raw.butcher), Missile(R.raw.missile), Water(
			R.raw.water), ExtraTurn(R.raw.extraturn), WonGame(R.raw.victory_01), LostGame(R.raw.defeat_01);

	//	private static final String TAG = SoundType.class.getSimpleName();

	//	private static SoundType[] NORMAL_BG_MUSIC = {};

	//	private static SoundType currentBGMusic = initBGMusic();
	//	private static Handler handler;

	public static boolean soundIsPlaying;

	//	private static SoundType initBGMusic() {
	//		handler = new Handler();
	//
	//		return null;
	//	}

	private final int n;

	//	private final boolean isBackgroundMusic;

	private SoundType(int n) {
		//		this.isBackgroundMusic = false;
		this.n = n;
	}

	//	private SoundType(boolean isBackgroundMusic, int n) {
	//		this.isBackgroundMusic = isBackgroundMusic;
	//		this.n = n;
	//	}

	private MediaPlayer mp;

	public void playSound(final Game game, final Runnable onCompletionRunnable) {
		if (!GameClass.soundIsOn())
			return;

		mp = MediaPlayer.create(GameClass.getContext(), n);
		if (mp != null) {
			mp.start();
			soundIsPlaying = true;

			mp.setOnCompletionListener(new OnCompletionListener() {

				@Override
				public void onCompletion(MediaPlayer mp) {
					//					if (handler != null && isBackgroundMusic) {
					//						handler.post(new Runnable() {
					//							@Override
					//							public void run() {
					//								SoundType last = currentBGMusic;
					//								currentBGMusic = null;
					//								startRandomBGMusic(game, last);
					//							}
					//						});
					//					}
					mp.release();
					soundIsPlaying = false;
					onCompletionRunnable.run();
				}
			});
		}
	}

	public static void stopAllSounds() {
		//		currentBGMusic = null;
		for (SoundType sound : SoundType.values()) {
			try {
				if (sound.mp != null) {
					sound.mp.stop();
					sound.mp.release();
				}
			} catch (Exception e) {
			}
		}
	}

	// private static void stopAllBackgroundSounds() {
	// for (SoundType sound : SoundType.values()) {
	// try {
	// if (sound.isBackgroundMusic)
	// sound.mp.stop();
	// } catch (Exception e) {
	// }
	// }
	// }

	//	public static void startRandomBGMusic(Game game) {
	//		startRandomBGMusic(game, null);
	//	}
	//
	//	private static void startRandomBGMusic(Game game, SoundType last) {
	//		// stopAllBackgroundSounds();
	//		if (currentBGMusic == null) {
	//			do {
	//				int n = (int) (Math.random() * NORMAL_BG_MUSIC.length);
	//				currentBGMusic = NORMAL_BG_MUSIC[n];
	//			} while (currentBGMusic == last);
	//
	//			currentBGMusic.playSound(game);
	//		}
	//	}

	public static boolean soundIsPlaying() {
		return soundIsPlaying;
	}
}