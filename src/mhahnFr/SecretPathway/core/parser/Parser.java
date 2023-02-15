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

package mhahnFr.SecretPathway.core.parser;

import mhahnFr.SecretPathway.core.parser.ast.*;
import mhahnFr.SecretPathway.core.parser.tokenizer.Token;
import mhahnFr.SecretPathway.core.parser.tokenizer.TokenType;
import mhahnFr.SecretPathway.core.parser.tokenizer.Tokenizer;
import mhahnFr.utils.StreamPosition;
import mhahnFr.utils.StringStream;

import java.util.*;

/**
 * This class parses LPC source code.
 *
 * @author mhahnFr
 * @since 02.02.23
 */
public class Parser {
    private final Tokenizer tokenizer;
    private Token previous;
    private Token current;
    private Token next;

    public Parser(final String source) {
        final var stream = new StringStream(source);
        tokenizer = new Tokenizer(stream);
        advance(2);
        previous = new StartToken(stream.createStreamPosition(0));
    }

    private void advance(final int count) {
        for (int i = 0; i < count; ++i) {
            advance();
        }
    }

    private void advance() {
        previous = current;
        current  = next;
        next     = tokenizer.nextToken();
    }

    private ASTExpression combine(ASTExpression main, List<ASTExpression> parts) {
        final var list = new Vector<ASTExpression>(parts.size() + 1);
        list.add(main);
        list.addAll(parts);
        return new ASTCombination(list);
    }

    private ASTExpression combine(ASTExpression main, ASTExpression... parts) {
        final var list = new Vector<ASTExpression>(parts.length + 1);
        list.add(main);
        list.addAll(Arrays.asList(parts));
        return new ASTCombination(list);
    }

    private ASTExpression parseInclude() {
        final ASTExpression toReturn;

        advance();
        if (current.type() != TokenType.STRING) {
            return combine(new ASTInclude(previous.beginPos(), current.beginPos(), null),
                           new ASTMissing(previous.endPos(), current.beginPos(), "Expected a string literal"));
        } else {
            toReturn = new ASTInclude(previous.beginPos(), current.endPos(), (String) current.payload());
        }

        advance();
        return toReturn;
    }

    private ASTExpression parseInherit() {
        final ASTExpression toReturn;

        advance();
        if (current.type() == TokenType.SEMICOLON) {
            toReturn = new ASTInheritance(previous.beginPos(), current.endPos(), null);
        } else if (next.type() == TokenType.SEMICOLON && current.type() != TokenType.STRING) {
            toReturn = combine(new ASTInheritance(previous.beginPos(), next.endPos(), null),
                               new ASTWrong(current, "Expected a string literal"));
            advance();
        } else if (current.type() == TokenType.STRING && next.type() != TokenType.SEMICOLON) {
            toReturn = combine(new ASTInheritance(previous.beginPos(), current.endPos(), (String) current.payload()),
                               new ASTMissing(current.endPos(), next.beginPos(), "Expected ';'"));
        } else if (current.type() != TokenType.SEMICOLON && next.type() != TokenType.SEMICOLON) {
            return combine(new ASTInheritance(previous.beginPos(), current.beginPos(), null),
                           new ASTMissing(previous.endPos(), current.beginPos(), "Expected ';'"));
        } else {
            toReturn = new ASTInheritance(previous.beginPos(), next.endPos(), (String) current.payload());
            advance();
        }
        advance();
        return toReturn;
    }

    private ASTExpression parseClass() {
        final var parts = new Vector<ASTExpression>(3);
        final var begin = current.beginPos();

        advance();

        final var name = parseName();

        if (current.type() == TokenType.SEMICOLON ||
                current.type() == TokenType.STRING ||
                (current.type() != TokenType.LEFT_CURLY && next.type() == TokenType.SEMICOLON)) {
            final ASTExpression inheritance;
            if (current.type() == TokenType.STRING) {
                inheritance = new ASTInheritance(current.beginPos(), current.endPos(), (String) current.payload());
                advance();
            } else if (current.type() == TokenType.SEMICOLON) {
                inheritance = null;
            } else {
                inheritance = combine(new ASTInheritance(current.beginPos(), current.endPos(), null),
                                      new ASTWrong(current, "Expected a string literal"));
            }
            return assertSemicolon(new ASTClass(begin, name, inheritance));
        } else if (current.type() != TokenType.LEFT_CURLY) {
            parts.add(new ASTMissing(previous.endPos(), current.beginPos(), "Missing '{'"));
        } else {
            advance();
        }
        final var statements = parse(TokenType.RIGHT_CURLY);
        if (current.type() != TokenType.RIGHT_CURLY) {
            parts.add(new ASTMissing(previous.endPos(), current.beginPos(), "Missing '}'"));
        } else {
            advance();
        }
        if (current.type() != TokenType.SEMICOLON) {
            parts.add(new ASTMissing(previous.endPos(), current.beginPos(), "Missing ';'"));
        } else {
            advance();
        }
        final var c = new ASTClass(begin, name, statements);
        if (!parts.isEmpty()) {
            return combine(c, parts);
        }
        return c;
    }

