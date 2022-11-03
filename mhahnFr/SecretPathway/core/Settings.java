/*
 * SecretPathway - A MUD client.
 *
 * Copyright (C) 2022  mhahnFr
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

import mhahnFr.SecretPathway.CLI;

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

    /** The {@link Preferences} instance.       */
    private final Preferences preferences;

    /**
     * Constructs this settings object.
     */
    private Settings() {
        preferences = Preferences.userNodeForPackage(CLI.class);
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

    public int getWindowWidth() {
        return preferences.getInt(Keys.WINDOW_WIDTH, -1);
    }

    public int getWindowHeight() {
        return preferences.getInt(Keys.WINDOW_HEIGHT, -1);
    }

    public int getWindowLocationX() {
        return preferences.getInt(Keys.WINDOW_LOCATION_X, -1);
    }

    public int getWindowLocationY() {
        return preferences.getInt(Keys.WINDOW_LOCATION_Y, -1);
    }

    /**
     * Stores the given hostname.
     *
     * @param hostname the hostname to store
     */
    public void setHostname(String hostname) {
        preferences.put(Keys.HOSTNAME, hostname);
    }

    /**
     * Stores the given port number.
     *
     * @param port the port number to store
     */
    public void setPort(int port) {
        preferences.putInt(Keys.PORT, port);
    }

    public void setWindowLocation(int x, int y) {
        preferences.putInt(Keys.WINDOW_LOCATION_X, x);
        preferences.putInt(Keys.WINDOW_LOCATION_Y, y);
    }

    public void setWindowSize(int width, int height) {
        preferences.putInt(Keys.WINDOW_WIDTH, width);
        preferences.putInt(Keys.WINDOW_HEIGHT, height);
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
    private static final class Keys {
        /** The identifier used for all keys.   */
        private static final String BUNDLE_ID = "mhahnFr.SecretPathway";

        /** Key for the hostname or IP address. */
        public static final String HOSTNAME          = BUNDLE_ID + ".hostname";
        /** Key for the port.                   */
        public static final String PORT              = BUNDLE_ID + ".port";
        public static final String WINDOW_WIDTH      = BUNDLE_ID + ".windowWidth";
        public static final String WINDOW_HEIGHT     = BUNDLE_ID + ".windowHeight";
        public static final String WINDOW_LOCATION_X = BUNDLE_ID + ".windowLocationX";
        public static final String WINDOW_LOCATION_Y = BUNDLE_ID + ".windowLocationY";
    }
}
