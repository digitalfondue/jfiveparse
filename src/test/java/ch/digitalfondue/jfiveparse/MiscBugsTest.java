package ch.digitalfondue.jfiveparse;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class MiscBugsTest {

    // case 11
    @Test
    void preNullTextNodeSerialize() {
        Element pre = new Element("pre");
        pre.appendChild(new Text((String) null));

        // chrome generate like that
        Assertions.assertEquals("<pre>null</pre>", HtmlSerializer.serialize(pre));


        Element pre2 = new Element("pre");
        pre2.appendChild(new Text(""));
        Assertions.assertEquals("<pre></pre>", HtmlSerializer.serialize(pre2));
    }

    // case 1
    @Test
    void addMissingDocumentedSelectorMethods() {
        // div + p
        NodeMatcher<Node> matcher = Selector.select()
                .element("div")
                .nextSibling()
                .element("p")
                .toMatcher();

        // div ~ p
        NodeMatcher<Node> matcher2 = Selector.select()
                .element("div")
                .subsequentSibling()
                .element("p")
                .toMatcher();

        var doc = JFiveParse.parse("""
                <div id="d1"></div>
                <p id="p1"></p>
                <span id="s1"></span>
                <p id="p2"></p>
                <div id="d2"></div>
                <p id="p3"></p>
                """);

        var nextSiblingMatches = doc.getAllNodesMatching(matcher);
        Assertions.assertEquals(2, nextSiblingMatches.size());
        Assertions.assertEquals("p1", ((Element) nextSiblingMatches.get(0)).getAttribute("id"));
        Assertions.assertEquals("p3", ((Element) nextSiblingMatches.get(1)).getAttribute("id"));

        var subsequentSiblingMatches = doc.getAllNodesMatching(matcher2);
        Assertions.assertEquals(3, subsequentSiblingMatches.size());
        Assertions.assertEquals("p1", ((Element) subsequentSiblingMatches.get(0)).getAttribute("id"));
        Assertions.assertEquals("p2", ((Element) subsequentSiblingMatches.get(1)).getAttribute("id"));
        Assertions.assertEquals("p3", ((Element) subsequentSiblingMatches.get(2)).getAttribute("id"));
    }

    // case 2
    @Test
    void testItemOutOfBoundsShouldReturnNull() {
        Element el = new Element("div");
        el.setAttribute("class", "plop coin");
        DOMTokenList list = el.getClassList();


        Assertions.assertNull(list.item(-1));
        Assertions.assertEquals("plop", list.item(0));
        Assertions.assertEquals("coin", list.item(1));
        Assertions.assertNull(list.item(2));
        Assertions.assertNull(list.item(3));
    }


    // case 3
    // https://developer.mozilla.org/en-US/docs/Web/API/DOMTokenList#trimming_of_whitespace_and_removal_of_duplicates
    @Test
    void testRemoveDuplicateTokens() {
        Element el = new Element("div");
        el.setAttribute("class", "    d   d e f");
        DOMTokenList list = el.getClassList();

        // it's already deduplicated
        Assertions.assertEquals(3, list.size());

        list.remove("d");

        Assertions.assertFalse(list.contains("d"));
        Assertions.assertEquals("e f", el.getAttribute("class"));
    }


    // case 8
    @Test
    void testGetElementsByTagNameWildcard() {
        Document doc = JFiveParse.parse("<div><p><span></span></p></div>");

        var all = doc.getElementsByTagName("*");
        Assertions.assertEquals(List.of("html", "head", "body", "div", "p", "span"), all.stream().map(Node::getNodeName).toList());
    }

    // case 14
    @Test
    void testToW3CDocumentPrefixedAttributeThrowsNamespaceErr() {
        Document doc = JFiveParse.parse("<div id=test xml:lang=\"en\" plop:coin=\"yoo\"><svg id=yolo xml:lang=\"en\" plop:coin=\"yoo\"></svg></div>");

        // behaviour checked against chrome...
        var w3cDoc = W3CDom.toW3CDocument(doc);
        var div = w3cDoc.getElementsByTagName("div").item(0);
        // like in a browser, no namespace in html context
        Assertions.assertNull(div.getAttributes().getNamedItem("id").getNamespaceURI());
        Assertions.assertNull(div.getAttributes().getNamedItem("xml:lang").getNamespaceURI());
        Assertions.assertNull(div.getAttributes().getNamedItem("plop:coin").getNamespaceURI());
        //

        var svg = w3cDoc.getElementsByTagName("svg").item(0);
        Assertions.assertNull(svg.getAttributes().getNamedItem("id").getNamespaceURI());
        Assertions.assertEquals(Node.NAMESPACE_XML, svg.getAttributes().getNamedItem("xml:lang").getNamespaceURI()); // in svg & co, we assign the namespace
        Assertions.assertNull(svg.getAttributes().getNamedItem("plop:coin").getNamespaceURI());
    }


    // case p-1
    @Test
    void checkProcessingInstructionsMatcher() {
        Document doc = JFiveParse.parse(
                "<!DOCTYPE html><html><head></head><body></body></html><?div hello?>");

        Assertions.assertTrue(doc.getAllNodesMatching(Selector.select().element("div").toMatcher()).isEmpty());

        Assertions.assertTrue(doc.getElementsByTagName("div").isEmpty());
    }

    // case 13 / p-11
    @Test
    void testHtmlSerializerDropsDoctypeIdentifiers() {
        String html = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\"><html><body></body></html>";
        Document doc = JFiveParse.parse(html);
        Assertions.assertEquals("html", doc.getDoctype().getName());
        Assertions.assertEquals("-//W3C//DTD HTML 4.01//EN", doc.getDoctype().getPublicId());
        Assertions.assertEquals("http://www.w3.org/TR/html4/strict.dtd", doc.getDoctype().getSystemId());
        String serialized = HtmlSerializer.serialize(doc);
        Assertions.assertEquals("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\"><html><head></head><body></body></html>", serialized);
    }


    // case p-2
    @Test
    void w3cDomDocumentMissingDoctype() {
        var doc = JFiveParse.parse(
                "<!DOCTYPE html><html><head></head><body></body></html>");
        var w3c = W3CDom.toW3CDocument(doc);
        Assertions.assertNotNull(w3c.getDoctype());
        Assertions.assertEquals("html", w3c.getDoctype().getName());
        Assertions.assertEquals("", w3c.getDoctype().getPublicId());
        Assertions.assertEquals("", w3c.getDoctype().getSystemId());

        String html = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\"><html><body></body></html>";
        doc = JFiveParse.parse(html);
        w3c = W3CDom.toW3CDocument(doc);
        Assertions.assertEquals("html", w3c.getDoctype().getName());
        Assertions.assertEquals("-//W3C//DTD HTML 4.01//EN", w3c.getDoctype().getPublicId());
        Assertions.assertEquals("http://www.w3.org/TR/html4/strict.dtd", w3c.getDoctype().getSystemId());
    }

    // case 5 + p 3 (part)
    @Test
    void testClonedDoctypeHandling() {
        Document doc = JFiveParse.parse("<!DOCTYPE html><html><body></body></html>");
        Document clonedShallow = (Document) doc.cloneNode(false);
        Assertions.assertNull(clonedShallow.getDoctype());
        Assertions.assertTrue(clonedShallow.getChildNodes().isEmpty());

        Document cloned = (Document) doc.cloneNode(true);

        DocumentType dt = cloned.getDoctype();
        Assertions.assertNotNull(dt);

        Assertions.assertSame(dt, cloned.getChildNodes().get(0));

        Assertions.assertNull(dt.getPreviousSibling());
    }


    @Test
    void testHtmlSerializerSystemDoctype() {
        String html = "<!DOCTYPE html SYSTEM \"about:legacy-compat\"><html><head></head><body></body></html>";
        Document doc = JFiveParse.parse(html);
        Assertions.assertEquals("about:legacy-compat", doc.getDoctype().getSystemId());
        String serialized = HtmlSerializer.serialize(doc);
        Assertions.assertEquals("<!DOCTYPE html SYSTEM \"about:legacy-compat\"><html><head></head><body></body></html>", serialized);
    }

    @Test
    void testW3CDomToDocumentWithNullDocumentElement() {
        Document doc = new Document();
        var d = Assertions.assertDoesNotThrow(() -> W3CDom.toW3CDocument(doc));
        Assertions.assertNull(d.getDocumentElement());
    }


    @Test
    void testMatchesHasSiblingOnDetachedElement() {
        Element div = new Element("div");
        Assertions.assertFalse(div.matches(":has(+ p)"));
        Assertions.assertFalse(div.matches(":has(~ p)"));
    }
}
