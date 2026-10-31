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

}
