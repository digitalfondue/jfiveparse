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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.NoSuchElementException;

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

    @Test
    void testDomTokenListCollectionContracts() {
        Element el = new Element("div");
        DOMTokenList list = el.getClassList();
        assertTrue(list.add("foo"));
        assertFalse(list.add("foo"));
    }


    @Test
    void testIterator() {
        var tokenList = classListFor("  foo       bar    baz     ");
        var it = tokenList.iterator();
        Assertions.assertTrue(it.hasNext());
        Assertions.assertTrue(it.hasNext());
        Assertions.assertEquals("foo", it.next());
        Assertions.assertTrue(it.hasNext());
        Assertions.assertTrue(it.hasNext());
        Assertions.assertEquals("bar", it.next());
        Assertions.assertTrue(it.hasNext());
        Assertions.assertTrue(it.hasNext());
        Assertions.assertEquals("baz", it.next());
        Assertions.assertFalse(it.hasNext());
        Assertions.assertFalse(it.hasNext());
        Assertions.assertThrows(NoSuchElementException.class, it::next);
    }

    @Test
    void checkInvalidAndNull() {
        Element div = new Element("div");
        DOMTokenList list = div.getClassList();

        assertThrows(IllegalArgumentException.class, () -> list.add(""));
        assertThrows(IllegalArgumentException.class, () -> list.add(" a "));
        assertThrows(IllegalArgumentException.class, () -> list.add("foo bar"));

        // browser do a null -> "null"
        list.add(null);
        assertEquals("null", div.getAttribute("class"));
    }

    @Test
    void notSetClassListReturnEmptyString() {
        Element div = new Element("div");
        assertEquals("", div.getClassList().toString());
    }

    @Test
    void checkNullContains() {
        Element el = new Element("div");
        el.setAttribute("class", "null foo");

        assertTrue(el.getClassList().contains(null));
    }

    @Timeout(value = 2, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    @Test
    void checkNextOnlyCall() {
        var el = new Element("div");
        el.setAttribute("class", "a a b");
        var it = el.getClassList().iterator();
        assertEquals("a", it.next());
        assertEquals("b", it.next());
        assertFalse(it.hasNext());
    }

    @Test
    void removeNonExistingTokenInNonExistingAttributeDontCreateEmptyAttr() {
        var el = new Element("div");
        el.getClassList().remove("x");
        Assertions.assertFalse(el.hasAttribute("class"));

        var el2 = new Element("div");
        assertFalse(el2.getClassList().toggle("x", false));
        assertFalse(el2.hasAttribute("class"));
    }
}
