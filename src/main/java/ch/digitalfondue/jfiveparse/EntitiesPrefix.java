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

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Trie of the html5 named character references, used by
 * {@link Tokenizer#parseEntity(boolean, ProcessedInputStream, Tokenizer)}.
 *
 * The data is stored in the classpath as a sorted and prefix compressed list of entity names (see
 * {@code GenerateEntities} in the test sources) and is loaded, at class initialization, directly
 * into flat arrays.
 *
 * A position in the trie is an {@code int} (the node id), {@link #ROOT} is the starting point and
 * {@link #NO_NODE} means "no match". The encoding of the resource is big endian:
 *
 * <pre>
 * count        u16            number of entities
 * nodeCount    u16            number of trie nodes, root included
 * valuesLength u16            number of replacement chars
 * count times, sorted by name (name = entity name without the leading '&amp;'):
 *     prefixLen  u8           number of chars shared with the previous name
 *     suffixLen  u8
 *     suffix     suffixLen ascii bytes
 *     cpCount    u8           number of codepoints (1 or 2)
 *     codepoints cpCount x uleb128
 * </pre>
 *
 * As the names are sorted, the trie is built in a single pass: the nodes of a name are only
 * created from the longest prefix it shares with the previous name.
 */
final class EntitiesPrefix {

    static final String RESOURCE = "/ch/digitalfondue/jfiveparse/entities-with-1-2-codepoint";

    private static final int HEADER_SIZE = 6; // count(2) + nodeCount(2) + valuesLength(2)
    private static final int NO_VALUE = -1;

    /** returned when there is no such node */
    static final int NO_NODE = -1;
    /** id of the root of the trie, the cursor a lookup must start with */
    static final int ROOT = 0;

    private static final byte[] NODE_CHAR;
    private static final int[] FIRST_CHILD;
    private static final int[] NEXT_SIBLING;
    /**
     * {@code (start << 3) | length} of the replacement chars in {@link #VALUES}, or
     * {@link #NO_VALUE} if the node does not represent a complete entity
     */
    private static final int[] VALUE;
    private static final char[] VALUES;

    static {
        byte[] data = readResource();
        int count = readUnsignedShort(data, 0);
        // nodeCount and valuesLength are pre-calculated by GenerateEntities, the flat arrays can
        // be allocated at their exact size in a single pass
        int nodeCount = readUnsignedShort(data, 2);
        int valuesLength = readUnsignedShort(data, 4);

        byte[] nodeChar = new byte[nodeCount]; // ascii
        int[] firstChild = new int[nodeCount];
        Arrays.fill(firstChild, NO_NODE); // 0 is the root, "nothing" must be explicit
        int[] nextSibling = new int[nodeCount];
        int[] value = new int[nodeCount];
        Arrays.fill(value, NO_VALUE);
        char[] values = new char[valuesLength];
        int nextNode = 1; // node 0 is the root
        int valuesPos = 0;
        int pos = HEADER_SIZE;

        // the names are sorted: the nodes of a name share a prefix with the previous one,
        // stack[d] is the node reached after d chars of the current name
        var stack = new int[64];
        var name = new char[64];

        for (int i = 0; i < count; i++) {
            int prefix = data[pos++] & 0xff;
            int suffix = data[pos++] & 0xff;
            if (prefix + suffix >= name.length) {
                name = new char[prefix + suffix];
                stack = new int[prefix + suffix];
            }
            for (int j = 0; j < suffix; j++) {
                name[prefix + j] = (char) (data[pos++] & 0xff);
            }
            int nameLength = prefix + suffix;

            int valueStart = valuesPos;
            int codePointCount = data[pos++] & 0xff;
            for (int j = 0; j < codePointCount; j++) {
                int codePoint = 0;
                for (int shift = 0; ; shift += 7) {
                    int b = data[pos++] & 0xff;
                    codePoint |= (b & 0x7f) << shift;
                    if ((b & 0x80) == 0) {
                        break;
                    }
                }
                if (codePoint < 0x10000) {
                    values[valuesPos++] = (char) codePoint;
                } else {
                    for (char c : Character.toChars(codePoint)) {
                        values[valuesPos++] = c;
                    }
                }
            }

            int node = prefix == 0 ? ROOT : stack[prefix - 1];
            for (int j = prefix; j < nameLength; j++) {
                char c = name[j];
                int child = firstChild[node];
                int last = NO_NODE;
                while (child != NO_NODE && nodeChar[child] != (byte) c) {
                    last = child;
                    child = nextSibling[child];
                }
                if (child == NO_NODE) {
                    child = nextNode++;
                    nodeChar[child] = (byte) c;
                    firstChild[child] = NO_NODE;
                    nextSibling[child] = NO_NODE;
                    value[child] = NO_VALUE;
                    if (last == NO_NODE) {
                        firstChild[node] = child;
                    } else {
                        nextSibling[last] = child;
                    }
                }
                node = child;
                stack[j] = node;
            }
            value[node] = (valueStart << 3) | (valuesPos - valueStart);
        }

        NODE_CHAR = nodeChar;
        FIRST_CHILD = firstChild;
        NEXT_SIBLING = nextSibling;
        VALUE = value;
        VALUES = values;
    }

    private EntitiesPrefix() {
    }

    /**
     * @return the child of {@code node} matching {@code c}, or {@link #NO_NODE}
     */
    static int getNode(int node, char c) {
        int child = FIRST_CHILD[node];
        while (child != NO_NODE) {
            if (NODE_CHAR[child] == (byte) c) {
                return child;
            }
            child = NEXT_SIBLING[child];
        }
        return NO_NODE;
    }

    /** @return true if {@code node} is a complete entity name */
    static boolean isComplete(int node) {
        return VALUE[node] != NO_VALUE;
    }

    /** @return the last char of the name of {@code node} */
    static char lastChar(int node) {
        return (char) (NODE_CHAR[node] & 0xff);
    }

    /** @return the chars of the entity represented by {@code node}, a fresh array */
    static char[] chars(int node) {
        int value = VALUE[node];
        int start = value >>> 3;
        return Arrays.copyOfRange(VALUES, start, start + (value & 0x7));
    }

    private static int readUnsignedShort(byte[] data, int offset) {
        return ((data[offset] & 0xff) << 8) | (data[offset + 1] & 0xff);
    }

    private static byte[] readResource() {
        try (InputStream raw = EntitiesPrefix.class.getResourceAsStream(RESOURCE)) {
            return raw.readAllBytes();
        } catch (IOException ioe) {
            throw new IllegalStateException("jfiveparse: unable to read resource " + RESOURCE, ioe);
        }
    }
}