    private boolean isModifier(final TokenType type) {
        return type == TokenType.PRIVATE    ||
               type == TokenType.PROTECTED  ||
               type == TokenType.PUBLIC     ||
               type == TokenType.DEPRECATED ||
               type == TokenType.OVERRIDE   ||
               type == TokenType.NOSAVE;
    }

    private List<ASTExpression> parseModifiers() {
        final var toReturn = new Vector<ASTExpression>();

        while (true) {
            if (isModifier(current.type())) {
                toReturn.add(new ASTModifier(current));
            } else if (isModifier(next.type())) {
                toReturn.add(combine(new ASTModifier(current.beginPos(), current.endPos()),
                                     new ASTWrong(current, "Expected a modifier")));
            } else {
                break;
            }
            advance();
        }

        return toReturn;
    }

    private boolean isType(final TokenType type) {
        return type == TokenType.VOID           ||
               type == TokenType.CHAR_KEYWORD   ||
               type == TokenType.INT_KEYWORD    ||
               type == TokenType.BOOL           ||
               type == TokenType.OBJECT         ||
               type == TokenType.STRING_KEYWORD ||
               type == TokenType.SYMBOL_KEYWORD ||
               type == TokenType.MAPPING        ||
               type == TokenType.ANY            ||
               type == TokenType.MIXED          ||
               type == TokenType.AUTO           ||
               type == TokenType.OPERATOR;
    }

    private ASTExpression parseType() {
        final ASTExpression toReturn;

        if (!isType(current.type()) && next.type() == TokenType.IDENTIFIER) {
            toReturn = combine(new ASTTypeDeclaration(current.beginPos(), current.endPos()),
                               new ASTWrong(current, "Expected a type"));
            advance();
        } else if (current.type() == TokenType.IDENTIFIER) {
            toReturn = combine(new ASTTypeDeclaration(previous.endPos(), current.beginPos()),
                               new ASTMissing(previous.endPos(), current.beginPos(), "Missing type"));
        } else {
            final var type = current;
            advance();
            if (current.type() == TokenType.LEFT_BRACKET || current.type() == TokenType.RIGHT_BRACKET) {
                final var parts = new Vector<ASTExpression>(2);
                if (current.type() != TokenType.LEFT_BRACKET) {
                    parts.add(new ASTMissing(previous.endPos(), current.beginPos(), "Missing '['"));
                } else {
                    advance();
                }
                if (current.type() != TokenType.RIGHT_BRACKET) {
                    parts.add(new ASTMissing(previous.endPos(), current.beginPos(), "Missing ']'"));
                } else {
                    advance();
                }
                final var typeDeclaration = new ASTTypeDeclaration(type, true);
                if (!parts.isEmpty()) {
                    toReturn = combine(typeDeclaration, parts);
                } else {
                    toReturn = typeDeclaration;
                }
            } else {
                toReturn = new ASTTypeDeclaration(type, false);
            }
        }

        return toReturn;
    }

    private ASTExpression parseName() {
        final ASTExpression toReturn;

        if (current.type() == TokenType.LEFT_PAREN    ||
            current.type() == TokenType.SEMICOLON     ||
            current.type() == TokenType.RIGHT_BRACKET ||
            current.type() == TokenType.EQUALS) {
            toReturn = combine(new ASTName(previous.endPos(), current.beginPos()),
                               new ASTMissing(previous.endPos(), current.beginPos(), "Missing name"));
        } else if (current.type() != TokenType.IDENTIFIER) {
            toReturn = combine(new ASTName(current.beginPos(), current.endPos()),
                               new ASTWrong(current, "Expected a name"));
            advance();
        } else {
            toReturn = new ASTName(current);
            advance();
        }

        return toReturn;
    }

    private ASTExpression parseVariableDefinition(final List<ASTExpression> modifiers,
                                                  final ASTExpression       type,
                                                  final ASTExpression       name) {
        final ASTExpression toReturn;

        final StreamPosition begin = modifiers.isEmpty() ? type.getBegin() : modifiers.get(0).getBegin();
        final var variable = new ASTVariableDefinition(begin, name.getEnd(), modifiers, type, name);

        if (current.type() == TokenType.SEMICOLON) {
            advance();
            toReturn = variable;
        } else if (current.type() == TokenType.ASSIGNMENT) {
            advance();
            toReturn = assertSemicolon(new ASTOperation(variable, parseBlockExpression(99), TokenType.ASSIGNMENT));
        } else {
            // TODO: read till ; and mark as wrong
            toReturn = null;
        }

        return toReturn;
    }

