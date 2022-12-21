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
        final static short binary_transmission            = 0;
        final static short echo                           = 1;
        final static short reconnection                   = 2;
        final static short suppress_go_ahead              = 3;
        final static short approx_message_size_negotation = 4;
        final static short status                         = 5;
        final static short timing_mark                    = 6;
        final static short rc_trans_echo                  = 7;
        final static short o_line_width                   = 8;
        final static short o_page_size                    = 9;
        final static short o_cr_disposition               = 10;
        final static short o_htab_stops                   = 11;
        final static short o_htab_disposition             = 12;
        final static short o_formfeed_disposition         = 13;
        final static short o_vtab_stops                   = 14;
        final static short o_vtab_disposition             = 15;
        final static short o_lf_disposition               = 16;
        final static short extended_ascii                 = 17;
        final static short logout                         = 18;
        final static short byte_macro                     = 19;
        final static short data_entry_terminal            = 20;
        final static short supdup                         = 21;
        final static short supdup_output                  = 22;
        final static short send_location                  = 23;
        final static short terminal_type                  = 24;
        final static short eor                            = 25;
        final static short tacacs_user_identification     = 26;
        final static short output_marking                 = 27;
        final static short terminal_location_number       = 28;
        final static short telnet_3270_regime             = 29;
        final static short x_3_pad                        = 30;
        final static short naws                           = 31;
        final static short terminal_speed                 = 32;
        final static short remote_flow_control            = 33;
        final static short linemode                       = 34;
        final static short x_display_location             = 35;
        final static short environment_option             = 36;
        final static short authentication_option          = 37;
        final static short encryption_option              = 38;
        final static short new_environment_option         = 39;
        final static short tn3270e                        = 40;
        final static short xauth                          = 41;
        final static short charset                        = 42;
        final static short telnet_rsp                     = 43;
        final static short com_port_control_option        = 44;
        final static short telnet_suppress_local_echo     = 45;
        final static short telnet_start_tls               = 46;
        final static short kermit                         = 47;
        final static short send_url                       = 48;
        final static short forward_x                      = 49;

        final static short telopt_pragma_logon            = 138;
        final static short telopt_sspi_logon              = 139;
        final static short telopt_pragma_heartbeat        = 140;
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
