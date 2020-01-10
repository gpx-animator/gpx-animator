/*
 *  Copyright 2019 Martin Ždila, Freemap Slovakia
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
package sk.freemap.gpxAnimator;

import com.drew.lang.annotations.NotNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.text.Collator;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Vector;

public final class TrackIcon {

    private static final String[] KEYS = {"airplane", "bicycle", "bus", "car", "jogging", "riding", "sailing", "ship", "train", "tramway", //NON-NLS
            "trekking"}; //NON-NLS

    private static Vector<TrackIcon> trackIcons = null;

    @SuppressFBWarnings(value = "DC_DOUBLECHECK", justification = "Before and after synchronization") //NON-NLS
    public static Vector<TrackIcon> getAllTrackIcons() {
        if (trackIcons == null) {
            synchronized (TrackIcon.class) {
                if (trackIcons == null) {
                    final ResourceBundle resourceBundle = Preferences.getResourceBundle();
                    trackIcons = new Vector<>();
                    for (final String key : KEYS) {
                        trackIcons.add(new TrackIcon(key, resourceBundle.getString("trackicon.icon.".concat(key)))); //NON-NLS
                    }
                    final Collator collator = Collator.getInstance();
                    trackIcons.sort((a, b) -> collator.compare(a.name, b.name));
                    trackIcons.add(0, new TrackIcon("", ""));
                }
            }
        }
        return trackIcons;
    }

    @NotNull
    private final String key;

    @NotNull
    private final String name;

    public TrackIcon(@NotNull final String key) {
        this(key, Preferences.getResourceBundle().getString("trackicon.icon.".concat(key))); //NON-NLS
    }

    public TrackIcon(@NotNull final String key, @NotNull final String name) {
        this.key = key;
        this.name = name;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TrackIcon trackIcon = (TrackIcon) o;
        return Objects.equals(key, trackIcon.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public String getFilename() {
        return String.format("/trackicons/%s.png", key); //NON-NLS
    }

}
