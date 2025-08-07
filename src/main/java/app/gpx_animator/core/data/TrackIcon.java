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
package app.gpx_animator.core.data;

import app.gpx_animator.core.preferences.Preferences;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.jetbrains.annotations.NonNls;

import java.text.Collator;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Vector;

public final class TrackIcon {

    private static final ResourceBundle RESOURCE_BUNDLE = Preferences.getResourceBundle();

    @NonNls
    private static final String[] KEYS = {"airplane", "bicycle", "bus", "car", "jogging", "motorcycle",
           "riding", "sailing", "ship", "train", "tramway", "trekking"};

    @NonNls
    private static final String RESOURCE_BUNDLE_TRACKICON_PREFIX = "trackicon.icon.";

    private static volatile List<TrackIcon> trackIcons = null;

    public static Vector<TrackIcon> getAllTrackIcons() {
        if (trackIcons == null) {
            synchronized (TrackIcon.class) {
                if (trackIcons == null) {
                    final var tmpTrackIcons = new ArrayList<TrackIcon>();
                    for (final var key : KEYS) {
                        tmpTrackIcons.add(new TrackIcon(key, RESOURCE_BUNDLE.getString(RESOURCE_BUNDLE_TRACKICON_PREFIX.concat(key))));
                    }
                    final var collator = Collator.getInstance();
                    tmpTrackIcons.sort((a, b) -> collator.compare(a.name, b.name));
                    tmpTrackIcons.add(0, new TrackIcon("", ""));
                    trackIcons = List.copyOf(tmpTrackIcons);
                }
            }
        }
        return new Vector<>(trackIcons);
    }

    @NonNull
    private final String key;

    @NonNull
    private final String name;

    public TrackIcon(@NonNull final String key) {
        this(key, RESOURCE_BUNDLE.getString(RESOURCE_BUNDLE_TRACKICON_PREFIX.concat(key)));
    }

    public TrackIcon(@NonNull final String key, @NonNull final String name) {
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
        var trackIcon = (TrackIcon) o;
        return Objects.equals(key, trackIcon.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }

    public @NonNull String getKey() {
        return key;
    }

    public @NonNull String getName() {
        return name;
    }

    public String getFilename() {
        return String.format("/trackicons/%s.png", key); //NON-NLS
    }

}
