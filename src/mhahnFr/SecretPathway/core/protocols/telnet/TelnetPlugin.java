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

package mhahnFr.SecretPathway.core.protocols.telnet;

import mhahnFr.SecretPathway.core.Settings;
import mhahnFr.SecretPathway.core.net.ConnectionSender;
import mhahnFr.SecretPathway.core.protocols.ProtocolPlugin;

import java.util.Vector;

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

        /**
         * Returns the opposite of the given telnet option. If the number
         * given has no opposite or is no telnet code, it is simply returned.
         *
         * @param option the option to be negated
         * @return the negated option
         */
        static short opposite(final short option) {
            var result = option;

            switch (option) {
                case SE -> result = SB;
                case SB -> result = SE;

                case WILL -> result = DONT;
                case WONT -> result = DO;

                case DO   -> result = WONT;
                case DONT -> result = WILL;
            }

            return result;
        }
    }

    /** An enumeration containing MUD specific additions. */
    abstract static class MudExtensions {
    }

    /** Indicates whether the currently received telnet sequence ends with IAC SE. */
    private Boolean hasEnd = null;
    /** The last telnet function received. Defaults to IAC.                        */
    private short last = TelnetFunction.IAC;
    /** A buffer storing longer received telnet sequences.                         */
    private final Vector<Short> buffer = new Vector<>();

    @Override
    public boolean isBegin(byte b) {
        return (b & 0xff) == TelnetFunction.IAC;
    }

    @Override
    public boolean process(byte b, ConnectionSender sender) {
        final short bb = (short) (b & 0xff);
        System.out.println(bb);

        var result = false;

        if (hasEnd != null) {
            if (hasEnd) {
                if (bb == TelnetFunction.IAC) {
                    if (last == TelnetFunction.IAC) {
                        buffer.add(bb);
                    } else {
                        last = TelnetFunction.IAC;
                    }
                    result = true;
                } else if (bb == TelnetFunction.SE && last == TelnetFunction.IAC) {
                    parseBuffer(buffer, sender);
                } else {
                    buffer.add(bb);
                    result = true;
                }
            } else {
                handleSingleOption(last, bb, sender);
            }
        } else {
            switch (bb) {
                case TelnetFunction.WILL, TelnetFunction.WONT, TelnetFunction.DO, TelnetFunction.DONT -> {
                    hasEnd = false;
                    result = true;
                }

                case TelnetFunction.SB -> {
                    hasEnd = true;
                    result = true;
                }
            }
            last = bb;
        }

        if (!result) {
            hasEnd = null;
            buffer.clear();
            last = TelnetFunction.IAC;
        }

        return result;
    }

    /**
     * Parses the given telnet buffer. It should consist of the contents
     * of an SB sub negotiation, but without the telnet control characters.
     *
     * @param buffer the buffer to be parsed
     * @param sender the sender used for sending back the response
     */
    private void parseBuffer(final Vector<Short> buffer, ConnectionSender sender) {
        switch (buffer.firstElement()) {
            case Code.TELNET_START_TLS -> {
                if (buffer.get(1) == 1) {
                    sender.startTLS();
                }
            }

            default -> throw new IllegalStateException("Sub negotiation received that was not permitted!");
        }
    }

    /**
     * This method handles a single telnet function.
     *
     * @param previous the previously received telnet option such as DO
     * @param option the telnet function to handle
     * @param sender the sender used for sending back the response
     */
    private void handleSingleOption(final short previous, final short option, ConnectionSender sender) {
        switch (option) {
            case Code.TELNET_START_TLS:
                if (Settings.getInstance().getStartTLS()) {
                    sendSingle(TelnetFunction.WILL, option, sender);
                    sendSB(sender, option, (short) 1);
                    break;
                }

            default: sendSingle(TelnetFunction.opposite(previous), option, sender); break;
        }
    }

    /**
     * Sends back a single telnet function response. The message sent looks
     * like: {@code IAC <previous> <option>}.
     *
     * @param previous the mode to send to the remote host, such as {@code WILL}
     * @param option the code of the telnet function
     * @param sender the sender used for sending the response
     */
    private void sendSingle(final short previous, final short option, ConnectionSender sender) {
        final var bytes = new byte[3];

        bytes[0] = (byte) TelnetFunction.IAC;
        bytes[1] = (byte) previous;
        bytes[2] = (byte) option;

        System.out.println("IAC " + previous + " " + option);

        sender.send(bytes);
    }

    /**
     * Sends a telnet sub negotiation using the given sender.
     *
     * @param sender the sender used for sending the negotiation
     * @param codes the codes to be sent
     */
    private void sendSB(ConnectionSender sender, short... codes) {
        final var bytes = new byte[codes.length + 4];

        bytes[0] = (byte) TelnetFunction.IAC;
        bytes[1] = (byte) TelnetFunction.SB;

        bytes[bytes.length - 2] = (byte) TelnetFunction.IAC;
        bytes[bytes.length - 1] = (byte) TelnetFunction.SE;

        for (int i = 0; i < codes.length; ++i) {
            bytes[i + 2] = (byte) codes[i];
        }

        sender.send(bytes);
    }
}