    private List<ASTExpression> parseParameterDefinitions() {
        final var toReturn = new ArrayList<ASTExpression>();

        if (current.type() != TokenType.RIGHT_PAREN) {
            boolean stop = false;
            do {
                if (current.type() == TokenType.ELLIPSIS && (next.type() == TokenType.RIGHT_PAREN ||
                                                             next.type() == TokenType.LEFT_CURLY)) {
                    toReturn.add(new ASTEllipsis(current));
                    if (next.type() == TokenType.LEFT_CURLY) {
                        toReturn.add(new ASTMissing(current.endPos(), next.beginPos(), "Expected ')'"));
                        advance();
                    } else {
                        advance(2);
                    }
                    break;
                }

                final ASTExpression type = parseType();

                final ASTExpression name;
                if (current.type() != TokenType.IDENTIFIER) {
                    if (current.type() == TokenType.COMMA || current.type() == TokenType.RIGHT_PAREN) {
                        name = combine(new ASTName(previous.endPos(), current.beginPos()),
                                       new ASTMissing(previous.endPos(), current.beginPos(), "Parameter's name missing"));
                    } else {
                        name = combine(new ASTName(current.beginPos(), current.endPos()),
                                       new ASTWrong(current, "Expected parameter's name"));
                        advance();
                    }
                } else {
                    name = new ASTName(current);
                    advance();
                }

                toReturn.add(new ASTParameter(type, name));

                if (current.type() == TokenType.RIGHT_PAREN || current.type() == TokenType.LEFT_CURLY) {
                    stop = true;
                    if (current.type() == TokenType.LEFT_CURLY) {
                        toReturn.add(new ASTMissing(previous.endPos(), current.beginPos(), "Expected ')'"));
                    } else {
                        advance();
                    }
                } else if (current.type() != TokenType.COMMA) {
                    toReturn.add(new ASTMissing(previous.endPos(), current.beginPos(), "Expected ','"));
                } else {
                    advance();
                }
            } while (!stop && current.type() != TokenType.EOF);
        } else {
            advance();
        }

        return toReturn;
    }

    private ASTExpression parseParenthesizedExpression() {
        final var parts = new Vector<ASTExpression>(2);

        if (current.type() != TokenType.LEFT_PAREN) {
            parts.add(new ASTMissing(previous.endPos(), current.beginPos(), "Missing '('"));
        } else {
            advance();
        }
        final var expression = parseBlockExpression(99);
        if (current.type() != TokenType.RIGHT_PAREN) {
            parts.add(new ASTMissing(previous.endPos(), current.beginPos(), "Missing ')'"));
        } else {
            advance();
        }

        if (!parts.isEmpty()) {
            return combine(expression, parts);
        }
        return expression;
    }

    private ASTExpression parseIf() {
        final var begin = current.beginPos();

        advance();

        final var condition   = parseParenthesizedExpression();
        final var instruction = parseInstruction();

        final ASTExpression elseInstruction;
        if (current.type() == TokenType.ELSE) {
            advance();
            elseInstruction = parseInstruction();
        } else {
            elseInstruction = null;
        }

        return new ASTIf(begin, condition, instruction, elseInstruction);
    }

    private ASTExpression parseWhile() {
        final var begin = current.beginPos();

        advance();

        final var condition = parseParenthesizedExpression();
        final var body      = parseInstruction();

        return new ASTWhile(begin, condition, body, false);
    }

    private ASTExpression parseDo() {
        final var begin = current.beginPos();

        advance();

        final var instruction = parseInstruction();

        final ASTExpression part;
        if (current.type() != TokenType.WHILE) {
            part = new ASTMissing(previous.endPos(), current.beginPos(), "Missing 'while'");
        } else {
            part = null;
            advance();
        }
        final var condition = parseParenthesizedExpression();

        final var loop = new ASTWhile(begin, condition, instruction, true);
        if (part != null) {
            return combine(loop, part);
        }
        return loop;
    }

    private ASTExpression parseFor() {
        final var parts = new Vector<ASTExpression>();
        final var begin = current.beginPos();

        advance();

        if (current.type() != TokenType.LEFT_PAREN) {
            parts.add(new ASTMissing(previous.endPos(), current.beginPos(), "Missing '('"));
        } else {
            advance();
        }
        var variable = parseMaybeVariableDeclaration();
        if (variable != null) {
            if (current.type() == TokenType.COLON) {
                advance();
                final var expression = parseBlockExpression(99);
                if (current.type() != TokenType.RIGHT_PAREN) {
                    parts.add(new ASTMissing(previous.endPos(), current.beginPos(), "Missing ')'"));
                } else {
                    advance();
                }
                final var loop = new ASTForEach(begin, variable, expression, parseInstruction());
                if (!parts.isEmpty()) {
                    return combine(loop, parts);
                }
                return loop;
            } else {
                variable = assertSemicolon(variable);
            }
        }
        final var initExpression = variable == null ? assertSemicolon(parseBlockExpression(99)) : variable;
        final var condition      = assertSemicolon(parseBlockExpression(99));
        final var after          = parseBlockExpression(99);

        if (current.type() != TokenType.RIGHT_PAREN) {
            parts.add(new ASTMissing(previous.endPos(), current.beginPos(), "Missing ')'"));
        } else {
            advance();
        }
        final var loop = new ASTFor(begin, initExpression, condition, after, parseInstruction());
        if (!parts.isEmpty()) {
            return combine(loop, parts);
        }
        return loop;
    }

