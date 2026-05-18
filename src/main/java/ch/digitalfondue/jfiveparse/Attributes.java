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

import java.util.*;

public final class Attributes implements Iterable<AttributeNode> {

    private AttributeNode[] smallAttributes;
    private int size;
    private Map<String, AttributeNode> attributes;

    public Attributes() {
    }

    public Attributes(Map<String, AttributeNode> attributes) {
        this.attributes = attributes;
    }

    public boolean containsKey(String key) {
        if (attributes != null) {
            return attributes.containsKey(key);
        }
        if (smallAttributes != null) {
            for (int i = 0; i < size; i++) {
                if (smallAttributes[i].name.equals(key)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof Attributes a) {
            if (attributes != null || a.attributes != null) {
                return Objects.equals(asMap(), a.asMap());
            }
            if (size != a.size) {
                return false;
            }
            for (int i = 0; i < size; i++) {
                if (!smallAttributes[i].equals(a.smallAttributes[i])) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private Map<String, AttributeNode> asMap() {
        if (attributes != null) {
            return attributes;
        }
        if (size == 0) {
            return Collections.emptyMap();
        }
        Map<String, AttributeNode> m = new LinkedHashMap<>();
        for (int i = 0; i < size; i++) {
            m.put(smallAttributes[i].name, smallAttributes[i]);
        }
        return m;
    }

    @Override
    public int hashCode() {
        if (attributes != null) {
            return Objects.hashCode(attributes);
        }
        int hash = 7;
        for (int i = 0; i < size; i++) {
            hash = 31 * hash + smallAttributes[i].hashCode();
        }
        return hash;
    }

    public Attributes copy() {
        Attributes a = new Attributes();
        if (attributes != null) {
            a.attributes = new LinkedHashMap<>();
            for (var v : attributes.values()) {
                a.put(new AttributeNode(v));
            }
        } else if (smallAttributes != null) {
            a.smallAttributes = new AttributeNode[smallAttributes.length];
            a.size = size;
            for (int i = 0; i < size; i++) {
                a.smallAttributes[i] = new AttributeNode(smallAttributes[i]);
            }
        }
        return a;
    }

    public AttributeNode get(String key) {
        if (attributes != null) {
            return attributes.get(key);
        }
        if (smallAttributes != null) {
            for (int i = 0; i < size; i++) {
                if (smallAttributes[i].name.equals(key)) {
                    return smallAttributes[i];
                }
            }
        }
        return null;
    }

    Set<String> keySet() {
        if (attributes != null) {
            return attributes.keySet();
        }
        if (size == 0) {
            return Collections.emptySet();
        }
        Set<String> s = new LinkedHashSet<>();
        for (int i = 0; i < size; i++) {
            s.add(smallAttributes[i].name);
        }
        return s;
    }

    public void put(AttributeNode attribute) {
        if (attributes != null) {
            attributes.put(attribute.getName(), attribute);
            return;
        }

        if (smallAttributes == null) {
            smallAttributes = new AttributeNode[2];
        }

        for (int i = 0; i < size; i++) {
            if (smallAttributes[i].name.equals(attribute.name)) {
                smallAttributes[i] = attribute;
                return;
            }
        }

        if (size < 8) {
            if (size == smallAttributes.length) {
                smallAttributes = Arrays.copyOf(smallAttributes, smallAttributes.length * 2);
            }
            smallAttributes[size++] = attribute;
        } else {
            attributes = new LinkedHashMap<>();
            for (int i = 0; i < size; i++) {
                attributes.put(smallAttributes[i].name, smallAttributes[i]);
            }
            attributes.put(attribute.name, attribute);
            smallAttributes = null;
        }
    }

    public void put(String key, String value) {
        put(new AttributeNode(key, value));
    }

    public void remove(String key) {
        if (attributes != null) {
            attributes.remove(key);
        } else if (smallAttributes != null) {
            for (int i = 0; i < size; i++) {
                if (smallAttributes[i].name.equals(key)) {
                    System.arraycopy(smallAttributes, i + 1, smallAttributes, i, size - i - 1);
                    smallAttributes[--size] = null;
                    return;
                }
            }
        }
    }

    public boolean isEmpty() {
        return (attributes == null || attributes.isEmpty()) && size == 0;
    }

    @Override
    public Iterator<AttributeNode> iterator() {
        if (attributes != null) {
            return attributes.values().iterator();
        }
        if (size == 0) {
            return Collections.emptyIterator();
        }
        return new Iterator<>() {
            int i = 0;

            @Override
            public boolean hasNext() {
                return i < size;
            }

            @Override
            public AttributeNode next() {
                return smallAttributes[i++];
            }
        };
    }

    public String getNamedItem(String name) {
        AttributeNode a = get(name);
        return a != null ? a.getValue() : null;
    }
}
