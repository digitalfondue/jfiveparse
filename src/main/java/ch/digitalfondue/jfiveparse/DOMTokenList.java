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

    private static String validateToken(String token) {
        if (token == null) {
            return "null"; // like browser...
        }
        if (token.isEmpty()) {
            throw new IllegalArgumentException("Token cannot be empty string.");
        }
        for (int i = 0; i < token.length(); i++) {
            if (Common.isTabLfFfCrOrSpace(token.charAt(i))) {
                throw new IllegalArgumentException("Token must not contain HTML whitespace: " + token);
            }
        }
        return token;
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
        value = value == null ? "null" : value;
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
        value = validateToken(value);
        var v = attrValues();
        v.remove(value);
        updateAttribute(v);
    }

    private void updateAttribute(LinkedHashSet<String> updated) {
        element.setAttribute(attrName, String.join(" ", updated));
    }

    public boolean toggle(String token) {
        token = validateToken(token);
        var v = attrValues();
        boolean res;
        if (v.contains(token)) {
            v.remove(token);
            res = false;
        } else {
            v.add(token);
            res = true;
        }
        updateAttribute(v);
        return res;
    }

    public boolean toggle(String token, boolean force) {
        token = validateToken(token);
        var v = attrValues();
        boolean res;
        if (force) {
            v.add(token);
            res = true;
        } else {
            v.remove(token);
            res = false;
        }
        updateAttribute(v);
        return res;
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
        token = validateToken(token);
        var values = attrValues();
        var res = values.add(token);
        if (others != null) {
            for (var o : others) {
                var added = values.add(validateToken(o));
                res = res && added;
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
        var v = element.getAttributeValue(attrName);
        return v == null ? "" : v;
    }
}