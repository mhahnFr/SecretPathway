/*
 * SecretPathway - A MUD client.
 *
 * Copyright (C) 2022  mhahnFr
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

package mhahnFr.SecretPathway.core.protocols;

import mhahnFr.SecretPathway.core.net.ConnectionSender;

import java.util.Arrays;
import java.util.Vector;

/**
 * Instances of this class act as a protocol abstraction.
 * This class consists of a state machine to determine the
 * {@link ProtocolPlugin} to be used.
 * <br>
 * Although plugins can be added at any time, they cannot be
 * removed.
 *
 * @author mhahnFr
 * @since 17.12.22
 */
public class Protocol {
    private final Vector<ProtocolPlugin> plugins;
    private final ConnectionSender sender;
    private ProtocolPlugin lastPlugin;

    public Protocol(final ConnectionSender sender, ProtocolPlugin... plugins) {
        this.plugins = new Vector<>(Arrays.asList(plugins));
        this.sender  = sender;
    }

    public void add(final ProtocolPlugin plugin) {
        plugins.add(plugin);
    }

    public boolean process(byte b) {
        if (lastPlugin != null) {
            if (!lastPlugin.process(b, sender)) {
                lastPlugin = null;
                return false;
            } else {
                return true;
            }
        } else {
            for (final var plugin : plugins) {
                if (plugin.isBegin(b)) {
                    lastPlugin = plugin;
                    return true;
                }
            }
        }
        return false;
    }
}
