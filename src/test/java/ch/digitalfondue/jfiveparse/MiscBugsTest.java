package ch.digitalfondue.jfiveparse;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.w3c.dom.Comment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class MiscBugsTest {

    // case 11
    @Test
    void preNullTextNodeSerialize() {
        Element pre = new Element("pre");
        pre.appendChild(new Text((String) null));

        // chrome generate like that
        assertEquals("<pre>null</pre>", HtmlSerializer.serialize(pre));


        Element pre2 = new Element("pre");
        pre2.appendChild(new Text(""));
        assertEquals("<pre></pre>", HtmlSerializer.serialize(pre2));
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
        assertEquals(2, nextSiblingMatches.size());
        assertEquals("p1", ((Element) nextSiblingMatches.get(0)).getAttribute("id"));
        assertEquals("p3", ((Element) nextSiblingMatches.get(1)).getAttribute("id"));

        var subsequentSiblingMatches = doc.getAllNodesMatching(matcher2);
        assertEquals(3, subsequentSiblingMatches.size());
        assertEquals("p1", ((Element) subsequentSiblingMatches.get(0)).getAttribute("id"));
        assertEquals("p2", ((Element) subsequentSiblingMatches.get(1)).getAttribute("id"));
        assertEquals("p3", ((Element) subsequentSiblingMatches.get(2)).getAttribute("id"));
    }

    // case 2
    @Test
    void testItemOutOfBoundsShouldReturnNull() {
        Element el = new Element("div");
        el.setAttribute("class", "plop coin");
        DOMTokenList list = el.getClassList();


        assertNull(list.item(-1));
        assertEquals("plop", list.item(0));
        assertEquals("coin", list.item(1));
        assertNull(list.item(2));
        assertNull(list.item(3));
    }


    // case 3
    // https://developer.mozilla.org/en-US/docs/Web/API/DOMTokenList#trimming_of_whitespace_and_removal_of_duplicates
    @Test
    void testRemoveDuplicateTokens() {
        Element el = new Element("div");
        el.setAttribute("class", "    d   d e f");
        DOMTokenList list = el.getClassList();

        // it's already deduplicated
        assertEquals(3, list.size());

        list.remove("d");

        Assertions.assertFalse(list.contains("d"));
        assertEquals("e f", el.getAttribute("class"));
    }


    // case 8
    @Test
    void testGetElementsByTagNameWildcard() {
        Document doc = JFiveParse.parse("<div><p><span></span></p></div>");

        var all = doc.getElementsByTagName("*");
        assertEquals(List.of("html", "head", "body", "div", "p", "span"), all.stream().map(Node::getNodeName).toList());
    }

    // case 14
    @Test
    void testToW3CDocumentPrefixedAttributeThrowsNamespaceErr() {
        Document doc = JFiveParse.parse("<div id=test xml:lang=\"en\" plop:coin=\"yoo\"><svg id=yolo xml:lang=\"en\" plop:coin=\"yoo\"></svg></div>");

        // behaviour checked against chrome...
        var w3cDoc = W3CDom.toW3CDocument(doc);
        var div = w3cDoc.getElementsByTagName("div").item(0);
        // like in a browser, no namespace in html context
        assertNull(div.getAttributes().getNamedItem("id").getNamespaceURI());
        assertNull(div.getAttributes().getNamedItem("xml:lang").getNamespaceURI());
        assertNull(div.getAttributes().getNamedItem("plop:coin").getNamespaceURI());
        //

        var svg = w3cDoc.getElementsByTagName("svg").item(0);
        assertNull(svg.getAttributes().getNamedItem("id").getNamespaceURI());
        assertEquals(Node.NAMESPACE_XML, svg.getAttributes().getNamedItem("xml:lang").getNamespaceURI()); // in svg & co, we assign the namespace
        assertNull(svg.getAttributes().getNamedItem("plop:coin").getNamespaceURI());
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
        assertEquals("html", doc.getDoctype().getName());
        assertEquals("-//W3C//DTD HTML 4.01//EN", doc.getDoctype().getPublicId());
        assertEquals("http://www.w3.org/TR/html4/strict.dtd", doc.getDoctype().getSystemId());
        String serialized = HtmlSerializer.serialize(doc);
        assertEquals("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\"><html><head></head><body></body></html>", serialized);
    }


    // case p-2
    @Test
    void w3cDomDocumentMissingDoctype() {
        var doc = JFiveParse.parse(
                "<!DOCTYPE html><html><head></head><body></body></html>");
        var w3c = W3CDom.toW3CDocument(doc);
        assertNotNull(w3c.getDoctype());
        assertEquals("html", w3c.getDoctype().getName());
        assertEquals("", w3c.getDoctype().getPublicId());
        assertEquals("", w3c.getDoctype().getSystemId());

        String html = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\"><html><body></body></html>";
        doc = JFiveParse.parse(html);
        w3c = W3CDom.toW3CDocument(doc);
        assertEquals("html", w3c.getDoctype().getName());
        assertEquals("-//W3C//DTD HTML 4.01//EN", w3c.getDoctype().getPublicId());
        assertEquals("http://www.w3.org/TR/html4/strict.dtd", w3c.getDoctype().getSystemId());
    }

    // case 5 + p 3 (part)
    @Test
    void testClonedDoctypeHandling() {
        Document doc = JFiveParse.parse("<!DOCTYPE html><html><body></body></html>");
        Document clonedShallow = (Document) doc.cloneNode(false);
        assertNull(clonedShallow.getDoctype());
        Assertions.assertTrue(clonedShallow.getChildNodes().isEmpty());

        Document cloned = (Document) doc.cloneNode(true);

        DocumentType dt = cloned.getDoctype();
        assertNotNull(dt);

        Assertions.assertSame(dt, cloned.getChildNodes().get(0));

        assertNull(dt.getPreviousSibling());
    }


    @Test
    void testHtmlSerializerSystemDoctype() {
        String html = "<!DOCTYPE html SYSTEM \"about:legacy-compat\"><html><head></head><body></body></html>";
        Document doc = JFiveParse.parse(html);
        assertEquals("about:legacy-compat", doc.getDoctype().getSystemId());
        String serialized = HtmlSerializer.serialize(doc);
        assertEquals("<!DOCTYPE html SYSTEM \"about:legacy-compat\"><html><head></head><body></body></html>", serialized);
    }

    @Test
    void testW3CDomToDocumentWithNullDocumentElement() {
        Document doc = new Document();
        var d = Assertions.assertDoesNotThrow(() -> W3CDom.toW3CDocument(doc));
        assertNull(d.getDocumentElement());
    }


    @Test
    void testMatchesHasSiblingOnDetachedElement() {
        Element div = new Element("div");
        Assertions.assertFalse(div.matches(":has(+ p)"));
        Assertions.assertFalse(div.matches(":has(~ p)"));
    }

    @Test
    @Timeout(value = 2, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    void testCycle() {
        // self cycle
        var div = new Element("div");
        Assertions.assertThrows(IllegalStateException.class, () -> div.appendChild(div));
        assertEquals("<div></div>", JFiveParse.serialize(div));

        // root -> span -> root
        var root = new Element("div");
        var span = new Element("span");
        var b = new Element("b");
        root.appendChild(span);
        span.appendChild(b);
        Assertions.assertThrows(IllegalStateException.class, () -> span.replaceChild(root, b)); // cycle: root -> span -> root
        assertEquals("<div><span><b></b></span></div>", JFiveParse.serialize(root));
    }

    @Test
    void testInsertChildrenAtIndexFailure() {
        Element p1 = new Element("div");
        Element p2 = new Element("div");
        Element c  = new Element("span");
        p1.appendChild(c);
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> p2.insertChildren(5, c));

        // when failing the insert, c should not have a parent node set
        assertEquals(p1, c.getParentNode());
        assertEquals(1, p1.getChildCount());
        assertEquals(0, p2.getChildCount());
    }

    @Test
    void testInsertBeforeNull() {
        Element div = new Element("div");
        Element b = new Element("b");
        Element a = new Element("a");
        div.insertBefore(b, null);
        div.insertBefore(a, null);
        assertEquals("<div><b></b><a></a></div>", JFiveParse.serialize(div));
    }

    @Test
    void testW3CDocumentWithTopLevelComments() {
        var doc = JFiveParse.parse("<!--before--><html></html><!--after-->");

        var w3c = W3CDom.toW3CDocument(doc);
        assertEquals(3, w3c.getChildNodes().getLength());
        Assertions.assertInstanceOf(Comment.class, w3c.getChildNodes().item(0));
        Assertions.assertInstanceOf(org.w3c.dom.Element.class, w3c.getChildNodes().item(1));
        Assertions.assertInstanceOf(Comment.class, w3c.getChildNodes().item(2));
    }

    @Test
    void testNamespacedSelector() {
        var doc = JFiveParse.parse("<div><svg><rect id=rect></rect></svg></div>");
        var m = Selector.parseSelector(
                "svg|rect",
                Map.of("svg", Node.NAMESPACE_SVG));
        assertEquals(1, doc.getAllNodesMatching(m).size());

        org.w3c.dom.Document w3c = W3CDom.toW3CDocument(doc);
        var m2 = W3CDom.W3CDomSelector.parseSelector(
                "svg|rect",
                Map.of("svg", Node.NAMESPACE_SVG));
        assertEquals(1, W3CDom.getAllNodesMatching(w3c, m2).count());
    }

    @Test
    void testCompleteVisitor() {
        // we expect our visitor to end asap
        var root = new Element("div");
        root.appendChild(new Element("span"));
        List<String> events = new ArrayList<>();
        root.traverseWithCurrentNode(new NodesVisitor() {
            public void start(Node n) { events.add("start:" + n.getNodeName()); }
            public void end(Node n)   { events.add("end:" + n.getNodeName()); }
            public boolean complete() { return !events.isEmpty(); }
        });
        assertEquals(List.of("start:div"), events);
    }

    @Test
    void checkEmptySelector() {
        Element e = new Element("div");
        e.appendChild(new Text(""));
        Assertions.assertTrue(e.matches(":empty"));
    }


    @Test
    void checkSetBody() {
        Document d = new Document();
        d.appendChild(new Element("html"));

        var b = new Element("body");
        d.setBody(b);
        assertEquals(b, d.getBody());

        Assertions.assertThrows(IllegalArgumentException.class, () -> d.setBody(new Element("div")));
    }

    @Test
    void normalizeKeepFirstTextNodeInstance() {
        // current bug: we always replace the text node instance instead of updating the content of the first one...
        Element e = new Element("div");
        Text t = new Text("a");
        e.appendChild(t);
        e.normalize();
        Assertions.assertSame(t, e.getFirstChild());

        e.appendChild(new Text("b"));

        e.normalize();
        Assertions.assertSame(t, e.getFirstChild());
        assertEquals("ab", t.getData());
    }

    @Test
    void createIdAttributeW3CDom() {
        Document doc = JFiveParse.parse(
                "<div><svg><a id=a xlink:href=a></a><b id=b href=b></b></svg></div>");
        var w3doc = W3CDom.toW3CDocument(doc);
        assertNotNull(w3doc.getElementById("a"));
        assertNotNull(w3doc.getElementById("b"));
    }

    @Test
    void checkNamespaceSelector() {
        Document doc = JFiveParse.parse(
                "<div><svg><a id=a xlink:href=a></a><b id=b href=b></b></svg></div>");

        var a = doc.getElementById("a");
        var b = doc.getElementById("b");

        var m1 = Selector.parseSelector("[*|href]", Map.of("xlink", Node.NAMESPACE_XLINK));
        var m2 = Selector.parseSelector("[xlink|href]", Map.of("xlink", Node.NAMESPACE_XLINK));
        var m3 = Selector.parseSelector("[|href]", Map.of("xlink", Node.NAMESPACE_XLINK));

        assertEquals(List.of(a, b), doc.getAllNodesMatching(m1));
        assertEquals(List.of(a), doc.getAllNodesMatching(m2));
        assertEquals(List.of(b), doc.getAllNodesMatching(m3));
        
        var w3doc = W3CDom.toW3CDocument(doc);
        var w3a = w3doc.getElementById("a");
        var w3b = w3doc.getElementById("b");

        var w3m1 = W3CDom.W3CDomSelector.parseSelector("[*|href]", Map.of("xlink", Node.NAMESPACE_XLINK));
        var w3m2 = W3CDom.W3CDomSelector.parseSelector("[xlink|href]", Map.of("xlink", Node.NAMESPACE_XLINK));
        var w3m3 = W3CDom.W3CDomSelector.parseSelector("[|href]", Map.of("xlink", Node.NAMESPACE_XLINK));

        assertEquals(List.of(w3a, w3b), W3CDom.getAllNodesMatching(w3doc, w3m1).toList());
        assertEquals(List.of(w3a), W3CDom.getAllNodesMatching(w3doc, w3m2).toList());
        assertEquals(List.of(w3b), W3CDom.getAllNodesMatching(w3doc, w3m3).toList());
    }

    @Test
    void cssUniversalSelectorUnknownNamespace() {
        Document doc = JFiveParse.parse("<div></div>");
        assertEquals(0, doc.getAllNodesMatching(Selector.parseSelector("svg|*")).size());
        assertEquals(0, doc.getAllNodesMatching(Selector.parseSelector("|*")).size());
    }


    // https://www.w3.org/TR/selectors-4/#attribute-representation
    // If "val" contains whitespace, it will never represent anything (since the words are separated by spaces).
    // Also if "val" is the empty string, it will never represent anything.
    @Test
    void selectorEmptyStringInList() {
        Document doc = JFiveParse.parse(
                "<div><span id=a class='x'></span><span id=b class=''></span><span id=c></span></div>");
        Assertions.assertTrue(doc.getAllNodesMatching(Selector.parseSelector("[class~='']")).isEmpty());

        Element e = new Element("div");
        e.setAttribute("class", "x");
        Assertions.assertFalse(e.matches("[class~='']"));
    }

    @Test
    void checkCaseFlagSelector() {
        var doc = JFiveParse.parse(
                "<div><span id=a data-x='VALUE'></span><span id=b data-x='value'></span></div>");

        var defaultSelector = Selector.parseSelector("[data-x=value]");
        var insensitiveLower = Selector.parseSelector("[data-x=value i]");
        var insensitiveUpper = Selector.parseSelector("[data-x=VALUE i]");

        var a = doc.getElementById("a");
        var b = doc.getElementById("b");

        assertEquals(List.of(b), doc.getAllNodesMatching(defaultSelector));
        assertEquals(List.of(a, b), doc.getAllNodesMatching(insensitiveLower));
        assertEquals(List.of(a, b), doc.getAllNodesMatching(insensitiveUpper));
    }

    @Test
    void checkCaseFlagSelectorOtherOperators() {
        var doc = JFiveParse.parse(
                "<div>" +
                        "<span id=a data-x='VALUE' class='Foo Bar'></span>" +
                        "<span id=b data-x='value' class='foo baz'></span>" +
                        "<span id=c data-x='Value-Other' class='qux'></span>" +
                        "</div>");

        var a = doc.getElementById("a");
        var b = doc.getElementById("b");
        var c = doc.getElementById("c");

        // class selector (~= on class)
        assertEquals(List.of(b), doc.getAllNodesMatching(Selector.parseSelector("[class~=foo]")));
        assertEquals(List.of(a, b), doc.getAllNodesMatching(Selector.parseSelector("[class~=foo i]")));
        assertEquals(List.of(a, b), doc.getAllNodesMatching(Selector.parseSelector("[class~=FOO i]")));

        // ~=
        assertEquals(List.of(b), doc.getAllNodesMatching(Selector.parseSelector("[data-x~=value]")));
        assertEquals(List.of(a, b), doc.getAllNodesMatching(Selector.parseSelector("[data-x~=value i]")));

        // ^=
        assertEquals(List.of(b), doc.getAllNodesMatching(Selector.parseSelector("[data-x^=val]")));
        assertEquals(List.of(a, b, c), doc.getAllNodesMatching(Selector.parseSelector("[data-x^=val i]")));

        // $=
        assertEquals(List.of(b), doc.getAllNodesMatching(Selector.parseSelector("[data-x$=ue]")));
        assertEquals(List.of(a, b), doc.getAllNodesMatching(Selector.parseSelector("[data-x$=ue i]")));

        // *=
        assertEquals(List.of(b, c), doc.getAllNodesMatching(Selector.parseSelector("[data-x*=alu]")));
        assertEquals(List.of(a, b, c), doc.getAllNodesMatching(Selector.parseSelector("[data-x*=alu i]")));

        // W3CDom implementation
        var w3doc = W3CDom.toW3CDocument(doc);
        var w3a = w3doc.getElementById("a");
        var w3b = w3doc.getElementById("b");
        var w3c = w3doc.getElementById("c");

        assertEquals(List.of(w3a, w3b), W3CDom.getAllNodesMatching(w3doc,
                W3CDom.W3CDomSelector.parseSelector("[class~=foo i]")).toList());
        assertEquals(List.of(w3a, w3b), W3CDom.getAllNodesMatching(w3doc,
                W3CDom.W3CDomSelector.parseSelector("[data-x~=value i]")).toList());
        assertEquals(List.of(w3a, w3b, w3c), W3CDom.getAllNodesMatching(w3doc,
                W3CDom.W3CDomSelector.parseSelector("[data-x^=val i]")).toList());
        assertEquals(List.of(w3a, w3b), W3CDom.getAllNodesMatching(w3doc,
                W3CDom.W3CDomSelector.parseSelector("[data-x$=ue i]")).toList());
        assertEquals(List.of(w3a, w3b, w3c), W3CDom.getAllNodesMatching(w3doc,
                W3CDom.W3CDomSelector.parseSelector("[data-x*=alu i]")).toList());
    }

    @Test
    void cssElementSelectorIsCaseInsensitiveForHtmlDocument() {
        // in a html document, chrome also is case insensitive for non html namespace
        // svg part taken from https://developer.mozilla.org/en-US/docs/Web/SVG/Reference/Element/textPath
        var doc = JFiveParse.parse("""
        <div id=x></div>
                <svg viewBox="0 0 100 100" xmlns="http://www.w3.org/2000/svg">
                  <path
                    id="MyPath"
                    fill="none"
                    stroke="red"
                    d="M10,90 Q90,90 90,45 Q90,10 50,10 Q10,10 10,40 Q10,70 45,70 Q70,70 75,50" />
                  <text>
                    <textPath id="tp" href="#MyPath">Quick brown fox jumps over the lazy dog.</textPath>
                  </text>
                </svg>
        """);
        var targetDiv = doc.getElementById("x");
        var targetTextPath = doc.getElementById("tp");
        var found = doc.getAllNodesMatching(Selector.parseSelector("DIV"));
        var textPath = doc.getAllNodesMatching(Selector.parseSelector("TEXTPATH"));
        assertEquals(1, found.size());
        Assertions.assertSame(targetDiv, found.get(0));

        assertEquals(1, textPath.size());
        Assertions.assertSame(targetTextPath, textPath.get(0));

    }


    @Test
    void testNullTextContentCommentProcessingInstruction() {
        // follow browser behaviour.

        // document.createComment(null) -> <!--null--> -> null is converted in the string 'null'
        // instead a.textContent = null -> a.textContent is '' (empty string)

        var c = new ch.digitalfondue.jfiveparse.Comment(null);
        assertEquals("null", c.getTextContent());
        assertEquals("<!--null-->", JFiveParse.serialize(c));

        c.setTextContent(null);
        assertEquals("", c.getTextContent());
        assertEquals("<!---->", JFiveParse.serialize(c));
        //
        var pi = new ProcessingInstruction("target", null);
        assertEquals("<?target null?>", JFiveParse.serialize(pi));

        assertEquals("null", pi.getTextContent());
        pi.setTextContent(null);
        assertEquals("", pi.getTextContent());
        assertEquals("<?target?>", JFiveParse.serialize(pi));
    }

    @Test
    void nthchildError() {
        Assertions.assertThrows(ParserException.class, () -> Selector.parseSelector(":nth-child()"));
        Assertions.assertThrows(ParserException.class, () -> Selector.parseSelector(":nth-child(2n+)"));
        Assertions.assertThrows(ParserException.class, () -> Selector.parseSelector(":nth-child(2n-)"));
    }

    @Test
    void attributesGetHasAttributeCaseInsesitiveForHtml() {
        Document d = JFiveParse.parse("<div id='x'></div>");
        Element div = d.getElementById("x");
        div.getAttributes().put(new AttributeNode("DATA-X", "1"));
        Assertions.assertTrue(div.hasAttribute("data-x"));
        assertEquals("1", div.getAttribute("data-x"));
        assertEquals("1", div.getAttribute("DATA-X"));
    }

    @Test
    void attributeCase() {
        var elem = new Element("div");
        elem.setAttributeNS(null, "class", "a");
        elem.setAttributeNS(null, "CLASS", "b");
        elem.setAttributeNS(null, "cLaSs", "c");

        // same behaviour as chrome
        assertEquals("<div class=\"a\" CLASS=\"b\" cLaSs=\"c\"></div>", JFiveParse.serialize(elem));
        assertEquals("a", elem.getAttribute("CLASS")); // <- lowercase
        assertEquals("a", elem.getAttribute("class")); //
        assertEquals("a", elem.getAttribute("cLaSs")); //

        assertEquals("a", elem.getAttributeNS(null, "class"));
        assertEquals("b", elem.getAttributeNS(null, "CLASS"));
        assertEquals("c", elem.getAttributeNS(null, "cLaSs"));

        //
        var div = new Element("div");
        div.setAttributeNS(null, "class", "a");
        assertEquals("<div class=\"a\"></div>", JFiveParse.serialize(div));
        div.setAttribute("CLASS", "b");
        assertEquals("<div class=\"b\"></div>", JFiveParse.serialize(div));

        // TODO: add more comprehensive test suite as our attribute handling may have some holes
        //       most likely we need to add some additional work when creating directly AttributeNode
    }

    @Test
    void cssAttrInListNamespace() {
        Document doc = JFiveParse.parse(
                "<div><svg><a id=a xlink:href='foo bar'></a><b id=b href='foo bar'></b></svg></div>");
        var matcher = Selector.parseSelector("[xlink|href~=foo]", Map.of("xlink", Node.NAMESPACE_XLINK));
        assertEquals(List.of(doc.getElementById("a")), doc.getAllNodesMatching(matcher));
    }

    @Test
    void elementByTagNameAndNS() {
        Document doc = JFiveParse.parse("<div id=div><svg><rect id=r></rect></svg></div>");

        assertEquals(List.of(doc.getElementById("r")), doc.getElementsByTagNameNS("rect", "*"));
        assertEquals(doc.getAllNodesMatching(Selector.parseSelector("*")) , doc.getElementsByTagNameNS("*", "*"));
        assertEquals(List.of(), doc.getElementsByTagNameNS("div", null));
    }

    @Test
    void cssTrailingCombinatorShouldFail() {
        Assertions.assertThrows(ParserException.class, () -> Selector.parseSelector("div >"));
        Assertions.assertThrows(ParserException.class, () -> Selector.parseSelector("div +"));
        Assertions.assertThrows(ParserException.class, () -> Selector.parseSelector("div ~"));
    }

    @Test
    void cssRelativeTopLevelShouldFail() {
        Assertions.assertThrows(ParserException.class, () -> Selector.parseSelector("> div"));
    }

    @Test
    void cssComment() {
        Document doc = JFiveParse.parse("<div><span id=s></span></div>");
        Assertions.assertTrue(doc.getElementById("s").matches("div /* comment */ span"));
        Assertions.assertTrue(doc.getElementById("s").matches("div/**/ span"));
        Assertions.assertTrue(doc.getElementById("s").matches("div /**/span"));
        Assertions.assertThrows(ParserException.class,
                () -> Assertions.assertTrue(doc.getElementById("s").matches("div/**/span")));
    }


    @Test
    void serializationPrintOriginalAttributeQuote() {
        Document doc = JFiveParse.parse("<div title='a&apos;b'></div>");
        Element div = doc.getElementsByTagName("div").get(0);
        String html = div.getOuterHTML(Set.of(Option.PRINT_ORIGINAL_ATTRIBUTE_QUOTE));
        assertEquals("<div title='a&#39;b'></div>", html);
    }

    @Test
    void w3cDomRootNamespace() {
        Document doc = new Document();
        Element svg = new Element("svg", Node.NAMESPACE_SVG);
        doc.appendChild(svg);
        var w3c = W3CDom.toW3CDocument(doc);
        assertEquals(Node.NAMESPACE_SVG, w3c.getDocumentElement().getNamespaceURI());
    }


    @Test
    void insertAdjacentToRootElementShouldFail() {
        var doc = JFiveParse.parse("<html><body></body></html>");
        var html = doc.getDocumentElement();
        Assertions.assertThrows(IllegalArgumentException.class, () -> html.insertAdjacentElement("beforebegin", new Element("div")));
        Assertions.assertThrows(IllegalArgumentException.class, () -> html.insertAdjacentText("beforebegin", "text"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> html.insertAdjacentElement("afterend", new Element("div")));
        Assertions.assertThrows(IllegalArgumentException.class, () -> html.insertAdjacentText("afterend", "text"));
    }

    @Test
    void checkInsertOnNonInsertableNodes() {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> new Text("a").appendChild(new Element("div")));
        Assertions.assertThrows(UnsupportedOperationException.class, () -> new Text("a").insertChildren(0, new Element("div")));
        Assertions.assertThrows(UnsupportedOperationException.class, () -> new Text("a").replaceChild(new Element("div"), new Element("div")));
    }

    @Test
    void checkEmptyStringElement() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Element(""));
    }

    @Test
    void cssContainsParenthesis() {
        var doc = JFiveParse.parse("<div id=d>)</div>");
        Assertions.assertTrue(doc.getElementById("d").matches(":contains(')')"));
        Assertions.assertTrue(doc.getElementById("d").matches(":contains(\")\")"));
    }

    @Test
    void cssSelectAttrHypen() {
        var doc = JFiveParse.parse("<html><body><p id=en lang='en'>a</p><p id=enus lang='en-US'>b</p><p lang='fr-en'>c</p></body></html>");
        var matches = doc.getAllNodesMatching(Selector.parseSelector("[lang|=en]"));
        assertEquals(List.of(doc.getElementById("en"), doc.getElementById("enus")), matches);
    }


    @Test
    void checkUpperCaseRemoveAttribute() {
        Element div = new Element("div");
        div.getAttributes().put(new AttributeNode("DATA-X", "1"));

        assertEquals("1", div.getAttribute("data-x"));
        div.removeAttribute("data-x");

        Assertions.assertFalse(div.hasAttribute("data-x"));
        Assertions.assertFalse(div.hasAttributes());
    }

    @Test
    void checkCssSelectorAttributeEmptyString() {
        var doc = JFiveParse.parse("<div bla=value></div>");

        Assertions.assertTrue(doc.getAllNodesMatching(Selector.parseSelector("[bla^='']")).isEmpty());
        Assertions.assertTrue(doc.getAllNodesMatching(Selector.parseSelector("[bla$='']")).isEmpty());
        Assertions.assertTrue(doc.getAllNodesMatching(Selector.parseSelector("[bla*='']")).isEmpty());
    }

    @Test
    void cssRootDetached() {
        var detached = new Element("div");
        Assertions.assertFalse(detached.matches(":root"));

        var doc = JFiveParse.parse("<html><body><div id=d></div></body></html>");
        Assertions.assertTrue(doc.getDocumentElement().matches(":root"));
        Assertions.assertFalse(doc.getElementById("d").matches(":root"));
    }

    @Test
    void setBodyInNewEmptyDocument() {
        Document doc = new Document();
        Element body = new Element("body");
        doc.setBody(body);
        Assertions.assertSame(body, doc.getBody());
    }

    @Test
    void cssBuilderBehaviour() {
        Document doc = JFiveParse.parse("<div id=d class=a></div><span id=s class=a></span>");
        Selector builder = Selector.select().element("div").hasClass("a").attr("id");
        NodeMatcher<Node> matcher = builder.toMatcher();

        assertEquals(List.of(doc.getElementById("d")), doc.getAllNodesMatching(matcher));

        builder.hasClass("b"); // calling builder.toMatcher(); should "detach" the matcher
        assertEquals(List.of(doc.getElementById("d")), doc.getAllNodesMatching(matcher));
    }

    @Test
    void tagNameUpperCaseOnlyASCII() {
        assertEquals("AéA", new Element("aéa").getTagName());
        assertEquals("viewBox", new Element("viewBox", Node.NAMESPACE_SVG).getTagName());
    }

    @Test
    void foreignAttrMustHaveNamespace() {
        var doc = JFiveParse.parse("<div><svg xml:lang='en' xlink:href='x'></svg></div>");
        var svg = doc.getElementsByTagName("svg").get(0);
        var lang = svg.getAttributeNodeNS(Node.NAMESPACE_XML, "lang");
        assertEquals(Node.NAMESPACE_XML, lang.getNamespace());
    }

    @Test
    void checkByTagName() {
        Document doc = JFiveParse.parse(
                "<div id=h></div><svg><linearGradient id=g></linearGradient><textPath id=t></textPath></svg>");

        // getElementsByTagName case insensitive for html
        assertEquals(1, doc.getElementsByTagName("DIV").size());
        assertEquals(1, doc.getElementsByTagName("linearGradient").size());
        assertEquals(1, doc.getElementsByTagName("textPath").size());
        // case sensitive for non-html
        Assertions.assertTrue(doc.getElementsByTagName("lineargradient").isEmpty());
        Assertions.assertTrue(doc.getElementsByTagName("TEXTPATH").isEmpty());


        assertEquals(1, doc.getElementsByTagNameNS("div", Node.NAMESPACE_HTML).size());
        Assertions.assertTrue(doc.getElementsByTagNameNS("DIV", Node.NAMESPACE_HTML).isEmpty());

        assertEquals(1, doc.getElementsByTagNameNS("linearGradient", Node.NAMESPACE_SVG).size());
        Assertions.assertTrue(doc.getElementsByTagNameNS("lineargradient", Node.NAMESPACE_SVG).isEmpty());
        Assertions.assertTrue(doc.getElementsByTagNameNS("TEXTPATH", Node.NAMESPACE_SVG).isEmpty());
    }

    @Test
    void checkAttributeApiCase() {
        var source = new Element("div");
        source.setAttributeNS(null, "CLASS", "x");
        var attribute = source.getAttributeNodeNS(null, "CLASS");
        assertEquals("CLASS", attribute.getName());

        source.removeAttributeNS(null, "CLASS");
        var target = new Element("div");
        target.setAttributeNode(attribute);

        assertEquals("CLASS", attribute.getName());
        assertEquals("CLASS", attribute.getLocalName());
        assertEquals("<div CLASS=\"x\"></div>", target.getOuterHTML());
        //
        var div = new Element("div");
        div.setAttribute("class", "a");
        div.getAttributes().put(new AttributeNode("CLASS", "b"));

        assertEquals(1, attributeCount(div.getAttributes()));
        assertEquals("b", div.getAttribute("class"));
        assertEquals("<div class=\"b\"></div>", div.getOuterHTML());

        var other = new Element("div");
        other.getAttributes().put(new AttributeNode("DATA-X", "1"));
        assertEquals("<div data-x=\"1\"></div>", other.getOuterHTML());
    }

    private static int attributeCount(Attributes attributes) {
        int count = 0;
        for (AttributeNode ignored : attributes) {
            count++;
        }
        return count;
    }

    @Test
    void emptyNameSpaceNormalization() {
        var el = new Element("div", "");
        assertNull(el.getNamespaceURI());
        assertEquals(new Element("div", null).getNamespaceURI(), el.getNamespaceURI());
    }

    @Test
    void nullNameLookupsMustNotDependOnAttributePresence() {
        var doc = JFiveParse.parse("<div id=x></div>");
        var div = doc.getElementById("x");

        Assertions.assertTrue(doc.getElementsByTagName(null).isEmpty());
        assertNull(div.getAttribute(null));
        Assertions.assertFalse(div.hasAttribute(null));
        assertNull(div.getAttributeNode(null));
        Assertions.assertDoesNotThrow(() -> div.removeAttribute(null));
    }

    @Test
    void checkNamespaceCoherence() {
        AttributeNode empty = new AttributeNode("foo", "v", "");
        assertNull(empty.getNamespace());

        AttributeNode plain = new AttributeNode("foo", "v");
        assertNull(plain.getNamespace());

        AttributeNode foreign = new AttributeNode("xlink:href", "v", Node.NAMESPACE_XLINK);
        assertEquals(Node.NAMESPACE_XLINK, foreign.getNamespace());
    }

    @Test
    void checkAttributeQuotingDontTransformEntities() {
        var el = new Element("div");
        el.setAttribute("title", "foo \"bar\"");
        String out = JFiveParse.serialize(el, Set.of(Option.DONT_TRANSFORM_ENTITIES));
        assertEquals("<div title=\"foo &quot;bar&quot;\"></div>", out);
        var back = JFiveParse.parse(out).getElementsByTagName("div").get(0);
        assertEquals("foo \"bar\"", back.getAttribute("title"));


        var d = JFiveParse.parse("<div test='\" &#39; &quot;' test2=\"' &#39; &quot;\"></div>");
        out = JFiveParse.serialize(d);
        assertEquals("<html><head></head><body><div test=\"&quot; ' &quot;\" test2=\"' ' &quot;\"></div></body></html>", out);

        out = JFiveParse.serialize(d, Set.of(Option.PRINT_ORIGINAL_ATTRIBUTE_QUOTE));
        assertEquals("<html><head></head><body><div test='&quot; &#39; &quot;' test2=\"' ' &quot;\"></div></body></html>", out);

        out = JFiveParse.serialize(d, Set.of(Option.DONT_TRANSFORM_ENTITIES));
        assertEquals("<html><head></head><body><div test=\"&quot; ' &quot;\" test2=\"' ' &quot;\"></div></body></html>", out);

        out = JFiveParse.serialize(d, Set.of(Option.DONT_TRANSFORM_ENTITIES, Option.PRINT_ORIGINAL_ATTRIBUTE_QUOTE));
        assertEquals("<html><head></head><body><div test='\" &#39; \"' test2=\"' ' &quot;\"></div></body></html>", out);
    }


    @Test
    void emptySelectorThrows() {
        Assertions.assertThrows(ParserException.class, () -> Selector.parseSelector(""));
        Assertions.assertThrows(ParserException.class, () -> Selector.parseSelector("   "));
        Assertions.assertThrows(ParserException.class, () -> JFiveParse.parse("<div></div>").getElementsByTagName("div").get(0).matches(""));
    }

    @Test
    void nullIdTagName() {
        var doc = JFiveParse.parse("<div id=null></div><null></null>");
        assertNotNull(doc.getElementById(null));
        assertEquals(1, doc.getElementsByTagNameNS(null, "*").size());
    }

    @Test
    void attributeNameNull() {
        var div = new Element("div");
        Assertions.assertDoesNotThrow(() -> div.setAttributeNode(new AttributeNode(null, "v")));
        assertEquals("<div null=\"v\"></div>", div.getOuterHTML());

        var div2 = new Element("div");
        Assertions.assertDoesNotThrow(() -> div2.setAttributeNode(new AttributeNode(null, "v", "urn:ns")));
        assertEquals("<div null=\"v\"></div>", div2.getOuterHTML());
    }


    @Test
    void nameSpaceWildcardCheck() {
        var svg = (Element) JFiveParse
                .parseFragment("<svg><a xlink:href=v></a></svg>")
                .get(0);
        var a = svg.getFirstElementChild();

        assertNull(a.getAttributeNS("*", "href"));
        Assertions.assertFalse(a.hasAttributeNS("*", "href"));

        var copy = (Element) a.cloneNode(true);
        copy.removeAttributeNS("*", "href");
        assertEquals("<a xlink:href=\"v\"></a>", copy.getOuterHTML());
    }


    @Test
    void domStringSemantics() {
        var doc = JFiveParse.parse("<div id=null></div><span id=''></span>");
        assertNotNull(doc.getElementById(null));
        assertNull(doc.getElementById(""));
    }

    @Test
    void nullAsParamGetElemAttr() {
        Document doc = JFiveParse.parse("<null id=x></null><div id=y null='v'></div>");

        assertEquals(1, doc.getElementsByTagNameNS("null", Node.NAMESPACE_HTML).size());
        assertEquals(1, doc.getElementsByTagNameNS(null, Node.NAMESPACE_HTML).size());

        Element y = doc.getElementById("y");
        assertEquals("v", y.getAttributeNS(null, null));
        assertTrue(y.hasAttributeNS(null, null));
    }

    @Test
    void nullValueIsNotAnEmptyString() {
        Element el = new Element("div");
        el.setAttribute("data-x", null);

        String html = el.getOuterHTML(Set.of(Option.HIDE_EMPTY_ATTRIBUTE_VALUE));
        assertEquals("<div data-x=\"null\"></div>", html);

        Element reparsed = JFiveParse.parse(html).getElementsByTagName("div").get(0);
        assertEquals("null", reparsed.getAttribute("data-x"));
    }

    @Test
    void ofType() {
        Element div = new Element("div");
        Element svgA = new Element("a", Node.NAMESPACE_SVG);
        Element htmlA = new Element("a");
        Element htmlA2 = new Element("a");

        div.appendChild(svgA);
        div.appendChild(htmlA);
        div.appendChild(htmlA2);

        assertTrue(htmlA.matches(":first-of-type"));
        assertTrue(htmlA.matches(":nth-of-type(1)"));
        assertFalse(htmlA.matches(":only-of-type"));
        assertTrue(htmlA2.matches(":nth-of-type(2)"));
        assertTrue(svgA.matches(":only-of-type"));
    }

    @Test
    void lastOfType() {
        Element div = new Element("div");
        Element htmlA = new Element("a");
        Element svgA = new Element("a", Node.NAMESPACE_SVG);
        Element htmlA2 = new Element("a");

        div.appendChild(htmlA);
        div.appendChild(svgA);
        div.appendChild(htmlA2);

        assertFalse(htmlA.matches(":last-of-type"));
        assertTrue(svgA.matches(":last-of-type"));
        assertTrue(htmlA2.matches(":last-of-type"));
    }

    @Test
    void voidElementsMustNotSerializeChildren() {
        Element img = new Element("img");
        img.appendChild(new Text("x"));

        assertEquals("", img.getInnerHTML());
        assertEquals("<img>", img.getOuterHTML());
        assertEquals("<img>", JFiveParse.serialize(img));

        Element br = new Element("br");
        br.appendChild(new Element("span"));

        assertEquals("", br.getInnerHTML());
        assertEquals("<br>", br.getOuterHTML());
    }

    @Test
    void selectorRejectEmptyAttr() {
        assertThrows(ParserException.class, () -> Selector.parseSelector("[a=]"));
        assertThrows(ParserException.class, () -> Selector.parseSelector("[a= ]"));
        assertThrows(ParserException.class, () -> Selector.parseSelector("[a^=]"));
        assertThrows(ParserException.class, () -> Selector.parseSelector("[a$=]"));
        assertThrows(ParserException.class, () -> Selector.parseSelector("[a*=]"));
        assertThrows(ParserException.class, () -> Selector.parseSelector("[a~=]"));
        assertThrows(ParserException.class, () -> Selector.parseSelector("[a|=]"));

        assertDoesNotThrow(() -> Selector.parseSelector("[a='']"));
        assertDoesNotThrow(() -> Selector.parseSelector("[a=\"\"]"));
    }
}
