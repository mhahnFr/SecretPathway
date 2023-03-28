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

package mhahnFr.SecretPathway.core.protocols.spp;

import mhahnFr.SecretPathway.core.net.ConnectionSender;
import mhahnFr.SecretPathway.core.protocols.ProtocolPlugin;
import mhahnFr.utils.ByteHelper;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Vector;

/**
 * This class acts as plugin for the SecretPathwayProtocol (SPP).
 *
 * @author mhahnFr
 * @since 19.12.22
 */
public class SPPPlugin implements ProtocolPlugin {
    /** The buffer for a message in the SPP.     */
    private final List<Byte> buffer = new Vector<>(256);
    /** Indicates whether this plugin is active. */
    private boolean active;

    @Override
    public boolean isBegin(byte b) {
        return b == 0x02;
    }

    @Override
    public boolean process(byte b, ConnectionSender sender) {
        if (b == 0x03) {
            processBuffer();
            buffer.clear();
            return false;
        }
        buffer.add(b);
        return true;
    }

    /**
     * Handles the received SPP message.
     */
    private void processBuffer() {
        System.out.println(new String(ByteHelper.castToByte(buffer.toArray(new Byte[0])), StandardCharsets.UTF_8));
    }

    /**
     * Returns whether the SecretPathwayProtocol (SPP) is
     * active.
     *
     * @return whether the SPP is active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Sets whether this plugin should be active.
     *
     * @param active whether this plugin is active
     */
    public void setIsActive(final boolean active) {
        this.active = active;
    }
}
