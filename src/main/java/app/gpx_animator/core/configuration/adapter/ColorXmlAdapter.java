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
package app.gpx_animator.core.configuration.adapter;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import java.awt.Color;
import java.util.Locale;

public final class ColorXmlAdapter extends XmlAdapter<String, Color> {

    @Override
    public Color unmarshal(final String string) {
        return new Color(Long.decode(string).intValue(), true);
    }

    @Override
    public String marshal(final Color color) {
        return "#".concat(Integer.toHexString(color.getRGB()).toUpperCase(Locale.getDefault()));
    }

}
