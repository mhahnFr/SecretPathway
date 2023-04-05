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

import mhahnFr.SecretPathway.core.lpc.parser.ast.*;
import mhahnFr.SecretPathway.core.lpc.parser.tokenizer.TokenType;

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
    /** The expected return type.                */
    private ASTTypeDefinition returnType;

    /**
     * Returns the requested return type at the last visited
     * position. Returns {@code null} if no return type is
     * expected.
     *
     * @return the requested return type
     * @see #getPosition()
     * @see #visit(ASTExpression, int)
     */
    public ASTTypeDefinition getType() {
        return returnType;
    }

    /**
     * Returns the type of suggestions determined at the last
     * visited position.
     *
     * @return the type of suggestions
     * @see #getPosition()
     * @see #visit(ASTExpression, int)
     */
    public SuggestionType getSuggestionType() {
        return type;
    }

    /**
     * Returns the position that was previously used for visiting.
     *
     * @return the last used position
     */
    public int getPosition() {
        return position;
    }

    /**
     * Visits the given AST node at the given position. Returns
     * the {@link SuggestionType} determined after visiting.
     *
     * @param node     the AST node to be visited
     * @param position the position
     * @return the type of suggestions that should be shown
     * @see #getPosition()
     * @see #getSuggestionType()
     */
    public SuggestionType visit(final ASTExpression node, final int position) {
        this.position = position;
//        return (type = visitImpl(node, position));
        type = visitImpl(node, position);
        System.out.println(type);
        return type;
    }

    /**
     * Performs the actual visiting. Returns the determined
     * {@link SuggestionType}.
     *
     * @param node     the AST node to be visited
     * @param position the position
     * @return the type of suggestions that should be shown
     * @see #visit(ASTExpression, int)
     */
    private SuggestionType visitImpl(final ASTExpression node, final int position) {
        System.out.print("Type for: " + node.getASTType() + ": ");
        returnType = null;
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
                            return visit(parameter, position);
                        }
                    }
                } else {
                    return visit(func.getBody(), position);
                }
            }

            case VARIABLE_DEFINITION -> {
                final var variable = (ASTVariableDefinition) node;

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

                // TODO: Maybe set expected type
                if (position <= op.getLhs().getEnd().position()) {
                    return visit(op.getLhs(), position);
                } else {
                    return visit(op.getRhs(), position);
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

                if (returned != null &&
                        position >= returned.getBegin().position() && position <= returned.getEnd().position()) {
                    // TODO: Expected type?
                    return returned.hasSubExpressions() ? visit(returned, position) : SuggestionType.IDENTIFIER;
                }
            }

            case FUNCTION_CALL -> {
                final var call = (ASTFunctionCall) node;

                if (position <= call.getName().getEnd().position()) {
                    return SuggestionType.IDENTIFIER;
                }
                for (final var param : call.getArguments()) {
                    if (position >= param.getBegin().position() && position <= param.getEnd().position()) {
                        // TODO: Expected type?
                        return param.hasSubExpressions() ? visit(param, position) : SuggestionType.IDENTIFIER;
                    }
                }
                return SuggestionType.LITERAL_IDENTIFIER;
            }

            case CAST -> {
                final var c = (ASTCast) node;

                if (position <= c.getType().getEnd().position()) {
                    return SuggestionType.TYPE;
                }
//                returnType = c.getType() as ASTReturnDefinition;
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
                            return visit(subNode, position);
                        }
                    }
                }
            }
        }
        return SuggestionType.ANY;
    }
}
