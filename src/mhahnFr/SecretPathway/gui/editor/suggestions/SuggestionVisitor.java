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

package mhahnFr.SecretPathway.gui.editor.suggestions;

import mhahnFr.SecretPathway.core.lpc.interpreter.*;
import mhahnFr.SecretPathway.core.lpc.parser.ast.*;
import mhahnFr.SecretPathway.core.lpc.parser.tokenizer.TokenType;

import java.util.List;

/**
 * This class represents an AST visitor, querying additional
 * information necessary for showing more relevant suggestions.
 *
 * @author mhahnFr
 * @since 04.04.23
 */
public class SuggestionVisitor {
    /** The type of the suggestions to be shown. */
    private SuggestionType type = SuggestionType.ANY;
    /** The position for visiting.               */
    private int position = -1;
    /** The node lastly visited.                 */
    private ASTExpression lastVisited = null;
    /** The expected return type.                */
    private ASTTypeDefinition returnType;

    /**
     * Returns the requested return type at the last visited
     * position. Returns {@code null} if no return type is
     * expected.
     *
     * @return the requested return type
     * @see #visit(ASTExpression, int, Context)
     */
    public ASTTypeDefinition getType() {
        return returnType;
    }

    /**
     * Returns the type of suggestions determined at the last
     * visited position.
     *
     * @return the type of suggestions
     * @see #visit(ASTExpression, int, Context)
     */
    public SuggestionType getSuggestionType() {
        return type;
    }

    /**
     * Visits the given AST node at the given position. Returns
     * the {@link SuggestionType} determined after visiting.
     *
     * @param node     the AST node to be visited
     * @param position the position
     * @param context  the interpretation context
     * @return the type of suggestions that should be shown
     * @see #getSuggestionType()
     */
    public SuggestionType visit(final ASTExpression node, final int position, final Context context) {
        if (this.position    == position &&
            this.lastVisited == node) {
            return type;
        }

        this.position    = position;
        this.lastVisited = node;
        returnType       = null;
        return (type = visitImpl(node, position, context));
    }

