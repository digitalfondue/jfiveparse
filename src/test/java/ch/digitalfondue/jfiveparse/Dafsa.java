package ch.digitalfondue.jfiveparse;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

// https://www.ryanliptak.com/blog/better-named-character-reference-tokenization/
// based on https://github.com/squeek502/named-character-references/tree/master
public class Dafsa {
    private int[] firstLayer;
    private int[] data;
    private long[] values;

    // Temporary builder state, cleared after compact()
    private Builder builder = new Builder();

    public Dafsa() {
    }

    public void insert(String word, long value) {
        if (builder == null) {
            throw new IllegalStateException("DAFSA is already compacted");
        }
        // FIXME obviously we expect the first char to be &, need to wrap in an api that take care of it
        if (word.startsWith("&")) {
            word = word.substring(1);
        }
        builder.insert(word, value);
    }

    public void compact() {
        if (builder == null) {
            return; // Already compacted
        }
        Dafsa compacted = builder.build();
        this.firstLayer = compacted.firstLayer;
        this.data = compacted.data;
        this.values = compacted.values;
        this.builder = null; // Free builder memory
    }

    public void writeTo(OutputStream os) throws IOException {
        //
        compact();
        //
        DataOutputStream dos = new DataOutputStream(new GZIPOutputStream(os));
        dos.writeInt(firstLayer.length);
        for (int v : firstLayer) {
            dos.writeInt(v);
        }
        dos.writeInt(data.length);
        for (int v : data) {
            dos.writeInt(v);
        }
        dos.writeInt(values.length);
        for (long v : values) {
            dos.writeLong(v);
        }
        dos.flush();
        dos.close();
    }

    public static Dafsa readFrom(InputStream is) throws IOException {
        DataInputStream dis = new DataInputStream(new GZIPInputStream(is));
        Dafsa dafsa = new Dafsa();
        int firstLayerLen = dis.readInt();
        dafsa.firstLayer = new int[firstLayerLen];
        for (int i = 0; i < firstLayerLen; i++) {
            dafsa.firstLayer[i] = dis.readInt();
        }
        int dataLen = dis.readInt();
        dafsa.data = new int[dataLen];
        for (int i = 0; i < dataLen; i++) {
            dafsa.data[i] = dis.readInt();
        }
        int valuesLen = dis.readInt();
        dafsa.values = new long[valuesLen];
        for (int i = 0; i < valuesLen; i++) {
            dafsa.values[i] = dis.readLong();
        }
        dafsa.builder = null; // Mark as compacted
        return dafsa;
    }

    private int getUniqueIndex(String word) {
        //
        compact();
        //
        if (word.isEmpty()) {
            return -1;
        }
        char firstChar = word.charAt(0);
        int lookupIdx;
        if (firstChar >= 'A' && firstChar <= 'Z') {
            lookupIdx = firstChar - 'A';
        } else if (firstChar >= 'a' && firstChar <= 'z') {
            lookupIdx = firstChar - 'a' + 26;
        } else {
            return -1;
        }

        int firstNode = firstLayer[lookupIdx];
        int childrenLen = getFirstLayerChildrenLen(firstNode);
        boolean firstLayerEnd = isFirstLayerEndOfWord(firstNode);

        int accumulatedIndex = getFirstLayerNumber(firstNode);
        if (firstLayerEnd) {
            accumulatedIndex += 1;
        }

        if (word.length() == 1) {
            return firstLayerEnd ? accumulatedIndex : 0;
        }

        if (childrenLen == 0) {
            return -1;
        }

        int childIndex = getFirstLayerChildIndex(firstNode);

        for (int i = 1; i < word.length(); i++) {
            char c = word.charAt(i);

            // Binary search for character c in children range [childIndex, childIndex + childrenLen]
            int low = childIndex;
            int high = childIndex + childrenLen - 1;
            int matchIdx = -1;
            while (low <= high) {
                int mid = (low + high) >>> 1;
                int node = data[mid];
                char tc = getNodeChar(node);
                if (tc < c) {
                    low = mid + 1;
                } else if (tc > c) {
                    high = mid - 1;
                } else {
                    matchIdx = mid;
                    break;
                }
            }

            if (matchIdx == -1) {
                return -1;
            }

            int matchedNode = data[matchIdx];
            accumulatedIndex += getNodeNumber(matchedNode);
            boolean isTerminal = isNodeEndOfWord(matchedNode);
            if (isTerminal) {
                accumulatedIndex += 1;
            }

            if (i == word.length() - 1) {
                return isTerminal ? accumulatedIndex : 0;
            }

            childIndex = getNodeChildIndex(matchedNode);
            childrenLen = getNodeChildrenLen(matchedNode);
        }

        return -1;
    }

