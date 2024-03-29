/*
 * SecretPathway - A MUD client.
 *
 * Copyright (C) 2022 - 2023  mhahnFr
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

package mhahnFr.SecretPathway.core.net;

import java.nio.charset.Charset;

/**
 * This interface defines the functionality a connection sender
 * should be capable of.
 *
 * @author mhahnFr
 * @since 17.12.22
 */
public interface ConnectionSender {
    /**
     * Sends the given bytes.
     *
     * @param bytes the bytes to be sent
     */
    void send(byte[] bytes);

    /**
     * Enables the TLS mode for the underlying connection.
     */
    void startTLS();

    /**
     * Enables or disables the escaping of the IAC telnet command.
     *
     * @param escape whether to escape the IAC
     */
    void escapeIAC(final boolean escape);

    /**
     * Sets the charset to be used for string encodings.
     *
     * @param charset the charset to be used
     */
    void setCharset(final Charset charset);

    /**
     * Requests to activate the SecretPathwayProtocol (SPP).
     */
    void enableSPP();

    /**
     * Sets whether the password mode of the SPP connection
     * should be activated.
     *
     * @param enabled whether the password mode should be enabled
     */
    void setPasswordMode(final boolean enabled);

    /**
     * Opens an editor for the given path.
     *
     * @param path the file to be opened
     */
    void openEditor(final String path, final String content);

    /**
     * Sets the prompt text displayed beneath the user input field.
     *
     * @param text the text to be displayed
     */
    void setPromptText(final String text);
}
