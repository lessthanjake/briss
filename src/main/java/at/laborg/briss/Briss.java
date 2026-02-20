/**
 * Copyright 2010 Gerhard Aigner
 *
 * This file is part of BRISS.
 *
 * BRISS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * BRISS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * BRISS. If not, see http://www.gnu.org/licenses/.
 */
package at.laborg.briss;

public final class Briss {

    private Briss() {
    };

    public static void main(final String[] args) {

        // this needs to be set in order to cope with jp2000 images
        System.setProperty("org.jpedal.jai", "true");

        if (hasFlag(args, "-f")) {
            // batch mode: recursively process folders
            BrissBatch.batchCrop(args);
        } else if (args.length > 1) {
            // single-file command-line mode
            BrissCMD.autoCrop(args);
        } else {
            new BrissGUI(args);
        }
    }

    private static boolean hasFlag(final String[] args, final String flag) {
        for (String arg : args) {
            if (arg.trim().equalsIgnoreCase(flag)) {
                return true;
            }
        }
        return false;
    }
}
