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
import java.util.regex.Pattern;

public final class DOMTokenList extends AbstractList<String> {

    private final String attrName;
    private final Element element;

    public DOMTokenList(Element element, String attrName) {
        this.element = element;
        this.attrName = attrName;
    }

    private static final Pattern SPACES = Pattern.compile("\\s+");

    static List<String> extractValues(CommonNode.CommonElement element, String attrName) {
        List<String> vals = new ArrayList<>();
        if (element.containsAttribute(attrName)) {
            String value = element.getAttributeValue(attrName);
            if (value != null) {
                for (String s : SPACES.split(value)) {
                    if (!s.trim().isEmpty()) {
                        vals.add(s);
                    }
                }
            }
        }
        return vals;
    }

    private List<String> attributeValues() {
        return extractValues(element, attrName);
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

    public void add(String val, String... values) {
        add(val);
        if (values != null) {
            addAll(Arrays.asList(values));
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
        return condition && toggle(name);
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