    private ASTExpression parseSwitch() {
        final var begin = current.beginPos();

        advance();

        final var var = parseParenthesizedExpression();

        final ASTExpression part;
        if (current.type() != TokenType.LEFT_CURLY) {
            part = new ASTMissing(previous.endPos(), current.beginPos(), "Missing '{'");
        } else {
            part = null;
            advance();
        }

        final var defCase = new ASTEmpty(previous.endPos(), current.beginPos());
        ASTExpression lastCase = defCase;
        final var lastCaseExpressions = new Vector<ASTExpression>();
        final var cases = new ArrayList<ASTExpression>();
        while (current.type() != TokenType.RIGHT_CURLY) { // TODO: Stop characters
            if (current.type() == TokenType.CASE) {
                if (lastCase != defCase || !lastCaseExpressions.isEmpty()) {
                    cases.add(new ASTCase(lastCase, lastCaseExpressions.toArray(new ASTExpression[0])));
                }

                advance();

                lastCase = parseBlockExpression(99);
                if (current.type() != TokenType.COLON) {
                    lastCase = combine(lastCase, new ASTMissing(previous.endPos(), current.beginPos(), "Missing ':'"));
                } else {
                    advance();
                }
                lastCaseExpressions.clear();
            } else if (current.type() == TokenType.DEFAULT) {
                cases.add(new ASTCase(lastCase, lastCaseExpressions.toArray(new ASTExpression[0])));

                lastCase = new ASTDefault(current);
                advance();
                if (current.type() != TokenType.COLON) {
                    lastCase = combine(lastCase, new ASTMissing(previous.endPos(), current.beginPos(), "Missing ':'"));
                } else {
                    advance();
                }
                lastCaseExpressions.clear();
            } else {
                lastCaseExpressions.add(parseInstruction());
            }
        }
        if (lastCase != defCase || !lastCaseExpressions.isEmpty()) {
            cases.add(new ASTCase(lastCase, lastCaseExpressions.toArray(new ASTExpression[0])));
        }

        advance();
        final var toReturn = new ASTSwitch(begin, previous.endPos(), var, cases);
        if (part != null) {
            return combine(toReturn, part);
        }
        return toReturn;
    }

    private ASTExpression parseReturn() {
        final ASTExpression toReturn;

        advance();
        if (current.type() != TokenType.SEMICOLON) {
            toReturn = new ASTReturn(previous.beginPos(), parseBlockExpression(99), previous.endPos());
        } else {
            toReturn = new ASTReturn(previous.beginPos(), null, current.endPos());
        }

        return toReturn;
    }

    private ASTExpression parseTryCatch() {
        final var parts = new Vector<ASTExpression>(3);
        final var begin = current.beginPos();

        advance();

        final var toTry = parseInstruction();
        if (current.type() != TokenType.CATCH) {
            parts.add(new ASTMissing(previous.endPos(), current.beginPos(), "Missing 'catch'"));
        } else {
            advance();
        }
        if (current.type() != TokenType.LEFT_PAREN) {
            parts.add(new ASTMissing(previous.endPos(), current.beginPos(), "Missing '('"));
        } else {
            advance();
        }
        final ASTExpression exception;
        if (current.type() != TokenType.RIGHT_PAREN) {
            exception = parseFancyVariableDeclaration();
            if (current.type() != TokenType.RIGHT_PAREN) {
                parts.add(new ASTMissing(previous.endPos(), current.beginPos(), "Missing ')'"));
            } else {
                advance();
            }
        } else {
            exception = null;
            advance();
        }
        final var caught = parseInstruction();

        final var tryCatch = new ASTTryCatch(begin, toTry, caught, exception);
        if (!parts.isEmpty()) {
            return combine(tryCatch, parts);
        }
        return tryCatch;
    }

    private ASTExpression parseNew() {
        advance();

        final var parts = new Vector<ASTExpression>(3);
        final var begin = previous.beginPos();

        if (current.type() != TokenType.LEFT_PAREN) {
            parts.add(new ASTMissing(previous.endPos(), current.beginPos(), "Missing '('"));
        } else {
            advance();
        }
        final var instancingExpression = parseBlockExpression(99);

        final List<ASTExpression> arguments;
        if (current.type() != TokenType.RIGHT_PAREN) {
            if (current.type() != TokenType.COMMA) {
                parts.add(new ASTMissing(previous.endPos(), current.beginPos(), "Missing ','"));
            } else {
                advance();
            }
            arguments = parseCallArguments(TokenType.RIGHT_PAREN);
            if (current.type() != TokenType.RIGHT_PAREN) {
                parts.add(new ASTMissing(previous.endPos(), current.beginPos(), "Missing ')'"));
            } else {
                advance();
            }
        } else {
            arguments = null;
            advance();
        }
        final var result = new ASTNew(begin, previous.endPos(), instancingExpression, arguments);
        if (!parts.isEmpty()) {
            return combine(result, parts);
        }
        return result;
    }

