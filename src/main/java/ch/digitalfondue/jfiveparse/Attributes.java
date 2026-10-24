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

    // either an AttributeNode (single attribute, the common case) or an AttributeNode[]
    Object attributes;
    int count;
    // true when the owning element is in the HTML namespace: name based
    // operations are then ASCII case-insensitive, mirroring the NamedNodeMap
    // behaviour in browsers.
    boolean html;

    public Attributes() {
    }

    public Attributes(Collection<AttributeNode> attributes) {
        if (attributes == null) {
            return;
        }
        for (var a : attributes) {
            put(a);
        }
    }

    public Attributes(Map<String, AttributeNode> attributes) {
        this(attributes.values());
    }

    public boolean containsKey(String key) {
        return get(key) != null;
    }

    public boolean containsKey(String namespace, String localName) {
        return get(namespace, localName) != null;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Attributes a) || count != a.count) {
            return false;
        }

        // order of insertion is not important
        for (int i = 0; i < count; i++) {
            var attr = attributeAt(i);
            var aAttr = a.get(attr.getNamespace(), attr.getLocalName());
            if (aAttr == null || !Objects.equals(attr.getValue(), aAttr.getValue())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = 0;
        for (int i = 0; i < count; i++) {
            result = result + Objects.hashCode(attributeAt(i));
        }
        return result;
    }

    AttributeNode attributeAt(int index) {
        return attributes instanceof AttributeNode[] arr ? arr[index] : (AttributeNode) attributes;
    }

    private String nameKey(String key) {
        return html ? Common.convertToAsciiLowerCase(key) : key;
    }

    public Attributes copy() {
        Attributes a = new Attributes();
        a.html = html;
        if (attributes instanceof AttributeNode single) {
            a.attributes = new AttributeNode(single);
            a.count = 1;
        } else if (attributes instanceof AttributeNode[] arr) {
            var newArr = new AttributeNode[count];
            for (int i = 0; i < count; i++) {
                newArr[i] = new AttributeNode(arr[i]);
            }
            a.attributes = newArr;
            a.count = count;
        }
        return a;
    }

    public AttributeNode get(String key) {
        key = nameKey(key);
        if (attributes instanceof AttributeNode single) {
            return Objects.equals(single.getName(), key) ? single : null;
        }
        if (attributes instanceof AttributeNode[] arr) {
            for (int i = 0; i < count; i++) {
                var attr = arr[i];
                if (Objects.equals(attr.getName(), key)) {
                    return attr;
                }
            }
        }
        return null;
    }

    public AttributeNode get(String namespace, String localName) {
        namespace = AttributeNode.normalizeNamespace(namespace);
        if (attributes instanceof AttributeNode single) {
            return single.matches(namespace, localName) ? single : null;
        }
        if (attributes instanceof AttributeNode[] arr) {
            for (int i = 0; i < count; i++) {
                if (arr[i].matches(namespace, localName)) {
                    return arr[i];
                }
            }
        }
        return null;
    }

    public void put(AttributeNode attribute) {
        if (attributes == null) {
            attributes = attribute;
            count = 1;
            return;
        }
        if (attributes instanceof AttributeNode single) {
            if (Objects.equals(attribute.getName(), single.getName())) {
                attributes = attribute; // replace
            } else {
                attributes = new AttributeNode[] { single, attribute };
                count = 2;
            }
            return;
        }
        var arr = (AttributeNode[]) attributes;
        // replace
        for (var i = 0; i < count; i++) {
            if (Objects.equals(attribute.getName(), arr[i].getName())) {
                arr[i] = attribute; // replace
                return;
            }
        }
        // add if not present
        add(attribute);
    }

    // add without checking if the attribute name is already present
    void add(AttributeNode attributeNode) {
        if (attributes == null) {
            attributes = attributeNode;
            count = 1;
        } else if (attributes instanceof AttributeNode single) {
            attributes = new AttributeNode[] { single, attributeNode };
            count = 2;
        } else {
            var arr = (AttributeNode[]) attributes;
            if (count == arr.length) {
                arr = Arrays.copyOf(arr, arr.length + 3);
                attributes = arr;
            }
            arr[count] = attributeNode;
            count++;
        }
    }

    public void put(String key, String value) {
        put(new AttributeNode(nameKey(key), value));
    }

    public void put(String namespace, String name, String value) {
        put(AttributeNode.createWithNamespace(namespace, name, value));
    }

    public void remove(String key) {
        key = nameKey(key);
        if (attributes instanceof AttributeNode single) {
            if (Objects.equals(key, single.getName())) {
                attributes = null;
                count = 0;
            }
            return;
        }
        if (attributes instanceof AttributeNode[] arr) {
            for (var i = 0; i < count; i++) {
                if (Objects.equals(key, arr[i].getName())) {
                    removeAt(arr, i);
                    return;
                }
            }
        }
    }

    /**
     * Remove the attribute with the given namespace and local name. An empty
     * namespace is treated as null.
     */
    public void remove(String namespace, String localName) {
        namespace = AttributeNode.normalizeNamespace(namespace);
        if (attributes instanceof AttributeNode single) {
            if (single.matches(namespace, localName)) {
                attributes = null;
                count = 0;
            }
            return;
        }
        if (attributes instanceof AttributeNode[] arr) {
            for (var i = 0; i < count; i++) {
                if (arr[i].matches(namespace, localName)) {
                    removeAt(arr, i);
                    return;
                }
            }
        }
    }

    private void removeAt(AttributeNode[] arr, int index) {
        // identify how many we need to shift left:
        int amountToShiftLeft = count - index - 1;
        // shift left
        System.arraycopy(arr, index + 1, arr, index, amountToShiftLeft);
        // set to null last element
        arr[--count] = null;
        // collapse back to a single attribute when possible
        if (count == 1) {
            attributes = arr[0];
        }
    }

    public boolean isEmpty() {
        return attributes == null || count == 0;
    }

    @Override
    public Iterator<AttributeNode> iterator() {
        if (attributes instanceof AttributeNode single) {
            return Collections.singleton(single).iterator();
        }
        return attributes == null ? Collections.emptyIterator() : Arrays.stream((AttributeNode[]) attributes, 0, count).iterator();
    }

    public String getNamedItem(String name) {
        var attr = get(name);
        return attr != null ? attr.getValue() : null;
    }


    public String getNamedItemNS(String namespace, String localName) {
        var attr = get(namespace, localName);
        return attr != null ? attr.getValue() : null;
    }
}
