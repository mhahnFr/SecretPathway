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

import mhahnFr.SecretPathway.core.lpc.parser.ast.*;
import mhahnFr.SecretPathway.core.lpc.parser.tokenizer.TokenType;

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

    /**
     * Creates an execution context for the given list of
     * {@link ASTExpression}s.
     *
     * @param expressions the expressions to be interpreted
     * @return the generated context
     */
    public Context createContextFor(final List<ASTExpression> expressions) {
        current = new Context();
        expressions.forEach(this::visit);
        return current;
    }

    @Override
    public boolean visitType(ASTType type) {
        return type != ASTType.BLOCK               &&
               type != ASTType.FUNCTION_DEFINITION &&
               type != ASTType.VARIABLE_DEFINITION;
    }

    @Override
    public void visit(ASTExpression expression) {
        switch (expression.getASTType()) {
            case VARIABLE_DEFINITION -> current.addIdentifier(visitName(((ASTVariableDefinition) expression).getName()), visitASTType(((ASTVariableDefinition) expression).getType()));
            case FUNCTION_DEFINITION -> {
                current.addIdentifier(visitName(((ASTFunctionDefinition) expression).getName()), visitASTType(((ASTFunctionDefinition) expression).getType()));
                current = current.pushScope(expression.getBegin().position());
                // TODO: add params
                visitBlock((ASTBlock) ((ASTFunctionDefinition) expression).getBody());
                current = current.popScope(expression.getEnd().position());
            }
            case BLOCK -> {
                current = current.pushScope(expression.getBegin().position());
                visitBlock((ASTBlock) expression);
                current = current.popScope(expression.getEnd().position());
            }
        }
    }

    private void visitBlock(final ASTBlock block) {
        final var iterator = block.getBody().listIterator();
        while (iterator.hasNext()) {
            visit(iterator.next());
        }
    }

    private TokenType visitASTType(final ASTExpression expression) {
        if (expression.getASTType() == ASTType.COMBINATION) {
            // search and visit
            return null;
        } else return ((ASTTypeDeclaration) expression).getType();
    }

    private String visitName(final ASTExpression expression) {
        if (expression.getASTType() == ASTType.COMBINATION) {
            // search and visit
            return null;
        } else return ((ASTName) expression).getName();
    }
}
