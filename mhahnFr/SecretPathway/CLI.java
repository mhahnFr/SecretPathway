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

import java.util.Arrays;

/**
 * This class contains all functions related to the command line interface.
 *
 * @since 29.08.2022
 * @author mhahnFr
 */
public class CLI {
    /** Indicates whether windows have been opened using the arguments. */
    private boolean hadWindows;

    /**
     * Prints a help text.
     */
    private void printHelp() {
        System.out.println("""
            SecretPathway usage:
            
            -h
            --help            Shows this help
            
            -l
            --license         Prints a license notice
            
            -w        [count]
            --windows [count] Opens as many windows as given
            """);
    }

    private void openWindows(int count) {
        hadWindows = true;
        // TODO
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
     * Processes the given arguments.
     *
     * @param args the arguments
     */
    private void process(String... args) {
        var it = Arrays.stream(args).iterator();
        while (it.hasNext()) {
            var arg = it.next();
            switch (arg) {
                case "-h", "--help"    -> printHelp();
                case "-l", "--license" -> printLicense();
                case "-w", "--windows" -> {
                    try {
                        openWindows(Integer.decode(it.next()));
                    } catch (Exception e) {
                        System.err.println("--windows: Could not process the given argument!");
                    }
                }

                default -> System.err.println("Argument \"" + arg + "\" dropped.");
            }
        }
        if (!hadWindows) openWindows(1);
    }

    public static void main(String[] args) {
        new CLI().process(args);
    }
}
