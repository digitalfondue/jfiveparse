package ch.digitalfondue.jfiveparse;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.w3c.dom.Comment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    @Test
    @Timeout(value = 2, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    void testCycle() {
        // self cycle
        var div = new Element("div");
        Assertions.assertThrows(IllegalStateException.class, () -> div.appendChild(div));
        Assertions.assertEquals("<div></div>", JFiveParse.serialize(div));

        // root -> span -> root
        var root = new Element("div");
        var span = new Element("span");
        var b = new Element("b");
        root.appendChild(span);
        span.appendChild(b);
        Assertions.assertThrows(IllegalStateException.class, () -> span.replaceChild(root, b)); // cycle: root -> span -> root
        Assertions.assertEquals("<div><span><b></b></span></div>", JFiveParse.serialize(root));
    }

    @Test
    void testInsertChildrenAtIndexFailure() {
        Element p1 = new Element("div");
        Element p2 = new Element("div");
        Element c  = new Element("span");
        p1.appendChild(c);
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> p2.insertChildren(5, c));

        // when failing the insert, c should not have a parent node set
        Assertions.assertEquals(p1, c.getParentNode());
        Assertions.assertEquals(1, p1.getChildCount());
        Assertions.assertEquals(0, p2.getChildCount());
    }

    @Test
    void testInsertBeforeNull() {
        Element div = new Element("div");
        Element b = new Element("b");
        Element a = new Element("a");
        div.insertBefore(b, null);
        div.insertBefore(a, null);
        Assertions.assertEquals("<div><b></b><a></a></div>", JFiveParse.serialize(div));
    }

    @Test
    void testW3CDocumentWithTopLevelComments() {
        var doc = JFiveParse.parse("<!--before--><html></html><!--after-->");

        var w3c = W3CDom.toW3CDocument(doc);
        Assertions.assertEquals(3, w3c.getChildNodes().getLength());
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
        Assertions.assertEquals(1, doc.getAllNodesMatching(m).size());

        org.w3c.dom.Document w3c = W3CDom.toW3CDocument(doc);
        var m2 = W3CDom.W3CDomSelector.parseSelector(
                "svg|rect",
                Map.of("svg", Node.NAMESPACE_SVG));
        Assertions.assertEquals(1, W3CDom.getAllNodesMatching(w3c, m2).count());
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
        Assertions.assertEquals(List.of("start:div"), events);
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
        Assertions.assertEquals(b, d.getBody());

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
        Assertions.assertEquals("ab", t.getData());
    }

    @Test
    void createIdAttributeW3CDom() {
        Document doc = JFiveParse.parse(
                "<div><svg><a id=a xlink:href=a></a><b id=b href=b></b></svg></div>");
        var w3doc = W3CDom.toW3CDocument(doc);
        Assertions.assertNotNull(w3doc.getElementById("a"));
        Assertions.assertNotNull(w3doc.getElementById("b"));
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

        Assertions.assertEquals(List.of(a, b), doc.getAllNodesMatching(m1));
        Assertions.assertEquals(List.of(a), doc.getAllNodesMatching(m2));
        Assertions.assertEquals(List.of(b), doc.getAllNodesMatching(m3));
        
        var w3doc = W3CDom.toW3CDocument(doc);
        var w3a = w3doc.getElementById("a");
        var w3b = w3doc.getElementById("b");

        var w3m1 = W3CDom.W3CDomSelector.parseSelector("[*|href]", Map.of("xlink", Node.NAMESPACE_XLINK));
        var w3m2 = W3CDom.W3CDomSelector.parseSelector("[xlink|href]", Map.of("xlink", Node.NAMESPACE_XLINK));
        var w3m3 = W3CDom.W3CDomSelector.parseSelector("[|href]", Map.of("xlink", Node.NAMESPACE_XLINK));

        Assertions.assertEquals(List.of(w3a, w3b), W3CDom.getAllNodesMatching(w3doc, w3m1).toList());
        Assertions.assertEquals(List.of(w3a), W3CDom.getAllNodesMatching(w3doc, w3m2).toList());
        Assertions.assertEquals(List.of(w3b), W3CDom.getAllNodesMatching(w3doc, w3m3).toList());
    }

    @Test
    void cssUniversalSelectorUnknownNamespace() {
        Document doc = JFiveParse.parse("<div></div>");
        Assertions.assertEquals(0, doc.getAllNodesMatching(Selector.parseSelector("svg|*")).size());
        Assertions.assertEquals(0, doc.getAllNodesMatching(Selector.parseSelector("|*")).size());
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

        Assertions.assertEquals(List.of(b), doc.getAllNodesMatching(defaultSelector));
        Assertions.assertEquals(List.of(a, b), doc.getAllNodesMatching(insensitiveLower));
        Assertions.assertEquals(List.of(a, b), doc.getAllNodesMatching(insensitiveUpper));
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
        Assertions.assertEquals(List.of(b), doc.getAllNodesMatching(Selector.parseSelector("[class~=foo]")));
        Assertions.assertEquals(List.of(a, b), doc.getAllNodesMatching(Selector.parseSelector("[class~=foo i]")));
        Assertions.assertEquals(List.of(a, b), doc.getAllNodesMatching(Selector.parseSelector("[class~=FOO i]")));

        // ~=
        Assertions.assertEquals(List.of(b), doc.getAllNodesMatching(Selector.parseSelector("[data-x~=value]")));
        Assertions.assertEquals(List.of(a, b), doc.getAllNodesMatching(Selector.parseSelector("[data-x~=value i]")));

        // ^=
        Assertions.assertEquals(List.of(b), doc.getAllNodesMatching(Selector.parseSelector("[data-x^=val]")));
        Assertions.assertEquals(List.of(a, b, c), doc.getAllNodesMatching(Selector.parseSelector("[data-x^=val i]")));

        // $=
        Assertions.assertEquals(List.of(b), doc.getAllNodesMatching(Selector.parseSelector("[data-x$=ue]")));
        Assertions.assertEquals(List.of(a, b), doc.getAllNodesMatching(Selector.parseSelector("[data-x$=ue i]")));

        // *=
        Assertions.assertEquals(List.of(b, c), doc.getAllNodesMatching(Selector.parseSelector("[data-x*=alu]")));
        Assertions.assertEquals(List.of(a, b, c), doc.getAllNodesMatching(Selector.parseSelector("[data-x*=alu i]")));

        // W3CDom implementation
        var w3doc = W3CDom.toW3CDocument(doc);
        var w3a = w3doc.getElementById("a");
        var w3b = w3doc.getElementById("b");
        var w3c = w3doc.getElementById("c");

        Assertions.assertEquals(List.of(w3a, w3b), W3CDom.getAllNodesMatching(w3doc,
                W3CDom.W3CDomSelector.parseSelector("[class~=foo i]")).toList());
        Assertions.assertEquals(List.of(w3a, w3b), W3CDom.getAllNodesMatching(w3doc,
                W3CDom.W3CDomSelector.parseSelector("[data-x~=value i]")).toList());
        Assertions.assertEquals(List.of(w3a, w3b, w3c), W3CDom.getAllNodesMatching(w3doc,
                W3CDom.W3CDomSelector.parseSelector("[data-x^=val i]")).toList());
        Assertions.assertEquals(List.of(w3a, w3b), W3CDom.getAllNodesMatching(w3doc,
                W3CDom.W3CDomSelector.parseSelector("[data-x$=ue i]")).toList());
        Assertions.assertEquals(List.of(w3a, w3b, w3c), W3CDom.getAllNodesMatching(w3doc,
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
        Assertions.assertEquals(1, found.size());
        Assertions.assertSame(targetDiv, found.get(0));

        Assertions.assertEquals(1, textPath.size());
        Assertions.assertSame(targetTextPath, textPath.get(0));

    }


    @Test
    void testNullTextContentCommentProcessingInstruction() {
        // follow browser behaviour.

        // document.createComment(null) -> <!--null--> -> null is converted in the string 'null'
        // instead a.textContent = null -> a.textContent is '' (empty string)

        var c = new ch.digitalfondue.jfiveparse.Comment(null);
        Assertions.assertEquals("null", c.getTextContent());
        Assertions.assertEquals("<!--null-->", JFiveParse.serialize(c));

        c.setTextContent(null);
        Assertions.assertEquals("", c.getTextContent());
        Assertions.assertEquals("<!---->", JFiveParse.serialize(c));
        //
        var pi = new ProcessingInstruction("target", null);
        Assertions.assertEquals("<?target null?>", JFiveParse.serialize(pi));

        Assertions.assertEquals("null", pi.getTextContent());
        pi.setTextContent(null);
        Assertions.assertEquals("", pi.getTextContent());
        Assertions.assertEquals("<?target?>", JFiveParse.serialize(pi));
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
        Assertions.assertEquals("1", div.getAttribute("data-x"));
        Assertions.assertEquals("1", div.getAttribute("DATA-X"));
    }

    @Test
    void attributeCase() {
        var elem = new Element("div");
        elem.setAttributeNS(null, "class", "a");
        elem.setAttributeNS(null, "CLASS", "b");
        elem.setAttributeNS(null, "cLaSs", "c");

        // same behaviour as chrome
        Assertions.assertEquals("<div class=\"a\" CLASS=\"b\" cLaSs=\"c\"></div>", JFiveParse.serialize(elem));
        Assertions.assertEquals("a", elem.getAttribute("CLASS")); // <- lowercase
        Assertions.assertEquals("a", elem.getAttribute("class")); //
        Assertions.assertEquals("a", elem.getAttribute("cLaSs")); //

        Assertions.assertEquals("a", elem.getAttributeNS(null, "class"));
        Assertions.assertEquals("b", elem.getAttributeNS(null, "CLASS"));
        Assertions.assertEquals("c", elem.getAttributeNS(null, "cLaSs"));

        //
        var div = new Element("div");
        div.setAttributeNS(null, "class", "a");
        Assertions.assertEquals("<div class=\"a\"></div>", JFiveParse.serialize(div));
        div.setAttribute("CLASS", "b");
        Assertions.assertEquals("<div class=\"b\"></div>", JFiveParse.serialize(div));

        // TODO: add more comprehensive test suite as our attribute handling may have some holes
        //       most likely we need to add some additional work when creating directly AttributeNode
    }

    @Test
    void cssAttrInListNamespace() {
        Document doc = JFiveParse.parse(
                "<div><svg><a id=a xlink:href='foo bar'></a><b id=b href='foo bar'></b></svg></div>");
        var matcher = Selector.parseSelector("[xlink|href~=foo]", Map.of("xlink", Node.NAMESPACE_XLINK));
        Assertions.assertEquals(List.of(doc.getElementById("a")), doc.getAllNodesMatching(matcher));
    }

    @Test
    void elementByTagNameAndNS() {
        Document doc = JFiveParse.parse("<div id=div><svg><rect id=r></rect></svg></div>");

        Assertions.assertEquals(List.of(doc.getElementById("r")), doc.getElementsByTagNameNS("rect", "*"));
        Assertions.assertEquals(doc.getAllNodesMatching(Selector.parseSelector("*")) , doc.getElementsByTagNameNS("*", "*"));
        Assertions.assertEquals(List.of(), doc.getElementsByTagNameNS("div", null));
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
        Assertions.assertEquals("<div title='a&#39;b'></div>", html);
    }

    @Test
    void w3cDomRootNamespace() {
        Document doc = new Document();
        Element svg = new Element("svg", Node.NAMESPACE_SVG);
        doc.appendChild(svg);
        var w3c = W3CDom.toW3CDocument(doc);
        Assertions.assertEquals(Node.NAMESPACE_SVG, w3c.getDocumentElement().getNamespaceURI());
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
        Assertions.assertEquals(List.of(doc.getElementById("en"), doc.getElementById("enus")), matches);
    }
}
