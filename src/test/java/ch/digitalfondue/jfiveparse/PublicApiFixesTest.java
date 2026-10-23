/**
 * Copyright © 2026 digitalfondue (info@digitalfondue.ch)
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

/**
 * Regression tests for the Node/Element public API fixes.
 */
class PublicApiFixesTest {

    final Parser parser = new Parser();

    private Element frag(String html) {
        return (Element) JFiveParse.parseFragment(html).get(0);
    }

    @Test
    void deepCloneDoctypeIsSameInstanceAsChild() {
        Document original = parser.parse("<!DOCTYPE html><div>Hello</div>");
        Document cloned = (Document) original.cloneNode(true);

        assertSame(cloned.getDoctype(), cloned.getChildNodes().get(0));
        assertNull(cloned.getDoctype().getPreviousSibling());
        assertEquals("html", cloned.getDoctype().getNextSibling().getNodeName());
        assertEquals(HtmlSerializer.serialize(original), HtmlSerializer.serialize(cloned));
    }

    @Test
    void removingDoctypeClearsDocumentDoctype() {
        Document doc = parser.parse("<!DOCTYPE html><div>Hello</div>");
        DocumentType doctype = doc.getDoctype();
        doc.removeChild(doctype);
        assertNull(doc.getDoctype());
    }

    @Test
    void emptyingDocumentClearsDoctype() {
        Document doc = parser.parse("<!DOCTYPE html><div>Hello</div>");
        doc.empty();
        assertNull(doc.getDoctype());
        assertEquals(0, doc.getChildCount());
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
    void foreignElementNameKeepsCase() {
        Element svg = new Element("foreignObject", Node.NAMESPACE_SVG);
        assertEquals("foreignObject", svg.getNodeName());
    }

    @Test
    void insertBeforeWithNullAppends() {
        Element e = frag("<div><a></a></div>");
        Element b = new Element("b");
        e.insertBefore(b, null);
        assertEquals("<div><a></a><b></b></div>", e.getOuterHTML());
        assertSame(e, b.getParentNode());
    }

    @Test
    void insertingSelfIsRejected() {
        Element e = frag("<div><a></a></div>");
        assertThrows(ParserException.class, () -> e.insertChildren(0, e));
        assertThrows(ParserException.class, () -> e.appendChild(e));
    }

    @Test
    void replacingWithAncestorIsRejected() {
        Element root = frag("<div><span><b></b></span></div>");
        Element span = (Element) root.getChildNodes().get(0);
        Element b = (Element) span.getChildNodes().get(0);
        assertThrows(ParserException.class, () -> span.replaceChild(root, b));
    }

    @Test
    void attributeLookupIsCaseInsensitiveForHtml() {
        Element e = new Element("div");
        e.setAttribute("DATA-Foo", "bar");
        assertTrue(e.containsAttribute("DATA-FOO"));
        assertTrue(e.hasAttribute("DATA-FOO"));
        assertEquals("bar", e.getAttribute("DATA-FOO"));
        assertEquals("bar", e.getAttributeValue("DATA-FOO"));
    }

    @Test
    void setTextContentNullClearsCharacterData() {
        Text text = new Text("x");
        text.setTextContent(null);
        assertEquals("", text.getTextContent());

        Comment comment = new Comment("x");
        comment.setTextContent(null);
        assertEquals("", comment.getTextContent());

        ProcessingInstruction pi = new ProcessingInstruction("target", "x");
        pi.setTextContent(null);
        assertEquals("", pi.getTextContent());
    }
}
