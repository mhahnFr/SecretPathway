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

import mhahnFr.SecretPathway.core.lpc.parser.ast.ASTType;
import mhahnFr.SecretPathway.core.lpc.parser.ast.ASTTypeDefinition;
import mhahnFr.SecretPathway.gui.editor.Suggestion;

import java.util.*;

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
    private final Map<Integer, Instruction> instructions = new TreeMap<>();

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
     * Returns the {@link Definition}s available at the given position.
     *
     * @param at the position
     * @return the available instructions
     */
    public List<Suggestion> availableDefinitions(final int at) {
        final var toReturn = new ArrayList<Suggestion>();

        for (final var instruction : instructions.entrySet()) {
            if (instruction.getKey() < at) {
                final var value = instruction.getValue();
                if (value instanceof Definition) {
                    toReturn.add(new Suggestion(value.getReturnType(), ((Definition) value).getName()));
                } else if (value instanceof Context && at < value.getEnd()) {
                    toReturn.addAll(((Context) value).availableDefinitions(at));
                }
            }
        }

        return toReturn;
    }

    public Definition getIdentifier(final String name, final int begin) {
        for (final var element : instructions.entrySet()) {
            if (element.getKey() < begin                 &&
                element.getValue() instanceof Definition &&
                ((Definition) element.getValue()).getName().equals(name)) {
                return (Definition) element.getValue();
            }
        }
        if (parent != null) {
            return parent.getIdentifier(name, begin);
        }
        return null;
    }

    /**
     * Adds the given identifier to this context.
     *
     * @param name the name of the identifier
     * @param type the type of the identifier
     * @param kind the AST type of the identifier
     */
    public void addIdentifier(final int begin, final String name, final ASTTypeDefinition type, final ASTType kind) {
        instructions.put(begin, new Definition(begin, name, type, kind));
    }
}
