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

package mhahnFr.SecretPathway.core.lpc.parser.ast;

import mhahnFr.utils.StreamPosition;

/**
 * This class represents an {@code if} statement as an AST node.
 *
 * @author mhahnFr
 * @since 14.02.23
 */
public class ASTIf extends ASTExpression {
    /** The condition expression.      */
    private final ASTExpression condition;
    /** The if instruction.            */
    private final ASTExpression instruction;
    /** The optional else instruction. */
    private final ASTExpression elseInstruction;

    /**
     * Constructs this AST node using the given information.
     *
     * @param begin           the beginning position
     * @param condition       the condition expression
     * @param instruction     the instruction
     * @param elseInstruction the optional {@code else} instruction
     */
    public ASTIf(final StreamPosition begin,
                 final ASTExpression  condition,
                 final ASTExpression  instruction,
                 final ASTExpression  elseInstruction) {
        super(begin, elseInstruction == null ? instruction.getEnd() : elseInstruction.getEnd(), ASTType.AST_IF);

        this.condition       = condition;
        this.instruction     = instruction;
        this.elseInstruction = elseInstruction;
    }

    /**
     * Returns the conditional expression of this {@code if} statement.
     *
     * @return the conditional expression
     */
    public ASTExpression getCondition() {
        return condition;
    }

    /**
     * Returns the instruction associated with this {@code if} statement.
     *
     * @return the {@code if true} instruction
     */
    public ASTExpression getInstruction() {
        return instruction;
    }

    /**
     * Returns the instruction associated with the {@code else} clause
     * of this {@code if} statement.
     *
     * @return the {@code else} clause
     */
    public ASTExpression getElseInstruction() {
        return elseInstruction;
    }

    @Override
    public void visit(ASTVisitor visitor) {
        if (visitor.maybeVisit(this)) {
            condition.visit(visitor);
            instruction.visit(visitor);

            if (elseInstruction != null) {
                elseInstruction.visit(visitor);
            }
        }
    }

    @Override
    public String describe(int indentation) {
        final var indent = " ".repeat(Math.max(0, indentation));

        return super.describe(indentation) + " Condition:\n" +
                condition.describe(indentation + 4) + "\n" +
                indent + "Instruction:\n" +
                instruction.describe(indentation + 4) +
                (elseInstruction == null ? "" : "\n" + indent + "Else:\n" + elseInstruction.describe(indentation + 4));
    }
}
