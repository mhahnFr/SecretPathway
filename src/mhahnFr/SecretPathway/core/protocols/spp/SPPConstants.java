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

package mhahnFr.SecretPathway.core.protocols.spp;

/**
 * This class defines the constants used for the SecretPathwayProtocol (SPP).
 *
 * @author mhahnFr
 * @since 10.12.22
 */
public abstract class SPPConstants {
    /** Indicates the start of an SPP escape sequence. */
    public static final char BEGIN = 0x02;
    /** Indicates the end of an SPP escape sequence.   */
    public static final char END = 0x03;
}
