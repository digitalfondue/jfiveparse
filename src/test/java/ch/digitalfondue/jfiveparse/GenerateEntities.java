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

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Generates the resource {@code entities-with-1-2-codepoint}, the trie of the named character
 * references used by {@link EntitiesPrefix}. At runtime the names are expanded into a
 * left-child/right-sibling trie (see {@link EntitiesPrefix} for its representation).
 *
 * entities.json is from https://html.spec.whatwg.org/entities.json
 *
 * The format is described in {@link EntitiesPrefix}:
 *
 * <pre>
 * count u16 | nodeCount u16 | valuesLength u16 |
 * control stream, count x 2 bytes (entries sorted by name):
 *   ctrl0 u8 = prefixLen (bits 0-4) | hasSemicolon (bit 5) | (unitCount - 1) &lt;&lt; 6
 *   ctrl1 u8 = suffixLen
 * suffix stream: the concatenated suffixes (ascii, without the trailing ';')
 * values stream: valuesLength x u16, the replacement chars as UTF-16 code units
 * </pre>
 *
 * {@code nodeCount} is the number of trie nodes (the root included) and {@code valuesLength} is
 * the number of replacement chars, so {@link EntitiesPrefix} can allocate its arrays in a single
 * pass.
 *
 * The names are sorted so the suffixes can be front coded (prefixLen = number of chars shared with
 * the previous name); the trailing ';' is part of the name, as the tokenizer distinguishes
 * {@code &amp} and {@code &amp;}, but it is stored as a flag instead of a suffix byte. The streams
 * are stored separately so the resource compresses better.
 *
 */
public class GenerateEntities {

    private static class EntityValues {
        int[] codepoints;
    }

    public static void main(String[] args) throws IOException {
        Path jsonPath = Paths.get("src/test/resources/entities.json");
        Path targetPath = Paths.get("src/main/resources" + EntitiesPrefix.RESOURCE);

        Type type = (new TypeToken<Map<String, EntityValues>>() {
        }).getType();
        String json = Files.readString(jsonPath);
        Map<String, EntityValues> entities = new GsonBuilder().create().fromJson(json, type);

        List<Map.Entry<String, EntityValues>> sorted = new ArrayList<>(entities.entrySet());
        // the leading '&' is implicit, the trailing ';' is not: "&amp" and "&amp;" are 2 entities
        sorted.sort(Map.Entry.comparingByKey((left, right) -> left.substring(1).compareTo(right.substring(1))));

        // count the trie nodes (the root included) and the replacement chars up front, so they
        // can be stored in the header and EntitiesPrefix can allocate its arrays in a single pass
        int nodeCount = 1; // node 0 is the root
        int valuesLength = 0;
        String previous = "";
        for (var entry : sorted) {
            String name = entry.getKey().substring(1);
            int prefix = commonPrefixLength(previous, name);
            nodeCount += name.length() - prefix;
            for (int codePoint : entry.getValue().codepoints) {
                valuesLength += codePoint < 0x10000 ? 1 : 2;
            }
            previous = name;
        }
        if (sorted.size() > 0xffff || nodeCount > 0xffff || valuesLength > 0xffff) {
            throw new IllegalStateException("entity trie too large: count=" + sorted.size()
                    + ", nodeCount=" + nodeCount + ", valuesLength=" + valuesLength);
        }

        ByteArrayOutputStream plain = new ByteArrayOutputStream();
        var out = new DataOutputStream(plain);
        out.writeShort(sorted.size());
        out.writeShort(nodeCount);
        out.writeShort(valuesLength);

        // the control, suffix and values streams are written separately so the resource
        // compresses better
        ByteArrayOutputStream ctrlStream = new ByteArrayOutputStream();
        ByteArrayOutputStream suffixStream = new ByteArrayOutputStream();
        ByteArrayOutputStream valuesStream = new ByteArrayOutputStream();
        var ctrl = new DataOutputStream(ctrlStream);
        var suffixOut = new DataOutputStream(suffixStream);
        var valuesOut = new DataOutputStream(valuesStream);

        previous = "";
        for (var entry : sorted) {
            String name = entry.getKey().substring(1);
            int[] codepoints = entry.getValue().codepoints;
            if (codepoints == null || codepoints.length < 1 || codepoints.length > 2) {
                throw new IllegalStateException("unexpected codepoints for " + entry.getKey());
            }

            int prefix = commonPrefixLength(previous, name);
            boolean endsWithSemicolon = name.endsWith(";");
            String suffix = name.substring(prefix, endsWithSemicolon ? name.length() - 1 : name.length());
            if (prefix > 0x1f || suffix.length() > 0xff) {
                throw new IllegalStateException("entity name too long: " + entry.getKey());
            }

            int unitCount = 0;
            for (int codePoint : codepoints) {
                unitCount += codePoint < 0x10000 ? 1 : 2;
            }
            if (unitCount > 4) {
                throw new IllegalStateException("too many code units for " + entry.getKey());
            }

            ctrl.writeByte(prefix | (endsWithSemicolon ? 0x20 : 0) | ((unitCount - 1) << 6));
            ctrl.writeByte(suffix.length());
            suffixOut.write(suffix.getBytes(StandardCharsets.US_ASCII));
            for (int codePoint : codepoints) {
                for (char c : Character.toChars(codePoint)) {
                    valuesOut.writeChar(c);
                }
            }
            previous = name;
        }
        ctrl.flush();
        suffixOut.flush();
        valuesOut.flush();
        ctrlStream.writeTo(out);
        suffixStream.writeTo(out);
        valuesStream.writeTo(out);
        out.flush();

        Path parent = targetPath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        try (OutputStream file = Files.newOutputStream(targetPath)) {
            plain.writeTo(file);
        }
        System.err.printf("%d entities -> %s (%d bytes)%n", sorted.size(), targetPath, Files.size(targetPath));
    }

    private static int commonPrefixLength(String left, String right) {
        int prefix = 0;
        while (prefix < left.length() && prefix < right.length() && left.charAt(prefix) == right.charAt(prefix)) {
            prefix++;
        }
        return prefix;
    }
}
