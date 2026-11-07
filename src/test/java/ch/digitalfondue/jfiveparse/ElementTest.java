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

import java.util.List;
import java.util.Set;

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
        assertThrows(ParserException.class, () ->
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
        assertThrows(ParserException.class, () ->
            startNode.insertAdjacentHTML("afterend", "<!-- beforebegin -->"));
    }

    @Test()
    void wrongPosition() {
        Element startNode = (Element) parser.parseFragment(new Element("div"), "<div><p id=myid>foo</p></div>").get(0);
        assertThrows(ParserException.class, () ->
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
        assertThrows(ParserException.class, () ->
            startNode.insertAdjacentElement("plop", new Element("plop")));
    }

    @Test
    void wrongTextPosition() {
        Element startNode = (Element) parser.parseFragment(new Element("div"), "<div><p id=myid>foo</p></div>").get(0);
        assertThrows(ParserException.class, () ->
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
    void insertAdjacentHTMLAfterBeginOnEmptyElement() {
        Element empty = new Element("div");
        empty.insertAdjacentHTML("afterbegin", "<h1>1</h1><h2>2</h2>");
        assertEquals("<div><h1>1</h1><h2>2</h2></div>", empty.getOuterHTML());
    }

    @Test
    void insertAdjacentElementAfterBeginOnEmptyElement() {
        Element empty = new Element("div");
        empty.insertAdjacentElement("afterbegin", new Element("span"));
        assertEquals("<div><span></span></div>", empty.getOuterHTML());
    }

    @Test
    void insertAdjacentElementAfterEndReparent() {
        Document doc = parser.parse("<div id=a><span id=s></span></div><div id=b></div>");
        Element a = doc.getElementById("a");
        Element b = doc.getElementById("b");
        Element s = doc.getElementById("s");
        b.insertAdjacentElement("afterend", s);
        assertEquals("<div id=\"a\"></div>", a.getOuterHTML());
        assertEquals(0, b.getChildCount());
        assertSame(b.getParentNode(), s.getParentNode());
    }

    @Test
    void insertAdjacentElementSameParent() {
        Element startNode = (Element) parser.parseFragment(new Element("div"), "<div><a></a><b></b><c></c></div>").get(0);
        Element a = (Element) startNode.getChildNodes().get(0);
        Element b = (Element) startNode.getChildNodes().get(1);
        a.insertAdjacentElement("beforebegin", b);
        assertEquals("<div><b></b><a></a><c></c></div>", startNode.getOuterHTML());
    }

    @Test
    void insertAdjacentHTMLNonElementParent() {
        Document doc = parser.parse("<html><body></body></html>");
        Element html = doc.getDocumentElement();
        assertThrows(ParserException.class, () -> html.insertAdjacentHTML("beforebegin", "<div></div>"));
        assertThrows(ParserException.class, () -> html.insertAdjacentHTML("afterend", "<div></div>"));
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
        assertNull(div.getAttributes().getNamedItem("test2"));
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

    @Test
    void htmlElementNameIsNormalizedToLowerCase() {
        Element e = new Element("DIV");
        assertEquals("div", e.getNodeName());
        assertEquals("DIV", e.getTagName());
        assertEquals("<div></div>", e.getOuterHTML());
        assertTrue(e.matches("div"));
    }

    @Test
    void htmlAttributeLookupIsCaseInsensitive() {
        // browser: getAttribute/hasAttribute and NamedNodeMap lower-case the name for HTML elements
        Element div = (Element) parser.parseFragment(new Element("div"), "<div CLASS=\"x\" data-Foo=\"b\"></div>").get(0);
        assertEquals("x", div.getAttribute("CLASS"));
        assertEquals("x", div.getAttribute("class"));
        assertEquals("b", div.getAttribute("DATA-FOO"));
        assertEquals("x", div.getAttributeValue("CLASS"));
        assertTrue(div.hasAttribute("cLaSs"));
        assertEquals("class", div.getAttributeNode("CLASS").getName());

        // NamedNodeMap behaviour
        assertEquals("x", div.getAttributes().getNamedItem("CLASS"));
        assertTrue(div.getAttributes().containsKey("CLASS"));
        assertNotNull(div.getAttributes().get("CLASS"));
        div.getAttributes().remove("CLASS");
        assertNull(div.getAttribute("class"));
    }

    @Test
    void htmlLazilyCreatedAttributesKeepHtmlCaseInsensitivity() {
        // getAttributes()/setAttribute must mark the collection as belonging to an HTML element
        Element div = new Element("div");
        div.setAttribute("CLASS", "x");
        assertEquals("x", div.getAttributes().getNamedItem("CLASS"));
    }

    @Test
    void nonHtmlAttributeLookupIsCaseSensitive() {
        // browser: SVG/MathML attributes are case sensitive
        Element svg = (Element) parser.parseFragment(new Element("div"), "<svg viewBox=\"0 0 1 1\" CLASS=\"y\"></svg>").get(0);
        assertEquals("0 0 1 1", svg.getAttribute("viewBox"));
        assertNull(svg.getAttribute("viewbox"));
        // the tokenizer lower-cases parsed attribute names, so CLASS is stored as class
        assertEquals("y", svg.getAttribute("class"));
        assertNull(svg.getAttribute("CLASS"));
        assertNull(svg.getAttributes().getNamedItem("viewbox"));
        assertEquals("0 0 1 1", svg.getAttributes().getNamedItem("viewBox"));
        assertNull(svg.getAttributes().get("viewbox"));
    }

    @Test
    void nonHtmlCreatedAttributeIsCaseSensitive() {
        Element svg = new Element("svg", Node.NAMESPACE_SVG);
        svg.setAttribute("CLASS", "q");
        assertEquals("q", svg.getAttribute("CLASS"));
        assertNull(svg.getAttribute("class"));
    }

    @Test
    void setAttributeNodeLowercasesForHtml() {
        // browser: an attribute node without a namespace set on an HTML element
        // behaves like document.createAttribute("CLASS"), i.e. it is lower-cased
        Element div = new Element("div");
        div.setAttributeNode(new AttributeNode("CLASS", "x"));
        assertEquals(Set.of("class"), AttributesUtils.keySet(div.getAttributes()));
        assertEquals("x", div.getAttribute("CLASS"));
        assertEquals("x", div.getAttribute("class"));
    }

    @Test
    void setAttributeNodeDoesNotLowercaseNamespacedAttributes() {
        // browser: setAttributeNode keeps the case of namespaced attributes
        Element div = new Element("div");
        div.setAttributeNode(AttributeNode.createWithNamespace("urn:ns", "CLASS", "x"));
        assertEquals(Set.of("CLASS"), AttributesUtils.keySet(div.getAttributes()));
        assertEquals("x", div.getAttributeNS("urn:ns", "CLASS"));
        assertNull(div.getAttributeNS("urn:ns", "class"));
        assertNull(div.getAttribute("class"));
    }

    @Test
    void setAttributeNodeReplacesExistingCaseInsensitivelyForHtml() {
        // browser: the lower-cased name matches an existing attribute, so it is replaced
        Element div = new Element("div");
        div.setAttribute("class", "foo");

        var old = div.setAttributeNode(new AttributeNode("CLASS", "bar"));

        assertNotNull(old);
        assertEquals("foo", old.getValue());
        assertEquals("bar", div.getAttribute("class"));
        assertEquals(1, AttributesUtils.size(div.getAttributes()));
    }

    @Test
    void setAttributeNSDoesNotLowercaseForHtml() {
        // browser: setAttributeNS stores the qualified name as provided, even on HTML elements
        Element div = new Element("div");
        div.setAttributeNS(null, "CLASS", "ns");
        assertNull(div.getAttribute("class"));
        assertNull(div.getAttribute("CLASS"));
        assertEquals("ns", div.getAttributeNS(null, "CLASS"));
        assertEquals("ns", div.getAttributes().getNamedItemNS(null, "CLASS"));

        // the null-namespace attribute can coexist with the one created by setAttribute
        div.setAttribute("CLASS", "plain");
        assertEquals(Set.of("CLASS", "class"), AttributesUtils.keySet(div.getAttributes()));
        assertEquals("plain", div.getAttribute("CLASS"));
        assertEquals("ns", div.getAttributeNS(null, "CLASS"));
    }

    @Test
    void namespacedAttributeLookupIsCaseSensitive() {
        // browser: getAttributeNS/getNamedItemNS are always case sensitive
        Element div = new Element("div");
        div.setAttributeNS("urn:ns", "Foo", "v");
        assertEquals("v", div.getAttributeNS("urn:ns", "Foo"));
        assertNull(div.getAttributeNS("urn:ns", "foo"));
        assertEquals("v", div.getAttributeNodeNS("urn:ns", "Foo").getValue());
        assertTrue(div.hasAttributeNS("urn:ns", "Foo"));
        assertFalse(div.hasAttributeNS("urn:ns", "foo"));
        // getAttribute lower-cases the name, so an upper-cased local name is not reachable
        assertNull(div.getAttribute("Foo"));
        assertNull(div.getAttribute("foo"));
    }

    @Test
    void setAttributeNSWithPrefixAndNullNamespaceThrows() {
        // browser: setAttributeNS(null, "P:FOO", ...) throws a NamespaceError
        Element div = new Element("div");
        assertThrows(ParserException.class, () -> div.setAttributeNS(null, "P:FOO", "v"));
        assertThrows(ParserException.class, () -> div.setAttributeNS("", "P:FOO", "v"));
        assertThrows(ParserException.class, () -> new AttributeNode("P:FOO", "v", "P", null));
    }

    @Test
    void setAttributeMatchesByQualifiedNameIgnoringNamespace() {
        // browser: setAttribute looks the attribute up by qualified name only,
        // even when the attribute belongs to a namespace, and changes only its
        // value (the namespace/prefix are preserved).
        Element div = new Element("div");
        div.setAttributeNS("urn:ns", "bar", "ns");
        div.setAttribute("bar", "plain");

        assertEquals(1, AttributesUtils.size(div.getAttributes()));
        AttributeNode attr = div.getAttributeNode("bar");
        assertEquals("plain", attr.getValue());
        assertEquals("urn:ns", attr.getNamespace());
        assertEquals("bar", attr.getLocalName());
        assertEquals("plain", div.getAttribute("bar"));
        assertEquals("plain", div.getAttributeNS("urn:ns", "bar"));
        assertNull(div.getAttributeNS(null, "bar"));
    }

    @Test
    void setAttributeDoesNotMatchOnLocalNameOnly() {
        // browser: the prefixed attribute has a different qualified name, so
        // setAttribute adds a second (namespace-less) attribute.
        Element div = new Element("div");
        div.setAttributeNS("urn:ns", "xlink:bar", "ns");
        div.setAttribute("bar", "plain");

        assertEquals(2, AttributesUtils.size(div.getAttributes()));
        assertEquals("ns", div.getAttributeNS("urn:ns", "bar"));
        assertEquals("plain", div.getAttributeNS(null, "bar"));
    }

    @Test
    void setAttributeNSMatchesByNamespaceAndLocalNamePreservingQualifiedName() {
        // browser: setAttributeNS looks the attribute up by namespace + local
        // name and changes only its value, keeping the existing qualified
        // name/prefix.
        Element div = new Element("div");
        div.setAttributeNS("urn:ns", "xlink:href", "a");
        div.setAttributeNS("urn:ns", "other:href", "b");

        assertEquals(1, AttributesUtils.size(div.getAttributes()));
        AttributeNode attr = div.getAttributeNodeNS("urn:ns", "href");
        assertEquals("b", attr.getValue());
        assertEquals("xlink:href", attr.getName());
        assertEquals("xlink", attr.getPrefix());
        assertEquals("b", div.getAttribute("xlink:href"));
        assertNull(div.getAttribute("other:href"));
    }

    @Test
    void setAttributeNSDoesNotMatchNamespaceLessAttribute() {
        // browser: setAttributeNS matches on namespace too, so the namespace-less
        // attribute is not touched.
        Element div = new Element("div");
        div.setAttribute("foo", "plain");
        div.setAttributeNS("urn:ns", "foo", "ns");

        assertEquals(2, AttributesUtils.size(div.getAttributes()));
        assertEquals("plain", div.getAttributeNS(null, "foo"));
        assertEquals("ns", div.getAttributeNS("urn:ns", "foo"));
    }

    @Test
    void setAttributeNodeReplacesByNamespaceAndLocalNameInPlace() {
        // browser: setAttributeNode matches by namespace + local name, replaces
        // the attribute without changing its position and returns the previous
        // node.
        Element div = new Element("div");
        div.setAttribute("a", "1");
        div.setAttributeNS("urn:ns", "xlink:href", "old");
        div.setAttribute("b", "2");

        var replacement = AttributeNode.createWithNamespace("urn:ns", "other:href", "new");
        var old = div.setAttributeNode(replacement);

        assertEquals("old", old.getValue());
        assertSame(replacement, div.getAttributeNodeNS("urn:ns", "href"));
        assertEquals(3, AttributesUtils.size(div.getAttributes()));

        var names = new java.util.ArrayList<String>();
        div.getAttributes().forEach(attr -> names.add(attr.getName()));
        assertEquals(List.of("a", "other:href", "b"), names);
    }

    @Test
    void setAttributeNodeDoesNotReplaceOnLocalNameOnly() {
        // browser: setAttributeNode needs the namespace to match, so the
        // namespace-less attribute is appended.
        Element div = new Element("div");
        div.setAttributeNS("urn:ns", "bar", "ns");

        var old = div.setAttributeNode(new AttributeNode("bar", "plain"));

        assertNull(old);
        assertEquals(2, AttributesUtils.size(div.getAttributes()));
        assertEquals("ns", div.getAttributeNS("urn:ns", "bar"));
        assertEquals("plain", div.getAttributeNS(null, "bar"));
    }

    @Test
    void removeAttributeMatchesByQualifiedNameIgnoringNamespace() {
        // browser: removeAttribute removes the first attribute matching the
        // qualified name, regardless of its namespace.
        Element div = new Element("div");
        div.setAttributeNS("urn:ns", "bar", "ns");
        div.setAttributeNode(new AttributeNode("bar", "plain"));

        div.removeAttribute("bar");

        assertEquals(1, AttributesUtils.size(div.getAttributes()));
        assertNull(div.getAttributeNS("urn:ns", "bar"));
        assertEquals("plain", div.getAttributeNS(null, "bar"));
    }
}
