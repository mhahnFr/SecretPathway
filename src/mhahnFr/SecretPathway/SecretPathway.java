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

package mhahnFr.SecretPathway;

import mhahnFr.SecretPathway.core.Constants;
import mhahnFr.SecretPathway.core.Settings;
import mhahnFr.SecretPathway.core.net.ConnectionFactory;
import mhahnFr.SecretPathway.gui.MainWindow;

import javax.swing.*;
import java.awt.EventQueue;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * This class contains all functions related to the command line interface.
 * It is named as the project for the JVM to display the correct name in the GUI.
 *
 * @since 29.08.2022
 * @author mhahnFr
 */
public class SecretPathway {
    /** The hostname or the IP address to connect to.                */
    private String  hostname;
    /** The port to use for the connection.                          */
    private Integer port;
    /** Indicates whether the settings for deploying should be used. */
    private boolean deploy;

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
            
            -d
            --deploy   Activates settings for deploying the app
            """);
    }

    /**
     * Prints a license notice.
     */
    private void printLicense() {
        System.out.println("""
            SecretPathway - A MUD client.
            
            Copyright (C) 2022 - 2023  mhahnFr
            
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
        System.out.println("Version " + Constants.VERSION);
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
     * Displays an instance of the {@link MainWindow} using the parsed {@link SecretPathway#hostname}
     * and {@link SecretPathway#port}.
     */
    private void openWindow() {
        setAppearance();
        EventQueue.invokeLater(() -> new MainWindow(ConnectionFactory.create(hostname, port), deploy).setVisible(true));
    }

    /**
     * Enables appearance related settings.
     */
    private void setAppearance() {
        final var settings = Settings.getInstance();

        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            if (settings.getDarkMode()) {
                System.setProperty("apple.awt.application.appearance", "NSAppearanceNameDarkAqua");
            } else {
                System.setProperty("apple.awt.application.appearance", "system");
            }
        }

        try {
            UIManager.setLookAndFeel(settings.getNativeLookAndFeel() ? UIManager.getSystemLookAndFeelClassName()
                                                                     : UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                 UnsupportedLookAndFeelException e) {
            System.err.println("Could not set platform L&F: Will use default");
            e.printStackTrace();
            System.err.println("--------------------------");
        }
    }

    /**
     * Activates the settings used when this app
     * is deployed.
     */
    private void activateDeploying() {
        deploy = true;
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
                case "-d", "--deploy"                -> activateDeploying();

                default -> System.err.println("Argument \"" + arg + "\" dropped.");
            }
        }
        openWindow();
    }

    public static void main(String[] args) {
        new SecretPathway().process(args);
    }
}
