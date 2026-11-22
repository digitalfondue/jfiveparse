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

    @Override
    public Iterator<String> iterator() {
        return null; // FIXME
    }

    public String item(int index) {
        return null; // FIXME
    }

    public boolean contains(String value) {
        return false; // FIXME
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
        return 0; // FIXME
    }

    public boolean add(String token) {
        return false; // FIXME
    }

    public boolean add(String token, String... others) {
        return false; // FIXME
    }

    public boolean isEmpty() {
        return false; // FIXME
    }
}