    private ASTExpression parseMaybeCast(final int priority) {
        if (next.type() == TokenType.RIGHT_PAREN && (isType(current.type()) || current.type() == TokenType.IDENTIFIER)) {
            return parseCast(priority);
        }
        return null;
    }

    private ASTExpression parseCast(final int priority) {
        final var begin = previous.beginPos();
        final var type  = parseType();

        final ASTExpression part;
        if (current.type() != TokenType.RIGHT_PAREN) {
            part = new ASTMissing(previous.endPos(), current.beginPos(), "Missing ')'");
        } else {
            part = null;
            advance();
        }
        final var expression = parseBlockExpression(priority);
        final var cast       = new ASTCast(begin, type, expression);
        if (part != null) {
            return combine(cast, part);
        }
        return cast;
    }

    private ASTExpression parseSimpleExpression(final int priority) {
        final ASTExpression toReturn;

        switch (current.type()) {
            case IDENTIFIER -> {
                switch (next.type()) {
                    case LEFT_PAREN -> {
                        final var name = new ASTName(current);
                        advance(2);

                        final var arguments = parseCallArguments(TokenType.RIGHT_PAREN);

                        final ASTExpression part;
                        if (current.type() != TokenType.RIGHT_PAREN) {
                            part = new ASTMissing(previous.endPos(), current.beginPos(), "Missing ')'");
                        } else {
                            part = null;
                            advance();
                        }
                        final var func = new ASTFunctionCall(name, arguments, previous.endPos());
                        if (part == null) {
                            toReturn = func;
                        } else {
                            toReturn = combine(func, part);
                        }
                    }

                    case ASSIGNMENT,
                         ASSIGNMENT_PLUS,
                         ASSIGNMENT_MINUS,
                         ASSIGNMENT_PERCENT,
                         ASSIGNMENT_SLASH,
                         ASSIGNMENT_STAR -> {

                        final var name = new ASTName(current);
                        final var type = next.type();
                        advance(2);
                        toReturn = new ASTOperation(name, parseBlockExpression(99), type);
                    }

                    case INCREMENT,
                         DECREMENT -> {
                        advance();
                        toReturn = new ASTUnaryOperator(previous.beginPos(), current.type(), new ASTName(previous));
                    }

                    default -> {
                        toReturn = new ASTName(current);
                        advance();
                    }
                }
            }

            case SCOPE -> toReturn = new ASTUnaryOperator(current.beginPos(), TokenType.SCOPE, parseFunctionCall());

            case STAR -> {
                advance();
                toReturn = new ASTUnaryOperator(previous.beginPos(), previous.type(), parseBlockExpression(1));
            }

            case NIL          -> { toReturn = new ASTNil(current);      advance(); }
            case THIS         -> { toReturn = new ASTThis(current);     advance(); }
            case STRING       -> { toReturn = new ASTString(current);   advance(); }
            case SYMBOL       -> { toReturn = new ASTSymbol(current);   advance(); }
            case INTEGER      -> { toReturn = new ASTInteger(current);  advance(); }
            case ELLIPSIS     -> { toReturn = new ASTEllipsis(current); advance(); }
            case NEW          ->   toReturn = parseNew();
            case LEFT_CURLY   ->   toReturn = parseArray();
            case LEFT_BRACKET ->   toReturn = parseMapping();

            case LEFT_PAREN -> {
                advance();

                final var cast = parseMaybeCast(priority);
                if (cast == null) {
                    final var expression = parseBlockExpression(99);
                    if (current.type() != TokenType.RIGHT_PAREN) {
                        toReturn = combine(expression, new ASTMissing(previous.endPos(), current.beginPos(), "Missing ')'"));
                    } else {
                        advance();
                        toReturn = expression;
                    }
                } else {
                    toReturn = cast;
                }
            }

            case TRUE,
                 FALSE -> { toReturn = new ASTBool(current); advance(); }

            default -> toReturn = new ASTMissing(previous.endPos(), current.beginPos(), "Missing expression");
        }
        return toReturn;
    }

    private ASTExpression parseMapping() {
        final var begin = current.beginPos();
        advance();

        final var args = parseCallArguments(TokenType.RIGHT_BRACKET);

        final ASTExpression part;
        if (current.type() != TokenType.RIGHT_BRACKET) {
            part = new ASTMissing(previous.endPos(), current.beginPos(), "Missing ']'");
        } else {
            part = null;
            advance();
        }

        final var mapping = new ASTMapping(begin, previous.endPos(), args);
        if (part != null) {
            return combine(mapping, part);
        }
        return mapping;
    }

