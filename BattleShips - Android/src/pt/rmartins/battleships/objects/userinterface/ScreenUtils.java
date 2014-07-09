package pt.rmartins.battleships.objects.userinterface;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import pt.rmartins.battleships.R;
import pt.rmartins.battleships.objects.Game;
import pt.rmartins.battleships.objects.PlayerClass.Shot.KindShot;
import pt.rmartins.battleships.objects.ShipClass;
import pt.rmartins.battleships.objects.modes.GameBonus;
import pt.rmartins.battleships.objects.modes.GameBonus.BonusTypes;
import pt.rmartins.battleships.objects.modes.GameBonus.ExtraTurn;
import pt.rmartins.battleships.objects.modes.GameMode;
import pt.rmartins.battleships.objects.modes.GameMode.BonusPlay;
import pt.rmartins.battleships.objects.modes.GameMode.MessagesMode;
import pt.rmartins.battleships.objects.modes.GameMode.ShipExtraInfo;
import pt.rmartins.battleships.objects.modes.GameMode.TimeLimitType;
import pt.rmartins.battleships.utilities.Draw;
import pt.rmartins.battleships.utilities.LanguageClass;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;

public abstract class ScreenUtils extends UserInterfaceClass {

	private static float TAB_SIZE = 25f;

	private static float MISSILES_IMAGES_SIZE_X = 11f;
	private static float MISSILES_IMAGES_SIZE_Y = 33f;

	private static final int GAMEINFO_FIELD_SIZE_CODE = R.string.gameinfo_field_size;
	private static final int GAMEINFO_TIMELIMIT_UNLIMITED_CODE = R.string.gameinfo_timelimit_unlimited;
	private static final int GAMEINFO_TIMELIMIT_CODE = R.string.gameinfo_timelimit;
	private static final int GAMEINFO_TIMELIMIT_TURN_CODE = R.string.gameinfo_timelimit_turn;
	private static final int GAMEINFO_TIMELIMIT_EXTRA_FAST_CODE = R.string.gameinfo_timelimit_extra_fast;
	private static final int GAMEINFO_MESSAGES_MODE_CODE = R.string.gameinfo_messages_mode;
	private static final int GAMEINFO_FULLSHIELD_CODE = R.string.gameinfo_full_shield;
	private static final int GAMEINFO_MULTIKILL_BONUS_CODE = R.string.gameinfo_multikill_bonus;

	//	private static final Matrix IMAGE_MATRIX;
	private static Locale lastLanguague;
	private static final Paint IMAGE_PAINT;
	static {
		//		IMAGE_MATRIX = new Matrix();
		lastLanguague = null;

		IMAGE_PAINT = new Paint(Paint.ANTI_ALIAS_FLAG);
		IMAGE_PAINT.setStyle(Style.FILL);
	}
	private static String GAMEINFO_TIMELIMIT_UNLIMITED_TEXT, GAMEINFO_MULTIKILL_BONUS_TEXT;

	private static Bitmap MISSILE_IMAGE, MISSILE_INDESTRUCTIBLE_IMAGE, SHIELD_IMAGE;

	private ScreenUtils() {
	}

	public static void initializeScreenMultiplier(Context context, float SCREEN_SUPPORT_MULTIPLIER) {
		TAB_SIZE *= SCREEN_SUPPORT_MULTIPLIER;
		MISSILES_IMAGES_SIZE_X *= SCREEN_SUPPORT_MULTIPLIER;
		MISSILES_IMAGES_SIZE_Y *= SCREEN_SUPPORT_MULTIPLIER;

		final Resources res = context.getResources();
		Bitmap missileImage = BitmapFactory.decodeResource(res, R.drawable.missile_image);
		MISSILE_IMAGE = Bitmap.createScaledBitmap(missileImage, (int) MISSILES_IMAGES_SIZE_X,
				(int) MISSILES_IMAGES_SIZE_Y, false);

		Bitmap indestructibleMissileImage = BitmapFactory.decodeResource(res, R.drawable.missile_image_indestructible);
		MISSILE_INDESTRUCTIBLE_IMAGE = Bitmap.createScaledBitmap(indestructibleMissileImage,
				(int) MISSILES_IMAGES_SIZE_X, (int) MISSILES_IMAGES_SIZE_Y, false);

		SHIELD_IMAGE = BitmapFactory.decodeResource(res, R.drawable.shield);

		missileImage.recycle();
		indestructibleMissileImage.recycle();
	}

