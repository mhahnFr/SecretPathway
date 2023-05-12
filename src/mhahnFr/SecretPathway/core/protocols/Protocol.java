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

package mhahnFr.SecretPathway.core.protocols;

import mhahnFr.SecretPathway.core.net.ConnectionSender;
import mhahnFr.SecretPathway.core.protocols.spp.SPPPlugin;

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
    /** The underlying vector of plugins.                                            */
    private final Vector<ProtocolPlugin> plugins;
    /** The {@link ConnectionSender} used by the plugins to send optional responses. */
    private final ConnectionSender sender;
    /** The {@link ProtocolPlugin} currently responsible for handling incoming data. */
    private ProtocolPlugin lastPlugin;

    /**
     * Constructs this instance using the given sender and the given plugins.
     * More plugins can be added later on.
     *
     * @param sender the sender used by the plugins to send optional responses
     * @param plugins the plugins initially available
     * @see #add(ProtocolPlugin)
     */
    public Protocol(final ConnectionSender sender, ProtocolPlugin... plugins) {
        this.plugins = new Vector<>(Arrays.asList(plugins));
        this.sender  = sender;
    }

    /**
     * Adds the given plugin to the list of used {@link ProtocolPlugin}s.
     * It will be used after the potential currently active plugin has
     * finished.
     *
     * @param plugin the new plugin to be used
     */
    public void add(final ProtocolPlugin plugin) {
        plugins.add(plugin);
    }

    /**
     * Processes the given byte of input. If the internal state machine
     * is in the state of a plugin being responsible for processing
     * incoming data, the byte is passed to that plugin. Otherwise,
     * all plugins are asked whether they handle the given byte.
     * <br>
     * This function is meant to be used in a state machine, so it returns
     * whether it should keep sending incoming input to this instance.
     *
     * @param b the incoming byte
     * @return whether the next input should be sent to this instance
     */
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

    /**
     * Returns whether the SecretPathwayProtocol (SPP) is
     * currently active.
     *
     * @return whether the SPP is active
     */
    public boolean isSPPActive() {
        for (final var plugin : plugins) {
            if (plugin instanceof final SPPPlugin sppPlugin) {
                return sppPlugin.isActive();
            }
        }
        return false;
    }

    /**
     * Activates the {@link SPPPlugin}, if available.
     */
    public void activateSPP() {
        for (final var plugin : plugins) {
            if (plugin instanceof final SPPPlugin sppPlugin) {
                sppPlugin.setIsActive(true);
            }
        }
    }

    public void onConnectionError() {
        for (final var plugin : plugins) {
            plugin.onConnectionError();
        }
    }
}
