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
import java.util.Optional;
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
    private List<Highlight<?>> highlights;

    /**
     * Creates an execution context for the given list of
     * {@link ASTExpression}s.
     *
     * @param expressions the expressions to be interpreted
     * @return the generated context
     */
    public Context createContextFor(final List<ASTExpression> expressions) {
        highlights = new ArrayList<>(expressions.size());
        current    = new Context();

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
               type != ASTType.VARIABLE_DEFINITION &&
               type != ASTType.OPERATION           &&
               type != ASTType.CAST                &&
               type != ASTType.UNARY_OPERATOR      &&
               type != ASTType.AST_IF              &&
               type != ASTType.AST_RETURN          &&
               type != ASTType.FUNCTION_REFERENCE  &&
               type != ASTType.FUNCTION_CALL;
    }

    @Override
    public void visit(ASTExpression expression) {
        var highlight = true;

        switch (expression.getASTType()) {
            case VARIABLE_DEFINITION -> {
                final var varDefinition = (ASTVariableDefinition) expression;

                ASTTypeDefinition type;
                if (varDefinition.getType() == null ||
                    ((type = cast(ASTTypeDefinition.class, varDefinition.getType())) instanceof final ASTTypeDeclaration declaration
                            && declaration.getType() == null)) {
                    type = new ReturnType(TokenType.ANY);
                } else {
                    type.visit(this);
                }

                current.addIdentifier(expression.getBegin(),
                        cast(ASTName.class, ((ASTVariableDefinition) expression).getName()),
                        type,
                        ASTType.VARIABLE_DEFINITION);
                maybeWrongVoid(type);
                currentType = type;
            }

            case FUNCTION_DEFINITION -> {
                final var function           = (ASTFunctionDefinition) expression;
                final var block              = function.getBody();
                final var paramExpressions   = function.getParameters();

                final var retType  = cast(ASTTypeDefinition.class, function.getType());
                retType.visit(this);
                final var params   = visitParams(paramExpressions);

                current = current.addFunction(expression.getBegin(),
                                              block.getBegin(),
                                              cast(ASTName.class, function.getName()),
                                              retType,
                                              params,
                                              !paramExpressions.isEmpty() &&
                                              paramExpressions.get(paramExpressions.size() - 1).getASTType()
                                                      == ASTType.AST_ELLIPSIS);
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
                highlights.add(new MessagedHighlight<>(begin.position(),
                                                       endPosition,
                                                       ASTType.MISSING,
                                                       ((ASTMissing) expression).getMessage()));
                highlight = false;
                currentType = new ReturnType(TokenType.ANY);
            }

            case WRONG -> {
                highlights.add(new MessagedHighlight<>(expression.getBegin(),
                                                       expression.getEnd(),
                                                       ASTType.WRONG,
                                                       ((ASTWrong) expression).getMessage()));
                highlight = false;
                currentType = new ReturnType(TokenType.ANY);
            }

            case FUNCTION_CALL -> {
                final var fc = (ASTFunctionCall) expression;

                final var name = cast(ASTName.class, fc.getName());
                name.visit(this);
                final var id = current.getIdentifier(name.getName(), name.getBegin().position());
                if (id instanceof final FunctionDefinition definition &&
                    fc.getArguments() != null) {
                    visitFunctionCall(fc, definition);
                    currentType = id.getReturnType();
                }
            }

            case NAME -> {
                final var name = (ASTName) expression;
                final var identifier = current.getIdentifier(name.getName(), expression.getBegin().position());
                if (identifier == null) {
                    if (name.getName() != null && name.getName().startsWith("$")) {
                        highlights.add(new MessagedHighlight<>(name.getBegin(),
                                                               name.getEnd(),
                                                               InterpretationType.NOT_FOUND_BUILTIN,
                                                               "Built-in not found"));
                    } else {
                        highlights.add(new MessagedHighlight<>(expression.getBegin(),
                                                               expression.getEnd(),
                                                               InterpretationType.NOT_FOUND,
                                                               "Identifier not found"));
                    }
                    currentType = new ReturnType(null);
                } else {
                    highlights.add(new Highlight<>(expression.getBegin().position(),
                                                   expression.getEnd().position(),
                                                   identifier.getType()));
                    currentType = identifier.getReturnType();
                }
                highlight = false;
            }

            case OPERATION -> {
                final var op  = (ASTOperation) expression;
                final var rhs = op.getRhs();
                op.getLhs().visit(this);
                final var lhsType = currentType;
                if (op.getOperatorType() == TokenType.DOT ||
                    op.getOperatorType() == TokenType.ARROW) {
                    cast(ASTFunctionCall.class, rhs);
                } else {
                    rhs.visit(this);
                }
                if (op.getOperatorType() == TokenType.ASSIGNMENT &&
                    !lhsType.isAssignableFrom(currentType)) {
                    highlights.add(new MessagedHighlight<>(rhs.getBegin(),
                                                           rhs.getEnd(),
                                                           InterpretationType.TYPE_MISMATCH,
                                                           lhsType + " is not assignable from " + currentType));
                }
                if (op.getOperatorType() != null) {
                    currentType = switch (op.getOperatorType()) {
                        case IS,
                             AND,
                             EQUALS,
                             NOT_EQUAL,
                             LESS,
                             LESS_OR_EQUAL,
                             GREATER,
                             GREATER_OR_EQUAL -> new ReturnType(TokenType.BOOL);

                        case RANGE,
                             ELLIPSIS,
                             ARROW -> new ReturnType(TokenType.ANY);

                        case DOT -> new ReturnType(null);

                        case ASSIGNMENT,
                             OR,
                             AMPERSAND,
                             PIPE,
                             LEFT_SHIFT,
                             RIGHT_SHIFT,
                             DOUBLE_QUESTION,
                             QUESTION,
                             INCREMENT,
                             DECREMENT,
                             COLON,
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
            }

            case AST_INHERITANCE -> {
                final var inheritance = (ASTInheritance) expression;
                if (inheritance.getInherited() == null) {
                    highlights.add(new MessagedHighlight<>(inheritance.getBegin(),
                                                           inheritance.getEnd(),
                                                           InterpretationType.WARNING,
                                                           "Inheriting from nothing"));
                    highlight = false;
                }
                currentType = new ReturnType(TokenType.VOID);
            }

            case CAST -> {
                final var cast = (ASTCast) expression;
                cast.getCast().visit(this);
                currentType = cast(ASTTypeDefinition.class, cast.getType());
            }

            case AST_ELLIPSIS -> {
                final var enclosing = current.queryEnclosingFunction();
                if (enclosing == null || !enclosing.isVariadic()) {
                    highlights.add(new MessagedHighlight<>(expression.getBegin(),
                                                           expression.getEnd(),
                                                           InterpretationType.ERROR,
                                                           "Enclosing function is not variadic"));
                    highlight = false;
                }
                currentType = new ReturnType(TokenType.ANY);
            }

            case AST_NEW             -> currentType = new ReturnType(TokenType.OBJECT); // TODO: Load new expression

            case AST_THIS            -> currentType = new ReturnType(null); // Cannot be known from here.
            case ARRAY, AST_MAPPING  -> currentType = new ReturnType(TokenType.ANY); // No array nor mapping types -> any.
            case AST_INTEGER         -> currentType = new ReturnType(TokenType.INT_KEYWORD);
            case AST_NIL             -> currentType = new ReturnType(TokenType.NIL);
            case AST_STRING, STRINGS -> currentType = new ReturnType(TokenType.STRING_KEYWORD);
            case AST_SYMBOL          -> currentType = new ReturnType(TokenType.SYMBOL_KEYWORD);
            case AST_BOOL            -> currentType = new ReturnType(TokenType.BOOL);
            case AST_CHARACTER       -> currentType = new ReturnType(TokenType.CHAR_KEYWORD);
            case UNARY_OPERATOR      -> ((ASTUnaryOperator) expression).getIdentifier().visit(this);

            case AST_IF -> {
                final var i         = (ASTIf) expression;
                final var condition = i.getCondition();

                condition.visit(this);
                if (!new ReturnType(TokenType.BOOL).isAssignableFrom(currentType)) {
                    highlights.add(new MessagedHighlight<>(condition.getBegin(),
                                                           condition.getEnd(),
                                                           InterpretationType.TYPE_MISMATCH,
                                                           "Condition should be a boolean expression"));
                }
                i.getInstruction().visit(this);
                if (i.getElseInstruction() != null) {
                    i.getElseInstruction().visit(this);
                }
            }

            case AST_RETURN -> {
                final var ret      = (ASTReturn) expression;
                final var returned = ret.getReturned();

                if (returned != null) {
                    returned.visit(this);
                } else {
                    currentType = new ReturnType(TokenType.VOID);
                }

                final var e = current.queryEnclosingFunction();
                if (e != null && !e.getReturnType().isAssignableFrom(currentType)) {
                    final var typeString = Optional.ofNullable(e.getReturnType().toString());
                    highlights.add(new MessagedHighlight<>(ret.getBegin(),
                                                           ret.getEnd(),
                                                           InterpretationType.TYPE_MISMATCH,
                                                           typeString.orElse("<< unknown >>") +
                                                           " is not assignable from " + currentType));
                }
            }

            case FUNCTION_REFERENCE -> {
                for (final var type : ((ASTFunctionReferenceType) expression).getCallTypes()) {
                    maybeWrongVoid(cast(ASTTypeDefinition.class, type));
                }
            }

            default -> currentType = new ReturnType(TokenType.VOID);
        }
        if (highlight) {
            highlights.add(new ASTHighlight(expression));
        }
    }

    /**
     * Visits a function call.
     *
     * @param fc         the actual function call
     * @param definition the fetched {@link Definition} of the function
     */
    private void visitFunctionCall(final ASTFunctionCall fc, final FunctionDefinition definition) {
        final var arguments = fc.getArguments();
        final var it  = arguments.listIterator();
        final var it2 = definition.getParameters().listIterator();
        Optional<StreamPosition> tooManyBegin = Optional.empty();
        ASTExpression elem = null;
        while (it.hasNext()) {
            elem = it.next();
            elem.visit(this);
            if (!it2.hasNext()) {
                if (!definition.isVariadic()) {
                    tooManyBegin = tooManyBegin.isEmpty() ? Optional.of(elem.getBegin()) : tooManyBegin;
                }
            } else {
                final var elem2 = it2.next();
                if (!elem2.getReturnType().isAssignableFrom(currentType)) {
                    final var typeString = Optional.ofNullable(elem2.getReturnType().toString());
                    highlights.add(new MessagedHighlight<>(elem.getBegin(),
                            elem.getEnd(),
                            InterpretationType.TYPE_MISMATCH,
                            typeString.orElse("<< unknown >>") +
                                    " is not assignable from " + currentType));
                }
            }
        }
        tooManyBegin.ifPresent(position -> highlights.add(new MessagedHighlight<>(
                position,
                arguments.get(arguments.size() - 1).getEnd(),
                InterpretationType.ERROR,
                String.format("Expected %d arguments, got %d",
                        definition.getParameters().size(),
                        arguments.size()))));
        if (it2.hasNext()) {
            highlights.add(new MessagedHighlight<>((elem == null ? fc.getBegin() : elem.getEnd()),
                    fc.getEnd(),
                    InterpretationType.ERROR,
                    String.format("Expected %d arguments, got %d",
                            definition.getParameters().size(),
                            arguments.size())));
        }
    }

    /**
     * Adds a type mismatch highlight if the given type represents {@code void}.
     *
     * @param definition the type definition to be checked
     */
    private void maybeWrongVoid(final ASTTypeDefinition definition) {
        if (definition instanceof final ASTTypeDeclaration declaration &&
            declaration.getType() == TokenType.VOID && !declaration.isArray()) {
            highlights.add(new MessagedHighlight<>(declaration.getBegin(),
                                                   declaration.getEnd(),
                                                   InterpretationType.TYPE_MISMATCH,
                                                   "'void' not allowed here"));
        }
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

        for (final var param : params) {
            if (param.getASTType() == ASTType.MISSING) {
                highlights.add(new MessagedHighlight<>(param.getBegin(),
                                                       param.getEnd(),
                                                       ASTType.MISSING,
                                                       ((ASTMissing) param).getMessage()));
            } else if (param.getASTType() != ASTType.AST_ELLIPSIS) {
                final var parameter = cast(ASTParameter.class, param);
                final var type      = cast(ASTTypeDefinition.class, parameter.getType());

                type.visit(this);
                maybeWrongVoid(type);

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
