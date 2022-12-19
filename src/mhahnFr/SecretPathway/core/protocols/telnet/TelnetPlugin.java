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

package mhahnFr.SecretPathway.core.protocols.telnet;

import mhahnFr.SecretPathway.core.net.ConnectionSender;
import mhahnFr.SecretPathway.core.protocols.ProtocolPlugin;

/**
 * This plugin adds telnet functionality.
 *
 * @author mhahnFr
 * @since 19.12.22
 */
public class TelnetPlugin implements ProtocolPlugin {
    @Override
    public boolean isBegin(byte b) {
        return (b & 0xff) == 0xff;
    }

    @Override
    public boolean process(byte b, ConnectionSender sender) {
        final int bb = b & 0xff;
        System.out.println(bb);
        switch (bb) {
            case 240                     -> { return false; }
            case 250, 251, 252, 253, 254 -> { return true;  }
        }
        return false;
    }
}
