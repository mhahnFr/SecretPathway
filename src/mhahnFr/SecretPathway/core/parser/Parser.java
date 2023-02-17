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
    /** The {@link Tokenizer} used by this parser. */
    private final Tokenizer tokenizer;
    /** The previous {@link Token} in the stream.  */
    private Token previous;
    /** The {@link Token} currently in the stream. */
    private Token current;
    /** The next {@link Token} in the stream.      */
    private Token next;

    /**
     * Initializes this parser using the given source code.
     *
     * @param source the source code to be parsed
     */
    public Parser(final String source) {
        final var stream = new StringStream(source);
        tokenizer = new Tokenizer(stream);
        advance(2);
        previous = new StartToken(stream.createStreamPosition(0));
    }

    /**
     * Advances the given amount of times.
     *
     * @param count how often to advance
     * @see #advance()
     */
    private void advance(final int count) {
        for (int i = 0; i < count; ++i) {
            advance();
        }
    }

    /**
     * Advances the stream by one token.
     */
    private void advance() {
        previous = current;
        current  = next;
        next     = tokenizer.nextToken();
    }

    /**
     * Combines the given {@link ASTExpression}s.
     *
     * @param main  the main expression
     * @param parts the parts to complete the main expression
     * @return an {@link ASTCombination} of the given expressions
     */
    private ASTExpression combine(ASTExpression main, List<ASTExpression> parts) {
        final var list = new Vector<ASTExpression>(parts.size() + 1);
        list.add(main);
        list.addAll(parts);
        return new ASTCombination(list);
    }

    /**
     * Combines the given {@link ASTExpression}s.
     *
     * @param main  the main expression
     * @param parts the parts to complete the main expression
     * @return an {@link ASTCombination} of the given expressions
     */
    private ASTExpression combine(ASTExpression main, ASTExpression... parts) {
        final var list = new Vector<ASTExpression>(parts.length + 1);
        list.add(main);
        list.addAll(Arrays.asList(parts));
        return new ASTCombination(list);
    }

    /**
     * Parses an include statement ({@code #include "foo"}). The 'include'
     * keyword is expected to be already consumed.
     *
     * @return the AST representation of the parsed include statement
     */
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

    /**
     * Parses an inherit statement ({@code inherit "secure/base";}). The
     * 'inherit' keyword is expected to be already consumed.
     *
     * @return the AST representation of the inherit statement
     */
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

    /**
     * Parses a class. The 'class' keyword is expected to be
     * already consumed. The class can be defined in either
     * shorthand form ({@code class foo "secure/base";}) or the
     * traditional form ({@code class foo { [...] };}).
     *
     * @return the AST representation of the class
     */
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

    /**
     * Returns whether the given {@link TokenType} represents
     * a modifier keyword.
     *
     * @param type the type to be checked
     * @return whether the given type represents a modifier
     */
    private boolean isModifier(final TokenType type) {
        return type == TokenType.PRIVATE    ||
               type == TokenType.PROTECTED  ||
               type == TokenType.PUBLIC     ||
               type == TokenType.DEPRECATED ||
               type == TokenType.OVERRIDE   ||
               type == TokenType.NOSAVE;
    }

    /**
     * Parses the modifiers currently in the stream.
     *
     * @return a list with AST representations of the read modifiers
     */
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

    /**
     * Returns whether the given {@link TokenType} is a type.
     *
     * @param type the type to be checked
     * @return whether the given type represents a type
     */
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

    /**
     * Parses a type. Types can come single ({@code int}),
     * they can be array types ({@code int *} and {@code int []})
     * or they can be function reference types ({@code int (int, int[], ...)}).
     *
     * @return the AST representation of the type currently in the stream
     */
    private ASTExpression parseType() {
        ASTExpression toReturn = null;
        final var type         = current;

        if (isType(type.type()) || type.type() == TokenType.IDENTIFIER) {
            final var parts = new Vector<ASTExpression>(2);
            if (type.type() == TokenType.IDENTIFIER) {
                parts.add(new ASTWrong(type, "Wrong type"));
            }
            advance();
            boolean array = true;
            if (current.type() == TokenType.STAR || current.type() == TokenType.RIGHT_BRACKET) {
                if (current.type() == TokenType.RIGHT_BRACKET) {
                    parts.add(new ASTMissing(previous.endPos(), current.beginPos(), "Missing '['"));
                }
                advance();
            } else if (current.type() == TokenType.LEFT_BRACKET && next.type() == TokenType.RIGHT_BRACKET) {
                advance(2);
            } else if (current.type() == TokenType.LEFT_BRACKET && next.type() != TokenType.RIGHT_BRACKET) {
                parts.add(new ASTMissing(current.endPos(), next.beginPos(), "Missing ']'"));
                advance();
            } else {
                array = false;
            }

            if (current.type() == TokenType.LEFT_PAREN) {
                advance();
                final var callTypes = new ArrayList<ASTExpression>();
                while (current.type() != TokenType.RIGHT_PAREN && !isStopToken(current)
                                                               && current.type() != TokenType.LEFT_CURLY) {
                    if (current.type() == TokenType.DOT      ||
                        current.type() == TokenType.ELLIPSIS ||
                        current.type() == TokenType.RANGE) {
                        final var ellipsis = new ASTEllipsis(current);
                        if (current.type() != TokenType.ELLIPSIS) {
                            callTypes.add(combine(ellipsis, new ASTWrong(current, "Expected '...'")));
                        } else {
                            callTypes.add(ellipsis);
                        }
                        advance();
                        continue;
                    }
                    callTypes.add(parseType());

                    if (current.type() != TokenType.RIGHT_PAREN && current.type() != TokenType.COMMA) {
                        parts.add(new ASTMissing(previous.endPos(), current.beginPos(), "Missing ','"));
                    } else if (current.type() == TokenType.COMMA) {
                        advance();
                    }
                }
                if (previous.type() == TokenType.COMMA) {
                    parts.add(new ASTMissing(previous.endPos(), current.beginPos(), "Missing type"));
                }
                if (current.type() != TokenType.RIGHT_PAREN) {
                    parts.add(new ASTMissing(previous.endPos(), current.beginPos(), "Missing ')'"));
                } else {
                    advance();
                }
                toReturn = new ASTFunctionReferenceType(type, array, callTypes, previous.endPos());
            }
            toReturn = toReturn == null ? new ASTTypeDeclaration(type, array) : toReturn;
            if (!parts.isEmpty()) {
                return combine(toReturn, parts);
            }
            return toReturn;
        } else {
            return combine(new ASTTypeDeclaration(previous.endPos(), current.beginPos()),
                           new ASTMissing(previous.endPos(), current.beginPos(), "Missing type"));
        }
    }

    /**
     * Returns whether the given {@link Token} is a stop
     * token.
     *
     * @param token the token to be checked
     * @return whether the token should stop a parsing loop
     */
    private boolean isStopToken(final Token token) {
        final var type = token.type();

        return type == TokenType.EOF                ||
               type == TokenType.RIGHT_PAREN        ||
               type == TokenType.RIGHT_BRACKET      ||
               type == TokenType.RIGHT_CURLY        ||
               type == TokenType.COLON              ||
               type == TokenType.SEMICOLON          ||
               type == TokenType.ASSIGNMENT         ||
               type == TokenType.ASSIGNMENT_PLUS    ||
               type == TokenType.ASSIGNMENT_MINUS   ||
               type == TokenType.ASSIGNMENT_STAR    ||
               type == TokenType.ASSIGNMENT_SLASH   ||
               type == TokenType.ASSIGNMENT_PERCENT ||
               type == TokenType.ELSE               ||
               type == TokenType.WHILE              ||
               type == TokenType.CATCH;
    }

    /**
     * Parses a name. A name can be almost anything ({@code test_no_1},
     * however, it can also be an operator identifier ({@code operator <<}).
     *
     * @return the AST representation of the next name in the stream
     */
    private ASTExpression parseName() {
        final ASTExpression toReturn;

        if (isStopToken(current)) {
            toReturn = combine(new ASTName(previous.endPos(), current.beginPos()),
                               new ASTMissing(previous.endPos(), current.beginPos(), "Missing name"));
        } else if (current.type() == TokenType.OPERATOR) {
            final var begin = current.beginPos();
            advance();

            final ASTExpression part;
            if (!isOperator(current.type())) {
                part = new ASTMissing(previous.endPos(), current.beginPos(), "Missing operator");
            } else {
                part = null;
                advance();
            }
            final var identifier = new ASTOperatorName(begin, previous);
            if (part != null) {
                toReturn = combine(identifier, part);
            } else {
                toReturn = identifier;
            }
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

    /**
     * Parses a top level variable definition. Variables can be assigned a
     * value immediately ({@code private int i = 42;}) or not ({@code deprecated int i;}).
     *
     * @param modifiers the modifiers of the variable
     * @param type      the type of the variable
     * @param name      the name of the variable
     * @return the AST representation of the read variable
     */
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
            toReturn = combine(variable, new ASTMissing(previous.endPos(), current.beginPos(), "Missing ';'"));
        }

        return toReturn;
    }

    /**
     * Parses the parameter definitions of a defined function. The opening
     * parenthesis is expected to be already consumed. Example: {@code int i, bool[] a, ...)}.
     *
     * @return a list with the AST representations of the read parameter definitions
     */
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

    /**
     * Parses an expression surrounded by parentheses ({@code ( <expression> )}).
     *
     * @return the AST representation of the parenthesized expression
     */
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

    /**
     * Parses an if statement. Example: {@code if ( <condition> ) <instruction>}.
     * It can optionally be followed by an else statement: {@code ... else <instruction>}.
     *
     * @return the AST representation of the full if statement
     */
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

    /**
     * Parses a while statement ({@code while ( <condition> ) <instruction>}).
     *
     * @return the AST representation of the full while statement
     */
    private ASTExpression parseWhile() {
        final var begin = current.beginPos();

        advance();

        final var condition = parseParenthesizedExpression();
        final var body      = parseInstruction();

        return new ASTWhile(begin, condition, body, false);
    }

    /**
     * Parses a do-while statement ({@code do <instruction> while ( <condition> );}).
     *
     * @return the AST representation of the full do-while statement
     */
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

    /**
     * Parses a for statement ({@code for ( <begin expression> ; <condition> ; <after expression>) <instruction>}).
     * Foreach loops are parsed by this method as well ({@code foreach ( <variable> : <expression> ) <instruction>}).
     *
     * @return the AST representation of the full for statement
     */
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

    /**
     * Parses a switch statement
     * ({@code switch ( <expression> ) { case <expression> : <instruction> ... default : <instruction> } }).
     *
     * @return the AST representation of the full switch statement
     */
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
        while (current.type() != TokenType.RIGHT_CURLY && !isStopToken(current)) {
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

    /**
     * Parses a return statement ({@code return <expression> ;}).
     *
     * @return the AST representation of the return statement
     */
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

    /**
     * Parses a try-catch statement ({@code try <instruction> catch <instruction>}).
     * The catch block can have a reference to the caught object:
     * {@code ... catch ( <variable> ) ...}.
     *
     * @return the AST representation of the full try-catch statement
     */
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

        final ASTExpression exception;
        if (current.type() == TokenType.LEFT_PAREN || current.type() == TokenType.RIGHT_PAREN) {
            if (current.type() != TokenType.LEFT_PAREN) {
                parts.add(new ASTMissing(previous.endPos(), current.beginPos(), "Missing '('"));
            } else {
                advance();
            }
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
        } else {
            exception = null;
        }
        final var caught = parseInstruction();

        final var tryCatch = new ASTTryCatch(begin, toTry, caught, exception);
        if (!parts.isEmpty()) {
            return combine(tryCatch, parts);
        }
        return tryCatch;
    }

    /**
     * Parses a new statement ({@code new("foo", <arguments>)}).
     *
     * @return the AST representation of the full new statement
     */
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

    /**
     * Parses a cast statement if the streamed tokens represent one.
     * Returns {@code null} if the next tokens do not represent a
     * cast statement.
     *
     * @param priority the priority to be used to parse the statement
     * @return either the AST representation of the cast expression or {@code null}
     * @see #parseCast(int)
     */
    private ASTExpression parseMaybeCast(final int priority) {
        if (next.type() == TokenType.RIGHT_PAREN && (isType(current.type()) || current.type() == TokenType.IDENTIFIER)) {
            return parseCast(priority);
        }
        return null;
    }

    /**
     * Parses a cast statement ({@code ( <type> )}).
     *
     * @param priority the priority to be used to parse the statement
     * @return the AST representation of the cast statement
     */
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

    /**
     * Parses a string expression ({@code "foo"}). Multiple following
     * strings are concatenated ({@code "foo" "bar"}).
     *
     * @return the AST representation of the read strings
     */
    private ASTExpression parseStrings() {
        final var toReturn = new ArrayList<ASTExpression>();

        while (current.type() == TokenType.STRING) {
            toReturn.add(new ASTString(current));
            advance();
        }

        return new ASTStrings(toReturn);
    }

    /**
     * Parses an ellipsis ({@code ...}).
     *
     * @return the AST representation of the read ellipsis
     */
    private ASTExpression parseEllipsis() {
        final var ellipsis = new ASTEllipsis(current);

        advance();
        if (previous.type() != TokenType.ELLIPSIS) {
            return combine(ellipsis, new ASTWrong(previous, "Expected '...'"));
        }
        return ellipsis;
    }

    /**
     * Parses a simple expression.
     *
     * @param priority the priority of the statement
     * @return the AST representation of the simple expression
     */
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
                        advance();
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

            case NIL          -> { toReturn = new ASTNil(current);       advance(); }
            case THIS         -> { toReturn = new ASTThis(current);      advance(); }
            case SYMBOL       -> { toReturn = new ASTSymbol(current);    advance(); }
            case INTEGER      -> { toReturn = new ASTInteger(current);   advance(); }
            case CHARACTER    -> { toReturn = new ASTCharacter(current); advance(); }
            case ELLIPSIS,
                 RANGE,
                 DOT          ->   toReturn = parseEllipsis();
            case NEW          ->   toReturn = parseNew();
            case STRING       ->   toReturn = parseStrings();
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

    /**
     * Parses a mapping expression ({@code [ <expression>, <expression> ]}).
     *
     * @return the AST representation of the mapping statement
     */
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

    /**
     * Parses an array expression ({@code { <expression>, <expression> }}).
     *
     * @return the AST representation of the array expression.
     */
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

    /**
     * Parses the comma separated call arguments until the given end
     * {@link TokenType} is reached ({@code <expression>, <expression> <end>}).
     *
     * @param end the end {@link TokenType}
     * @return a list with the AST representations of the parsed arguments
     */
    private List<ASTExpression> parseCallArguments(final TokenType end) {
        final var list = new ArrayList<ASTExpression>();

        var previousType = current.type();
        var count        = 0;
        while (current.type() != end && !isStopToken(current)) {
            if (current.type() == previousType) {
                if (count >= 10) {
                    System.err.println("4 >>>>>>> " + previousType + " <<<<<<<");
                    advance();
                    count = 0;
                    continue;
                } else {
                    ++count;
                }
            } else {
                previousType = current.type();
            }
            list.add(parseBlockExpression(99));
            if (current.type() != TokenType.COMMA && current.type() != end) {
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

    /**
     * Parses a function call ({@code foo( <arguments> )}).
     *
     * @return the AST representation of the function call
     */
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

    /**
     * Parses a subscript expression. The subscript can either be traditional
     * ({@code ... [ <expression> ]}) and might be assigned: {@code ... = <expression>}.
     * It can also be a range expression: {@code ...[ <expression> .. <expression> ]}.
     * However, range subscripts cannot be assigned.
     *
     * @param priority the priority used to parse the statement
     * @return the AST representation of the subscript statement
     */
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

    /**
     * Parses a ternary ({@code ... ? <expression> : <expression>}).
     *
     * @return the AST representation of the ternary
     */
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

    /**
     * Parses an operation.
     *
     * @param priority the priority used to parse the operation
     * @return the AST representation of the operation
     * @see #isOperator(TokenType)
     */
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

    /**
     * Parses an expression that can be found inside of functions.
     *
     * @param priority the priority to be used to parse the statement.
     * @return the AST representation of the parsed expression
     */
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
                advance();
            }
        } else if (type == TokenType.STAR) {
            advance();
            lhs = new ASTUnaryOperator(previous.beginPos(), TokenType.STAR, parseBlockExpression(1));
        } else if (priority >= 2 && (type == TokenType.PLUS   ||
                                     type == TokenType.MINUS  ||
                                     type == TokenType.SIZEOF ||
                                     type == TokenType.NOT)) {
            advance();
            if (type == TokenType.SIZEOF && current.type() == TokenType.LEFT_PAREN) {
                advance();
            }
            final var expression = parseBlockExpression(1);
            if (type == TokenType.SIZEOF && current.type() == TokenType.RIGHT_PAREN) {
                advance();
            }
            lhs = new ASTUnaryOperator(previous.beginPos(), type, expression);

            if (type == TokenType.PLUS) { return lhs; }
        } else {
            lhs = parseSimpleExpression(priority);
        }

        ASTExpression previousExpression = lhs;

        var previousType = current.type();
        var count        = 0;
        for (TokenType operatorType = current.type(); isOperator(operatorType) && !isStopToken(current); operatorType = current.type()) {
            if (current.type() == previousType) {
                if (count >= 10) {
                    System.err.println("3 >>>>>>> " + previousType + " <<<<<<<");
                    advance();
                    count = 0;
                    continue;
                } else {
                    ++count;
                }
            } else {
                previousType = current.type();
            }
            final var rhs = parseOperation(priority);
            if (rhs == null) break;
            previousExpression = new ASTOperation(previousExpression, rhs, operatorType);
        }

        return previousExpression;
    }

    /**
     * Asserts a semicolon follows after the given expression.
     *
     * @param expression the expression to be followed by a semicolon
     * @return the AST representation of the semicolon-ed expression
     */
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

    /**
     * Checks and parses a variable declaration. If no variable
     * declaration follows, {@code null} is returned.
     *
     * @return the AST representation of the variable declaration or {@code null}
     */
    private ASTExpression parseMaybeVariableDeclaration() {
        if (current.type() == TokenType.LET ||
                ((current.type() == TokenType.IDENTIFIER || isType(current.type())) && next.type() == TokenType.IDENTIFIER) ||
                (isType(current.type()) && (next.type() == TokenType.LEFT_BRACKET || next.type() == TokenType.STAR || next.type() == TokenType.RIGHT_BRACKET)) ||
                (isType(current.type()) && isStopToken(next)) || (isType(current.type()) && (next.type() == TokenType.LEFT_PAREN || next.type() == TokenType.RIGHT_PAREN))) {
            return parseFancyVariableDeclaration();
        }
        return null;
    }

    /**
     * Parses a variable declaration ({@code <type> <name>} or {@code let <name> : <type>}).
     * Let declarations fo not need a type definition: {@code let <name>}.
     *
     * @return the AST representation of the variable declaration
     */
    private ASTExpression parseFancyVariableDeclaration() {
        final ASTExpression variable;

        if (current.type() == TokenType.LET) {
            final var begin = current.beginPos();
            advance();

            final var name = parseName();
            final ASTExpression type;
            if (current.type() == TokenType.COLON) {
                advance();
                type = parseType();
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

    /**
     * Parses a break statement ({@code break ;}).
     *
     * @return the AST representation of the statement.
     */
    private ASTExpression parseBreak() {
        final var b = new ASTBreak(current);
        advance();
        return assertSemicolon(b);
    }

    /**
     * Parses a continue statement ({@code continue ;}).
     *
     * @return the AST representation of the statement
     */
    private ASTExpression parseContinue() {
        final var c = new ASTContinue(current);
        advance();
        return assertSemicolon(c);
    }

    /**
     * Parses an instruction. An instruction can be an expression
     * or an instruction (such as if-statements).
     *
     * @return the AST representation of the parsed instruction
     */
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

    /**
     * Parses a block ({@code { <statements> }}).
     *
     * @return the AST representation of the parsed block
     */
    private ASTExpression parseBlock() {
        final var block = new ArrayList<ASTExpression>();
        final var begin = current.beginPos();

        if (current.type() != TokenType.LEFT_CURLY) {
            block.add(new ASTMissing(previous.endPos(), current.beginPos(), "Missing '{'"));
        } else {
            advance();
        }

        var previousType = current.type();
        var count        = 0;
        while (current.type() != TokenType.RIGHT_CURLY
            && current.type() != TokenType.EOF) {
            if (current.type() == previousType) {
                if (count >= 10) {
                    advance();
                    count = 0;
                    System.err.println("2 >>>>>>> " + previousType + " <<<<<<<");
                    continue;
                }
                ++count;
            } else {
                previousType = current.type();
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

    /**
     * Parses a function definition ({@code <modifiers> <return type> <name> ( <parameters> ) <block>}).
     *
     * @param modifiers the modifiers of the function
     * @param type      the return type of the function
     * @param name      tha name of the function
     * @return the AST representation of the function definition
     */
    private ASTExpression parseFunctionDefinition(final List<ASTExpression> modifiers,
                                                  final ASTExpression       type,
                                                  final ASTExpression       name) {
        final var parameters = parseParameterDefinitions();
        final var body       = parseBlock();

        return new ASTFunctionDefinition(modifiers, type, name, parameters, body);
    }

    /**
     * Parses a top level expression as function definition or as variable
     * definition.
     *
     * @return the AST representation of the parsed expression
     */
    private ASTExpression parseFileExpression() {
        final var modifiers = parseModifiers();
        final var type      = parseType();
        final var name      = parseName();

        if (current.type() == TokenType.LEFT_PAREN) {
            advance();
            return parseFunctionDefinition(modifiers, type, name);
        } else {
            return parseVariableDefinition(modifiers, type, name);
        }
    }

    /**
     * Parses a first level expression. First level statements include: {@code #include},
     * {@code inherit}, {@code class} statements and variable and function definitions.
     *
     * @return the AST representation of the parsed top level expression
     */
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

    /**
     * Parses the source code until the given end {@link TokenType}
     * is reached.
     *
     * @param end the end {@link TokenType}
     * @return a list with the AST representations of the parsed statements
     */
    private List<ASTExpression> parse(final TokenType end) {
        final var expressions = new ArrayList<ASTExpression>();

        var previousType = current.type();
        var count        = 0;
        while (current.type() != TokenType.EOF && current.type() != end) {
            if (current.type() == previousType) {
                if (count >= 10) {
                    advance();
                    count = 0;
                    System.err.println("1 >>>>>>> " + previousType + " <<<<<<<");
                    continue;
                }
                ++count;
            } else {
                previousType = current.type();
            }
            expressions.add(parseExpression());
        }

        return expressions;
    }

    /**
     * Parses the whole source code.
     *
     * @return an array with all expressions that have been read
     */
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
