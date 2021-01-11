/*
 *  Copyright 2013 Martin Ždila, Freemap Slovakia
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package app.gpx_animator.core.util;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.text.Collator;

public final class Utils {

    private static final Collator COLLATOR = Collator.getInstance();

    private Utils() throws InstantiationException {
        throw new InstantiationException("Utility classes can't be instantiated!");
    }

    // copied from https://stackoverflow.com/questions/3514158/how-do-you-clone-a-bufferedimage
    public static BufferedImage deepCopy(final BufferedImage bi) {
        final var b = new BufferedImage(bi.getWidth(), bi.getHeight(), bi.getType());
        final var g = (Graphics2D) b.getGraphics();
        g.drawImage(bi, 0, 0, null);
        g.dispose();
        return b;
    }

    public static BufferedImage deepCopy(final BufferedImage bi, final Integer x, final Integer y, final Integer width, final Integer height) {
        final var cm = bi.getColorModel();
        final var isAlphaPremultiplied = cm.isAlphaPremultiplied();
        final var raster = (WritableRaster) bi.getData(new Rectangle(x, y, width, height));
        final var movedRaster = raster.createWritableTranslatedChild(0, 0);
        return new BufferedImage(cm, movedRaster, isAlphaPremultiplied, null);
    }


    @SuppressWarnings({"PMD.CompareObjectsWithEquals", "StringEquality"})
    @SuppressFBWarnings("ES_COMPARING_PARAMETER_STRING_WITH_EQ") //NON-NLS
    public static boolean isEqual(final String source, final String target) {
        if (source == target) {
            return true;
        }

        if (source == null || target == null) {
            return false;
        }

        return COLLATOR.compare(source, target) == 0;
    }

}
