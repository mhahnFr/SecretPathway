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

import java.util.Map;
import java.util.TreeMap;

/**
 * This class acts as a context of interpreted LPC source code.
 *
 * @author mhahnFr
 * @since 21.02.23
 */
public class Context extends Instruction {
    private final Context parent;
    private Map<Integer, Instruction> instructions = new TreeMap<>();

    public Context() {
        this(null, 0);
    }

    public Context(final Context parent, final int begin) {
        super(begin);

        this.parent = parent;
    }

    public Context pushScope(final int begin) {
        final var newContext = new Context(this, begin);
        instructions.put(begin, newContext);
        return newContext;
    }

    public Context popScope(final int end) {
        setEnd(end);

        return parent;
    }
}
