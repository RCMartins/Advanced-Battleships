package pt.rmartins.battleships.utilities;

public class StopWatch {

	private long startTime;
	private long elapsed;
	private boolean running, wasRunning;

	public StopWatch() {
		resetAndStop();
	}

	/**
	 * @return elaspsed time in milliseconds
	 */
	public long getElapsedTime() {
		final long elapsed2 = running ? System.currentTimeMillis() - startTime : 0;
		return elapsed + elapsed2;
	}

	/**
	 * @return elaspsed time in seconds
	 */
	public long getElapsedTimeSecs() {
		return (long) Math.floor(getElapsedTime() / 1000);
	}

	// // elaspsed time in Milliseconds
	// public long getElapsedTimeMilli() {
	// return getElapsedTime();
	// }

	public void resetAndStop() {
		elapsed = 0;
		running = false;
	}

	public void start() {
		if (!running) {
			startTime = System.currentTimeMillis();
			running = true;
		}
	}

	public void pause() {
		wasRunning = running;
		stop();
	}

	public void continueWatch() {
		if (wasRunning)
			start();
	}

	public void stop() {
		if (running) {
			elapsed += System.currentTimeMillis() - startTime;
			running = false;
		}
	}

	public boolean isRunning() {
		return running;
	}

	public void increaseTime(long milliseconds) {
		elapsed -= milliseconds;
	}

	@Override
	public String toString() {
		final long tsec = getElapsedTimeSecs();
		return format(tsec);
	}

	/**
	 * @param tsec
	 *            time in seconds
	 * @return time formated 00:00 if less then 1 hour, else 00:00:00
	 */
	public static String format(long tsec) {
		if (tsec < 0)
			tsec = 0;

		if (tsec < 3600) {
			final long min = tsec / 60;
			final long sec = tsec % 60;
			return LanguageClass.format("%02d:%02d", min, sec);
		} else {
			final long hour = tsec / 3600;
			final long min = (tsec - hour * 3600) / 60;
			final long sec = tsec % 60;
			return LanguageClass.format("%02d:%02d:%02d", hour, min, sec);
		}
	}

	public static String formatOnlySecs(long tsec) {
		if (tsec < 0)
			tsec = 0;

		if (tsec >= 60)
			return format(tsec);
		else
			return LanguageClass.format("%02d", tsec);
	}
}
