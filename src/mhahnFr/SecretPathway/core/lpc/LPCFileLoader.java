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
public interface LPCFileLoader {
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
}
