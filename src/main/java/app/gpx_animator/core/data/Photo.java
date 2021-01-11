/*
 *  Copyright 2019 Marcus Fihlon, Switzerland
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
package app.gpx_animator.core.data;

import com.drew.lang.annotations.NotNull;
import org.jetbrains.annotations.NonNls;

import java.io.File;
import java.util.Objects;

@SuppressWarnings("PMD.BeanMembersShouldSerialize") // This class is not serializable
public final class Photo {
    private final Long epochSeconds;
    private final File file;

    public Photo(@NotNull final Long epochSeconds, @NotNull final File file) {
        this.epochSeconds = epochSeconds;
        this.file = file;
    }

    public Long getEpochSeconds() {
        return epochSeconds;
    }

    public File getFile() {
        return file;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        var photo = (Photo) o;
        return epochSeconds.equals(photo.epochSeconds)
                && file.equals(photo.file);
    }

    @Override
    public int hashCode() {
        return Objects.hash(epochSeconds, file);
    }

    @NonNls
    @Override
    public String toString() {
        return "Photo{"
                + "epochSeconds=" + epochSeconds
                + ", file=" + file
                + '}';
    }
}
