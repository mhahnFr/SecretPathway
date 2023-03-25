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
 * This class represents a file manager for LPC source files
 * found on the disk this application is running on.
 *
 * @author mhahnFr
 * @since 25.03.23
 */
public class LocalFileManager implements LPCFileManager {
    @Override
    public String load(String fileName) throws Exception {
        // TODO: Implement
        return null;
    }

    @Override
    public void save(String fileName, String content) throws Exception {
        // TODO: Implement
    }
}
