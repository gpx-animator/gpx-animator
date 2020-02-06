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
package app.gpx_animator;

public class Waypoint extends LatLon {

    private static final long serialVersionUID = -5247395863022008558L;

    private final String name;

    public Waypoint(final double lat, final double lon, final long time, final String name) {
        super(lat, lon, time);
        this.name = name;
    }

    public final String getName() {
        return name;
    }

}
