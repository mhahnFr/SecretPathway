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

package mhahnFr.SecretPathway.core.parser.ast;

import java.util.List;

/**
 * This class represents a function definition as an AST node.
 *
 * @author mhahnFr
 * @since 02.02.23
 */
public class ASTFunctionDefinition extends ASTExpression {
    /** The declared modifiers of this function.   */
    private final List<ASTExpression> modifiers;
    /** The declared return type of this function. */
    private final ASTExpression type;
    /** The declared name of this function.        */
    private final ASTExpression name;
    /** The declared parameters.                   */
    private final List<ASTExpression> parameters;
    /** The body of this declared function.        */
    private final ASTExpression body;

    /**
     * Constructs this AST node using the given information.
     *
     * @param modifiers the declared modifiers
     * @param type the declared type
     * @param name the declared name
     * @param parameters the declared parameters
     * @param body the body of the function
     */
    public ASTFunctionDefinition(final List<ASTExpression> modifiers,
                                 final ASTExpression       type,
                                 final ASTExpression       name,
                                 final List<ASTExpression> parameters,
                                 final ASTExpression       body) {
        super(modifiers.isEmpty() ? type.getBegin() : modifiers.get(0).getBegin(),
              body.getEnd(),
              ASTType.FUNCTION_DEFINITION);

        this.modifiers  = modifiers;
        this.type       = type;
        this.name       = name;
        this.parameters = parameters;
        this.body       = body;
    }

    /**
     * Returns the declared modifiers of this declared function.
     *
     * @return the declared modifiers
     */
    public List<ASTExpression> getModifiers() {
        return modifiers;
    }

    /**
     * Returns the declared type of this declared function.
     *
     * @return the declared type
     */
    public ASTExpression getType() {
        return type;
    }

    /**
     * Returns the declared name of this declared function.
     *
     * @return the declared name
     */
    public ASTExpression getName() {
        return name;
    }

    /**
     * Returns the declared parameters of this declared function.
     *
     * @return the declared parameters
     */
    public List<ASTExpression> getParameters() {
        return parameters;
    }

    /**
     * Returns the body of this declared function.
     *
     * @return the body
     */
    public ASTExpression getBody() {
        return body;
    }

    @Override
    public void visit(ASTVisitor visitor) {
        if (visitor.maybeVisit(this)) {
            var iterator = modifiers.listIterator();
            while (iterator.hasNext()) {
                iterator.next().visit(visitor);
            }

            type.visit(visitor);
            name.visit(visitor);

            iterator = parameters.listIterator();
            while (iterator.hasNext()) {
                iterator.next().visit(visitor);
            }

            body.visit(visitor);
        }
    }

    @Override
    public String describe(int indentation) {
        final var builder = new StringBuilder();

        builder.append(super.describe(indentation)).append(" modifiers:\n");
        var iterator = modifiers.listIterator();
        while (iterator.hasNext()) {
            builder.append(iterator.next().describe(indentation + 4)).append('\n');
        }
        final var indent = " ".repeat(Math.max(0, indentation));
        builder.append(indent).append("return type:\n").append(type.describe(indentation + 4)).append('\n')
               .append(indent).append("name:\n").append(name.describe(indentation + 4)).append('\n')
               .append(" ".repeat(Math.max(0, indentation))).append("parameters:\n");
        iterator = parameters.listIterator();
        while (iterator.hasNext()) {
            builder.append(iterator.next().describe(indentation + 4)).append('\n');
        }
        builder.append(indent).append("body:\n").append(body.describe(indentation + 4));
        return builder.toString();
    }
}
