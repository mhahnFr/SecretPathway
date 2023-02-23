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

import mhahnFr.SecretPathway.core.lpc.parser.ast.ASTExpression;
import mhahnFr.SecretPathway.core.lpc.parser.ast.ASTVariableDefinition;
import mhahnFr.SecretPathway.core.lpc.parser.ast.ASTVisitor;

import java.util.List;
import java.util.Stack;

/**
 * This class interprets LPC source code.
 *
 * @author mhahnFr
 * @since 21.02.23
 */
public class Interpreter implements ASTVisitor {
    /** The currently active context. */
    private Context current;
    private Stack<Integer> scopeEnd;

    /**
     * Creates an execution context for the given list of
     * {@link ASTExpression}s.
     *
     * @param expressions the expressions to be interpreted
     * @return the generated context
     */
    public Context createContextFor(final List<ASTExpression> expressions) {
        current = new Context();
        scopeEnd = new Stack<>();
        expressions.forEach(this::visit);
        return current;
    }

    @Override
    public void visit(ASTExpression expression) {
        if (expression.getBegin().position() > scopeEnd.peek()) {
            
        }
        switch (expression.getASTType()) {
            case VARIABLE_DEFINITION -> current.addIdentifier(null, null);
            case FUNCTION_DEFINITION -> {
                current.addIdentifier(null, null);
                current = current.pushScope(expression.getBegin().position());
            }
            case BLOCK -> {
                current = current.pushScope(expression.getBegin().position());
                scopeEnd.push(expression.getEnd().position());
            }
        }
    }
}