    public int lookup(String word) {
        // FIXME, we don't remove the & in the api
        if (word.startsWith("&")) {
            word = word.substring(1);
        }
        int idx = getUniqueIndex(word);
        if (idx > 0) {
            return 1;
        }
        return idx;
    }

    public long lookupValue(String word) {
        // FIXME, we don't remove the & in the api
        if (word.startsWith("&")) {
            word = word.substring(1);
        }
        int idx = getUniqueIndex(word);
        if (idx <= 0) {
            throw new NoSuchElementException("Word not found: " + word);
        }
        return values[idx - 1];
    }

    // FirstLayerNode packing: childIndex (12 bits), childrenLen (6 bits), endOfWord (1 bit), number (13 bits)
    private static int packFirstLayerNode(int childIndex, int childrenLen, boolean endOfWord, int number) {
        return (childIndex & 0xFFF) | ((childrenLen & 0x3F) << 12) | ((endOfWord ? 1 : 0) << 18) | ((number & 0x1FFF) << 19);
    }
    private static int getFirstLayerChildIndex(int packed) {
        return packed & 0xFFF;
    }
    private static int getFirstLayerChildrenLen(int packed) {
        return (packed >>> 12) & 0x3F;
    }
    private static boolean isFirstLayerEndOfWord(int packed) {
        return ((packed >>> 18) & 1) != 0;
    }
    private static int getFirstLayerNumber(int packed) {
        return (packed >>> 19) & 0x1FFF;
    }

    // Node packing: char (7 bits), number (8 bits), end_of_word (1 bit), children_len (4 bits), child_index (12 bits)
    private static int packNode(char c, int number, boolean endOfWord, int childrenLen, int childIndex) {
        return (c & 0x7F) |
               ((number & 0xFF) << 7) |
               ((endOfWord ? 1 : 0) << 15) |
               ((childrenLen & 0xF) << 16) |
               ((childIndex & 0xFFF) << 20);
    }
    private static char getNodeChar(int packed) {
        return (char) (packed & 0x7F);
    }
    private static int getNodeNumber(int packed) {
        return (packed >>> 7) & 0xFF;
    }
    private static boolean isNodeEndOfWord(int packed) {
        return ((packed >>> 15) & 1) != 0;
    }
    private static int getNodeChildrenLen(int packed) {
        return (packed >>> 16) & 0xF;
    }
    private static int getNodeChildIndex(int packed) {
        return (packed >>> 20) & 0xFFF;
    }

    public static class Builder {
        private static class TrieNode {
            boolean isTerminal;
            long value;
            TreeMap<Character, TrieNode> children = new TreeMap<>();
            int minimizedId = -1;
        }

        private final TrieNode root = new TrieNode();
        private final TreeMap<String, Long> inserted = new TreeMap<>();

        public void insert(String word, long value) {
            inserted.put(word, value);
            TrieNode current = root;
            for (int i = 0; i < word.length(); i++) {
                char c = word.charAt(i);
                current = current.children.computeIfAbsent(c, k -> new TrieNode());
            }
            current.isTerminal = true;
            current.value = value;
        }

