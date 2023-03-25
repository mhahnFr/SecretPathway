/*
 * SecretPathway - A MUD client.
 *
 * Copyright (C) 2023  mhahnFr
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

package mhahnFr.SecretPathway.core.lpc;

/**
 * This interface defines an LPC file loader.
 *
 * @author mhahnFr
 * @since 25.03.23
 */
public interface LPCFileManager {
    /**
     * Loads the file named by the given parameter. If the
     * file could not be loaded, an {@link Exception} is thrown.
     * Otherwise, the content of the file is returned.
     *
     * @param fileName the name of the file to be loaded
     * @return the content of the file
     * @throws Exception if the file could not be loaded for some reason
     */
    String load(final String fileName) throws Exception;

    /**
     * Saves the given content in a file with the given name.
     *
     * @param fileName the name of the file
     * @param content  the content of the file
     * @throws Exception if the file could not be written for some reason
     */
    void save(final String fileName, final String content) throws Exception;

    /**
     * Returns whether this manager can compile files.
     *
     * @return whether this instance supports compilations
     */
    default boolean canCompile() {
        return false;
    }

    /**
     * Attempts to compile the source file named by the given parameter.
     * <br>
     * The default implementation throws an {@link UnsupportedOperationException}.
     *
     * @param fileName the file to be compiled
     */
    default void compile(final String fileName) {
        throw new UnsupportedOperationException("Not implemented! Hint: Check using LPCFileManager#canCompile()");
    }
}
