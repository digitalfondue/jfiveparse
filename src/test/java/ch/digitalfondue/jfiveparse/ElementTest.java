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

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by mattia on 28/04/16.
 */
class ElementTest {
    final Parser parser = new Parser();

    //https://developer.mozilla.org/en-US/docs/Web/API/Element/insertAdjacentHTML
    @Test
    void insertAdjacentHTMLBeforeBegin() {
        Element startNode = (Element) parser.parseFragment(new Element("div"), "<div><p id=myid>foo</p></div>").get(0);
        Element myIdElement = startNode.getElementById("myid");
        myIdElement.insertAdjacentHTML("beforebegin", "<!-- beforebegin -->");
        assertEquals("<div><!-- beforebegin --><p id=\"myid\">foo</p></div>", startNode.getOuterHTML());
    }

    @Test
    void insertAdjacentHTMLAfterBeginMultiple() {
        Element startNode = (Element) parser.parseFragment(new Element("div"), "<div><p id=myid>foo</p></div>").get(0);
        Element myIdElement = startNode.getElementById("myid");
        myIdElement.insertAdjacentHTML("afterbegin", "<h1>1</h1><h2>2</h2>");
        assertEquals("<div><p id=\"myid\"><h1>1</h1><h2>2</h2>foo</p></div>", startNode.getOuterHTML());
    }

    @Test
    void beforeBeginWithoutParent() {
        Element startNode = (Element) parser.parseFragment(new Element("div"), "<div><p id=myid>foo</p></div>").get(0);
        startNode.parentNode = null;
        assertThrows(IllegalStateException.class, () ->
            startNode.insertAdjacentHTML("beforebegin", "<!-- beforebegin -->"));
    }

    @Test
    void insertAdjacentHTMLAfterBegin() {
        Element startNode = (Element) parser.parseFragment(new Element("div"), "<div><p id=myid>foo</p></div>").get(0);
        Element myIdElement = startNode.getElementById("myid");
        myIdElement.insertAdjacentHTML("afterbegin", "<!-- afterbegin -->");
        assertEquals("<div><p id=\"myid\"><!-- afterbegin -->foo</p></div>", startNode.getOuterHTML());
    }

    @Test
    void insertAdjacentHTMLBeforeEnd() {
        Element startNode = (Element) parser.parseFragment(new Element("div"), "<div><p id=myid>foo</p></div>").get(0);
        Element myIdElement = startNode.getElementById("myid");
        myIdElement.insertAdjacentHTML("beforeend", "<!-- beforeend -->");
        assertEquals("<div><p id=\"myid\">foo<!-- beforeend --></p></div>", startNode.getOuterHTML());
    }

    @Test
    void insertAdjacentHTMLBeforeEndMultiple() {
        Element startNode = (Element) parser.parseFragment(new Element("div"), "<div><p id=myid>foo</p></div>").get(0);
        Element myIdElement = startNode.getElementById("myid");
        myIdElement.insertAdjacentHTML("beforeend", "<h1>1</h1><h2>2</h2>");
        assertEquals("<div><p id=\"myid\">foo<h1>1</h1><h2>2</h2></p></div>", startNode.getOuterHTML());
    }

    @Test
    void insertAdjacentHTMLAfterEnd() {
        Element startNode = (Element) parser.parseFragment(new Element("div"), "<div><p id=myid>foo</p></div>").get(0);
        Element myIdElement = startNode.getElementById("myid");
        myIdElement.insertAdjacentHTML("afterend", "<!-- afterend -->");
        assertEquals("<div><p id=\"myid\">foo</p><!-- afterend --></div>", startNode.getOuterHTML());
    }

    @Test
    void afterEndWithoutParent() {
        Element startNode = (Element) parser.parseFragment(new Element("div"), "<div><p id=myid>foo</p></div>").get(0);
        startNode.parentNode = null;
        assertThrows(IllegalStateException.class, () ->
            startNode.insertAdjacentHTML("afterend", "<!-- beforebegin -->"));
    }

    @Test()
    void wrongPosition() {
        Element startNode = (Element) parser.parseFragment(new Element("div"), "<div><p id=myid>foo</p></div>").get(0);
        assertThrows(IllegalStateException.class, () ->
            startNode.insertAdjacentHTML("plop", "<!-- plop -->"));
    }

