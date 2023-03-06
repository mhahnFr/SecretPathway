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
import mhahnFr.SecretPathway.gui.editor.ASTHighlight;
import mhahnFr.SecretPathway.gui.editor.ErrorHighlight;
import mhahnFr.SecretPathway.gui.editor.Highlight;
import mhahnFr.utils.StreamPosition;

import java.util.ArrayList;
import java.util.List;

/**
 * This class interprets LPC source code.
 *
 * @author mhahnFr
 * @since 21.02.23
 */
public class Interpreter implements ASTVisitor {
    /** The currently active context.               */
    private Context current;
    /** A list with the elements to be highlighted. */
    private final List<Highlight<?>> highlights = new ArrayList<>();

    /**
     * Creates an execution context for the given list of
     * {@link ASTExpression}s.
     *
     * @param expressions the expressions to be interpreted
     * @return the generated context
     */
    public Context createContextFor(final List<ASTExpression> expressions) {
        current = new Context();
        for (final var expression : expressions) {
            expression.visit(this);
        }
        return current;
    }

    /**
     * Returns the list with the highlighting elements.
     *
     * @return the highlighting list
     */
    public List<Highlight<?>> getHighlights() {
        return highlights;
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
            case VARIABLE_DEFINITION -> current.addIdentifier(expression.getBegin().position(), visitName(((ASTVariableDefinition) expression).getName()), visitASTType(((ASTVariableDefinition) expression).getType()), ASTType.VARIABLE_DEFINITION);
            case FUNCTION_DEFINITION -> {
                final var block = ((ASTFunctionDefinition) expression).getBody();
                current.addIdentifier(expression.getBegin().position(), visitName(((ASTFunctionDefinition) expression).getName()), visitASTType(((ASTFunctionDefinition) expression).getType()), ASTType.FUNCTION_DEFINITION);
                current = current.pushScope(block.getBegin().position());
                visitParams(((ASTFunctionDefinition) expression).getParameters());
                visitBlock((ASTBlock) block);
                current = current.popScope(expression.getEnd().position());
            }
            case BLOCK -> {
                current = current.pushScope(expression.getBegin().position());
                visitBlock((ASTBlock) expression);
                current = current.popScope(expression.getEnd().position());
            }
            case MISSING -> {
                final StreamPosition begin = expression.getBegin(),
                                     end   = expression.getEnd();
                final int endPosition;
                if (!begin.isOnSameLine(end)) {
                    endPosition = begin.getLineEnd().position();
                } else {
                    endPosition = end.position();
                }
                highlights.add(new ErrorHighlight<>(begin.position(), endPosition, ASTType.MISSING, ((ASTMissing) expression).getMessage()));
            }
            case WRONG -> highlights.add(new ErrorHighlight<>(expression.getBegin().position(), expression.getEnd().position(), ASTType.WRONG, ((ASTWrong) expression).getMessage()));
            case NAME -> {
                final var identifier = current.getIdentifier(((ASTName) expression).getName(), expression.getBegin().position());
                if (identifier == null) {
                    highlights.add(new ErrorHighlight<>(expression.getBegin().position(), expression.getEnd().position(), InterpretationType.ERROR, "Identifier not found"));
                } else {
                    highlights.add(new ASTHighlight(expression.getBegin().position(), expression.getEnd().position(), identifier.getType()));
                }
            }
            default -> highlights.add(new ASTHighlight(expression));
        }
    }

    /**
     * Adds the given {@link ASTParameter} to the current {@link Context}.
     *
     * @param parameter the parameter to be added
     * @see #current
     */
    private void addParameter(final ASTParameter parameter) {
        current.addIdentifier(parameter.getBegin().position(), visitName(parameter.getName()), visitASTType(parameter.getType()), ASTType.PARAMETER);
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
            } else if (param.getASTType() == ASTType.MISSING) {
                highlights.add(new ErrorHighlight<>(param.getBegin().position(), param.getEnd().position(), ASTType.MISSING, ((ASTMissing) param).getMessage()));
            } else if (param.getASTType() != ASTType.AST_ELLIPSIS) {
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
        for (final var expression : block.getBody()) {
            expression.visit(this);
        }
    }

    /**
     * Returns the {@link ASTTypeDefinition} represented by the given
     * {@link ASTExpression}. If the given {@link ASTExpression}
     * is an {@link ASTCombination}, all other nodes are visited beforehand.
     *
     * @param expression the expression representing the type
     * @return the represented type
     */
    private ASTTypeDefinition visitASTType(final ASTExpression expression) {
        if (expression.getASTType() == ASTType.COMBINATION) {
            ASTTypeDefinition found = null;
            for (final var node : ((ASTCombination) expression).getExpressions()) {
                if (node.getASTType() == ASTType.TYPE) {
                    found = (ASTTypeDefinition) node;
                } else {
                    node.visit(this);
                }
            }
            return found;
        } else if (expression.getASTType() == ASTType.FUNCTION_REFERENCE) {
            final var parameterTypes = ((ASTFunctionReferenceType) expression).getCallTypes();
            if (parameterTypes != null) {
                for (final var parameterType : parameterTypes) {
                    parameterType.visit(this);
                }
            }
        }
        return (ASTTypeDefinition) expression;
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
