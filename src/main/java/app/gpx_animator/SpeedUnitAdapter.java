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

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.Arrays;

public final class SpeedUnitAdapter extends XmlAdapter<String, SpeedUnit> {

    @Override
    public SpeedUnit unmarshal(final String string) {
        return Arrays.stream(SpeedUnit.values())
                .filter(speedUnit -> string.equals(speedUnit.getUnit()))
                .findAny().orElse(SpeedUnit.KMH);
    }

    @Override
    public String marshal(final SpeedUnit speedUnit) {
        return speedUnit.getUnit();
    }

}
