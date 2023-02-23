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

import mhahnFr.SecretPathway.core.lpc.parser.tokenizer.TokenType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * This class acts as a context of interpreted LPC source code.
 *
 * @author mhahnFr
 * @since 21.02.23
 */
public class Context extends Instruction {
    /** The parent context.                           */
    private final Context parent;
    /** The instructions found in this scope context. */
    private Map<Integer, Instruction> instructions = new TreeMap<>();

    /**
     * Constructs a global scope.
     */
    public Context() {
        this(null, 0);
    }

    /**
     * Constructs a scope context with the given beginning
     * position and inside the given parent.
     *
     * @param parent the parent scope context
     * @param begin  the beginning position
     */
    public Context(final Context parent, final int begin) {
        super(begin);

        this.parent = parent;
    }

    /**
     * Pushes and returns a new scope context.
     *
     * @param begin the beginning position of the new context
     * @return the new context
     */
    public Context pushScope(final int begin) {
        final var newContext = new Context(this, begin);
        instructions.put(begin, newContext);
        return newContext;
    }

    /**
     * Closes this scope context and returns the parent context.
     *
     * @param end the end position
     * @return the parent scope context
     */
    public Context popScope(final int end) {
        setEnd(end);

        return parent;
    }

    /**
     * Returns the {@link Instruction} available at the given position.
     *
     * @param at the position
     * @return the available instructions
     */
    public List<Instruction> availableInstructions(final int at) {
        final var toReturn = new ArrayList<Instruction>();

        if (parent != null) {
            toReturn.addAll(parent.availableInstructions(at));
        }

        for (final var instruction : instructions.entrySet()) {
            if (instruction.getKey() < at) {
                toReturn.add(instruction.getValue());
            }
        }

        return toReturn;
    }

    public void addIdentifier(final String name, final TokenType type) {}
}
