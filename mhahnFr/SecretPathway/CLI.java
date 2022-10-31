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

/**
 * This class contains all functions related to the command line interface.
 *
 * @since 29.08.2022
 * @author mhahnFr
 */
public class CLI {
    /**
     * Processes the given arguments.
     *
     * @param args the arguments
     */
    private void process(String... args) {
        // TODO
        System.out.println("SecretPathway - the arguments:");
        for (var arg : args) {
            System.out.println(arg);
        }
    }

    public static void main(String[] args) {
        new CLI().process(args);
    }
}