    private ASTExpression parseArray() {
        final var begin = current.beginPos();
        advance();

        final var args = parseCallArguments(TokenType.RIGHT_CURLY);

        final ASTExpression part;
        if (current.type() != TokenType.RIGHT_CURLY) {
            part = new ASTMissing(previous.endPos(), current.beginPos(), "Missing '}'");
        } else {
            part = null;
            advance();
        }

        final var array = new ASTArray(begin, previous.endPos(), args);
        if (part != null) {
            return combine(array, part);
        }
        return array;
    }

    private List<ASTExpression> parseCallArguments(final TokenType end) {
        final var list = new ArrayList<ASTExpression>();

        while (current.type() != end && current.type() != TokenType.EOF && current.type() != TokenType.RIGHT_BRACKET && current.type() != TokenType.RIGHT_CURLY && current.type() != TokenType.SEMICOLON) {
            list.add(parseBlockExpression(99));
            if (current.type() != TokenType.COMMA && current.type() != end) { // TODO: Implement other stopping characters
                list.add(new ASTMissing(previous.endPos(), current.beginPos(), "Missing ','"));
            } else if (current.type() == TokenType.COMMA) {
                advance();
            }
        }
        if (previous.type() == TokenType.COMMA) {
            list.add(new ASTMissing(previous.endPos(), current.beginPos(), "Missing expression"));
        }

        return list;
    }

    private ASTExpression parseFunctionCall() {
        final ASTExpression toReturn;

        final var parts = new Vector<ASTExpression>();

        advance();

        final var name = parseName();

        if (current.type() != TokenType.LEFT_PAREN) {
            parts.add(new ASTMissing(previous.endPos(), current.beginPos(), "Missing '('"));
        } else {
            advance();
        }

        final var arguments = parseCallArguments(TokenType.RIGHT_PAREN);

        if (current.type() != TokenType.RIGHT_PAREN) {
            parts.add(new ASTMissing(previous.endPos(), current.beginPos(), "Missing ')'"));
        } else {
            advance();
        }

        toReturn = new ASTFunctionCall(name, arguments, previous.endPos());

        if (!parts.isEmpty()) {
            return combine(toReturn, parts);
        }
        return toReturn;
    }

    private ASTExpression parseSubscript(final int priority) {
        final ASTExpression toReturn;

        advance();

        final var expression = parseBlockExpression(99);
        if (current.type() == TokenType.RANGE) {
            advance();
            final var rhs = parseBlockExpression(99);

            final var result = new ASTOperation(expression, rhs, TokenType.RANGE);
            if (current.type() != TokenType.RIGHT_BRACKET) {
                toReturn = new ASTSubscript(combine(result, new ASTMissing(previous.endPos(), current.beginPos(), "Missing ']'")));
            } else {
                advance();
                toReturn = new ASTSubscript(result);
            }
        } else {
            final ASTExpression part;
            if (current.type() != TokenType.RIGHT_BRACKET) {
                part = new ASTMissing(previous.endPos(), current.beginPos(), "Missing ']'");
            } else {
                part = null;
                advance();
            }

            final ASTExpression subscript;
            if (part == null) {
                subscript = new ASTSubscript(expression);
            } else {
                subscript = combine(new ASTSubscript(expression), part);
            }

            if (current.type() == TokenType.ASSIGNMENT) {
                advance();
                final var rhs = parseBlockExpression(priority);
                toReturn = new ASTOperation(subscript, rhs, TokenType.ASSIGNMENT);
            } else {
                toReturn = subscript;
            }
        }
        return toReturn;
    }

    private ASTExpression parseTernary() {
        advance();

        final var truePart = parseBlockExpression(12);

        final ASTExpression part;
        if (current.type() != TokenType.COLON) {
            part = new ASTMissing(previous.endPos(), current.beginPos(), "Missing ':'");
        } else {
            part = null;
            advance();
        }

        final var falsePart = parseBlockExpression(12);

        final var toReturn = new ASTOperation(truePart, falsePart, null);

        if (part != null) {
            return combine(toReturn, part);
        }
        return toReturn;
    }

    private ASTExpression parseOperation(final int priority) {
        final var type = current.type();

        if (priority >= 1 && (type == TokenType.ARROW ||
                              type == TokenType.DOT)) {
            return parseFunctionCall();
        } else if (type == TokenType.LEFT_BRACKET) {
            return parseSubscript(priority);
        } else if (priority >= 13 && type == TokenType.QUESTION) {
            return parseTernary();
        } else if (priority >= 13 && type == TokenType.DOUBLE_QUESTION) {
            advance();
            return parseBlockExpression(12);
        } else if (priority >= 12 && type == TokenType.OR) {
            advance();
            return parseBlockExpression(11);
        } else if (priority >= 11 && type == TokenType.AND) {
            advance();
            return parseBlockExpression(10);
        } else if (priority >= 10 && type == TokenType.PIPE) {
            advance();
            return parseBlockExpression(9);
        } else if (priority >= 8 && type == TokenType.AMPERSAND) {
            advance();
            return parseBlockExpression(7);
        } else if (priority >= 5 && (type == TokenType.RIGHT_SHIFT ||
                                     type == TokenType.LEFT_SHIFT)) {
            advance();
            return parseBlockExpression(4);
        } else if (priority >= 7 && (type == TokenType.NOT_EQUAL ||
                                     type == TokenType.EQUALS)) {
            advance();
            return parseBlockExpression(6);
        } else if (priority >= 6 && (type == TokenType.LESS             ||
                                     type == TokenType.LESS_OR_EQUAL    ||
                                     type == TokenType.GREATER          ||
                                     type == TokenType.GREATER_OR_EQUAL)) {
            advance();
            return parseBlockExpression(6);
        } else if (priority >= 4 && (type == TokenType.MINUS ||
                                     type == TokenType.PLUS)) {
            advance();
            return parseBlockExpression(3);
        } else if (priority >= 3 && (type == TokenType.SLASH   ||
                                     type == TokenType.PERCENT ||
                                     type == TokenType.STAR)) {
            advance();
            return parseBlockExpression(2);
        } else if (priority >= 2 && type == TokenType.IS) {
            advance();
            return parseType();
        }

        return null;
    }

