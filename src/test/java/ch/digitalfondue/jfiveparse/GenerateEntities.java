/**
 * Copyright © 2015 digitalfondue (info@digitalfondue.ch)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.digitalfondue.jfiveparse;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * Generate the file "entities-with-1-2-codepoint", used by Entities
 */
public class GenerateEntities {

    private static class EntityValues {
        int[] codepoints;
    }

    public static void main(String[] args) throws IOException {
        Type type = (new TypeToken<Map<String, EntityValues>>() {
        }).getType();
        String json = new String(Files.readAllBytes(Paths.get("src/test/resources/entities.json")), StandardCharsets.UTF_8);
        Map<String, EntityValues> m = new GsonBuilder().create().fromJson(json, type);

        Prefix p = new Prefix(null);

        ByteArrayOutputStream baosOneCodePoint = new ByteArrayOutputStream();
        GZIPOutputStream osOneCodePoint = new GZIPOutputStream(baosOneCodePoint);
        DataOutputStream daos = new DataOutputStream(osOneCodePoint);

        int oneCodePointLength = 0;
        int twoCodePointLength = 0;

        for (String key : m.keySet()) {
            p.addWord(key, m.get(key).codepoints);

            if (m.get(key).codepoints.length == 1) {
                oneCodePointLength++;
                daos.writeUTF(key);
                daos.writeInt(m.get(key).codepoints[0]);
            }

            if (m.get(key).codepoints.length > 2) {
                throw new IllegalStateException("more than 2 codepoints");
            }
        }

        for (String key : m.keySet()) {
            if (m.get(key).codepoints.length == 2) {
                twoCodePointLength++;
                daos.writeUTF(key);
                daos.writeInt(m.get(key).codepoints[0]);
                daos.writeInt(m.get(key).codepoints[1]);
            }
        }

        daos.flush();
        daos.close();

        System.err.println(oneCodePointLength);
        System.err.println(twoCodePointLength);

        Files.write(Paths.get("entities-with-1-2-codepoint"), baosOneCodePoint.toByteArray());
    }
}
