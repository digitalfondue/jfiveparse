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
 * Trie of the html5 named character references.
 *
 * The data is stored in the classpath as a sorted and prefix compressed list of entity names (see
 * GenerateEntities).
 *
 * A position in the trie is an {@code int} (the node id), {@link #ROOT} is the starting point and
 * {@link #NO_NODE} means "no match".
 *
 * The trie is stored as a left-child/right-sibling tree: every node has a pointer to its left
 * child ({@link #LEFT_CHILD}) and to its right sibling ({@link #RIGHT_SIBLING}), while
 * {@link #NODE_CHAR} holds the character carried by the edge from the parent. Looking up a child is
 * a linear scan of the right-sibling chain.
 *
 * The encoding of the resource is big endian, split in streams so it compresses well:
 *
 * <pre>
 * count        u16            number of entities
 * nodeCount    u16            number of trie nodes, root included
 * valuesLength u16            number of replacement chars (UTF-16 code units)
 * count x 2 bytes, control stream, entries sorted by name (name = entity name without the leading '&amp;'):
 *     ctrl0      u8           bits 0-4: prefixLen, chars shared with the previous name
 *                             bit 5:    the name ends with ';'
 *                             bits 6-7: unitCount - 1 (number of replacement code units, 1 to 4)
 *     ctrl1      u8           suffixLen
 * then the suffix stream: the concatenated suffixes (the name after the shared prefix, without
 * the trailing ';'), ascii bytes
 * then the values stream: valuesLength x u16, the replacement chars as UTF-16 code units
 * </pre>
 *
 * As the names are sorted, the trie is built in a single pass: the nodes of a name are only
 * created from the longest prefix it shares with the previous name. The trailing ';' is not
 * stored as a suffix byte but as a flag in ctrl0.
 */
final class EntitiesPrefix {

    static final String RESOURCE = "/ch/digitalfondue/jfiveparse/entities-with-1-2-codepoint";

    private static final int HEADER_SIZE = 6; // count(2) + nodeCount(2) + valuesLength(2)
    private static final int NO_VALUE = -1;

    /** returned when there is no such node */
    static final int NO_NODE = -1;
    /** id of the root of the trie, the cursor a lookup must start with */
    static final int ROOT = 0;

    /** char of the edge leading to the node (left-child/right-sibling tree) */
    private static final byte[] NODE_CHAR;
    /** id of the left child, or {@link #NO_NODE} */
    private static final int[] LEFT_CHILD;
    /** id of the right sibling, or {@link #NO_NODE} */
    private static final int[] RIGHT_SIBLING;
    /**
     * {@code (start << 3) | length} of the replacement chars in {@link #VALUES}, or
     * {@link #NO_VALUE} if the node does not represent a complete entity
     */
    private static final int[] VALUE;
    private static final char[] VALUES;

    static {
        byte[] data;
        try (InputStream raw = EntitiesPrefix.class.getResourceAsStream(RESOURCE)) {
            data = raw.readAllBytes();
        } catch (IOException ioe) {
            throw new IllegalStateException("jfiveparse: unable to read resource " + RESOURCE, ioe);
        }
        int count = readUnsignedShort(data, 0);
        int nodeCount = readUnsignedShort(data, 2);
        int valuesLength = readUnsignedShort(data, 4);

        byte[] nodeChar = new byte[nodeCount]; // ascii
        int[] leftChild = new int[nodeCount];
        Arrays.fill(leftChild, NO_NODE); // 0 is the root, "nothing" must be explicit
        int[] rightSibling = new int[nodeCount];
        int[] value = new int[nodeCount];
        Arrays.fill(value, NO_VALUE);
        char[] values = new char[valuesLength];
        int nextNode = 1; // node 0 is the root
        int valuesPos = 0;
        int ctrlPos = HEADER_SIZE;
        int suffixPos = HEADER_SIZE + 2 * count;
        int valuesBytePos = data.length - 2 * valuesLength;

        // the names are sorted: the nodes of a name share a prefix with the previous one,
        // stack[d] is the node reached after d chars of the current name
        var stack = new int[64];
        var name = new char[64];

        for (int i = 0; i < count; i++) {
            int ctrl0 = data[ctrlPos++] & 0xff;
            int prefix = ctrl0 & 0x1f;
            boolean endsWithSemicolon = (ctrl0 & 0x20) != 0;
            int unitCount = ((ctrl0 >> 6) & 0x3) + 1;
            int suffix = data[ctrlPos++] & 0xff;
            int nameLength = prefix + suffix + (endsWithSemicolon ? 1 : 0);
            if (nameLength >= name.length) {
                name = new char[nameLength + 1];
                stack = new int[nameLength + 1];
            }
            for (int j = 0; j < suffix; j++) {
                name[prefix + j] = (char) (data[suffixPos++] & 0xff);
            }
            if (endsWithSemicolon) {
                name[nameLength - 1] = ';';
            }

            int valueStart = valuesPos;
            for (int j = 0; j < unitCount; j++) {
                values[valuesPos++] = (char) (((data[valuesBytePos] & 0xff) << 8) | (data[valuesBytePos + 1] & 0xff));
                valuesBytePos += 2;
            }

            int node = prefix == 0 ? ROOT : stack[prefix - 1];
            for (int j = prefix; j < nameLength; j++) {
                char c = name[j];
                int child = leftChild[node];
                int last = NO_NODE;
                while (child != NO_NODE && nodeChar[child] != (byte) c) {
                    last = child;
                    child = rightSibling[child];
                }
                if (child == NO_NODE) {
                    child = nextNode++;
                    nodeChar[child] = (byte) c;
                    leftChild[child] = NO_NODE;
                    rightSibling[child] = NO_NODE;
                    value[child] = NO_VALUE;
                    if (last == NO_NODE) {
                        leftChild[node] = child;
                    } else {
                        rightSibling[last] = child;
                    }
                }
                node = child;
                stack[j] = node;
            }
            value[node] = (valueStart << 3) | (valuesPos - valueStart);
        }

        NODE_CHAR = nodeChar;
        LEFT_CHILD = leftChild;
        RIGHT_SIBLING = rightSibling;
        VALUE = value;
        VALUES = values;
    }

    private EntitiesPrefix() {
    }

    /**
     * @return the child of {@code node} matching {@code c}, or {@link #NO_NODE}
     */
    static int getNode(int node, char c) {
        int child = LEFT_CHILD[node];
        while (child != NO_NODE) {
            if (NODE_CHAR[child] == (byte) c) {
                return child;
            }
            child = RIGHT_SIBLING[child];
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

    /** @return the chars of the entity represented by {@code node} */
    static char[] chars(int node) {
        int value = VALUE[node];
        int start = value >>> 3;
        return Arrays.copyOfRange(VALUES, start, start + (value & 0x7));
    }

    private static int readUnsignedShort(byte[] data, int offset) {
        return ((data[offset] & 0xff) << 8) | (data[offset + 1] & 0xff);
    }
}
