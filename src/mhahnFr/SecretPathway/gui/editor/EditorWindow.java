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

package mhahnFr.SecretPathway.gui.editor;

import mhahnFr.SecretPathway.core.Constants;
import mhahnFr.SecretPathway.core.Settings;
import mhahnFr.SecretPathway.core.lpc.LPCFileManager;

import javax.swing.JFrame;
import java.awt.Dimension;

/**
 * This class represents a window for the {@link EditorView}.
 *
 * @author mhahnFr
 * @since 03.02.23
 */
public class EditorWindow extends JFrame {
    /** The underlying {@link EditorView}. */
    private final EditorView editorView;

    /**
     * Constructs this editor window. Size and location are
     * restored if possible, otherwise the location is set to
     * be relative to the given parent.
     *
     * @param parent  the parent to be used if the location cannot be restored
     * @param manager the LPC file manager
     */
    public EditorWindow(final JFrame         parent,
                        final LPCFileManager manager) {
        this(parent, manager, null);
    }

    /**
     * Constructs this editor window. Size and location are
     * restored if possible, otherwise the location is set to
     * be relative to the given parent.
     *
     * @param parent  the parent to be used if the location cannot be restored
     * @param manager the LPC file manager
     * @param name    the name of the file to be opened
     */
    public EditorWindow(final JFrame         parent,
                        final LPCFileManager manager,
                        final String         name) {
        super(Constants.NAME + ": Editor" + (name == null ? "" : " - '" + name + "'"));

        editorView = new EditorView(manager, name);

        editorView.onDispose(__ -> internalDispose());

        getContentPane().add(editorView);

        restoreLocation(parent);
        restoreSize();

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    /**
     * Restores the location. If that is not possible,
     * the location is set to be relative to the given
     * parent window.
     *
     * @param parent the parent to be used if the location cannot be restored
     */
    private void restoreLocation(final JFrame parent) {
        final var settings = Settings.getInstance();

        final int x = settings.getEditorWindowX(),
                  y = settings.getEditorWindowY();

        if (x < 0 || y < 0) {
            setLocationRelativeTo(parent);
        } else {
            setLocation(x, y);
        }
    }

    /**
     * Restores the size.
     */
    private void restoreSize() {
        final var settings = Settings.getInstance();

        final int width  = settings.getEditorWindowWidth(),
                  height = settings.getEditorWindowHeight();

        if (width < 0 || height < 0) {
            pack();
        } else {
            setSize(new Dimension(width, height));
        }
    }

    /**
     * Disposes this window without calling {@link EditorView#dispose()}.
     */
    private void internalDispose() {
        Settings.getInstance().setEditorWindowLocation(getX(), getY())
                .setEditorWindowSize(getWidth(), getHeight())
                .flush();

        super.dispose();
    }

    @Override
    public void dispose() {
        editorView.dispose();

        internalDispose();
    }
}
