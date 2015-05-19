package org.iviPro.utils;

import java.util.prefs.Preferences;

public class PreferencesHelper {

	public static void storePreference(String key, String value) {
		Preferences root = Preferences.userRoot();
		root.put(key, value);
	}

	public static String getPreference(String key, String defaultValue) {
		Preferences root = Preferences.userRoot();
		return root.get(key, defaultValue);
	}
}
