package pt.rmartins.battleships.utilities;

import java.util.Locale;

import pt.rmartins.battleships.R;
import android.content.res.Configuration;
import android.content.res.Resources;

public class LanguageClass {

	private static Locale currentLanguage;
	private static Resources res;

	public static void initialize(Locale currentLanguage, Resources res) {
		LanguageClass.currentLanguage = currentLanguage;
		LanguageClass.res = res;
	}

	public static void setLanguage(Locale language) {
		currentLanguage = language;
		Locale.setDefault(currentLanguage);
		Configuration config = new Configuration();
		config.locale = currentLanguage;
		res.updateConfiguration(config, res.getDisplayMetrics());
	}

	public static Locale getCurrentLanguage() {
		return currentLanguage;
	}

	public static void setNextLanguage() {
		String[] languages = res.getStringArray(R.array.languages);
		final String language = currentLanguage.getLanguage();
		for (int i = 0; i < languages.length; i++) {
			if (language.equals(LanguageClass.getLocale(languages[i]).getLanguage())) {
				setLanguage(LanguageClass.getLocale(languages[(i + 1) % languages.length]));
			}
		}
	}

	public static Locale getLocale(String str) {
		final Locale locale;
		if (str.contains("-")) {
			final String[] split = str.split("-");
			locale = new Locale(split[0], split[1]);
		} else {
			locale = new Locale(str);
		}
		return locale;
	}

	public static String getString(int resourceCode) {
		return res.getString(resourceCode);
	}

	public static String getString(int resourceCode, Object... params) {
		return res.getString(resourceCode, params);
	}

	public static String format(String format, Object... args) {
		return String.format(currentLanguage, format, args);
	}

}