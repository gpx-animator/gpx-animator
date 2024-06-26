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
package app.gpx_animator.ui;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public enum UIMode {

    CLI,
    EXPERT;

    private static UIMode mode = EXPERT;

    @SuppressWarnings("java:S3066")
    @SuppressFBWarnings(value = "EI_EXPOSE_STATIC_REP2", justification = "the UIMode is public anyway")
    public static void setMode(@NonNull final UIMode mode) {
        UIMode.mode = mode;
    }

    @SuppressFBWarnings({
            "SING_SINGLETON_GETTER_NOT_SYNCHRONIZED", // false positive
            "MS_EXPOSE_REP" // the UIMode is public anyway
    })
    public static UIMode getMode() {
        return mode;
    }

}
