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

import edu.umd.cs.findbugs.annotations.NonNull;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public final class DateUtil {

    private static final DateTimeFormatter[] ZONED_DATE_TIME_FORMATTERS = {
            DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss xxx"),
            DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss xx"),
            DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss x"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss xxx"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss xx"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss x")
    };

    private DateUtil() throws InstantiationException {
        throw new InstantiationException("Utility classes can't be instantiated!");
    }

    public static @NonNull ZonedDateTime parseZonedDateTime(@NonNull final String text) {
        DateTimeParseException lastException = null;
        for (final DateTimeFormatter dateTimeFormatter : ZONED_DATE_TIME_FORMATTERS) {
            try {
                return ZonedDateTime.parse(text, dateTimeFormatter);
            } catch (final DateTimeParseException e) {
                lastException = e;
            }
        }
        throw lastException != null ? lastException
                : new DateTimeParseException("Can't parse ZonedDateTime object!", text, -1, null);
    }
}
