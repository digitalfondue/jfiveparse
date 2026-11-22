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

public final class DOMTokenList implements Iterable<String> {

    private final String attrName;
    private final Element element;

    public DOMTokenList(Element element, String attrName) {
        this.element = element;
        this.attrName = attrName;
    }

    static Iterator<String> fromValue(String value) {
        if (value == null) {
            return Collections.emptyIterator();
        }
        int size = value.length();
        return new Iterator<>() {

            LinkedHashSet<String> current;
            int currentIndex = 0;

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
        };
    }

    @Override
    public Iterator<String> iterator() {
        return fromValue(element.getAttributeValue(attrName));
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

    public void remove(String value) {
        // FIXME
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
        return false; // FIXME
    }

    public boolean add(String token, String... others) {
        return false; // FIXME
    }

    public boolean isEmpty() {
        return size() == 0;
    }
}