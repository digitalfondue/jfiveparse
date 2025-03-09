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

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NodeMatchersTest {

    final Parser parser = new Parser();

    @Test
    void firstChildTest() {
        Document doc = parser.parse("<div></div><div id=myid></div><div></div>");
        List<Element> div = doc.getElementsByTagName("body").get(0).getAllNodesMatching(Selector.select().isFirstChild().toMatcher());
        assertEquals(1, div.size());
        assertEquals(doc.getElementsByTagName("body").get(0).getChildNodes().get(0), div.get(0));
    }

    @Test
    void firstElementTest() {
        Document doc = parser.parse("text<div></div><div id=myid></div><div></div>");
        List<Element> div = doc.getElementsByTagName("body").get(0).getAllNodesMatching(Selector.select().isFirstElementChild().toMatcher());
        assertEquals(1, div.size());
        assertEquals(doc.getElementsByTagName("body").get(0).getChildNodes().get(1), div.get(0));
    }

    @Test
    void lastElementTest() {
        Document doc = parser.parse("text<div></div><div id=myid></div><div></div>text");
        List<Element> div = doc.getElementsByTagName("body").get(0).getAllNodesMatching(Selector.select().isLastElementChild().toMatcher());
        assertEquals(1, div.size());
        assertEquals(doc.getElementsByTagName("body").get(0).getChildNodes().get(3), div.get(0));

    }

    @Test
    void lastChildTest() {
        Document doc = parser.parse("text<div></div><div id=myid></div><div></div>");
        List<Element> div = doc.getElementsByTagName("body").get(0).getAllNodesMatching(Selector.select().isLastChild().toMatcher());
        assertEquals(1, div.size());
        assertEquals(doc.getElementsByTagName("body").get(0).getChildNodes().get(3), div.get(0));
    }

    @Test
    void hasAttributeValueEqTest() {
        Document doc = parser.parse("text<div></div><div id=myid></div><div id=myid2></div>");
        List<Element> divIdMyId = doc.getAllNodesMatching(new NodeMatchers.HasAttribute("id", "myid", NodeMatchers.ATTRIBUTE_MATCH_VALUE_EQ));
        List<Element> divId = doc.getAllNodesMatching(new NodeMatchers.HasAttribute("id"));
        assertEquals(1, divIdMyId.size());
        assertEquals(2, divId.size());
        assertEquals("myid", divId.get(0).getAttribute("id"));
        assertEquals("myid2", divId.get(1).getAttribute("id"));
    }

    @Test
    void hasAttributeValueInListTest() {
        Document doc = parser.parse("text<div class='class2 class3'></div><div class='class2 class1 class3'></div><div id=myid2></div>");
        List<Element> divClass = doc.getAllNodesMatching(new NodeMatchers.HasAttribute("class", "class1", NodeMatchers.ATTRIBUTE_MATCH_VALUE_IN_LIST));
        assertEquals(1, divClass.size());
        assertEquals("class2 class1 class3", divClass.get(0).getAttribute("class"));

        List<Element> divClasses = doc.getAllNodesMatching(new NodeMatchers.HasAttribute("class", "class2", NodeMatchers.ATTRIBUTE_MATCH_VALUE_IN_LIST));
        assertEquals(2, divClasses.size());
    }

    @Test
    void hasAttributeValueStartWithTest() {
        Document doc = parser.parse("text<div class='class2 class3'></div><div class='class2 class1 class3'></div><div id=myid2></div>");
        List<Element> divClass = doc.getAllNodesMatching(new NodeMatchers.HasAttribute("class", "class2 class1", NodeMatchers.ATTRIBUTE_MATCH_VALUE_START_WITH));
        assertEquals(1, divClass.size());
        assertEquals("class2 class1 class3", divClass.get(0).getAttribute("class"));

        List<Element> divClasses = doc.getAllNodesMatching(new NodeMatchers.HasAttribute("class", "class2", NodeMatchers.ATTRIBUTE_MATCH_VALUE_START_WITH));
        assertEquals(2, divClasses.size());
    }

    @Test
    void hasAttributeValueEndWithTest() {
        Document doc = parser.parse("text<div class='class2 class3'></div><div class='class2 class1 class3'></div><div id=myid2></div>");
        List<Element> divClass = doc.getAllNodesMatching(new NodeMatchers.HasAttribute("class", "class1 class3", NodeMatchers.ATTRIBUTE_MATCH_VALUE_END_WITH));
        assertEquals(1, divClass.size());
        assertEquals("class2 class1 class3", divClass.get(0).getAttribute("class"));

        List<Element> divClasses = doc.getAllNodesMatching(new NodeMatchers.HasAttribute("class", "class3", NodeMatchers.ATTRIBUTE_MATCH_VALUE_END_WITH));
        assertEquals(2, divClasses.size());
    }

    @Test
    void hasAttributeValueContainTest() {
        Document doc = parser.parse("text<div class='class2 class3'></div><div class='class2 class1 class3'></div><div id=myid2></div>");
        List<Element> divClass = doc.getAllNodesMatching(new NodeMatchers.HasAttribute("class", "class2 class1", NodeMatchers.ATTRIBUTE_MATCH_VALUE_CONTAINS));
        assertEquals(1, divClass.size());
        assertEquals("class2 class1 class3", divClass.get(0).getAttribute("class"));

        List<Element> divClasses = doc.getAllNodesMatching(new NodeMatchers.HasAttribute("class", "cl", NodeMatchers.ATTRIBUTE_MATCH_VALUE_CONTAINS));
        assertEquals(2, divClasses.size());
    }

    @Test
    void hasParentMatch() {
        Document doc = parser.parse("<div id=depth1><div id=depth2><div id=depth3></div></div></div>");
        List<Element> e = doc.getAllNodesMatching(Selector.select().attrValEq("id", "depth1").withChild().toMatcher());
        assertEquals(1, e.size());
        assertEquals("depth2", e.get(0).getAttribute("id"));

        List<Element> empty = doc.getAllNodesMatching(Selector.select().attrValEq("id", "depth0").withChild().toMatcher());
        assertEquals(0, empty.size());
    }

    @Test
    void hasAncestorMatch() {
        Document doc = parser.parse("<div id=depth1><div id=depth2><div id=depth3></div></div></div>");

        List<Node> e0 = doc.getAllNodesMatching(Selector.select().attrValEq("id", "depth3").withDescendant().toMatcher());
        assertEquals(0, e0.size());

        List<Element> e1 = doc.getAllNodesMatching(Selector.select().attrValEq("id", "depth2").withDescendant().toMatcher());
        assertEquals(1, e1.size());
        assertEquals("depth3", e1.get(0).getAttribute("id"));

        List<Element> e2 = doc.getAllNodesMatching(Selector.select().attrValEq("id", "depth1").withDescendant().toMatcher());
        assertEquals(2, e2.size());
        assertEquals("depth2", e2.get(0).getAttribute("id"));
        assertEquals("depth3", e2.get(1).getAttribute("id"));

        List<Node> empty = doc.getAllNodesMatching(Selector.select().attrValEq("id", "depth0").withDescendant().toMatcher());
        assertEquals(0, empty.size());
    }

    @Test
    void hasSelectorMatch() {
        // #depth1 > #depth2 > #depth3
        Document doc = parser.parse("<div id=depth1 class='a b c'><div id=depth2 class='a c'><div id=depth3 class='b c'></div></div></div>");
        List<Element> e1 = doc.getAllNodesMatching(Selector.select().id("depth1").withChild().id("depth2").withChild().id("depth3").toMatcher());
        assertEquals(1, e1.size());
        assertEquals("depth3", e1.get(0).getAttribute("id"));

        // div > [id]
        List<Element> e2 = doc.getAllNodesMatching(Selector.select().element("div").withDescendant().attr("id").toMatcher());
        assertEquals(2, e2.size());
        assertEquals("depth2", e2.get(0).getAttribute("id"));
        assertEquals("depth3", e2.get(1).getAttribute("id"));

        // div.a.b
        List<Element> e3 = doc.getAllNodesMatching(Selector.select().element("div").hasClass("a", "b").toMatcher());
        assertEquals(1, e3.size());
        assertEquals("depth1", e3.get(0).getAttribute("id"));

        // .c
        List<Element> e4 = doc.getAllNodesMatching(Selector.select().hasClass("c").toMatcher());
        assertEquals(3, e4.size());

        // .b
        List<Element> e5 = doc.getAllNodesMatching(Selector.select().hasClass("b").toMatcher());
        assertEquals(2, e5.size());
        assertEquals("depth1", e5.get(0).getAttribute("id"));
        assertEquals("depth3", e5.get(1).getAttribute("id"));
        
        // .a .c
        List<Element> e6 = doc.getAllNodesMatching(Selector.select().hasClass("a").withDescendant().hasClass("c").toMatcher());
        assertEquals(2, e6.size());
        assertEquals("depth2", e6.get(0).getAttribute("id"));
        assertEquals("depth3", e6.get(1).getAttribute("id"));
        
        // .a .a
        List<Element> e7 = doc.getAllNodesMatching(Selector.select().hasClass("a").withDescendant().hasClass("a").toMatcher());
        assertEquals(1, e7.size());
        assertEquals("depth2", e7.get(0).getAttribute("id"));
    }

    @Test
    void selectorFirstLast() {
        Document doc = parser.parse("text1<div></div><div id=myid></div><div></div>text2");
        // div:first-child
        assertTrue(doc.getAllNodesMatching(Selector.select().element("div").isFirstChild().toMatcher()).isEmpty());

        // :first-child
        List<Node> nodes = doc.getAllNodesMatching(Selector.select().isFirstChild().toMatcher());
        assertEquals(3, nodes.size());
        assertEquals("html", nodes.get(0).getNodeName());
        assertEquals("head", nodes.get(1).getNodeName());
        assertEquals("#text", nodes.get(2).getNodeName());
        assertEquals("text1", ((Text) nodes.get(2)).getData());

        // body :first-child
        List<Text> txtNode = doc.getAllNodesMatching(Selector.select().element("body").withDescendant().isFirstChild().toMatcher());
        assertEquals(1, txtNode.size());
        assertEquals("text1", txtNode.get(0).getData());
    }
}