    /**
     * Returns whether the given type represents an operator.
     *
     * @param type the type to be checked
     * @return whether the given type is an operator
     */
    private boolean isOperator(final TokenType type) {
        return type == TokenType.DOT              ||
               type == TokenType.ARROW            ||
               type == TokenType.PIPE             ||
               type == TokenType.LEFT_SHIFT       ||
               type == TokenType.RIGHT_SHIFT      ||
               type == TokenType.DOUBLE_QUESTION  ||
               type == TokenType.QUESTION         ||
               type == TokenType.PLUS             ||
               type == TokenType.MINUS            ||
               type == TokenType.STAR             ||
               type == TokenType.SLASH            ||
               type == TokenType.PERCENT          ||
               type == TokenType.LESS             ||
               type == TokenType.LESS_OR_EQUAL    ||
               type == TokenType.GREATER          ||
               type == TokenType.GREATER_OR_EQUAL ||
               type == TokenType.EQUALS           ||
               type == TokenType.NOT_EQUAL        ||
               type == TokenType.AMPERSAND        ||
               type == TokenType.AND              ||
               type == TokenType.OR               ||
               type == TokenType.LEFT_BRACKET     ||
               type == TokenType.IS;
    }

    private ASTExpression parseBlockExpression(final int priority) {
        final ASTExpression lhs;

        final var type = current.type();
        if (type == TokenType.AMPERSAND) {
            if (next.type() != TokenType.IDENTIFIER) {
                lhs = new ASTUnaryOperator(current.beginPos(), TokenType.AMPERSAND,
                        combine(new ASTName(current.endPos(), next.beginPos()),
                                new ASTMissing(current.endPos(), next.beginPos(), "Missing identifier!")));
            } else {
                advance();
                lhs = new ASTUnaryOperator(previous.beginPos(), TokenType.AMPERSAND, new ASTName(current));
            }
        } else if (type == TokenType.STAR) {
            advance();
            lhs = new ASTUnaryOperator(previous.beginPos(), TokenType.STAR, parseBlockExpression(1));
        } else if (priority >= 2 && (type == TokenType.PLUS   ||
                                     type == TokenType.MINUS  ||
                                     type == TokenType.SIZEOF ||
                                     type == TokenType.NOT)) {
            advance();
            lhs = new ASTUnaryOperator(previous.beginPos(), type, parseBlockExpression(1));

            if (type == TokenType.PLUS) { return lhs; }
        } else {
            lhs = parseSimpleExpression(priority);
        }

        ASTExpression previousExpression = lhs;
        for (TokenType operatorType = current.type(); isOperator(operatorType); operatorType = current.type()) {
            final var rhs = parseOperation(priority);
            if (rhs == null) break;
            previousExpression = new ASTOperation(previousExpression, rhs, operatorType);
        }

        return previousExpression;
    }

    private ASTExpression assertSemicolon(final ASTExpression expression) {
        final ASTExpression toReturn;

        if (current.type() != TokenType.SEMICOLON) {
            toReturn = combine(expression, new ASTMissing(previous.endPos(), current.beginPos(), "Missing ';'"));
        } else {
            advance();
            toReturn = expression;
        }

        return toReturn;
    }

    private ASTExpression parseMaybeVariableDeclaration() {
        if (current.type() == TokenType.LET || (next.type() == TokenType.IDENTIFIER &&
                                                (current.type() == TokenType.IDENTIFIER || isType(current.type())))) {
            return parseFancyVariableDeclaration();
        }
        return null;
    }

