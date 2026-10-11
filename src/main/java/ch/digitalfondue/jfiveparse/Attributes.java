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

    ArrayList<AttributeNode> attributes;

    public Attributes() {
    }

    public Attributes(Map<String, AttributeNode> attributes) {
        this.attributes = new ArrayList<>(attributes.values());
    }

    public boolean containsKey(String key) {
        return get(key) != null;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof Attributes a) {
            return Objects.equals(attributes, a.attributes);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(attributes);
    }

    public Attributes copy() {
        Attributes a = new Attributes();
        if (attributes != null) {
            for (var v : attributes) {
                a.put(new AttributeNode(v));
            }
            return a;
        }
        return a;
    }

    public AttributeNode get(String key) {
        if (attributes == null) {
            return null;
        }
        for (AttributeNode attr : attributes) {
            if (Objects.equals(attr.getName(), key)) {
                return attr;
            }
        }
        return null;
    }

    void ensureAttributes() {
        if (attributes == null) {
            attributes = new ArrayList<>(3);
        }
    }

    public void put(AttributeNode attribute) {
        ensureAttributes();
        // replace
        var attrSize = attributes.size();
        for (var i = 0; i < attrSize; i++) {
            if (Objects.equals(attribute.getName(), attributes.get(i).getName())) {
                attributes.set(i, attribute); // replace
                return;
            }
        }
        // add if not present
        attributes.add(attribute);
    }

    public void put(String key, String value) {
        put(new AttributeNode(key, value));
    }

    public void remove(String key) {
        if (attributes == null) {
            return;
        }
        var attrSize = attributes.size();
        for (var i = 0; i < attrSize; i++) {
            if (Objects.equals(key, attributes.get(i).getName())) {
                attributes.remove(i);
                return;
            }
        }
    }

    public boolean isEmpty() {
        return attributes == null || attributes.isEmpty();
    }

    @Override
    public Iterator<AttributeNode> iterator() {
        return attributes == null ? Collections.emptyIterator() : attributes.iterator();
    }

    public String getNamedItem(String name) {
        var attr = get(name);
        return attr != null ? attr.getValue() : null;
    }
}
