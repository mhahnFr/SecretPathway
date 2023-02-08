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

/**
 * This enumeration contains the possible types of AST nodes.
 *
 * @author mhahnFr
 * @since 31.01.23
 */
public enum ASTType {
    /** Represents a combination of AST nodes. */
    COMBINATION,
    /** Represents a function definition.      */
    FUNCTION_DEFINITION,
    /** Represents an include statement.       */
    INCLUDE,
    /** Represents an inherit statement.       */
    INHERITANCE,
    /** Represents a missing AST node.         */
    MISSING,
    /** Represents a parameter declaration.    */
    PARAMETER,
    /** Represents a variable definition.      */
    VARIABLE_DEFINITION,
    /** Represents a wrong AST node.           */
    WRONG,
    /** Represents an ellipsis.                */
    ELLIPSIS,
    /** Represents a type.                     */
    TYPE,
    /** Represents a name.                     */
    NAME,
    /** Represents a modifier.                 */
    MODIFIER,
    /** Represents a block of code.            */
    BLOCK,
    /** Represents a break.                    */
    BREAK,
    /** Represents a continue.                 */
    CONTINUE,
    /** Represents a return statement.         */
    RETURN,
    /** Represents a unary operator.           */
    UNARY_OPERATOR,
    /** Represents a binary operation.         */
    OPERATION,
    /** Represents a function call.            */
    FUNCTION_CALL,
    /** Represents a new expression.           */
    NEW,
    /** Represents a cast expression.          */
    CAST
}