    /**
     * Performs the actual visiting. Returns the determined
     * {@link SuggestionType}.
     *
     * @param node     the AST node to be visited
     * @param position the position
     * @param context  the interpretation context
     * @return the type of suggestions that should be shown
     * @see #visit(ASTExpression, int, Context)
     */
    private SuggestionType visitImpl(final ASTExpression node, final int position, final Context context) {
        switch (node.getASTType()) {
            case FUNCTION_DEFINITION -> {
                final var func = (ASTFunctionDefinition) node;

                final var funcModifiers  = func.getModifiers();
                final var funcParameters = func.getParameters();
                if (funcModifiers != null && !funcModifiers.isEmpty() &&
                        position <= funcModifiers.get(func.getModifiers().size() - 1).getEnd().position()) {
                    return SuggestionType.TYPE_MODIFIER;
                } else if (position <= func.getType().getEnd().position()) {
                    return SuggestionType.TYPE_MODIFIER;
                } else if (position <= func.getName().getEnd().position()) {
                    return SuggestionType.LITERAL;
                } else if (!funcParameters.isEmpty() &&
                        position <= funcParameters.get(funcParameters.size() - 1).getEnd().position()) {
                    for (final var parameter : funcParameters) {
                        if (position >= parameter.getBegin().position() && position <= parameter.getEnd().position()) {
                            return visitImpl(parameter, position, context);
                        }
                    }
                } else {
                    return visitImpl(func.getBody(), position, context);
                }
            }

            case VARIABLE_DEFINITION -> {
                final var variable = (ASTVariableDefinition) node;

                if (!variable.getType().getEnd().isOnSameLine(variable.getName().getBegin())) {
                    // Assume this expression is not intended to be a variable definition.
                    return context.isGlobalScope(position) ? SuggestionType.TYPE_MODIFIER : SuggestionType.ANY;
                }
                final var varModifiers = variable.getModifiers();
                if (varModifiers != null && !varModifiers.isEmpty() &&
                        position >= varModifiers.get(0).getBegin().position() &&
                        position <= varModifiers.get(varModifiers.size() - 1).getEnd().position()) {
                    return SuggestionType.TYPE_MODIFIER;
                } else if (position >= variable.getType().getBegin().position() &&
                        position <= variable.getType().getEnd().position()) {
                    return SuggestionType.TYPE_MODIFIER;
                } else if (position >= variable.getName().getBegin().position() &&
                        position <= variable.getName().getEnd().position()) {
                    return SuggestionType.LITERAL;
                }
            }

            case OPERATION -> {
                final var op = (ASTOperation) node;

                if (op.getOperatorType() == TokenType.ASSIGNMENT) {
                    final var lhs = op.getLhs();
                    switch (lhs.getASTType()) {
                        case VARIABLE_DEFINITION -> returnType = cast(ASTTypeDefinition.class, ((ASTVariableDefinition) lhs).getType());
                        case NAME -> {
                            final var def = context.digOutIdentifier(((ASTName) lhs).getName(), position);
                            if (def != null) {
                                returnType = def.getReturnType();
                            }
                        }
                    }
                }
                if (position <= op.getLhs().getEnd().position()) {
                    return visitImpl(op.getLhs(), position, context);
                } else {
                    return visitImpl(op.getRhs(), position, context);
                }
            }

            case AST_INHERITANCE, AST_INCLUDE -> {
                return SuggestionType.LITERAL;
            }

            case PARAMETER -> {
                final var param = (ASTParameter) node;

                if (position <= param.getType().getEnd().position()) {
                    return SuggestionType.TYPE;
                } else {
                    return SuggestionType.LITERAL;
                }
            }

            case AST_RETURN -> {
                final var ret      = (ASTReturn) node;
                final var returned = ret.getReturned();

                final var func = context.queryEnclosingFunction(position);
                if (func != null) {
                    returnType = func.getReturnType();
                }
                if (returned != null &&
                        position >= returned.getBegin().position() && position <= returned.getEnd().position()) {
                    if (returned.hasSubExpressions()) {
                        return visitImpl(returned, position, context);
                    }
                    return SuggestionType.LITERAL_IDENTIFIER;
                }
            }

            case FUNCTION_CALL -> {
                final var call = (ASTFunctionCall) node;

                if (position <= call.getName().getEnd().position()) {
                    return SuggestionType.IDENTIFIER;
                }

                final var func = context.getIdentifier(cast(ASTName.class, call.getName()).getName(), position);
                final List<Definition> args;
                final FunctionDefinition funcDef;
                if (func instanceof final FunctionDefinition def) {
                    args    = def.getParameters();
                    funcDef = def;
                } else {
                    args    = null;
                    funcDef = null;
                }

                final var callArgs = call.getArguments();
                for (int i = 0; i < callArgs.size(); ++i) {
                    final var param = callArgs.get(i);

                    if (position >= param.getBegin().position() && position <= param.getEnd().position()) {
                        if (param.hasSubExpressions()) {
                            return visitImpl(param, position, context);
                        }
                        if (funcDef != null) {
                            if (i < args.size()) {
                                returnType = args.get(i).getReturnType();
                            } else if (funcDef.isVariadic()) {
                                returnType = new ReturnType(TokenType.ANY);
                            }
                        }
                        return SuggestionType.IDENTIFIER;
                    }
                }
                if (returnType == null && args != null && !args.isEmpty()) {
                    returnType = args.get(0).getReturnType();
                }
                return SuggestionType.LITERAL_IDENTIFIER;
            }

            case CAST -> {
                final var c = (ASTCast) node;

                if (position <= c.getType().getEnd().position()) {
                    return SuggestionType.TYPE;
                }
                returnType = cast(ASTTypeDefinition.class, c.getType());
                return SuggestionType.LITERAL_IDENTIFIER;
            }

            case MISSING, WRONG -> {
                final var hole = (ASTHole) node;

                if (hole.getExpected() == ASTType.NAME) {
                    return SuggestionType.LITERAL;
                } else if (hole.getExpected() == ASTType.TYPE) {
                    return SuggestionType.TYPE;
                } else if (hole.getExpected() == TokenType.IDENTIFIER) {
                    return SuggestionType.IDENTIFIER;
                }
            }

            default -> {
                if (node.hasSubExpressions()) {
                    for (final var subNode : node.getSubExpressions()) {
                        if (position >= subNode.getBegin().position() && position <= subNode.getEnd().position()) {
                            return visitImpl(subNode, position, context);
                        }
                    }
                }
            }
        }
        return SuggestionType.ANY;
    }

    /**
     * Casts the given {@link ASTExpression} to the given {@link Class}.
     * If the given expression is a {@link ASTCombination}, it is unwrapped.
     *
     * @param type       the class to which to cast
     * @param expression the expression to be cast or unwrapped
     * @return The cast or unwrapped expression
     * @param <T> The actual type of the expression, passed as parameter by the given class
     */
    private <T extends ASTExpression> T cast(final Class<T> type, final ASTExpression expression) {
        if (type.isAssignableFrom(expression.getClass())) {
            return type.cast(expression);
        } else if (expression instanceof final ASTCombination combination) {
            for (final var e : combination.getExpressions()) {
                if (type.isAssignableFrom(e.getClass())) {
                    return type.cast(e);
                }
            }
        }
        throw new IllegalArgumentException("Given expression is neither a combination nor " + type + "!");
    }
}
