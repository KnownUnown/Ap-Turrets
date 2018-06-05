package com.snowleopard1863.APTurrets;

import org.bukkit.entity.Player;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class I18n {
	private static I18n instance;
	private ResourceBundle bundle;

	private I18n() {
	}

	public void pprintf(Player player, String id, Object... args) {
		player.sendMessage(sprintf(id, args));
	}

	public String sprintf(String id, Object... args) {
		String localized;
		try {
			localized = bundle.getString(id);
		} catch(MissingResourceException e) {
			return id;
		}
		return String.format(localized, args);
	}

	public void init(Config config) {
		if(bundle != null)
			throw new RuntimeException("Singleton already initialized.");
		bundle = ResourceBundle.getBundle(
				"com.snowleopard1863.APTurrets.Localization", config.locale.toJavaLocale());
	}

	public static I18n getInstance() {
		if(instance == null) instance = new I18n();
		return instance;
	}
}
