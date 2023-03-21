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

package mhahnFr.SecretPathway.core.lpc.interpreter;

import mhahnFr.SecretPathway.gui.editor.HighlightType;

/**
 * This enumeration contains the possible interpretation
 * types.
 *
 * @author mhahnFr
 * @since 20.02.23
 */
public enum InterpretationType implements HighlightType {
    /** Indicates a syntax error.      */
    ERROR,
    /** Indicates a warning.           */
    WARNING,
    /** Indicates a not found symbol.  */
    NOT_FOUND,
    /** Indicates a not found builtin. */
    NOT_FOUND_BUILTIN,
    /** Indicates a type mismatch.     */
    TYPE_MISMATCH
}
