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
    /** An enumeration with telnet codes defined by the IANA. */
    abstract static class Code {
        final static short BINARY_TRANSMISSION             = 0;
        final static short ECHO                            = 1;
        final static short RECONNECTION                    = 2;
        final static short SUPPRESS_GO_AHEAD               = 3;
        final static short APPROX_MESSAGE_SIZE_NEGOTIATION = 4;
        final static short STATUS                          = 5;
        final static short TIMING_MARK                     = 6;
        final static short RC_TRANS_ECHO                   = 7;
        final static short O_LINE_WIDTH                    = 8;
        final static short O_PAGE_SIZE                     = 9;
        final static short O_CR_DISPOSITION                = 10;
        final static short O_HTAB_STOPS                    = 11;
        final static short O_HTAB_DISPOSITION              = 12;
        final static short O_FORMFEED_DISPOSITION          = 13;
        final static short O_BTAB_STOPS                    = 14;
        final static short O_VTAB_DISPOSITION              = 15;
        final static short O_LF_DISPOSITION                = 16;
        final static short EXTENDED_ASCII                  = 17;
        final static short LOGOUT                          = 18;
        final static short BYTE_MACRO                      = 19;
        final static short DATA_ENTRY_TERMINAL             = 20;
        final static short SUPDUP                          = 21;
        final static short SUPDUP_OUTPUT                   = 22;
        final static short SEND_LOCATION                   = 23;
        final static short TERMINAL_TYPE                   = 24;
        final static short EOR                             = 25;
        final static short TACACS_USER_IDENTIFICATION      = 26;
        final static short OUTPUT_MARKING                  = 27;
        final static short TERMINAL_LOCATION_NUMBER        = 28;
        final static short TELNET_3270_REGIME              = 29;
        final static short X_3_PAD                         = 30;
        final static short NAWS                            = 31;
        final static short TERMINAL_SPEED                  = 32;
        final static short REMOTE_FLOW_CONTROL             = 33;
        final static short LINEMODE                        = 34;
        final static short X_DOSPLAY_LOCATION              = 35;
        final static short ENVIRONMENT_OPTION              = 36;
        final static short AUTHENTICATION_OPTION           = 37;
        final static short ENCRYPTION_OPTION               = 38;
        final static short NEW_ENVIRONMENT_OPTION          = 39;
        final static short TN3270E                         = 40;
        final static short XAUTH                           = 41;
        final static short CHARSET                         = 42;
        final static short TELNET_RSP                      = 43;
        final static short COM_PORT_CONTROL_OPTION         = 44;
        final static short TELNET_SUPPRESS_LOCAL_ECHO      = 45;
        final static short TELNET_START_TLS                = 46;
        final static short KERMIT                          = 47;
        final static short SEND_URL                        = 48;
        final static short FORWARD_X                       = 49;

        final static short TELOPT_PRAGMA_LOGON             = 138;
        final static short TELOPT_SSPI_LOGON               = 139;
        final static short TELOPT_PRAGMA_HEARTBEAT         = 140;
    }

    /** An enumeration of the basic telnet functions. */
    abstract static class TelnetFunction {
        final static short SE   = 240;
        final static short SB   = 250;
        final static short WILL = 251;
        final static short WONT = 252;
        final static short DO   = 253;
        final static short DONT = 254;
        final static short IAC  = 255;
    }

    /** An enumeration containing MUD specific additions. */
    abstract static class MudExtensions {
    }

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
