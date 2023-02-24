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
            case VARIABLE_DEFINITION -> current.addIdentifier(visitName(((ASTVariableDefinition) expression).getName()), visitASTType(((ASTVariableDefinition) expression).getType()), ASTType.VARIABLE_DEFINITION);
            case FUNCTION_DEFINITION -> {
                current.addIdentifier(visitName(((ASTFunctionDefinition) expression).getName()), visitASTType(((ASTFunctionDefinition) expression).getType()), ASTType.FUNCTION_DEFINITION);
                current = current.pushScope(expression.getBegin().position());
                visitParams(((ASTFunctionDefinition) expression).getParameters());
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

    /**
     * Adds the given {@link ASTParameter} to the current {@link Context}.
     *
     * @param parameter the parameter to be added
     * @see #current
     */
    private void addParameter(final ASTParameter parameter) {
        current.addIdentifier(visitName(parameter.getName()), visitASTType(parameter.getType()), ASTType.PARAMETER);
    }

    /**
     * Adds the parameters represented by the {@link ASTExpression}s in the
     * given list. If some parameter expressions are {@link ASTCombination}s,
     * the contained nodes are visited.
     *
     * @param params the list of expressions representing parameters
     */
    private void visitParams(final List<ASTExpression> params) {
        for (final var param : params) {
            if (param.getASTType() == ASTType.COMBINATION) {
                for (final var node : ((ASTCombination) param).getExpressions()) {
                    if (node.getASTType() == ASTType.PARAMETER) {
                        addParameter((ASTParameter) node);
                    } else {
                        node.visit(this);
                    }
                }
            } else {
                addParameter((ASTParameter) param);
            }
        }
    }

    /**
     * Visits the given {@link ASTBlock}.
     *
     * @param block the block to be visited
     */
    private void visitBlock(final ASTBlock block) {
        final var iterator = block.getBody().listIterator();
        while (iterator.hasNext()) {
            iterator.next().visit(this);
        }
    }

    /**
     * Returns the {@link TokenType} represented by the given
     * {@link ASTExpression}. If the given {@link ASTExpression}
     * is an {@link ASTCombination}, all other nodes are visited beforehand.
     *
     * @param expression the expression representing the type
     * @return the represented type
     */
    private TokenType visitASTType(final ASTExpression expression) {
        final TokenType toReturn;

        if (expression.getASTType() == ASTType.COMBINATION) {
            TokenType found = null;
            for (final var node : ((ASTCombination) expression).getExpressions()) {
                if (node.getASTType() == ASTType.TYPE) {
                    found = ((ASTTypeDeclaration) node).getType();
                } else {
                    node.visit(this);
                }
            }
            toReturn = found;
        } else {
            toReturn = ((ASTTypeDeclaration) expression).getType();
        }
        return toReturn;
    }

    /**
     * Returns the name represented by the given {@link ASTExpression}.
     * If the given {@link ASTExpression} is an {@link ASTCombination},
     * all contained nodes are visited.
     *
     * @param expression the expression representing a name
     * @return the represented name
     */
    private String visitName(final ASTExpression expression) {
        final String toReturn;

        if (expression.getASTType() == ASTType.COMBINATION) {
            String found = null;
            for (final var node : ((ASTCombination) expression).getExpressions()) {
                if (node.getASTType() == ASTType.NAME) {
                    found = ((ASTName) node).getName();
                } else {
                    node.visit(this);
                }
            }
            toReturn = found;
        } else {
            toReturn = ((ASTName) expression).getName();
        }
        return toReturn;
    }
}
