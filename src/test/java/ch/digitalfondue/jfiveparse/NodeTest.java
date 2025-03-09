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

import java.util.Arrays;
import java.util.EnumSet;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NodeTest {

    final Parser parser = new Parser();

    @Test
    void getElementById() {
        Document doc = parser.parse("<div>1</div><div id=myid>2</div><div>3</div>");
        assertNotNull(doc.getElementById("myid"));
        assertEquals("2", doc.getElementById("myid").getTextContent());
    }


    @Test
    void getElementByIdNotFound() {
        Document doc = parser.parse("<div>1</div><div id=myid>2</div><div>3</div>");
        assertNull(doc.getElementById("myid2"));
    }

    @Test
    void nodeSibling() {
        Document doc = parser.parse("<div>1</div><div id=myid>2</div><div>3</div>");
        Element e = doc.getElementById("myid");
        assertNotNull(e);
        Node parent = e.getParentNode();

        assertEquals(parent.getChildNodes().get(0), e.getPreviousSibling());
        assertEquals(parent.getChildNodes().get(0), e.getPreviousElementSibling());
        assertEquals(parent.getChildNodes().get(2), e.getNextSibling());
        assertEquals(parent.getChildNodes().get(2), e.getNextElementSibling());

        assertEquals(parent.getChildNodes().get(2), parent.getLastChild());
        assertEquals(parent.getChildNodes().get(2), parent.getLastElementChild());

        assertEquals(parent.getChildNodes().get(0), parent.getFirstChild());
        assertEquals(parent.getChildNodes().get(0), parent.getFirstElementChild());
    }

    @Test
    void innerAndOuterHtml() {
        Document doc = parser.parse("<div>1</div><div id=myid>2</div><div>3</div>");
        Element e = doc.getElementById("myid");
        assertEquals("2", e.getInnerHTML());
        assertEquals("<div id=\"myid\">2</div>", e.getOuterHTML());
        assertEquals("<div id=myid>2</div>", e.getOuterHTML(EnumSet.of(Option.PRINT_ORIGINAL_ATTRIBUTE_QUOTE)));
    }

    @Test
    void getElementsByTagName() {
        Document doc = parser.parse("<div>1</div><span id=myid>2</span><div>3</div>");
        assertEquals(2, doc.getElementsByTagName("div").size());
        assertEquals(1, doc.getElementsByTagName("span").size());

        assertEquals(2, doc.getElementsByTagNameNS("div", Node.NAMESPACE_HTML).size());
        assertEquals(1, doc.getElementsByTagNameNS("span", Node.NAMESPACE_HTML).size());

        assertEquals(0, doc.getElementsByTagNameNS("div", Node.NAMESPACE_SVG).size());
        assertEquals(0, doc.getElementsByTagNameNS("span", Node.NAMESPACE_SVG).size());
    }

    @Test
    void replaceChild() {
        Document doc = parser.parse("<div id=myid>1<span>2</span><div>3</div></div>");
        Element e = doc.getElementById("myid");
        Node last = e.getChildNodes().get(2);
        Node newNode = new Element("span");
        e.replaceChild(newNode, last);

        assertNull(last.getParentNode());
        assertEquals(newNode, e.getChildNodes().get(2));
    }

    @Test
    void insertBeforeChild() {
        Document doc = parser.parse("<div id=myid>1<span>2</span><div>3</div></div>");
        Element e = doc.getElementById("myid");
        Node last = e.getChildNodes().get(2);
        Node newNode = new Element("span");
        e.insertBefore(newNode, last);

        assertEquals(newNode, e.getChildNodes().get(2));
        assertEquals(last, e.getChildNodes().get(3));
        assertEquals(e, newNode.getParentNode());
    }

    @Test
    void testClass() {
        Document doc = parser.parse("<div id=myid class=></div>");
        Element e = doc.getElementById("myid");
        assertEquals("", e.getClassName());
        assertTrue(e.getClassList().isEmpty());

        e.getClassList().add("plop");

        assertEquals("plop", e.getClassName());
        assertEquals(Arrays.asList("plop"), e.getClassList());

        e.getClassList().remove("plop");
        assertEquals("", e.getClassName());
        assertTrue(e.getClassList().isEmpty());

        e.getClassList().add("plop", "hurr");
        assertEquals("plop hurr", e.getClassName());
        assertEquals(Arrays.asList("plop", "hurr"), e.getClassList());

        assertFalse(e.getClassList().toggle("plop"));
        assertEquals(Arrays.asList("hurr"), e.getClassList());
        assertTrue(e.getClassList().toggle("plop"));
        assertEquals(Arrays.asList("hurr", "plop"), e.getClassList());

        assertFalse(e.getClassList().toggle("plop", false));
        assertEquals(Arrays.asList("hurr", "plop"), e.getClassList());
        assertFalse(e.getClassList().toggle("plop", true));
        assertEquals(Arrays.asList("hurr"), e.getClassList());

        Document doc2 = parser.parse("<div id=myid class=' my class    \n abc '></div>");
        Element e2 = doc2.getElementById("myid");
        assertEquals("my class abc", e2.getClassName());
        assertFalse(e2.getClassList().isEmpty());
        assertEquals(Arrays.asList("my", "class", "abc"), e2.getClassList());
    }

    @Test
    void setInnerHTML() {
        Document doc = parser.parse("<div id=myid class=></div>");
        Element e = doc.getElementById("myid");
        assertEquals("", e.getInnerHTML());
        e.setInnerHTML("<h1>test</h1>plop <p>test");
        assertEquals("<h1>test</h1>plop <p>test</p>", e.getInnerHTML());
    }

    @Test
    void contains() {
        Document doc = parser.parse("<div id=cont> text<div> bla bla <div id=myid class=>blabla</div>plop</div></div>");
        Element e = doc.getElementById("myid");
        Element cont = doc.getElementById("cont");
        assertTrue(cont.contains(e));
        assertFalse(e.contains(cont));
    }

    @Test
    void match() {
        Document doc = parser.parse("<div id=cont> text<div> bla bla <div id=myid class=>blabla</div>plop</div></div>");
        assertEquals(1, doc.getAllNodesMatching(Selector.select().attr("id").toMatcher(), true).size());
        assertEquals(2, doc.getAllNodesMatching(Selector.select().attr("id").toMatcher(), false).size());
    }

    @Test
    void normalize() {

        //same case of https://developer.mozilla.org/en-US/docs/Web/API/Node/normalize

        Element wrapper = new Element("div");
        wrapper.appendChild(new Text("Part 1"));
        wrapper.appendChild(new Text("Part 2"));

        wrapper.normalize();

        assertEquals(1, wrapper.getChildNodes().size());
        assertEquals("Part 1Part 2", wrapper.getChildNodes().get(0).getTextContent());

    }

    @Test
    void normalizeEmpty1() {

        //same case of https://developer.mozilla.org/en-US/docs/Web/API/Node/normalize

        Element wrapper = new Element("div");
        wrapper.appendChild(new Text(""));

        wrapper.normalize();

        assertEquals(0, wrapper.getChildNodes().size());

    }

    @Test
    void normalizeEmpty2() {

        //same case of https://developer.mozilla.org/en-US/docs/Web/API/Node/normalize

        Element wrapper = new Element("div");
        wrapper.appendChild(new Text(""));
        wrapper.appendChild(new Text(""));

        wrapper.normalize();

        assertEquals(0, wrapper.getChildNodes().size());

    }

    @Test
    void normalizeNested() {

        //same case of https://developer.mozilla.org/en-US/docs/Web/API/Node/normalize

        Element wrapper = new Element("div");
        wrapper.appendChild(new Text("Part 1"));
        wrapper.appendChild(new Text("Part 2"));
        Element wrapper2 = new Element("div");
        wrapper.appendChild(wrapper2);
        Element wrapper3 = new Element("div");
        wrapper2.appendChild(wrapper3);
        wrapper3.appendChild(new Text("Part 3"));
        wrapper3.appendChild(new Text("Part 4"));

        wrapper.appendChild(new Text("Part 5"));
        wrapper.appendChild(new Text("Part 6"));


        wrapper.normalize();

        assertEquals(3,wrapper.getChildNodes().size());
        assertEquals("Part 1Part 2", wrapper.getChildNodes().get(0).getTextContent());
        assertEquals(1, wrapper3.getChildNodes().size());
        assertEquals("Part 3Part 4", wrapper3.getChildNodes().get(0).getTextContent());
        assertEquals("Part 5Part 6", wrapper.getChildNodes().get(2).getTextContent());

    }

    @Test
    void normalizeEmptyNested() {

        //same case of https://developer.mozilla.org/en-US/docs/Web/API/Node/normalize

        Element wrapper = new Element("div");
        wrapper.appendChild(new Text(""));
        wrapper.appendChild(new Text(""));
        Element wrapper2 = new Element("div");
        wrapper.appendChild(wrapper2);
        Element wrapper3 = new Element("div");
        wrapper2.appendChild(wrapper3);
        wrapper3.appendChild(new Text(""));
        wrapper3.appendChild(new Text(""));

        wrapper.appendChild(new Text(""));
        wrapper.appendChild(new Text(""));


        wrapper.normalize();

        assertEquals(1,wrapper.getChildNodes().size());
        assertEquals("<div><div></div></div>", wrapper.getInnerHTML());
        assertEquals(1, wrapper.getChildNodes().get(0).getChildCount());
        assertEquals(0, wrapper.getChildNodes().get(0).getChildNodes().get(0).getChildCount());

    }

    @Test
    void sameNode() {
        Node a = new Element("div");
        Node b = new Element("div");
        assertFalse(a.isSameNode(b));
        assertFalse(a.isSameNode(null));
        assertTrue(a.isSameNode(a));
        assertTrue(b.isSameNode(b));
    }

}
