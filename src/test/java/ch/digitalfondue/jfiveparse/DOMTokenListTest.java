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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class DOMTokenListTest {

    private DOMTokenList classListFor(String classAttr) {
        Element el = new Element("div");
        if (classAttr != null) {
            el.setAttribute("class", classAttr);
        }
        return new DOMTokenList(el, "class");
    }

    @Test
    void toggleWithForceTrueAddsAbsentToken() {
        DOMTokenList list = classListFor(null);
        assertTrue(list.toggle("foo", true));
        assertTrue(list.contains("foo"));
    }

    @Test
    void toggleWithForceTrueKeepsPresentToken() {
        DOMTokenList list = classListFor("foo");
        assertTrue(list.toggle("foo", true));
        assertTrue(list.contains("foo"));
    }

    @Test
    void toggleWithForceFalseRemovesPresentToken() {
        DOMTokenList list = classListFor("foo bar");
        assertFalse(list.toggle("foo", false));
        assertFalse(list.contains("foo"));
        assertTrue(list.contains("bar"));
    }

    @Test
    void toggleWithForceFalseIsNoOpOnAbsentToken() {
        DOMTokenList list = classListFor("bar");
        assertFalse(list.toggle("foo", false));
        assertFalse(list.contains("foo"));
        assertTrue(list.contains("bar"));
    }
}
