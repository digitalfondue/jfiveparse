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

public final class DOMTokenList implements Iterable<String> {

    private final String attrName;
    private final Element element;

    public DOMTokenList(Element element, String attrName) {
        this.element = element;
        this.attrName = attrName;
    }

    static class ValuesIterator implements Iterator<String> {
        final String value;
        final int size;
        LinkedHashSet<String> current;
        int currentIndex = 0;

        ValuesIterator(String value) {
            this.value = value;
            this.size = value.length();
        }

        private String nextToken() {
            int i = currentIndex;
            while (i < size && Common.isTabLfFfCrOrSpace(value.charAt(i))) {
                i++;
            }
            int startToken = i;
            currentIndex = startToken;
            while (i < size && !Common.isTabLfFfCrOrSpace(value.charAt(i))) {
                i++;
            }
            int endToken = i;
            return startToken >= endToken ? null : value.substring(startToken, endToken);
        }

        @Override
        public boolean hasNext() {
            var res = nextToken();
            while(true) {
                if (res == null) {
                    return false;
                }
                if (current == null) {
                    current = new LinkedHashSet<>();
                }
                if (!current.contains(res)) {
                    return true;
                }
                currentIndex = currentIndex + res.length();
                res = nextToken();
            }
        }

        @Override
        public String next() {
            var next = nextToken();
            while (true) {
                if (next == null) {
                    throw new NoSuchElementException("No more element in " + value);
                }
                if (current == null) {
                    current = new LinkedHashSet<>();
                }
                if (!current.contains(next)) {
                    current.add(next);
                    currentIndex = currentIndex + next.length();
                    return next;
                }
                next = nextToken();
            }
        }
    }

    @Override
    public Iterator<String> iterator() {
        String value = element.getAttributeValue(attrName);
        if (Common.isEmpty(value) || isAllWhiteSpace(value)) {
            return Collections.emptyIterator();
        }
        return new ValuesIterator(value);
    }

    private static boolean isAllWhiteSpace(String value) {
        int size = value.length();
        for (int i = 0; i < size; i++) {
            if (!Common.isTabLfFfCrOrSpace(value.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public String item(int index) {
        int i = 0;
        for (var v : this) {
            if (i == index) {
                return v;
            }
            i++;
        }
        return null;
    }

    public boolean contains(String value) {
        for (var v : this) {
            if (Objects.equals(v, value)) {
                return true;
            }
        }
        return false;
    }

    private LinkedHashSet<String> attrValues() {
        String value = element.getAttributeValue(attrName);
        var r = new LinkedHashSet<String>();
        if (Common.isEmpty(value) || isAllWhiteSpace(value)) {
            return r;
        }
        var it = new ValuesIterator(value);
        while (it.hasNext()) {
            it.next();
        }
        return it.current;
    }

    public void remove(String value) {
        var v = attrValues();
        v.remove(value);
        updateAttribute(v);
    }

    private void updateAttribute(LinkedHashSet<String> updated) {
        element.setAttribute(attrName, String.join(" ", updated));
    }

    public void replace(String oldToken, String newToken) {
        // FIXME
    }

    public boolean toggle(String token) {
        return false; // FIXME
    }

    public boolean toggle(String token, boolean force) {
        return false; // FIXME
    }

    public int size() {
        int count = 0;
        for (var v : this) {
            count++;
        }
        return count;
    }

    public boolean add(String token) {
        return add(token, null);
    }

    public boolean add(String token, String... others) {
        var values = attrValues();
        var res = values.add(token);
        if (others != null) {
            for (var o : others) {
                res = res && values.add(o);
            }
        }
        updateAttribute(values);
        return res;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public String toString() {
        return element.getAttributeValue(attrName);
    }
}