/**
 * Copyright (C) 2015 digitalfondue (info@digitalfondue.ch)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.digitalfondue.jfiveparse;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class Attributes implements Iterable<Attribute> {

    private Map<String, Attribute> attributes;

    public Attributes() {
    }

    public Attributes(Map<String, Attribute> attributes) {
        this.attributes = attributes;
    }

    public boolean containsKey(String key) {
        return attributes == null ? false : attributes.containsKey(key);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Attributes)) {
            return false;
        }

        return Objects.equals(attributes, ((Attributes) obj).attributes);
    }

    public Attributes copy() {
        return attributes == null ? new Attributes() : new Attributes(new LinkedHashMap<>(attributes));
    }

    public Attribute get(String key) {
        return attributes == null ? null : attributes.get(key);
    }

    Set<String> keySet() {
        return attributes == null ? Collections.<String> emptySet() : attributes.keySet();
    }

    private void ensureMap() {
        if (attributes == null) {
            attributes = new LinkedHashMap<>();
        }
    }

    public void put(Attribute attribute) {
        ensureMap();
        attributes.put(attribute.getName(), attribute);
    }

    public void put(String key, String value) {
        ensureMap();
        attributes.put(key, new Attribute(key, value));
    }

    public void remove(String key) {
        if (attributes != null) {
            attributes.remove(key);
        }
    }

    public boolean isEmpty() {
        return attributes == null ? true : attributes.isEmpty();
    }

    @Override
    public Iterator<Attribute> iterator() {
        return attributes == null ? Collections.<Attribute> emptyIterator() : attributes.values().iterator();
    }

    public String getNamedItem(String name) {
        return attributes != null && attributes.containsKey(name) ? attributes.get(name).value : null;
    }
}