	private static void updateText() {
		GAMEINFO_TIMELIMIT_UNLIMITED_TEXT = LanguageClass.getString(GAMEINFO_TIMELIMIT_UNLIMITED_CODE);
		GAMEINFO_MULTIKILL_BONUS_TEXT = LanguageClass.getString(GAMEINFO_MULTIKILL_BONUS_CODE);
	}

	public static void drawGameInfo(Canvas canvas, Game game, GameMode gameMode, RectF GAME_INFO_AREA,
			Paint LEFT_TEXT_PAINT, Paint CENTER_TEXT_PAINT, boolean showFieldSize) {
		if (lastLanguague != LanguageClass.getCurrentLanguage()) {
			lastLanguague = LanguageClass.getCurrentLanguage();
			updateText();
		}

		canvas.save();
		canvas.clipRect(GAME_INFO_AREA);

		final float TEXT_HEIGHT = Draw.getStrHeight(LEFT_TEXT_PAINT);
		final float dy = TEXT_HEIGHT + OUT_PADDING * 2;
		float x = GAME_INFO_AREA.left;
		float y = GAME_INFO_AREA.top + TEXT_HEIGHT;

		canvas.drawText(gameMode.getName(), GAME_INFO_AREA.centerX(), y, CENTER_TEXT_PAINT);

		if (showFieldSize) {
			y += dy;
			final int fieldX = game.getCurrentFleet().maxX;
			final int fieldY = game.getCurrentFleet().maxY;
			canvas.drawText(LanguageClass.getString(GAMEINFO_FIELD_SIZE_CODE, fieldX, fieldY), x, y, LEFT_TEXT_PAINT);
		}

		final TimeLimitType timeLimitType = gameMode.getTimeLimitType();
		final int timeLimit = gameMode.getTimeLimit();
		final String timeStr;
		if (timeLimitType == TimeLimitType.NoTimeLimit) {
			timeStr = GAMEINFO_TIMELIMIT_UNLIMITED_TEXT;
		} else if (timeLimitType == TimeLimitType.TotalTime) {
			timeStr = LanguageClass.getString(GAMEINFO_TIMELIMIT_CODE, timeLimit);
		} else if (timeLimitType == TimeLimitType.TotalTimeAndPerTurn) {
			timeStr = LanguageClass.getString(GAMEINFO_TIMELIMIT_TURN_CODE, timeLimit, gameMode.getTimePerTurn());
		} else if (timeLimitType == TimeLimitType.ExtraFastMode) {
			timeStr = LanguageClass.getString(GAMEINFO_TIMELIMIT_EXTRA_FAST_CODE, timeLimit, gameMode.getTimePerTurn(),
					gameMode.getTimeExtraPerTurn());
		} else {
			timeStr = "<Unknown>";
		}
		y += dy;
		canvas.drawText(timeStr, x, y, LEFT_TEXT_PAINT);

		final MessagesMode messagesMode = gameMode.getMessagesMode();
		final String mModeStr;
		if (messagesMode == MessagesMode.NORMAL) {
			mModeStr = LanguageClass.getString(GAMEINFO_MESSAGES_MODE_CODE, messagesMode.getName());
		} else if (messagesMode == MessagesMode.NDELAY) {
			mModeStr = LanguageClass.getString(GAMEINFO_MESSAGES_MODE_CODE, messagesMode.getName(),
					gameMode.getMessagesModeParameter());
		} else {
			mModeStr = "<Unknown>";

		}
		y += dy;
		canvas.drawText(mModeStr, x, y, LEFT_TEXT_PAINT);

		final float fullShield = gameMode.getFullShield();
		if (fullShield > 0) {
			y += dy;
			canvas.drawText(LanguageClass.getString(GAMEINFO_FULLSHIELD_CODE, fullShield * 100), x, y, LEFT_TEXT_PAINT);
		}

		final Set<BonusPlay> possibleBonus = gameMode.getPossibleBonus();
		if (!possibleBonus.isEmpty()) {
			y += dy;//+ OUT_PADDING * 2;
			canvas.drawText(GAMEINFO_MULTIKILL_BONUS_TEXT, x, y, LEFT_TEXT_PAINT);
			for (BonusPlay bonusPlay : possibleBonus) {
				x = GAME_INFO_AREA.left + TAB_SIZE;
				final String name = bonusPlay.condition.name;
				final List<GameBonus> actions = bonusPlay.actions;

				final float maxSpace = Math.max(MISSILES_IMAGES_SIZE_Y + OUT_PADDING * 2, dy);
				y += maxSpace / 2;

				final String text = name + ":";
				canvas.drawText(text, x, y + TEXT_HEIGHT / 2, LEFT_TEXT_PAINT);
				final float text_width = Draw.getStrWidth(LEFT_TEXT_PAINT, text);

				x += text_width + MISSILES_IMAGES_SIZE_X;
				for (GameBonus gameBonus : actions) {
					if (gameBonus.getType() == BonusTypes.ExtraTurn) {
						ExtraTurn extraTurn = (ExtraTurn) gameBonus;
						final List<KindShot> turnShots = extraTurn.getTurnShots();
						for (int i = 0; i < turnShots.size(); i++) {
							KindShot kindShot = turnShots.get(i);
							Bitmap image = null;
							if (kindShot == KindShot.NormalShot)
								image = MISSILE_IMAGE;
							else if (kindShot == KindShot.IndestructibleShot)
								image = MISSILE_INDESTRUCTIBLE_IMAGE;
							else
								image = MISSILE_IMAGE;
							canvas.drawBitmap(image, x + i * MISSILES_IMAGES_SIZE_X * 1.5f, y - MISSILES_IMAGES_SIZE_Y
									/ 2, IMAGE_PAINT);
						}
					}
					break;
				}
				y += maxSpace / 2;
			}
		}
		x = GAME_INFO_AREA.left;

		final Map<Integer, ShipExtraInfo> shipsExtraInfo = gameMode.getShipsExtraInfo();
		for (Entry<Integer, ShipExtraInfo> entry : shipsExtraInfo.entrySet()) {
			int shipId = entry.getKey();
			final ShipExtraInfo info = entry.getValue();

			String shipName = ShipClass.getName(shipId);
			y += dy;
			x = GAME_INFO_AREA.left;
			canvas.drawText(shipName + ":", x, y, LEFT_TEXT_PAINT);
			x = GAME_INFO_AREA.left + TAB_SIZE;
			final List<Double> shield = info.getShield();
			if (!shield.isEmpty()) {
				for (int i = 0; i < shield.size(); i++) {
					double value = shield.get(i);
					y += dy;
					canvas.drawText(LanguageClass.format("Shield: %2.1f%%", value * 100), x, y, LEFT_TEXT_PAINT);
				}
			}
			final int explosiveSize = info.getExplosiveSize();
			if (explosiveSize > 0) {
				y += dy;
				canvas.drawText(LanguageClass.format("Explosive: %d", explosiveSize), x, y, LEFT_TEXT_PAINT);
			}
		}

		//		final List<Turn> turnGameModes = gameMode.getTurnGameModes();
		//		for (Turn turn : turnGameModes) {
		//
		//		}

		canvas.restore();
	}
}
