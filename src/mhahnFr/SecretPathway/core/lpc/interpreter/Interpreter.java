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
import mhahnFr.SecretPathway.gui.editor.ASTHighlight;
import mhahnFr.SecretPathway.gui.editor.MessagedHighlight;
import mhahnFr.SecretPathway.gui.editor.Highlight;
import mhahnFr.utils.StreamPosition;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * This class interprets LPC source code.
 *
 * @author mhahnFr
 * @since 21.02.23
 */
public class Interpreter implements ASTVisitor {
    /** The currently active context.               */
    private Context current;
    /** The current return type of the expression.  */
    private ASTTypeDefinition currentType;
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
        System.out.println();
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
               type != ASTType.VARIABLE_DEFINITION &&
               type != ASTType.OPERATION;
    }

    @Override
    public void visit(ASTExpression expression) {
        var highlight = true;

        switch (expression.getASTType()) {
            case VARIABLE_DEFINITION -> {
                final var type = cast(ASTTypeDefinition.class, ((ASTVariableDefinition) expression).getType());

                current.addIdentifier(expression.getBegin(),
                        cast(ASTName.class, ((ASTVariableDefinition) expression).getName()),
                        type,
                        ASTType.VARIABLE_DEFINITION);
                if (type instanceof final ASTTypeDeclaration declaration && declaration.getType() == TokenType.VOID) {
                    highlights.add(new MessagedHighlight<>(declaration.getBegin().position(),
                                                           declaration.getEnd().position(),
                                                           InterpretationType.ERROR,
                                                           "'void' not allowed here"));
                }
                currentType = type;
            }

            case FUNCTION_DEFINITION -> {
                final var function = (ASTFunctionDefinition) expression;
                final var block    = function.getBody();

                final var params = visitParams(function.getParameters());
                current = current.addFunction(expression.getBegin(),
                                              block.getBegin(),
                                              cast(ASTName.class, function.getName()),
                                              cast(ASTTypeDefinition.class, function.getType()),
                                              params);
                visitBlock(cast(ASTBlock.class, block));
                current = current.popScope(expression.getEnd().position());
                currentType = new ReturnType(TokenType.VOID);
            }

            case BLOCK -> {
                current = current.pushScope(expression.getBegin().position());
                visitBlock((ASTBlock) expression);
                current = current.popScope(expression.getEnd().position());
                currentType = new ReturnType(TokenType.VOID);
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
                highlights.add(new MessagedHighlight<>(begin.position(), endPosition, ASTType.MISSING, ((ASTMissing) expression).getMessage()));
                highlight = false;
                currentType = new ReturnType(TokenType.ANY);
            }

            case WRONG -> {
                highlights.add(new MessagedHighlight<>(expression.getBegin().position(), expression.getEnd().position(), ASTType.WRONG, ((ASTWrong) expression).getMessage()));
                highlight = false;
                currentType = new ReturnType(TokenType.ANY);
            }

            case NAME -> {
                final var name = (ASTName) expression;
                final var identifier = current.getIdentifier(name.getName(), expression.getBegin().position());
                if (identifier == null) {
                    if (name.getName().startsWith("$")) {
                        highlights.add(new MessagedHighlight<>(name.getBegin().position(), name.getEnd().position(), InterpretationType.NOT_FOUND_BUILTIN, "Built-in not found"));
                    } else {
                        highlights.add(new MessagedHighlight<>(expression.getBegin().position(), expression.getEnd().position(), InterpretationType.NOT_FOUND, "Identifier not found"));
                    }
                    currentType = new ReturnType(TokenType.ANY);
                } else {
                    highlights.add(new Highlight<>(expression.getBegin().position(), expression.getEnd().position(), identifier.getType()));
                    currentType = identifier.getReturnType();
                }
                highlight = false;
            }

            case OPERATION -> {
                final var op = (ASTOperation) expression;
                op.getLhs().visit(this);
                if (op.getOperatorType() == TokenType.DOT ||
                    op.getOperatorType() == TokenType.ARROW) {
                    cast(ASTFunctionCall.class, op.getRhs());
                } else {
                    op.getRhs().visit(this);
                }
                currentType = switch (op.getOperatorType()) {
                    case IS,
                         OR,
                         AND,
                         EQUALS,
                         NOT_EQUAL,
                         LESS,
                         LESS_OR_EQUAL,
                         GREATER,
                         GREATER_OR_EQUAL -> new ReturnType(TokenType.BOOL);
                    case RANGE,
                         ELLIPSIS,
                         DOT,
                         ARROW            -> new ReturnType(TokenType.ANY);
                    case ASSIGNMENT,
                         AMPERSAND,
                         PIPE,
                         LEFT_SHIFT,
                         RIGHT_SHIFT,
                         DOUBLE_QUESTION,
                         QUESTION,
                         INCREMENT,
                         DECREMENT,
                         PLUS,
                         MINUS,
                         STAR,
                         SLASH,
                         PERCENT,
                         ASSIGNMENT_PLUS,
                         ASSIGNMENT_MINUS,
                         ASSIGNMENT_STAR,
                         ASSIGNMENT_SLASH,
                         ASSIGNMENT_PERCENT -> currentType;

                    default -> new ReturnType(TokenType.VOID);
                };
            }

            case AST_INHERITANCE -> {
                final var inheritance = (ASTInheritance) expression;
                if (inheritance.getInherited() == null) {
                    highlights.add(new MessagedHighlight<>(inheritance.getBegin().position(), inheritance.getEnd().position(), InterpretationType.WARNING, "Inheriting from nothing"));
                    highlight = false;
                }
                currentType = new ReturnType(TokenType.VOID);
            }

            // TODO: AST_RETURN, CAST, UNARY_OPERATOR

            case AST_NEW             -> currentType = new ReturnType(TokenType.ANY); // TODO: Load new expression
            case AST_THIS            -> currentType = new ReturnType(TokenType.ANY); // Cannot be known from here.
            case AST_INTEGER         -> currentType = new ReturnType(TokenType.INTEGER);
            case AST_NIL             -> currentType = new ReturnType(TokenType.NIL);
            case AST_STRING, STRINGS -> currentType = new ReturnType(TokenType.STRING);
            case AST_SYMBOL          -> currentType = new ReturnType(TokenType.SYMBOL);
            case AST_BOOL            -> currentType = new ReturnType(TokenType.BOOL);
            case ARRAY, AST_MAPPING  -> currentType = new ReturnType(TokenType.ANY); // No array nor mapping types -> any.
            case AST_CHARACTER       -> currentType = new ReturnType(TokenType.CHARACTER);

            // TODO AST_If

            default -> currentType = new ReturnType(TokenType.VOID);
        }
        if (highlight) {
            highlights.add(new ASTHighlight(expression));
        }
        System.out.println(expression.getASTType() + ", return type: " + currentType);
    }

    /**
     * Casts the given expression to the given type. If the given
     * expression is an {@link ASTCombination}, its contents are visited
     * and the desired AST node of the given type is extracted.
     *
     * @param type       the {@link Class} of the requested type
     * @param expression the expression to be cast
     * @return the cast expression
     * @param <T> the requested type
     */
    private <T extends ASTExpression> T cast(final Class<T> type, final ASTExpression expression) {
        if (type.isAssignableFrom(expression.getClass())) {
            return type.cast(expression);
        } else if (expression instanceof final ASTCombination combination) {
            return unwrap(combination, type);
        }
        throw new IllegalArgumentException("Given expression is neither a combination nor " + type + "!");
    }

    /**
     * Unwraps the given {@link ASTCombination} and returns the {@link ASTExpression}
     * of the given type found in the combination. If it is not found {@code null} is
     * returned.
     *
     * @param combination the combination to unwrap
     * @param type        the class of the requested type
     * @return the expression of the given type contained in the given combination
     * @param <T> the type of the requested AST node
     */
    private <T extends ASTExpression> T unwrap(final ASTCombination combination, final Class<T> type) {
        T toReturn = null;
        for (final var expression : combination.getExpressions()) {
            if (type.isAssignableFrom(expression.getClass())) {
                toReturn = type.cast(expression);
            } else {
                expression.visit(this);
            }
        }
        return toReturn;
    }

    /**
     * Returns the parameters represented by the {@link ASTExpression}s in the
     * given list. If some parameter expressions are {@link ASTCombination}s,
     * the contained nodes are visited.
     *
     * @param params the list of expressions representing parameters
     * @return a list with the parameter definitions
     */
    private List<Definition> visitParams(final List<ASTExpression> params) {
        final List<Definition> parameters = new Vector<>(params.size());

        // TODO: bool (any (int (string) ) )
        for (final var param : params) {
            if (param.getASTType() == ASTType.MISSING) {
                highlights.add(new MessagedHighlight<>(param.getBegin().position(), param.getEnd().position(), ASTType.MISSING, ((ASTMissing) param).getMessage()));
            } else if (param.getASTType() != ASTType.AST_ELLIPSIS) {
                final var parameter = cast(ASTParameter.class, param);
                final var type      = cast(ASTTypeDefinition.class, parameter.getType());

                if (type instanceof final ASTTypeDeclaration declaration && declaration.getType() == TokenType.VOID) {
                    highlights.add(new MessagedHighlight<>(declaration.getBegin().position(),
                                                           declaration.getEnd().position(),
                                                           InterpretationType.ERROR,
                                                           "'void' not allowed here"));
                }
                parameters.add(new Definition(parameter.getBegin().position(),
                                              cast(ASTName.class, parameter.getName()).getName(),
                                              type,
                                              ASTType.PARAMETER));
            }
        }
        return parameters;
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
}
