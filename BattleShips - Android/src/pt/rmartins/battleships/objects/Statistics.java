package pt.rmartins.battleships.objects;

public interface Statistics {

	public int getTotalShotsFired();

	public int getTotalShotsHitted();

	public double getTotalTimeTurns();

	public float getAimPercentage();

	public double getMedianTurnTime();

	public float getPercentageGameCleared();

	/**
	 * @return Total Fleet Size
	 */
	public int getTotalShips();

	/**
	 * 
	 * @return Total Ships Sunked
	 */
	public int getTotalSunkedShips();

}
