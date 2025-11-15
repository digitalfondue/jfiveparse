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

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// FIXME
class NodeMatchersTest {

    final Parser parser = new Parser();
/*
    @Test
    void firstChildTest() {
        var matcher = Selector.select().isFirstChild().toMatcher();
        Document doc = parser.parse("<div></div><div id=myid></div><div></div>");
        List<Node> div = doc.getElementsByTagName("body").get(0).getAllNodesMatching(matcher);
        assertEquals(1, div.size());
        assertSame(doc.getElementsByTagName("body").get(0).getChildNodes().get(0), div.get(0));


        var w3cDoc = W3CDom.toW3CDocument(doc);
        var w3cDiv = W3CDom.getAllNodesMatching(w3cDoc.getElementsByTagName("body").item(0), matcher).toList();
        assertEquals(1, w3cDiv.size());
        assertSame(w3cDoc.getElementsByTagName("body").item(0).getChildNodes().item(0), w3cDiv.get(0));
    }

    @Test
    void universalTest() {
        Document doc = parser.parse("<div></div><div id=myid><span><i></i></span></div><div></div>");
        var matcher = Selector.select().element("div").withDescendant().universal().toMatcher();
        var w3cDoc = W3CDom.toW3CDocument(doc);

        // div *
        List<Node> universal = doc.getElementsByTagName("body").get(0).getAllNodesMatching(matcher);
        var w3cUniversal = W3CDom.getAllNodesMatching(w3cDoc.getElementsByTagName("body").item(0), matcher).toList();
        assertEquals(2, universal.size());
        assertSame(doc.getElementsByTagName("body").get(0).getChildNodes().get(1).getChildNodes().get(0), universal.get(0));
        assertSame(doc.getElementsByTagName("body").get(0).getChildNodes().get(1).getChildNodes().get(0).getChildNodes().get(0), universal.get(1));

        assertEquals(2, w3cUniversal.size());
        assertSame(w3cDoc.getElementsByTagName("body").item(0).getChildNodes().item(1).getChildNodes().item(0), w3cUniversal.get(0));
        assertSame(w3cDoc.getElementsByTagName("body").item(0).getChildNodes().item(1).getChildNodes().item(0).getChildNodes().item(0), w3cUniversal.get(1));



        matcher = Selector.select().element("div").withChild().universal().toMatcher();
        // div > *
        universal = doc.getElementsByTagName("body").get(0).getAllNodesMatching(matcher);
        assertEquals(1, universal.size());
        assertSame(doc.getElementsByTagName("body").get(0).getChildNodes().get(1).getChildNodes().get(0), universal.get(0));
    }

    @Test
    void universalWithSpaces() {
        Document doc = parser.parse(" <div> </div> <div id=myid> <span> <i> </i> </span> </div> <div> </div> ");
        // div > *
        var universal = doc.getElementsByTagName("body").get(0).getAllNodesMatching(Selector.select().element("div").withChild().universal().toMatcher());
        assertEquals(1, universal.size());
        assertEquals("span", universal.get(0).getNodeName());
    }

    @Test
    void firstElementTest() {
        Document doc = parser.parse("text<div></div><div id=myid></div><div></div>");
        var matcher = Selector.select().isFirstElementChild().toMatcher();
        List<Node> div = doc.getElementsByTagName("body").get(0).getAllNodesMatching(matcher);
        assertEquals(1, div.size());
        assertEquals(doc.getElementsByTagName("body").get(0).getChildNodes().get(1), div.get(0));

        var w3cDoc = W3CDom.toW3CDocument(doc);
        var w3cDiv = W3CDom.getAllNodesMatching(w3cDoc.getElementsByTagName("body").item(0), matcher).toList();
        assertEquals(1, w3cDiv.size());
        assertEquals(w3cDoc.getElementsByTagName("body").item(0).getChildNodes().item(1), w3cDiv.get(0));
    }

    @Test
    void lastElementTest() {
        Document doc = parser.parse("text<div></div><div id=myid></div><div></div>text");
        var matcher = Selector.select().isLastElementChild().toMatcher();
        List<Node> div = doc.getElementsByTagName("body").get(0).getAllNodesMatching(matcher);
        assertEquals(1, div.size());
        assertSame(doc.getElementsByTagName("body").get(0).getChildNodes().get(3), div.get(0));


        var w3cDoc = W3CDom.toW3CDocument(doc);
        var w3cDiv = W3CDom.getAllNodesMatching(w3cDoc.getElementsByTagName("body").item(0), matcher).toList();
        assertEquals(1, w3cDiv.size());
        assertSame(w3cDoc.getElementsByTagName("body").item(0).getChildNodes().item(3), w3cDiv.get(0));
    }

    @Test
    void lastChildTest() {
        Document doc = parser.parse("text<div></div><div id=myid></div><div></div>");
        var w3cDoc = W3CDom.toW3CDocument(doc);

        var matcher = Selector.select().isLastChild().toMatcher();

        List<Node> div = doc.getElementsByTagName("body").get(0).getAllNodesMatching(matcher);
        assertEquals(1, div.size());
        assertSame(doc.getElementsByTagName("body").get(0).getChildNodes().get(3), div.get(0));

        var w3cDiv = W3CDom.getAllNodesMatching(w3cDoc.getElementsByTagName("body").item(0), matcher).toList();
        assertEquals(1, w3cDiv.size());
        assertSame(w3cDoc.getElementsByTagName("body").item(0).getChildNodes().item(3), w3cDiv.get(0));
    }

    @Test
    void hasAttributeValueEqTest() {
        Document doc = parser.parse("text<div></div><div id=myid></div><div id=myid2></div>");
        var w3cDoc = W3CDom.toW3CDocument(doc);

        var matcher = Selector.select().attrValEq("id", "myid").toMatcher();
        List<Node> divIdMyId = doc.getAllNodesMatching(matcher);
        assertEquals(1, divIdMyId.size());

        var w3cDivIdMyId = W3CDom.getAllNodesMatching(w3cDoc, matcher).toList();
        assertEquals(1, w3cDivIdMyId.size());


        matcher = Selector.select().attr("id").toMatcher();
        List<Node> divId = doc.getAllNodesMatching(matcher);
        assertEquals(2, divId.size());
        assertEquals("myid", ((Element) divId.get(0)).getAttribute("id"));
        assertEquals("myid2", ((Element) divId.get(1)).getAttribute("id"));

        var w3cDivId = W3CDom.getAllNodesMatching(w3cDoc, matcher).toList();
        assertEquals(2, w3cDivId.size());
        assertEquals("myid", ((org.w3c.dom.Element) w3cDivId.get(0)).getAttribute("id"));
        assertEquals("myid2", ((org.w3c.dom.Element) w3cDivId.get(1)).getAttribute("id"));
    }

    @Test
    void hasAttributeValueInListTest() {
        Document doc = parser.parse("text<div class='class2 class3'></div><div class='class2 class1 class3'></div><div id=myid2></div>");
        var w3cDoc = W3CDom.toW3CDocument(doc);

        var matcher = Selector.select().attrValInList("class", "class1").toMatcher();

        List<Node> divClass = doc.getAllNodesMatching(matcher);
        assertEquals(1, divClass.size());
        assertEquals("class2 class1 class3", ((Element) divClass.get(0)).getAttribute("class"));

        var w3cDivClass = W3CDom.getAllNodesMatching(w3cDoc, matcher).toList();
        assertEquals(1, w3cDivClass.size());
        assertEquals("class2 class1 class3", ((org.w3c.dom.Element) w3cDivClass.get(0)).getAttribute("class"));

        matcher = Selector.select().attrValInList("class", "class2").toMatcher();
        List<Node> divClasses = doc.getAllNodesMatching(matcher);
        assertEquals(2, divClasses.size());

        var w3cDivClasses = W3CDom.getAllNodesMatching(w3cDoc, matcher).toList();
        assertEquals(2, w3cDivClasses.size());
    }

    @Test
    void hasAttributeValueStartWithTest() {
        Document doc = parser.parse("text<div class='class2 class3'></div><div class='class2 class1 class3'></div><div id=myid2></div>");
        var w3cDoc = W3CDom.toW3CDocument(doc);

        var matcher = Selector.select().attrValStartWith("class", "class2 class1").toMatcher();

        List<Node> divClass = doc.getAllNodesMatching(matcher);
        assertEquals(1, divClass.size());
        assertEquals("class2 class1 class3", ((Element) divClass.get(0)).getAttribute("class"));

        var w3cDivClass = W3CDom.getAllNodesMatching(w3cDoc, matcher).toList();
        assertEquals(1, w3cDivClass.size());
        assertEquals("class2 class1 class3", ((org.w3c.dom.Element) w3cDivClass.get(0)).getAttribute("class"));

        matcher = Selector.select().attrValStartWith("class", "class2").toMatcher();
        List<Node> divClasses = doc.getAllNodesMatching(matcher);
        assertEquals(2, divClasses.size());

        var w3cDivClasses = W3CDom.getAllNodesMatching(w3cDoc, matcher).toList();
        assertEquals(2, w3cDivClasses.size());
    }

    @Test
    void hasAttributeValueEndWithTest() {
        Document doc = parser.parse("text<div class='class2 class3'></div><div class='class2 class1 class3'></div><div id=myid2></div>");
        var w3cDoc = W3CDom.toW3CDocument(doc);

        var matcher = Selector.select().attrValEndWith("class", "class1 class3").toMatcher();
        List<Node> divClass = doc.getAllNodesMatching(matcher);
        assertEquals(1, divClass.size());
        assertEquals("class2 class1 class3", ((Element) divClass.get(0)).getAttribute("class"));

        var w3cDivClass = W3CDom.getAllNodesMatching(w3cDoc, matcher).toList();
        assertEquals(1, w3cDivClass.size());
        assertEquals("class2 class1 class3", ((org.w3c.dom.Element) w3cDivClass.get(0)).getAttribute("class"));

        matcher = Selector.select().attrValEndWith("class", "class3").toMatcher();
        List<Node> divClasses = doc.getAllNodesMatching(matcher);
        assertEquals(2, divClasses.size());

        var w3cDivClasses = W3CDom.getAllNodesMatching(w3cDoc, matcher).toList();
        assertEquals(2, w3cDivClasses.size());
    }

    @Test
    void hasAttributeValueContainTest() {
        Document doc = parser.parse("text<div class='class2 class3'></div><div class='class2 class1 class3'></div><div id=myid2></div>");
        var w3cDoc = W3CDom.toW3CDocument(doc);

        var matcher = Selector.select().attrValContains("class", "class2 class1").toMatcher();
        List<Node> divClass = doc.getAllNodesMatching(matcher);
        assertEquals(1, divClass.size());
        assertEquals("class2 class1 class3", ((Element) divClass.get(0)).getAttribute("class"));

        var w3cDivClass = W3CDom.getAllNodesMatching(w3cDoc, matcher).toList();
        assertEquals(1, w3cDivClass.size());
        assertEquals("class2 class1 class3", ((org.w3c.dom.Element) w3cDivClass.get(0)).getAttribute("class"));

        matcher = Selector.select().attrValContains("class", "cl").toMatcher();
        List<Node> divClasses = doc.getAllNodesMatching(matcher);
        assertEquals(2, divClasses.size());

        var w3cDivClasses = W3CDom.getAllNodesMatching(w3cDoc, matcher).toList();
        assertEquals(2, w3cDivClasses.size());
    }

    @Test
    void hasParentMatch() {
        Document doc = parser.parse("<div id=depth1><div id=depth2><div id=depth3></div></div></div>");
        var w3cDoc = W3CDom.toW3CDocument(doc);

        var matcher = Selector.select().attrValEq("id", "depth1").withChild().toMatcher();
        List<Node> e = doc.getAllNodesMatching(matcher);
        assertEquals(1, e.size());
        assertEquals("depth2", ((Element) e.get(0)).getAttribute("id"));

        var w3ce = W3CDom.getAllNodesMatching(w3cDoc, matcher).toList();
        assertEquals(1, w3ce.size());
        assertEquals("depth2", ((org.w3c.dom.Element) w3ce.get(0)).getAttribute("id"));

        matcher = Selector.select().attrValEq("id", "depth0").withChild().toMatcher();
        List<Node> empty = doc.getAllNodesMatching(matcher);
        assertEquals(0, empty.size());

        assertEquals(0, W3CDom.getAllNodesMatching(w3cDoc, matcher).toList().size());
    }

    @Test
    void hasAncestorMatch() {
        Document doc = parser.parse("<div id=depth1><div id=depth2><div id=depth3></div></div></div>");
        var w3cDoc = W3CDom.toW3CDocument(doc);

        var matcher = Selector.select().attrValEq("id", "depth3").withDescendant().toMatcher();

        List<Node> e0 = doc.getAllNodesMatching(matcher);
        assertEquals(0, e0.size());

        var w3ce0 = W3CDom.getAllNodesMatching(w3cDoc, matcher).toList();
        assertEquals(0, w3ce0.size());


        matcher = Selector.select().attrValEq("id", "depth2").withDescendant().toMatcher();
        List<Node> e1 = doc.getAllNodesMatching(matcher);
        assertEquals(1, e1.size());
        assertEquals("depth3", ((Element) e1.get(0)).getAttribute("id"));

        var w3ce1 = W3CDom.getAllNodesMatching(w3cDoc, matcher).toList();
        assertEquals(1, w3ce1.size());
        assertEquals("depth3", ((org.w3c.dom.Element) w3ce1.get(0)).getAttribute("id"));


        matcher = Selector.select().attrValEq("id", "depth1").withDescendant().toMatcher();
        List<Node> e2 = doc.getAllNodesMatching(matcher);
        assertEquals(2, e2.size());
        assertEquals("depth2", ((Element) e2.get(0)).getAttribute("id"));
        assertEquals("depth3", ((Element) e2.get(1)).getAttribute("id"));

        var w3ce2 = W3CDom.getAllNodesMatching(w3cDoc, matcher).toList();
        assertEquals(2, w3ce2.size());
        assertEquals("depth2", ((org.w3c.dom.Element) w3ce2.get(0)).getAttribute("id"));
        assertEquals("depth3", ((org.w3c.dom.Element) w3ce2.get(1)).getAttribute("id"));

        matcher = Selector.select().attrValEq("id", "depth0").withDescendant().toMatcher();
        List<Node> empty = doc.getAllNodesMatching(matcher);
        assertEquals(0, empty.size());

        var w3cEmpty = W3CDom.getAllNodesMatching(w3cDoc, matcher).toList();
        assertEquals(0, w3cEmpty.size());
    }

    @Test
    void hasSelectorMatch() {
        // #depth1 > #depth2 > #depth3
        Document doc = parser.parse("<div id=depth1 class='a b c'><div id=depth2 class='a c'><div id=depth3 class='b c'></div></div></div>");
        var w3cDoc = W3CDom.toW3CDocument(doc);
        var matcher = Selector.select().id("depth1").withChild().id("depth2").withChild().id("depth3").toMatcher();
        List<Node> e1 = doc.getAllNodesMatching(matcher);
        assertEquals(1, e1.size());
        assertEquals("depth3", ((Element) e1.get(0)).getAttribute("id"));

        var w3ce1 = W3CDom.getAllNodesMatching(w3cDoc, matcher).toList();
        assertEquals(1, w3ce1.size());
        assertEquals("depth3", ((org.w3c.dom.Element) w3ce1.get(0)).getAttribute("id"));


        // div > [id]
        matcher = Selector.select().element("div").withDescendant().attr("id").toMatcher();
        List<Node> e2 = doc.getAllNodesMatching(matcher);
        assertEquals(2, e2.size());
        assertEquals("depth2", ((Element) e2.get(0)).getAttribute("id"));
        assertEquals("depth3", ((Element) e2.get(1)).getAttribute("id"));

        var w3ce2 = W3CDom.getAllNodesMatching(w3cDoc, matcher).toList();
        assertEquals(2, w3ce2.size());
        assertEquals("depth2", ((org.w3c.dom.Element) w3ce2.get(0)).getAttribute("id"));
        assertEquals("depth3", ((org.w3c.dom.Element) w3ce2.get(1)).getAttribute("id"));

        // div.a.b
        matcher = Selector.select().element("div").hasClass("a", "b").toMatcher();
        List<Node> e3 = doc.getAllNodesMatching(matcher);
        assertEquals(1, e3.size());
        assertEquals("depth1", ((Element) e3.get(0)).getAttribute("id"));

        var w3ce3 = W3CDom.getAllNodesMatching(w3cDoc, matcher).toList();
        assertEquals(1, w3ce3.size());
        assertEquals("depth1", ((org.w3c.dom.Element) w3ce3.get(0)).getAttribute("id"));

        // .c
        matcher = Selector.select().hasClass("c").toMatcher();
        List<Node> e4 = doc.getAllNodesMatching(matcher);
        assertEquals(3, e4.size());

        var w3ce4 = W3CDom.getAllNodesMatching(w3cDoc, matcher).toList();
        assertEquals(3, w3ce4.size());

        // .b
        matcher = Selector.select().hasClass("b").toMatcher();
        List<Node> e5 = doc.getAllNodesMatching(matcher);
        assertEquals(2, e5.size());
        assertEquals("depth1", ((Element) e5.get(0)).getAttribute("id"));
        assertEquals("depth3", ((Element) e5.get(1)).getAttribute("id"));

        var w3ce5 = W3CDom.getAllNodesMatching(w3cDoc, matcher).toList();
        assertEquals(2, w3ce5.size());
        assertEquals("depth1", ((org.w3c.dom.Element) w3ce5.get(0)).getAttribute("id"));
        assertEquals("depth3", ((org.w3c.dom.Element) w3ce5.get(1)).getAttribute("id"));
        
        // .a .c
        matcher = Selector.select().hasClass("a").withDescendant().hasClass("c").toMatcher();
        List<Node> e6 = doc.getAllNodesMatching(matcher);
        assertEquals(2, e6.size());
        assertEquals("depth2", ((Element) e6.get(0)).getAttribute("id"));
        assertEquals("depth3", ((Element) e6.get(1)).getAttribute("id"));

        var w3ce6 = W3CDom.getAllNodesMatching(w3cDoc, matcher).toList();
        assertEquals(2, w3ce6.size());
        assertEquals("depth2", ((org.w3c.dom.Element) w3ce6.get(0)).getAttribute("id"));
        assertEquals("depth3", ((org.w3c.dom.Element) w3ce6.get(1)).getAttribute("id"));
        
        // .a .a
        matcher = Selector.select().hasClass("a").withDescendant().hasClass("a").toMatcher();
        List<Node> e7 = doc.getAllNodesMatching(matcher);
        assertEquals(1, e7.size());
        assertEquals("depth2", ((Element) e7.get(0)).getAttribute("id"));

        var w3ce7 = W3CDom.getAllNodesMatching(w3cDoc, matcher).toList();
        assertEquals(1, w3ce7.size());
        assertEquals("depth2", ((org.w3c.dom.Element) w3ce7.get(0)).getAttribute("id"));
    }

    @Test
    void selectorFirstLast() {
        Document doc = parser.parse("text1<div></div><div id=myid></div><div></div>text2");
        var w3cDoc = W3CDom.toW3CDocument(doc);
        // div:first-child
        var matcher = Selector.select().element("div").isFirstChild().toMatcher();
        assertTrue(doc.getAllNodesMatching(matcher).isEmpty());
        assertTrue(W3CDom.getAllNodesMatching(w3cDoc, matcher).toList().isEmpty());

        // :first-child
        matcher = Selector.select().isFirstChild().toMatcher();
        List<Node> nodes = doc.getAllNodesMatching(matcher);
        assertEquals(3, nodes.size());
        assertEquals("html", nodes.get(0).getNodeName());
        assertEquals("head", nodes.get(1).getNodeName());
        assertEquals("#text", nodes.get(2).getNodeName());
        assertEquals("text1", ((Text) nodes.get(2)).getData());
        var w3cNodes = W3CDom.getAllNodesMatching(w3cDoc, matcher).toList();
        assertEquals(3, w3cNodes.size());
        assertEquals("html", w3cNodes.get(0).getNodeName());
        assertEquals("head", w3cNodes.get(1).getNodeName());
        assertEquals("#text", w3cNodes.get(2).getNodeName());
        assertEquals("text1", ((org.w3c.dom.Text) w3cNodes.get(2)).getData());

        // body :first-child
        matcher = Selector.select().element("body").withDescendant().isFirstChild().toMatcher();
        List<Node> txtNode = doc.getAllNodesMatching(matcher);
        var w3cTxtNode = W3CDom.getAllNodesMatching(w3cDoc, matcher).toList();
        assertEquals(1, txtNode.size());
        assertEquals("text1", ((Text) txtNode.get(0)).getData());

        assertEquals(1, w3cTxtNode.size());
        assertEquals("text1", ((org.w3c.dom.Text) w3cTxtNode.get(0)).getData());
    }

 */
}
