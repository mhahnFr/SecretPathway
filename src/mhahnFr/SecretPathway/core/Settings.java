/*
 * SecretPathway - A MUD client.
 *
 * Copyright (C) 2022 - 2023  mhahnFr
 *
 * This file is part of the SecretPathway. This program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program, see the file LICENSE.  If not, see <https://www.gnu.org/licenses/>.
 */

package mhahnFr.SecretPathway.core;

import mhahnFr.SecretPathway.SecretPathway;
import mhahnFr.SecretPathway.gui.editor.theme.DefaultTheme;
import mhahnFr.SecretPathway.gui.editor.theme.SPTheme;
import mhahnFr.SecretPathway.gui.editor.theme.json.JSONTheme;
import mhahnFr.utils.SettingsListener;
import mhahnFr.utils.gui.DarkModeListener;

import java.util.List;
import java.util.Vector;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * This class provides a singleton using which the settings and states of
 * the application can be stored. Access the singleton using {@link Settings#getInstance()}.
 *
 * @since 31.10.2022
 * @author mhahnFr
 */
public final class Settings {
    /** The one and only instance of this class. */
    private static final Settings instance = new Settings();

    /** The {@link Preferences} instance.        */
    private final Preferences preferences;
    /** The {@link DarkModeListener}s.           */
    private final List<DarkModeListener> darkListeners;
    /** The {@link SettingsListener}s.           */
    private final List<SettingsListener> listeners;
    /** The cached editor theme.                 */
    private SPTheme cachedTheme;

    /**
     * Constructs this settings object.
     */
    private Settings() {
        preferences   = Preferences.userNodeForPackage(SecretPathway.class);
        darkListeners = new Vector<>();
        listeners     = new Vector<>();
    }

    /**
     * Calls all {@link SettingsListener}s with the given values.
     *
     * @param key the key of the changed setting
     * @param newValue the new value of the changed setting
     */
    private void callListeners(final String key, final Object newValue) {
        for (final var listener : listeners) {
            listener.settingChanged(key, newValue);
        }
    }

    /**
     * Adds a dark mode listener.
     *
     * @param listener the new listener to be added
     */
    public void addDarkModeListener(final DarkModeListener listener) {
        darkListeners.add(listener);
    }

    /**
     * Adds the given listener to this instance.
     *
     * @param listener the listener to be added
     */
    public void addListener(final SettingsListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes the given dark mode listener.
     *
     * @param listener the listener to be removed
     */
    public void removeDarkModeListener(final DarkModeListener listener) {
        darkListeners.remove(listener);
    }

    /**
     * Removes the given listener from this instance.
     *
     * @param listener the listener to be removed
     */
    public void removeListener(final SettingsListener listener) {
        listeners.remove(listener);
    }

    /**
     * Returns the saved hostname or {@code null} if not set.
     *
     * @return the saved hostname
     */
    public String getHostname() {
        return preferences.get(Keys.HOSTNAME, null);
    }

    /**
     * Returns the saved port number or {@code -1} if not set.
     *
     * @return the saved port
     */
    public int getPort() {
        return preferences.getInt(Keys.PORT, -1);
    }

    /**
     * Returns the saved width of the main window or -1 if not set.
     *
     * @return the saved width of the main window
     */
    public int getWindowWidth() {
        return preferences.getInt(Keys.WINDOW_WIDTH, -1);
    }

    /**
     * Returns the saved height of the main window or -1 if not set.
     *
     * @return the saved height of the main window
     */
    public int getWindowHeight() {
        return preferences.getInt(Keys.WINDOW_HEIGHT, -1);
    }

    /**
     * Returns the saved X-coordinate of the window location or -1 if not set.
     *
     * @return the saved X-coordinate of the window
     */
    public int getWindowLocationX() {
        return preferences.getInt(Keys.WINDOW_LOCATION_X, -1);
    }

    /**
     * Returns the saved Y-coordinate of the window location or -1 if not set.
     *
     * @return the saved Y-coordinate of the window
     */
    public int getWindowLocationY() {
        return preferences.getInt(Keys.WINDOW_LOCATION_Y, -1);
    }

    /**
     * Returns the stored font size.
     *
     * @return the store font size
     */
    public int getFontSize() {
        return preferences.getInt(Keys.FONT_SIZE, 12);
    }

    /**
     * Returns whether the dark mode should be enabled.
     *
     * @return whether the dark mode is enabled
     */
    public boolean getDarkMode() {
        return preferences.getInt(Keys.DARK_MODE, 0) != 0;
    }

    /**
     * Returns whether the syntax highlighting should be enabled
     * by default in the LPC editor.
     *
     * @return whether to enable the syntax highlighting by default
     */
    public boolean getSyntaxHighlighting() {
        return preferences.getInt(Keys.EDITOR_SYNTAX_HIGHLIGHTING, 1) != 0;
    }

    /**
     * Returns whether the LPC editor should be inlined into the
     * main window.
     *
     * @return whether to inline the editor
     */
    public boolean getEditorInlined() {
        return preferences.getInt(Keys.EDITOR_INLINED, 0) != 0;
    }

    /**
     * Returns the path to theme to be used by the editor.
     *
     * @return the path to the theme
     */
    public String getEditorThemePath() {
        return preferences.get(Keys.EDITOR_THEME_PATH, "");
    }

    /**
     * Returns whether StartTLS is enabled.
     *
     * @return whether StartTLS is enabled
     */
    public boolean getStartTLS() {
        return preferences.getInt(Keys.START_TLS, 0) == 1;
    }

    /**
     * Returns the width of the editor window.
     *
     * @return the width
     */
    public int getEditorWindowWidth() {
        return preferences.getInt(Keys.EDITOR_WINDOW_WIDTH, -1);
    }

    /**
     * Returns the height of the editor window.
     *
     * @return the height
     */
    public int getEditorWindowHeight() {
        return preferences.getInt(Keys.EDITOR_WINDOW_HEIGHT, -1);
    }

    /**
     * Returns the X-coordinate of the editor window.
     *
     * @return the X-coordinate
     */
    public int getEditorWindowX() {
        return preferences.getInt(Keys.EDITOR_WINDOW_X, -1);
    }

    /**
     * Returns the Y-coordinate of the editor window.
     *
     * @return the Y-coordinate
     */
    public int getEditorWindowY() {
        return preferences.getInt(Keys.EDITOR_WINDOW_Y, -1);
    }

    /**
     * Returns whether to use UTF-8 by default.
     *
     * @return whether to use UTF-8 by default
     */
    public boolean useUTF8() {
        return preferences.getInt(Keys.USE_UTF8, 1) == 1;
    }

    /**
     * Returns the theme that should be used by the editor.
     * If a cached theme is available it is returned. Otherwise,
     * the theme in the stored path is opened.
     * If no path is stored or the file could not be read, a
     * {@link DefaultTheme} is cached and returned.
     *
     * @return the theme to be used by the editor
     */
    public SPTheme getEditorTheme() {
        if (cachedTheme == null) {
            final var editorThemePath = getEditorThemePath();
            if (editorThemePath.isBlank()) {
                cachedTheme = new DefaultTheme();
            } else {
                final var from = JSONTheme.from(editorThemePath);
                cachedTheme = from == null ? new DefaultTheme() : from;
            }
        }
        return cachedTheme;
    }

    /**
     * Returns whether to use the native Look & Feel implementation.
     *
     * @return whether the native L&F should be used
     */
    public boolean getNativeLookAndFeel() {
        return preferences.getInt(Keys.NATIVE_LF, 1) == 1;
    }

    /**
     * Sets whether to use the native Look & Feel implementation.
     *
     * @param enabled whether the native L&F should be used
     */
    public void setNativeLookAndFeel(final boolean enabled) {
        callListeners(Keys.NATIVE_LF, enabled);
        preferences.putInt(Keys.NATIVE_LF, enabled ? 1 : 0);
    }

    /**
     * Stores the given hostname.
     *
     * @param hostname the hostname to store
     */
    public Settings setHostname(String hostname) {
        callListeners(Keys.HOSTNAME, hostname);
        preferences.put(Keys.HOSTNAME, hostname);
        return this;
    }

    /**
     * Stores the given port number.
     *
     * @param port the port number to store
     */
    public Settings setPort(int port) {
        callListeners(Keys.PORT, port);
        preferences.putInt(Keys.PORT, port);
        return this;
    }

    /**
     * Stores the window location.
     *
     * @param x the X-coordinate of the window position
     * @param y the Y-coordinate of the window position
     */
    public Settings setWindowLocation(int x, int y) {
        callListeners(Keys.WINDOW_LOCATION_X, x);
        callListeners(Keys.WINDOW_LOCATION_Y, y);
        preferences.putInt(Keys.WINDOW_LOCATION_X, x);
        preferences.putInt(Keys.WINDOW_LOCATION_Y, y);
        return this;
    }

    /**
     * Stores the window size.
     *
     * @param width the width of the window
     * @param height the height of the window
     */
    public Settings setWindowSize(int width, int height) {
        callListeners(Keys.WINDOW_WIDTH, width);
        callListeners(Keys.WINDOW_HEIGHT, height);
        preferences.putInt(Keys.WINDOW_WIDTH, width);
        preferences.putInt(Keys.WINDOW_HEIGHT, height);
        return this;
    }

    /**
     * Sets the font size.
     *
     * @param size the new font size
     * @return this instance
     */
    public Settings setFontSize(int size) {
        callListeners(Keys.FONT_SIZE, size);
        preferences.putInt(Keys.FONT_SIZE, size);
        return this;
    }

    /**
     * Sets whether the dark mode should be enabled.
     *
     * @param enabled whether the dark mode is enabled
     * @return this instance
     */
    public Settings setDarkMode(boolean enabled) {
        final var value = enabled ? 1 : 0;

        callListeners(Keys.DARK_MODE, enabled);
        preferences.putInt(Keys.DARK_MODE, value);

        for (final var listener : darkListeners) {
            listener.darkModeToggled(enabled);
        }

        return this;
    }

    /**
     * Sets whether the syntax highlighting should be enabled by
     * default in the LPC editor.
     *
     * @param enabled whether to enable the syntax highlighting
     * @return this instance
     */
    public Settings setSyntaxHighlighting(final boolean enabled) {
        final var value = enabled ? 1 : 0;

        callListeners(Keys.EDITOR_SYNTAX_HIGHLIGHTING, enabled);
        preferences.putInt(Keys.EDITOR_SYNTAX_HIGHLIGHTING, value);

        return this;
    }

    /**
     * Sets whether the LPC editor should be inlined into the
     * main window.
     *
     * @param inlined whether to inline the editor
     * @return this instance
     */
    public Settings setEditorInlined(final boolean inlined) {
        final var value = inlined ? 1 : 0;

        callListeners(Keys.EDITOR_INLINED, inlined);
        preferences.putInt(Keys.EDITOR_INLINED, value);

        return this;
    }

    /**
     * Sets the path to the theme used for the editor. Invalidates
     * the cached theme.
     *
     * @param path the path
     * @return this instance
     */
    public Settings setEditorThemePath(final String path) {
        return setEditorTheme(path, null);
    }

    /**
     * Sets the path to the editor theme and caches the given
     * {@link SPTheme}.
     *
     * @param path the path
     * @param cache the theme to be cached
     * @return this instance
     */
    public Settings setEditorTheme(final String path, final SPTheme cache) {
        preferences.put(Keys.EDITOR_THEME_PATH, path == null ? "" : path);
        cachedTheme = cache;
        callListeners(Keys.EDITOR_THEME_PATH, path);
        return this;
    }

    /**
     * Sets whether to enable StartTLS.
     *
     * @param enabled whether StartTLS is enabled
     * @return this instance
     */
    public Settings setStartTLS(final boolean enabled) {
        final var value = enabled ? 1 : 0;

        callListeners(Keys.START_TLS, enabled);
        preferences.putInt(Keys.START_TLS, value);

        return this;
    }

    /**
     * Stores the size of the editor window.
     *
     * @param width the width of the editor window
     * @param height the height of the editor window
     * @return this instance
     */
    public Settings setEditorWindowSize(int width, int height) {
        callListeners(Keys.EDITOR_WINDOW_WIDTH, width);
        callListeners(Keys.EDITOR_WINDOW_HEIGHT, height);
        preferences.putInt(Keys.EDITOR_WINDOW_WIDTH, width);
        preferences.putInt(Keys.EDITOR_WINDOW_HEIGHT, height);
        return this;
    }

    /**
     * Stores the location of the editor window.
     *
     * @param x the X-coordinate of the editor window
     * @param y the Y-coordinate of the editor window
     * @return this instance
     */
    public Settings setEditorWindowLocation(int x, int y) {
        callListeners(Keys.EDITOR_WINDOW_X, x);
        callListeners(Keys.EDITOR_WINDOW_Y, y);
        preferences.putInt(Keys.EDITOR_WINDOW_X, x);
        preferences.putInt(Keys.EDITOR_WINDOW_Y, y);
        return this;
    }

    /**
     * Sets whether to use UTF-8 by default.
     *
     * @param useIt whether to use it
     * @return this instance
     */
    public Settings setUseUTF8(final boolean useIt) {
        final var value = useIt ? 1 : 0;

        callListeners(Keys.USE_UTF8, useIt);
        preferences.putInt(Keys.USE_UTF8, value);

        return this;
    }

    /**
     * Attempts to flush the underlying {@link Preferences}. Returns whether the
     * operation was successful.
     *
     * @return whether the settings were flushed successfully
     */
    public boolean flush() {
        try {
            preferences.flush();
        } catch (BackingStoreException e) {
            return false;
        }
        return true;
    }

    /**
     * Removes all previously stored settings.
     *
     * @return whether the underlying {@link Preferences} instance was cleared successfully
     */
    public boolean removeAll() {
        try {
            preferences.clear();
        } catch (BackingStoreException e) {
            return false;
        }
        return true;
    }

    /**
     * Returns the single instance of this class.
     *
     * @return the one and only instance of this class
     */
    public static Settings getInstance() { return instance; }

    /**
     * Helper class that contains the keys used to store the settings.
     */
    public static final class Keys {
        /** The identifier used for all keys.                               */
        private static final String BUNDLE_ID = "mhahnFr.SecretPathway";

        /** Key for the hostname or IP address.                             */
        public static final String HOSTNAME                   = BUNDLE_ID + ".hostname";
        /** Key for the port.                                               */
        public static final String PORT                       = BUNDLE_ID + ".port";
        /** Key for the width of the main window.                           */
        public static final String WINDOW_WIDTH               = BUNDLE_ID + ".windowWidth";
        /** Key for the height of the main window.                          */
        public static final String WINDOW_HEIGHT              = BUNDLE_ID + ".windowHeight";
        /** Key for the X-coordinate of the window position.                */
        public static final String WINDOW_LOCATION_X          = BUNDLE_ID + ".windowLocationX";
        /** Key for the Y-coordinate of the window position.                */
        public static final String WINDOW_LOCATION_Y          = BUNDLE_ID + ".windowLocationY";
        /** The key used to store the font size.                            */
        public static final String FONT_SIZE                  = BUNDLE_ID + ".fontSize";
        /** The key used to store the dark mode state.                      */
        public static final String DARK_MODE                  = BUNDLE_ID + ".darkMode";
        /** The key used to store the auto dark mode.                       */
        public static final String AUTO_DARK_MODE             = BUNDLE_ID + ".autoDarkMode";
        /** The key used to store the StartTLS state.                       */
        public static final String START_TLS                  = BUNDLE_ID + ".startTLS";
        /** The key used to store the UTF-8 state.                          */
        public static final String USE_UTF8                   = BUNDLE_ID + ".useUTF8";
        /** The key used to store the syntax highlighting state.            */
        public static final String EDITOR_SYNTAX_HIGHLIGHTING = BUNDLE_ID + ".editorSyntaxHighlighting";
        /** The key used to store the editor inlining state.                */
        public static final String EDITOR_INLINED             = BUNDLE_ID + ".editorInlined";
        /** The key used to store the path to the theme used by the editor. */
        public static final String EDITOR_THEME_PATH          = BUNDLE_ID + ".editorThemePath";
        /** The key used to store the X-coordinate of the editor window.    */
        public static final String EDITOR_WINDOW_X            = BUNDLE_ID + ".editorWindowX";
        /** The key used to store the Y-coordinate of the editor window.    */
        public static final String EDITOR_WINDOW_Y            = BUNDLE_ID + ".editorWindowY";
        /** The key used to store the width of the editor window.           */
        public static final String EDITOR_WINDOW_WIDTH        = BUNDLE_ID + ".editorWindowWidth";
        /** The key used to store the height of the editor window.          */
        public static final String EDITOR_WINDOW_HEIGHT       = BUNDLE_ID + ".editorWindowHeight";
        /** The key used to store the usage of the native Look&Feel usage.  */
        public static final String NATIVE_LF                  = BUNDLE_ID + ".nativeLookAndFeel";
    }
}
