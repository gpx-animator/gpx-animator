/*
 *  Copyright Contributors to the GPX Animator project.
 *
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
package app.gpx_animator.tools.maps;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class OpenStreetMapImporter {

    private OpenStreetMapImporter() {
    }

    public static void main(final String... args) {
        try {
            final var client = HttpClient.newHttpClient();
            final var request = HttpRequest.newBuilder()
                    .uri(URI.create("https://josm.openstreetmap.de/maps"))
                    .GET()
                    .build();

            final var response = client.send(
                    request, HttpResponse.BodyHandlers.ofString());

            final var fileName = "src/main/resources/maps.xml";
            final var path = Paths.get(fileName);
            final var output = response.body();

            Files.writeString(path, output);
        } catch (final InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

}
