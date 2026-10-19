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

    AttributeNode[] attributes;
    int count;

    public Attributes() {
    }

    public Attributes(Map<String, AttributeNode> attributes) {
        this.attributes = attributes.values().toArray(new AttributeNode[0]);
        this.count = attributes.size();
    }

    public boolean containsKey(String key) {
        return get(key) != null;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Attributes a) || count != a.count) {
            return false;
        }
        for (int i = 0; i < count; i++) {
            if (!Objects.equals(attributes[i], a.attributes[i])) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = 0;
        for (int i = 0; i < count; i++) {
            result = 31 * result + Objects.hashCode(attributes[i]);
        }
        return result;
    }

    public Attributes copy() {
        Attributes a = new Attributes();
        if (attributes != null) {
            a.attributes = new AttributeNode[count];
            a.count = count;
            for (int i = 0; i < count; i++) {
                a.attributes[i] = new AttributeNode(attributes[i]);
            }
            return a;
        }
        return a;
    }

    public AttributeNode get(String key) {
        if (attributes == null) {
            return null;
        }
        for (int i = 0; i < count; i++) {
            var attr = attributes[i];
            if (Objects.equals(attr.getName(), key)) {
                return attr;
            }
        }
        return null;
    }

    void ensureAttributes() {
        if (attributes == null) {
            attributes = new AttributeNode[3];
        }
    }

    public void put(AttributeNode attribute) {
        ensureAttributes();
        // replace
        for (var i = 0; i < count; i++) {
            if (Objects.equals(attribute.getName(), attributes[i].getName())) {
                attributes[i] = attribute; // replace
                return;
            }
        }
        // add if not present
        add(attribute);
    }

    void add(AttributeNode attributeNode) {
        if (count == attributes.length) {
            attributes = Arrays.copyOf(attributes, attributes.length + 3);
        }
        attributes[count] = attributeNode;
        count++;
    }

    public void put(String key, String value) {
        put(new AttributeNode(key, value));
    }

    public void remove(String key) {
        if (attributes == null) {
            return;
        }
        for (var i = 0; i < count; i++) {
            if (Objects.equals(key, attributes[i].getName())) {
                // identify how many we need to shift left:
                int amountToShiftLeft = count - i - 1;
                // shift left
                System.arraycopy(attributes, i + 1, attributes, i, amountToShiftLeft);
                // set to null last element
                attributes[--count] = null;
                return;
            }
        }
    }

    public boolean isEmpty() {
        return attributes == null || count == 0;
    }

    @Override
    public Iterator<AttributeNode> iterator() {
        return attributes == null ? Collections.emptyIterator() : Arrays.stream(attributes, 0, count).iterator();
    }

    public String getNamedItem(String name) {
        var attr = get(name);
        return attr != null ? attr.getValue() : null;
    }
}
