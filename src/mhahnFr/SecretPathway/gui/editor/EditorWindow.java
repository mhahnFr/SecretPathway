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

import javax.swing.*;
import java.awt.*;

public class EditorWindow extends JFrame {
    private final EditorView editorView;

    public EditorWindow(final JFrame parent) {
        super(Constants.NAME + ": Editor");

        editorView = new EditorView();

        getContentPane().add(editorView);

        restoreLocation(parent);
        restoreSize();

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

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

    @Override
    public void dispose() {
        editorView.dispose();

        Settings.getInstance().setEditorWindowLocation(getX(), getY())
                              .setEditorWindowSize(getWidth(), getHeight())
                              .flush();

        super.dispose();
    }
}
