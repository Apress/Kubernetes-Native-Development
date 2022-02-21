package com.apress.kubdev.rss;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class ApplicationProperties {
	private static final String BUNDLE_NAME = "com.apress.kubdev.rss.application";

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

	private ApplicationProperties() {
	}

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
