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

import mhahnFr.SecretPathway.core.lpc.parser.ast.ASTTypeDefinition;

/**
 * This class represents an interpreted instruction.
 *
 * @author mhahnFr
 * @since 21.02.23
 */
public abstract class Instruction {
    /** The beginning position of this instruction. */
    private final int begin;
    /** The end position of this instruction.       */
    private int end;
    /** The return type of this instruction.        */
    private ASTTypeDefinition returnType;

    /**
     * Constructs this instruction using the given beginning
     * position.
     *
     * @param begin the beginning position
     */
    protected Instruction(int begin) {
        this.begin = begin;
    }

    /**
     * Returns the end position of this instruction.
     *
     * @return the end position
     */
    public int getEnd() {
        return end;
    }

    /**
     * Sets the end position of this instruction.
     *
     * @param end the end position
     */
    public void setEnd(int end) {
        this.end = end;
    }

    /**
     * Returns the beginning position of this instruction.
     *
     * @return the beginning position
     */
    public int getBegin() {
        return begin;
    }

    /**
     * Returns the return type of this instruction.
     *
     * @return the return type
     */
    public ASTTypeDefinition getReturnType() {
        return returnType;
    }

    /**
     * Sets the return type of this instruction.
     *
     * @param returnType the new return type
     */
    public void setReturnType(final ASTTypeDefinition returnType) {
        this.returnType = returnType;
    }
}
