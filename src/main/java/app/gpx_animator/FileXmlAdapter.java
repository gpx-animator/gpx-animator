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
import java.io.File;
import java.nio.file.Path;

@SuppressWarnings("PMD.BeanMembersShouldSerialize") // This class is not serializable
public final class FileXmlAdapter extends XmlAdapter<String, File> {

    private final Path base;

    public FileXmlAdapter(final File base) {
        this.base = base == null ? null : base.toPath();
    }

    @Override
    public File unmarshal(final String string) {
        return base != null
                ? base.resolve(string).toFile()
                : new File(string);
    }

    @Override
    public String marshal(final File file) {
        return base != null
                ? relativize(file.getAbsoluteFile().toPath())
                : file.getAbsolutePath();
    }

    private String relativize(final Path path) {
        if (base.getRoot().equals(path.getRoot())) {
            return base.relativize(path).toString();
        }
        return path.toString();
    }

}
