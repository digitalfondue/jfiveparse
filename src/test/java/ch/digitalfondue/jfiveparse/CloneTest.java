/**
 * Copyright © 2019 digitalfondue (info@digitalfondue.ch)
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


class CloneTest {

    @Test
    void cloneDeep() {
        Document document = new Parser().parse("<div><p>Hello World</p></div>");
        Document cloned = (Document) document.cloneNode(true);
        assertEquals(document.getFirstElementChild().getOuterHTML(), cloned.getFirstElementChild().getOuterHTML());

        ((Text) cloned.getElementsByTagName("p").get(0).getChildNodes().get(0)).setData("Hello World!");

        assertNotEquals(document.getFirstElementChild().getOuterHTML(), cloned.getFirstElementChild().getOuterHTML());
        assertEquals("<html><head></head><body><div><p>Hello World</p></div></body></html>", document.getFirstElementChild().getOuterHTML());
        assertEquals("<html><head></head><body><div><p>Hello World!</p></div></body></html>", cloned.getFirstElementChild().getOuterHTML());
    }


    @Test
    void cloneShallow() {
        Document document = new Parser().parse("<div><p>Hello World</p></div>");
        Document cloned = (Document) document.cloneNode(false);
        assertEquals("<html><head></head><body><div><p>Hello World</p></div></body></html>", document.getFirstElementChild().getOuterHTML());
        assertNull(cloned.getFirstElementChild());

        Element div = document.getElementsByTagName("div").get(0);
        Element divCloned = (Element) div.cloneNode(false);
        assertEquals("<div><p>Hello World</p></div>", div.getOuterHTML());
        assertEquals("<div></div>", divCloned.getOuterHTML());
    }
}
