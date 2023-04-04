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

public class SuggestionVisitor {
    private SuggestionType type = SuggestionType.ANY;
    private int position = -1;
    private ASTTypeDefinition returnType;

    public ASTTypeDefinition getType() {
        return returnType;
    }

    public SuggestionType getSuggestionType() {
        return type;
    }

    public int getPosition() {
        return position;
    }

    public SuggestionType visit(final ASTExpression node, final int position) {
        this.position = position;
        return (type = visitImpl(node, position));
    }

    private SuggestionType visitImpl(final ASTExpression node, final int position) {
        System.out.println("Generating for: " + node.getASTType());
        switch (node.getASTType()) {
            case FUNCTION_DEFINITION -> {
                final var func = (ASTFunctionDefinition) node;

                final var funcModifiers  = func.getModifiers();
                final var funcParameters = func.getParameters();
                if (funcModifiers != null && !funcModifiers.isEmpty() &&
                        position <= funcModifiers.get(func.getModifiers().size() - 1).getEnd().position()) {
                    return SuggestionType.MODIFIER;
                } else if (position <= func.getType().getEnd().position()) {
                    return SuggestionType.TYPE;
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
                        position <= varModifiers.get(varModifiers.size() - 1).getEnd().position()) {
                    return SuggestionType.MODIFIER;
                } else if (position <= variable.getType().getEnd().position()) {
                    return SuggestionType.TYPE;
                } else {
                    return SuggestionType.LITERAL;
                }
            }

            case OPERATION -> {
                final var op = (ASTOperation) node;

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
                return SuggestionType.LITERAL_IDENTIFIER;
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
