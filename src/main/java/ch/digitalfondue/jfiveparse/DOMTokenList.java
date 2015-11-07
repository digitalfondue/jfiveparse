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

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class DOMTokenList extends AbstractList<String> {

    private final String attrName;
    private final Element element;

    public DOMTokenList(Element element, String attrName) {
        this.element = element;
        this.attrName = attrName;
    }

    private List<String> attributeValues() {
        AttributeNode a = element.attributes != null ? element.attributes.get(attrName) : null;
        if (a != null && a.getValue() != null) {
            List<String> vals = new ArrayList<>();
            for (String s : a.getValue().split("\\s+")) {
                if (s != null && s.trim().length() > 0) {
                    vals.add(s);
                }
            }
            return vals;
        } else {
            return new ArrayList<>(1);
        }
    }

    @Override
    public void add(int index, String value) {
        if (contains(value)) {
            return;
        }
        List<String> vals = attributeValues();
        vals.add(index, value);
        element.getAttributes().put(attrName, Common.join(vals));
    }

    @SafeVarargs
    public final void add(String val, String... values) {
        add(val);
        if (values != null) {
            for (String s : values) {
                add(s);
            }
        }
    }

    @Override
    public String remove(int index) {
        List<String> vals = attributeValues();
        String val = vals.remove(index);
        element.getAttributes().put(attrName, Common.join(vals));
        return val;
    }

    @Override
    public boolean remove(Object o) {
        List<String> vals = attributeValues();
        boolean removed = vals.remove(o);
        if (removed) {
            element.getAttributes().put(attrName, Common.join(vals));
        }
        return removed;
    }

    @Override
    public String set(int index, String value) {
        if (contains(value)) {
            return null;
        }

        List<String> vals = attributeValues();
        String replaced = vals.set(index, value);
        element.getAttributes().put(attrName, Common.join(vals));
        return replaced;
    }

    public boolean toggle(String name) {
        if (contains(name)) {
            remove(name);
            return false;
        } else {
            add(name);
            return true;
        }
    }

    public boolean toggle(String name, boolean condition) {
        return condition ? toggle(name) : false;
    }

    public String item(int index) {
        return get(index);
    }

    @Override
    public ListIterator<String> listIterator(int idx) {
        return Collections.unmodifiableList(attributeValues()).listIterator(idx);
    }

    @Override
    public ListIterator<String> listIterator() {
        return Collections.unmodifiableList(attributeValues()).listIterator();
    }

    @Override
    public Iterator<String> iterator() {
        return Collections.unmodifiableList(attributeValues()).iterator();
    }

    @Override
    public String get(int index) {
        return attributeValues().get(index);
    }

    @Override
    public int size() {
        return attributeValues().size();
    }
}