    @Test
    void all() {
        Element startNode = (Element) parser.parseFragment(new Element("div"), "<div><p id=myid>foo</p></div>").get(0);
        Element myIdElement = startNode.getElementById("myid");
        myIdElement.insertAdjacentHTML("beforebegin", "<!-- beforebegin -->");
        myIdElement.insertAdjacentHTML("afterbegin", "<!-- afterbegin -->");
        myIdElement.insertAdjacentHTML("beforeend", "<!-- beforeend -->");
        myIdElement.insertAdjacentHTML("afterend", "<!-- afterend -->");
        assertEquals("<div><!-- beforebegin --><p id=\"myid\"><!-- afterbegin -->foo<!-- beforeend --></p><!-- afterend --></div>", startNode.getOuterHTML());
    }

    @Test
    void insertAdjacentElementAll() {
        Element startNode = (Element) parser.parseFragment(new Element("div"), "<div><p id=myid>foo</p></div>").get(0);
        Element myIdElement = startNode.getElementById("myid");
        myIdElement.insertAdjacentElement("beforebegin", new Element("beforebegin"));
        myIdElement.insertAdjacentElement("afterbegin", new Element("afterbegin"));
        myIdElement.insertAdjacentElement("beforeend", new Element("beforeend"));
        myIdElement.insertAdjacentElement("afterend", new Element("afterend"));
        assertEquals("<div><beforebegin></beforebegin><p id=\"myid\"><afterbegin></afterbegin>foo<beforeend></beforeend></p><afterend></afterend></div>", startNode.getOuterHTML());
    }

    @Test
    void wrongElementPosition() {
        Element startNode = (Element) parser.parseFragment(new Element("div"), "<div><p id=myid>foo</p></div>").get(0);
        assertThrows(IllegalStateException.class, () ->
            startNode.insertAdjacentElement("plop", new Element("plop")));
    }

    @Test
    void wrongTextPosition() {
        Element startNode = (Element) parser.parseFragment(new Element("div"), "<div><p id=myid>foo</p></div>").get(0);
        assertThrows(IllegalStateException.class, () ->
            startNode.insertAdjacentText("plop", "plop"));
    }

    @Test
    void insertAdjacentTextAll() {
        Element startNode = (Element) parser.parseFragment(new Element("div"), "<div><p id=myid>foo</p></div>").get(0);
        Element myIdElement = startNode.getElementById("myid");
        myIdElement.insertAdjacentText("beforebegin", "beforebegin");
        myIdElement.insertAdjacentText("afterbegin", "afterbegin");
        myIdElement.insertAdjacentText("beforeend", "beforeend");
        myIdElement.insertAdjacentText("afterend", "afterend");
        assertEquals("<div>beforebegin<p id=\"myid\">afterbeginfoobeforeend</p>afterend</div>", startNode.getOuterHTML());
    }

    @Test
    void getTagName() {
        Element div = (Element) parser.parseFragment(new Element("div"), "<div><p id=myid>foo</p></div>").get(0);
        assertEquals("DIV", div.getTagName());


        Element svg = (Element) parser.parseFragment(new Element("div"), "<svg><path id=myid>foo</path></svg>").get(0);
        assertEquals("svg", svg.getTagName());
        assertEquals("path", svg.getFirstElementChild().getTagName());
    }

    @Test
    void checkAttributes() {
        Element div = new Element("div");
        assertTrue(div.getAttributes().isEmpty());
        assertFalse(div.hasAttributes());
        assertFalse(div.hasAttribute("test"));
        div.setAttribute("test", "value");
        div.setAttribute("test2", null);
        assertTrue(div.hasAttributes());
        assertFalse(div.getAttributes().isEmpty());
        assertTrue(div.hasAttribute("test"));
        assertEquals("value", div.getAttributes().getNamedItem("test"));
        assertEquals(null, div.getAttributes().getNamedItem("test2"));
        div.removeAttribute("test");
        assertFalse(div.hasAttribute("test"));
        assertFalse(div.getAttributes().containsKey("test"));
        assertTrue(div.getAttributes().containsKey("test2"));
        assertEquals("<div test2=\"null\"></div>", div.getOuterHTML());
    }

    @Test
    void checkAddNull() {
        Element div = new Element("div");
        Assertions.assertThrows(NullPointerException.class, () -> div.appendChild(null));
        Assertions.assertThrows(NullPointerException.class, () -> div.insertChildren(0, null));
    }
}
