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

import mhahnFr.SecretPathway.core.lpc.interpreter.Context;
import mhahnFr.SecretPathway.core.lpc.interpreter.Interpreter;
import mhahnFr.SecretPathway.core.lpc.parser.Parser;

import java.util.HashMap;
import java.util.Map;

/**
 * This interface defines an LPC file loader.
 *
 * @author mhahnFr
 * @since 25.03.23
 */
public abstract class LPCFileManager {
    /** A mapping with the cached contexts, mapped to their file name. */
    private final Map<String, Context> cachedContexts = new HashMap<>();

    /**
     * Loads the file named by the given parameter. If the
     * file could not be loaded, an {@link Exception} is thrown.
     * Otherwise, the content of the file is returned.
     *
     * @param fileName the name of the file to be loaded
     * @return the content of the file
     * @throws Exception if the file could not be loaded for some reason
     */
    public abstract String load(final String fileName, String referrer) throws Exception;

    /**
     * Loads and parses the file named by the given parameter. The result
     * of the parsing is returned and cached for later use.
     *
     * @param fileName the name of the file to load and parse
     * @return the {@link Context} of the interpretation
     * @throws Exception if an error happens during resolving
     * @see #load(String, String)
     */
    public Context loadAndParse(final String fileName, final String referrer) throws Exception {
        final var toReturn = cachedContexts.get(fileName);
        if (toReturn == null) {
            return loadAndParseIntern(fileName, referrer);
        }
        return toReturn;
    }

    /**
     * Loads and parses the file named by the given parameter. Does not touch
     * the cache.
     *
     * @param fileName the name of the file to load and parse
     * @return the {@link Context} of the interpretation
     * @throws Exception if an error happens during resolving
     * @see #load(String, String)
     */
    private Context loadAndParseIntern(final String fileName, final String referrer) throws Exception {
        final var context = new Interpreter(this).createContextFor(new Parser(load(fileName, referrer)).parse());
        cachedContexts.put(fileName, context);
        return context;
    }

    /**
     * Saves the given content in a file with the given name.
     *
     * @param fileName the name of the file
     * @param content  the content of the file
     * @throws Exception if the file could not be written for some reason
     */
    public abstract void save(final String fileName, final String content) throws Exception;

    /**
     * Returns whether this manager can compile files.
     *
     * @return whether this instance supports compilations
     */
    public boolean canCompile() {
        return false;
    }

    /**
     * Attempts to compile the source file named by the given parameter.
     * <br>
     * The default implementation throws an {@link UnsupportedOperationException}.
     *
     * @param fileName the file to be compiled
     */
    public void compile(final String fileName) {
        throw new UnsupportedOperationException("Not implemented! Hint: Check using LPCFileManager#canCompile()");
    }
}
