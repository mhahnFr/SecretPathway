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

package mhahnFr.SecretPathway.core.lpc.parser.ast;

import java.util.List;

/**
 * This class represents multiple following strings as
 * an AST node.
 *
 * @author mhahnFr
 * @since 17.02.23
 */
public class ASTStrings extends ASTExpression {
    /** The list with the actual strings. */
    private final List<ASTExpression> strings;

    /**
     * Constructs this AST node using the given strings.
     *
     * @param strings the strings
     */
    public ASTStrings(final List<ASTExpression> strings) {
        super(strings.get(0).getBegin(), strings.get(strings.size() - 1).getEnd(), ASTType.STRINGS);

        this.strings = strings;
    }

    /**
     * Returns the strings represented by this AST node.
     *
     * @return the strings
     */
    public List<ASTExpression> getStrings() {
        return strings;
    }

    @Override
    public void visit(ASTVisitor visitor) {
        if (visitor.maybeVisit(this)) {
            strings.forEach(e -> e.visit(visitor));
        }
    }

    @Override
    public String describe(int indentation) {
        final var builder = new StringBuilder();

        builder.append(super.describe(indentation)).append(" Strings:\n");

        final var iterator = strings.listIterator();
        while (iterator.hasNext()) {
            builder.append(iterator.next().describe(indentation + 4));
            if (iterator.hasNext()) {
                builder.append('\n');
            }
        }
        return builder.toString();
    }

    @Override
    public boolean hasSubExpressions() {
        return true;
    }

    @Override
    public List<ASTExpression> getSubExpressions() {
        return strings;
    }
}
