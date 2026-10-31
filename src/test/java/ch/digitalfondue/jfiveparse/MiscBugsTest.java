package ch.digitalfondue.jfiveparse;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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

}