        public Dafsa build() {
            Map<String, MinimizedNode> registry = new HashMap<>();
            List<MinimizedNode> minimizedNodes = new ArrayList<>();

            minimize(root, registry, minimizedNodes);

            int rootId = root.minimizedId;
            MinimizedNode rootNode = minimizedNodes.get(rootId);

            int[] weights = new int[minimizedNodes.size()];
            Arrays.fill(weights, -1);
            for (MinimizedNode node : minimizedNodes) {
                getWeight(node, weights, minimizedNodes);
            }

            int[] nodeStartIndices = new int[minimizedNodes.size()];
            Arrays.fill(nodeStartIndices, -1);
            int currentIndex = 0;
            for (MinimizedNode node : minimizedNodes) {
                if (node.id == rootId) {
                    continue;
                }
                nodeStartIndices[node.id] = currentIndex;
                currentIndex += node.transitions.size();
            }

            int[] firstLayer = new int[52];
            int rootPrecedingSum = 0;
            for (int k = 0; k < 52; k++) {
                char c;
                if (k < 26) {
                    c = (char) ('A' + k);
                } else {
                    c = (char) ('a' + (k - 26));
                }

                Integer targetId = rootNode.transitions.get(c);
                if (targetId == null) {
                    firstLayer[k] = packFirstLayerNode(0, 0, false, 0);
                } else {
                    MinimizedNode targetNode = minimizedNodes.get(targetId);
                    int childIndex = nodeStartIndices[targetNode.id];
                    int childrenLen = targetNode.transitions.size();
                    boolean endOfWord = targetNode.isTerminal;
                    int number = rootPrecedingSum;

                    if (childIndex > 0xFFF) {
                        throw new IllegalStateException("First layer childIndex " + childIndex + " exceeds 12 bits!");
                    }
                    if (childrenLen > 0x3F) {
                        throw new IllegalStateException("First layer childrenLen " + childrenLen + " exceeds 6 bits!");
                    }
                    if (number > 0x1FFF) {
                        throw new IllegalStateException("First layer number " + number + " exceeds 13 bits!");
                    }

                    firstLayer[k] = packFirstLayerNode(childIndex, childrenLen, endOfWord, number);
                    rootPrecedingSum += weights[targetNode.id];
                }
            }

            int[] data = new int[currentIndex];
            for (MinimizedNode node : minimizedNodes) {
                if (node.id == rootId) {
                    continue;
                }
                int start = nodeStartIndices[node.id];
                int precedingSum = 0;
                int j = 0;
                for (Map.Entry<Character, Integer> entry : node.transitions.entrySet()) {
                    char childChar = entry.getKey();
                    MinimizedNode childNode = minimizedNodes.get(entry.getValue());

                    int childIndex = nodeStartIndices[childNode.id];
                    int childrenLen = childNode.transitions.size();
                    boolean endOfWord = childNode.isTerminal;
                    int number = precedingSum;

                    if (childIndex > 0xFFF) {
                        throw new IllegalStateException("childIndex " + childIndex + " exceeds 12 bits!");
                    }
                    if (childrenLen > 0xF) {
                        throw new IllegalStateException("childrenLen " + childrenLen + " exceeds 4 bits!");
                    }
                    if (number > 0xFF) {
                        throw new IllegalStateException("number " + number + " exceeds 8 bits!");
                    }

                    data[start + j] = packNode(childChar, number, endOfWord, childrenLen, childIndex);

                    precedingSum += weights[childNode.id];
                    j++;
                }
            }

            long[] valuesArray = new long[inserted.size()];
            int valIdx = 0;
            for (long v : inserted.values()) {
                valuesArray[valIdx++] = v;
            }

            Dafsa dafsa = new Dafsa();
            dafsa.firstLayer = firstLayer;
            dafsa.data = data;
            dafsa.values = valuesArray;
            return dafsa;
        }

        private int getWeight(MinimizedNode node, int[] weights, List<MinimizedNode> minimizedNodes) {
            if (weights[node.id] != -1) {
                return weights[node.id];
            }
            int w = node.isTerminal ? 1 : 0;
            for (int childId : node.transitions.values()) {
                MinimizedNode child = minimizedNodes.get(childId);
                w += getWeight(child, weights, minimizedNodes);
            }
            weights[node.id] = w;
            return w;
        }

        private int minimize(TrieNode node, Map<String, MinimizedNode> registry, List<MinimizedNode> minimizedNodes) {
            TreeMap<Character, Integer> minimizedChildren = new TreeMap<>();
            for (Map.Entry<Character, TrieNode> entry : node.children.entrySet()) {
                int childId = minimize(entry.getValue(), registry, minimizedNodes);
                minimizedChildren.put(entry.getKey(), childId);
            }

            StringBuilder sigBuilder = new StringBuilder();
            sigBuilder.append(node.isTerminal ? "T" : "N").append("|");
            for (Map.Entry<Character, Integer> entry : minimizedChildren.entrySet()) {
                sigBuilder.append(entry.getKey()).append(":").append(entry.getValue()).append(",");
            }
            String signature = sigBuilder.toString();

            MinimizedNode minimized = registry.get(signature);
            if (minimized == null) {
                minimized = new MinimizedNode();
                minimized.id = registry.size();
                minimized.isTerminal = node.isTerminal;
                minimized.transitions = minimizedChildren;
                registry.put(signature, minimized);
                minimizedNodes.add(minimized);
            }

            node.minimizedId = minimized.id;
            return node.minimizedId;
        }
    }

