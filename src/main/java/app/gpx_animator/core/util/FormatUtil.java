/*
 *  Copyright Contributors to the GPX Animator project.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package app.gpx_animator.core.util;

import java.text.DecimalFormat;

public final class FormatUtil {

    private FormatUtil() throws InstantiationException {
        throw new InstantiationException("Utility classes can't be instantiated!");
    }

    // copied from https://stackoverflow.com/questions/3263892/format-file-size-as-mb-gb-etc#answer-5599842
    public static String readableFileSize(final long size) {
        if (size <= 0) {
            return "0";
        }
        final var units = new String[] {"B", "KB", "MB", "GB", "TB"};
        final var digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#")
                .format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
}
