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

package mhahnFr.SecretPathway;

import mhahnFr.SecretPathway.core.net.Connection;
import mhahnFr.SecretPathway.gui.MainWindow;

import java.awt.EventQueue;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * This class contains all functions related to the command line interface.
 *
 * @since 29.08.2022
 * @author mhahnFr
 */
public class CLI {
    /** The hostname or the IP address to connect to. */
    private String  hostname;
    /** The port to use for the connection.           */
    private Integer port;

    /**
     * Prints a help text.
     */
    private void printHelp() {
        System.out.println("""
            SecretPathway usage:
            
            -h
            --help     Shows this help
            
            -l
            --license  Prints a license notice
            
            -a
            --address
            --hostname The hostname or IP address to connect to
            
            -p
            --port     The port to use to connect
            """);
    }

    /**
     * Prints a license notice.
     */
    private void printLicense() {
        System.out.println("""
            SecretPathway - A MUD client.
            
            Copyright (C) 2022  mhahnFr
            
            This file is part of the SecretPathway. This program is free software:
            you can redistribute it and/or modify it under the terms of the
            GNU General Public License as published by the Free Software Foundation,
            either version 3 of the License, or (at your option) any later version.
            
            This program is distributed in the hope that it will be useful,
            but WITHOUT ANY WARRANTY; without even the implied warranty of
            MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
            GNU General Public License for more details.
            
            You should have received a copy of the GNU General Public License along with
            this program, see the file LICENSE.  If not, see <https://www.gnu.org/licenses/>.
            """);
    }

    /**
     * Prints the version information.
     */
    private void printVersion() {
        System.out.println("Version 0.1");
    }

    /**
     * Prints an error message using the given arguments.
     *
     * @param parameter the parameter raising the error
     * @param message   a descriptive message
     */
    private void printError(String parameter, String message) {
        System.err.println(parameter + ": " + message);
    }

    /**
     * Tries to parse the hostname or IP address. Prints a descriptive message on error.
     *
     * @param it the iterator used to access the parameter of the CLI argument
     */
    private void setHostname(Iterator<String> it) {
        if (!it.hasNext()) {
            printError("--hostname", "Missing argument!");
            return;
        }
        hostname = it.next();
    }

    /**
     * Tries to parse the port number. Prints a descriptive message on error.
     *
     * @param it the iterator used to access the parameter of the CLI argument
     */
    private void setPort(Iterator<String> it) {
        try {
            port = Integer.decode(it.next());
        } catch (NumberFormatException | NoSuchElementException e) {
            printError("--port", "Could not set port!");
        }
    }

    /**
     * Displays an instance of the {@link MainWindow} using the parsed {@link CLI#hostname}
     * and {@link CLI#port}.
     */
    private void openWindow() {
        EventQueue.invokeLater(() -> new MainWindow(Connection.create(hostname, port)).setVisible(true));
    }

    /**
     * Processes the given arguments.
     *
     * @param args the arguments
     */
    private void process(String... args) {
        var it = Arrays.stream(args).iterator();
        while (it.hasNext()) {
            var arg = it.next();
            switch (arg) {
                case "-h", "--help"                  -> printHelp();
                case "-l", "--license"               -> printLicense();
                case "-v", "--version"               -> printVersion();
                case "-a", "--address", "--hostname" -> setHostname(it);
                case "-p", "--port"                  -> setPort(it);

                default -> System.err.println("Argument \"" + arg + "\" dropped.");
            }
        }
        openWindow();
    }

    public static void main(String[] args) {
        new CLI().process(args);
    }
}