    private static class MinimizedNode {
        int id;
        boolean isTerminal;
        TreeMap<Character, Integer> transitions;
    }

    @Test
    void testBasic() {
        Dafsa dafsa = new Dafsa();
        var words = List.of("dog", "apple", "app", "banana", "band", "b");
        for (int i = 0; i < words.size(); i++) {
            dafsa.insert(words.get(i), i + 10);
        }
        dafsa.compact();

        Assertions.assertEquals(1, dafsa.lookup("dog"));
        Assertions.assertEquals(10, dafsa.lookupValue("dog"));

        Assertions.assertEquals(1, dafsa.lookup("apple"));
        Assertions.assertEquals(11, dafsa.lookupValue("apple"));

        Assertions.assertEquals(1, dafsa.lookup("app"));
        Assertions.assertEquals(12, dafsa.lookupValue("app"));

        Assertions.assertEquals(0, dafsa.lookup("ap"));
        Assertions.assertEquals(-1, dafsa.lookup("cat"));

        Assertions.assertEquals(1, dafsa.lookup("banana"));
        Assertions.assertEquals(13, dafsa.lookupValue("banana"));

        Assertions.assertEquals(1, dafsa.lookup("band"));
        Assertions.assertEquals(14, dafsa.lookupValue("band"));

        Assertions.assertEquals(1, dafsa.lookup("b"));
        Assertions.assertEquals(15, dafsa.lookupValue("b"));

        Assertions.assertEquals(0, dafsa.lookup("ban"));
    }

    private static class EntityValues {
        int[] codepoints;
    }

    private static long pack1Or2IntToLong(int[] codepoint) {
        if (codepoint.length == 1) {
            return ((long) codepoint[0] << 32);
        } else if (codepoint.length == 2) {
            return ((long) codepoint[0] << 32) | (codepoint[1] & 0xFFFFFFFFL);
        }
        throw new IllegalStateException("1 or 2 codepoint");
    }

    @Test
    void testHtmlEntities() throws IOException {
        java.lang.reflect.Type type = (new com.google.gson.reflect.TypeToken<Map<String, EntityValues>>() {}).getType();
        String json = Files.readString(Paths.get("src/test/resources/entities.json"));
        Map<String, EntityValues> entities = new com.google.gson.GsonBuilder().create().fromJson(json, type);

        Dafsa dafsa = new Dafsa();
        for (String key : entities.keySet()) {
            var codepoints = entities.get(key).codepoints;
            dafsa.insert(key, pack1Or2IntToLong(codepoints));
        }
        dafsa.compact();

        // Print stats
        System.out.println("DAFSA first layer size:" + dafsa.firstLayer.length + " ints (" + (dafsa.firstLayer.length * 4) + " bytes)");
        System.out.println("DAFSA data size: " + dafsa.data.length + " ints (" + (dafsa.data.length * 4) + " bytes)");
        System.out.println("DAFSA values size: " + dafsa.values.length + " longs");

        // Verify all entities
        for (String key : entities.keySet()) {
            Assertions.assertEquals(1, dafsa.lookup(key));
            long val = dafsa.lookupValue(key);
            int h = (int) (val >> 32);
            int l = (int) val;
            int[] codePoints = l == 0 ? new int[] { h } : new int[] { h, l };
            Assertions.assertArrayEquals(entities.get(key).codepoints, codePoints);
        }

        // Verify serialization and deserialization
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        dafsa.writeTo(baos);
        byte[] serialized = baos.toByteArray();
        System.out.println("DAFSA serialized & gzipped size: " + serialized.length + " bytes");

        Files.write(Path.of("dafsa-payload"), serialized);

        ByteArrayInputStream bais = new ByteArrayInputStream(serialized);
        Dafsa deserializedDafsa = Dafsa.readFrom(bais);

        // Verify all entities against deserialized DAFSA
        for (String key : entities.keySet()) {
            Assertions.assertEquals(1, deserializedDafsa.lookup(key));
            long val = deserializedDafsa.lookupValue(key);
            int h = (int) (val >> 32);
            int l = (int) val;
            int[] codePoints = l == 0 ? new int[] { h } : new int[] { h, l };
            Assertions.assertArrayEquals(entities.get(key).codepoints, codePoints);
        }

        // Verify some negative cases
        Assertions.assertEquals(-1, deserializedDafsa.lookup("&lol;"));
        Assertions.assertEquals(0, deserializedDafsa.lookup("&am"));
    }
}