    private ASTExpression parseFancyVariableDeclaration() {
        final ASTExpression variable;

        if (current.type() == TokenType.LET) {
            final var begin = current.beginPos();
            advance();

            final var name = parseName();
            final ASTExpression type;
            if (current.type() == TokenType.COLON) {
                advance();
                type = parseType(); // FIXME: Looses the assignment if type is missing!
            } else if (next.type() == TokenType.ASSIGNMENT && (current.type() == TokenType.IDENTIFIER || isType(current.type()))) {
                final var missing = new ASTMissing(previous.endPos(), current.beginPos(), "Missing ':'");
                type = combine(parseType(), missing);
            } else {
                type = null;
            }
            variable = new ASTVariableDefinition(begin, type == null ? name.getEnd() : type.getEnd(), null, type, name);
        } else {
            final var type = parseType();
            final var name = parseName();

            variable = new ASTVariableDefinition(type.getBegin(), name.getEnd(), null, type, name);
        }

        final ASTExpression toReturn;
        if (current.type() == TokenType.ASSIGNMENT) {
            advance();
            toReturn = new ASTOperation(variable, parseBlockExpression(99), TokenType.ASSIGNMENT);
        } else {
            toReturn = variable;
        }
        return toReturn;
    }

    private ASTExpression parseBreak() {
        final var b = new ASTBreak(current);
        advance();
        return assertSemicolon(b);
    }

    private ASTExpression parseContinue() {
        final var c = new ASTContinue(current);
        advance();
        return assertSemicolon(c);
    }

    private ASTExpression parseInstruction() {
        final ASTExpression toReturn;

        final var maybeVariable = parseMaybeVariableDeclaration();
        if (maybeVariable != null) {
            return assertSemicolon(maybeVariable);
        }
        switch (current.type()) {
            case LEFT_CURLY   -> toReturn = parseBlock();
            case IF           -> toReturn = parseIf();
            case WHILE        -> toReturn = parseWhile();
            case FOR, FOREACH -> toReturn = parseFor();
            case SWITCH       -> toReturn = parseSwitch();
            case DO           -> toReturn = assertSemicolon(parseDo());
            case BREAK        -> toReturn = parseBreak();
            case CONTINUE     -> toReturn = parseContinue();
            case RETURN       -> toReturn = assertSemicolon(parseReturn());
            case TRY          -> toReturn = parseTryCatch();
            case SEMICOLON    -> {
                return assertSemicolon(new ASTEmpty(current.beginPos(), current.endPos()));
            }

            default -> toReturn = assertSemicolon(parseBlockExpression(99));
        }

        return toReturn;
    }

    private ASTExpression parseBlock() {
        final var block = new ArrayList<ASTExpression>();
        final var begin = current.beginPos();

        if (current.type() != TokenType.LEFT_CURLY) {
            block.add(new ASTMissing(previous.endPos(), current.beginPos(), "Missing '{'"));
        } else {
            advance();
        }
        TokenType p = current.type();
        int i = 0;
        while (current.type() != TokenType.RIGHT_CURLY
            && current.type() != TokenType.EOF) {
            if (current.type() == p) {
                if (i >= 10) {
                    advance();
                    continue;
                }
                ++i;
            } else {
                p = current.type();
            }
            block.add(parseInstruction());
        }
        if (current.type() == TokenType.EOF) {
            block.add(new ASTMissing(previous.endPos(), current.beginPos(), "Missing '}'"));
        } else {
            advance();
        }

        return new ASTBlock(begin, current.endPos(), block);
    }

    private ASTExpression parseFunctionDefinition(final List<ASTExpression> modifiers,
                                                  final ASTExpression       type,
                                                  final ASTExpression       name) {
        final var parameters = parseParameterDefinitions();
        final var body       = parseBlock();

        return new ASTFunctionDefinition(modifiers, type, name, parameters, body);
    }

    private ASTExpression parseFileExpression() {
        final var modifiers = parseModifiers();
        final var type      = parseType();
        final var name      = parseName();

        if (current.type() == TokenType.LEFT_PAREN) {
            // def. func
            advance();
            return parseFunctionDefinition(modifiers, type, name);
        } else if (current.type() == TokenType.SEMICOLON || current.type() == TokenType.ASSIGNMENT) {
            // def. var
            return parseVariableDefinition(modifiers, type, name);
        } else {
            // TODO
        }
        return null;
    }

    private ASTExpression parseExpression() {
        if (current.type() == TokenType.INCLUDE) {
            return parseInclude();
        } else if (current.type() == TokenType.INHERIT) {
            return parseInherit();
        } else if (current.type() == TokenType.CLASS) {
            return parseClass();
        }
        return parseFileExpression();
    }

    private List<ASTExpression> parse(final TokenType end) {
        final var expressions = new ArrayList<ASTExpression>();

        while (current.type() != TokenType.EOF && current.type() != end) {
            expressions.add(parseExpression());
        }

        return expressions;
    }

    public ASTExpression[] parse() {
        return parse(TokenType.EOF).toArray(new ASTExpression[0]);
    }

    /**
     * This class represents a dummy starting token.
     *
     * @author mhahnFr
     * @since 02.02.23
     */
    private static class StartToken extends Token {
        /**
         * Constructs this dummy token using the given position.
         *
         * @param position the position of this dummy token
         */
        public StartToken(final StreamPosition position) {
            super(position, null, null, position);
        }
    }
}
