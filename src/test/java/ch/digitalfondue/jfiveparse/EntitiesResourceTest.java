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
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EntitiesResourceTest {

    private static class EntityValues {
        int[] codepoints;
    }

    private static Map<String, EntityValues> referenceEntities() throws IOException {
        Type type = (new TypeToken<Map<String, EntityValues>>() {
        }).getType();
        String json = Files.readString(Paths.get("src/test/resources/entities.json"));
        return new GsonBuilder().create().fromJson(json, type);
    }

    private static int walk(String name) {
        int node = EntitiesPrefix.ROOT;
        for (int i = 0; i < name.length(); i++) {
            node = EntitiesPrefix.getNode(node, name.charAt(i));
            if (node == EntitiesPrefix.NO_NODE) {
                return EntitiesPrefix.NO_NODE;
            }
        }
        return node;
    }

    private static String textOf(String html) {
        return JFiveParse.parse(html).getBody().getTextContent();
    }

    @Test
    void everyReferenceEntityIsInTheTrie() throws IOException {
        Map<String, EntityValues> entities = referenceEntities();
        assertFalse(entities.isEmpty());
        for (var entry : entities.entrySet()) {
            String key = entry.getKey();
            // the leading '&' is not part of the name, the trailing ';' is
            String name = key.substring(1);
            int node = walk(name);
            assertNotEquals(EntitiesPrefix.NO_NODE, node, "not found: " + key);
            assertTrue(EntitiesPrefix.isComplete(node), "not a complete entity: " + key);
            assertEquals(name.charAt(name.length() - 1), EntitiesPrefix.lastChar(node), "last char of " + key);

            StringBuilder expected = new StringBuilder();
            for (int codePoint : entry.getValue().codepoints) {
                expected.appendCodePoint(codePoint);
            }
            assertEquals(expected.toString(), new String(EntitiesPrefix.chars(node)), "chars of " + key);
        }
    }

    @Test
    void namesWithoutSemicolonArePreserved() {
        // many entities are valid both with and without the trailing ';'
        assertEquals(';', EntitiesPrefix.lastChar(walk("amp;")));
        assertEquals('p', EntitiesPrefix.lastChar(walk("amp")));
        assertEquals("& &", textOf("<p>&amp; &amp</p>"));
    }

    @Test
    void unknownNames() {
        assertFalse(EntitiesPrefix.isComplete(EntitiesPrefix.ROOT));
        assertEquals(EntitiesPrefix.NO_NODE, walk("zzz;"));
        assertEquals(EntitiesPrefix.NO_NODE, walk("ampz"));
        assertEquals(EntitiesPrefix.NO_NODE, EntitiesPrefix.getNode(EntitiesPrefix.ROOT, (char) Characters.EOF));
        assertEquals(EntitiesPrefix.NO_NODE, EntitiesPrefix.getNode(EntitiesPrefix.ROOT, 'é'));
    }

    @Test
    void longestMatchWins() {
        // &notin is not an entity, but &not is: the longest match is kept, the rest is text
        assertEquals("¬in", textOf("<p>&notin</p>"));
        assertEquals("∉", textOf("<p>&notin;</p>"));
        assertEquals("¬ ¬", textOf("<p>&not &not;</p>"));
    }

    @Test
    void multiCodePointEntities() {
        int node = walk("NotNestedGreaterGreater;");
        assertTrue(EntitiesPrefix.isComplete(node));
        char[] chars = EntitiesPrefix.chars(node);
        assertEquals(2, chars.length);
        assertEquals(new String(Character.toChars(0x2AA2)) + new String(Character.toChars(0x0338)), new String(chars));
    }

    @Test
    void tokenizerUsesTheTrie() {
        assertEquals("& … ¬ ∉", textOf("<p>&amp; &hellip; &not &notin;</p>"));
        // in an attribute, a legacy entity followed by an alphanumeric char is not interpreted
        Element a = (Element) JFiveParse.parse("<a title=\"&amp &amp; &ampx\"></a>").getBody().getFirstChild();
        assertEquals("& & &ampx", a.getAttribute("title"));
        // an unknown reference is kept as-is and is not an error
        assertEquals("&zzz;", textOf("<p>&zzz;</p>"));
    }

    @Test
    void entitiesAreNotInterpretedWhenDisabled() {
        var parser = new Parser(Set.of(Option.DONT_TRANSFORM_ENTITIES));
        assertEquals("&amp; &hellip;", parser.parse("<p>&amp; &hellip;</p>").getBody().getTextContent());
    }
}
