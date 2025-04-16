package com.sqlexecutor.ui.i18n;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Manages language resources for the application.
 * Automatically loads the appropriate language bundle based on system settings.
 */
public class LanguageManager {
    private static final String BUNDLE_NAME = "com.sqlexecutor.i18n.messages";
    private static ResourceBundle bundle;
    
    static {
        loadBundle();
    }
    
    /**
     * Loads the appropriate language bundle based on system locale.
     */
    public static void loadBundle() {
        Locale locale = Locale.getDefault();
        
        // If language is Chinese, use Chinese bundle, otherwise default to English
        if (locale.getLanguage().equals(Locale.CHINESE.getLanguage())) {
            bundle = ResourceBundle.getBundle(BUNDLE_NAME, Locale.CHINESE);
        } else {
            bundle = ResourceBundle.getBundle(BUNDLE_NAME, Locale.ENGLISH);
        }
    }
    
    /**
     * Gets a localized string from the resource bundle.
     * 
     * @param key The key for the string in the resource bundle
     * @return The localized string
     */
    public static String getString(String key) {
        try {
            return bundle.getString(key);
        } catch (Exception e) {
            return "!" + key + "!";
        }
    }
    
    /**
     * Changes the current language to the specified locale.
     * 
     * @param locale The locale to switch to
     */
    public static void setLocale(Locale locale) {
        Locale.setDefault(locale);
        loadBundle();
    }
}