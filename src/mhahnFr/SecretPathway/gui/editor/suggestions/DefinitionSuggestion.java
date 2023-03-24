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

import mhahnFr.SecretPathway.core.lpc.interpreter.Definition;
import mhahnFr.SecretPathway.core.lpc.parser.ast.ASTTypeDefinition;

/**
 * This class represents a {@link Definition} suggestion.
 *
 * @author mhahnFr
 * @since 13.03.23
 */
public class DefinitionSuggestion implements Suggestion {
    /** The (return) type of the suggested definition.    */
    private final ASTTypeDefinition type;
    /** The suggested definition.                         */
    private final Definition definition;
    /** Indicates whether the suggestion is a super send. */
    private boolean isSuper;

    /**
     * Constructs this suggestion using the provided information.
     *
     * @param type       the (return) type of the suggested definition
     * @param definition the suggested definition
     */
    public DefinitionSuggestion(final ASTTypeDefinition type,
                                final Definition        definition) {
        this.type       = type;
        this.definition = definition;
    }

    /**
     * Sets whether this suggestion comes from a super context.
     *
     * @param isSuper whether this suggestion comes from a super context
     */
    public void setIsSuperDefinition(final boolean isSuper) {
        this.isSuper = isSuper;
    }

    /**
     * Returns the suggested {@link Definition}.
     *
     * @return the suggested {@link Definition}
     */
    public Definition getDefinition() {
        return definition;
    }

    @Override
    public String getSuggestion() {
        return (isSuper ? "::" : "") + definition.getName();
    }

    @Override
    public String getDescription() {
        return (isSuper ? "::" : "") + definition.toString();
    }

    @Override
    public String getRightSite() {
        return type == null ? null : type.toString();
    }
}
