/**
 * Copyright (C) 2015 digitalfondue (info@digitalfondue.ch)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.digitalfondue.jfiveparse;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class NodeMatchersTest {

    final Parser parser = new Parser();

    @Test
    public void firstChildTest() {
        Document doc = parser.parse("<div></div><div id=myid></div><div></div>");
        List<Element> div = doc.getElementsByTagName("body").get(0).getAllNodesMatching(new NodeMatchers.IsFirstChild());
        Assert.assertEquals(1, div.size());
        Assert.assertEquals(doc.getElementsByTagName("body").get(0).getChildNodes().get(0), div.get(0));
    }

    @Test
    public void firstElementTest() {
        Document doc = parser.parse("text<div></div><div id=myid></div><div></div>");
        List<Element> div = doc.getElementsByTagName("body").get(0).getAllNodesMatching(new NodeMatchers.IsFirstElementChild());
        Assert.assertEquals(1, div.size());
        Assert.assertEquals(doc.getElementsByTagName("body").get(0).getChildNodes().get(1), div.get(0));
    }

    @Test
    public void lastElementTest() {
        Document doc = parser.parse("text<div></div><div id=myid></div><div></div>text");
        List<Element> div = doc.getElementsByTagName("body").get(0).getAllNodesMatching(new NodeMatchers.IsLastElementChild());
        Assert.assertEquals(1, div.size());
        Assert.assertEquals(doc.getElementsByTagName("body").get(0).getChildNodes().get(3), div.get(0));

    }

    @Test
    public void lastChildTest() {
        Document doc = parser.parse("text<div></div><div id=myid></div><div></div>");
        List<Element> div = doc.getElementsByTagName("body").get(0).getAllNodesMatching(new NodeMatchers.IsLastChild());
        Assert.assertEquals(1, div.size());
        Assert.assertEquals(doc.getElementsByTagName("body").get(0).getChildNodes().get(3), div.get(0));
    }

    @Test
    public void hasAttributeValueEqTest() {
        Document doc = parser.parse("text<div></div><div id=myid></div><div id=myid2></div>");
        List<Element> divIdMyId = doc.getAllNodesMatching(new NodeMatchers.HasAttribute("id", "myid", NodeMatchers.ATTRIBUTE_MATCH_VALUE_EQ));
        List<Element> divId = doc.getAllNodesMatching(new NodeMatchers.HasAttribute("id"));
        Assert.assertEquals(1, divIdMyId.size());
        Assert.assertEquals(2, divId.size());
        Assert.assertEquals("myid", divId.get(0).getAttribute("id"));
        Assert.assertEquals("myid2", divId.get(1).getAttribute("id"));
    }

    @Test
    public void hasAttributeValueInListTest() {
        Document doc = parser.parse("text<div class='class2 class3'></div><div class='class2 class1 class3'></div><div id=myid2></div>");
        List<Element> divClass = doc.getAllNodesMatching(new NodeMatchers.HasAttribute("class", "class1", NodeMatchers.ATTRIBUTE_MATCH_VALUE_IN_LIST));
        Assert.assertEquals(1, divClass.size());
        Assert.assertEquals("class2 class1 class3", divClass.get(0).getAttribute("class"));

        List<Element> divClasses = doc.getAllNodesMatching(new NodeMatchers.HasAttribute("class", "class2", NodeMatchers.ATTRIBUTE_MATCH_VALUE_IN_LIST));
        Assert.assertEquals(2, divClasses.size());
    }

    @Test
    public void hasAttributeValueStartWithTest() {
        Document doc = parser.parse("text<div class='class2 class3'></div><div class='class2 class1 class3'></div><div id=myid2></div>");
        List<Element> divClass = doc.getAllNodesMatching(new NodeMatchers.HasAttribute("class", "class2 class1", NodeMatchers.ATTRIBUTE_MATCH_VALUE_START_WITH));
        Assert.assertEquals(1, divClass.size());
        Assert.assertEquals("class2 class1 class3", divClass.get(0).getAttribute("class"));

        List<Element> divClasses = doc.getAllNodesMatching(new NodeMatchers.HasAttribute("class", "class2", NodeMatchers.ATTRIBUTE_MATCH_VALUE_START_WITH));
        Assert.assertEquals(2, divClasses.size());
    }

    @Test
    public void hasAttributeValueEndWithTest() {
        Document doc = parser.parse("text<div class='class2 class3'></div><div class='class2 class1 class3'></div><div id=myid2></div>");
        List<Element> divClass = doc.getAllNodesMatching(new NodeMatchers.HasAttribute("class", "class1 class3", NodeMatchers.ATTRIBUTE_MATCH_VALUE_END_WITH));
        Assert.assertEquals(1, divClass.size());
        Assert.assertEquals("class2 class1 class3", divClass.get(0).getAttribute("class"));

        List<Element> divClasses = doc.getAllNodesMatching(new NodeMatchers.HasAttribute("class", "class3", NodeMatchers.ATTRIBUTE_MATCH_VALUE_END_WITH));
        Assert.assertEquals(2, divClasses.size());
    }

    @Test
    public void hasAttributeValueContainTest() {
        Document doc = parser.parse("text<div class='class2 class3'></div><div class='class2 class1 class3'></div><div id=myid2></div>");
        List<Element> divClass = doc.getAllNodesMatching(new NodeMatchers.HasAttribute("class", "class2 class1", NodeMatchers.ATTRIBUTE_MATCH_VALUE_CONTAINS));
        Assert.assertEquals(1, divClass.size());
        Assert.assertEquals("class2 class1 class3", divClass.get(0).getAttribute("class"));

        List<Element> divClasses = doc.getAllNodesMatching(new NodeMatchers.HasAttribute("class", "cl", NodeMatchers.ATTRIBUTE_MATCH_VALUE_CONTAINS));
        Assert.assertEquals(2, divClasses.size());
    }
}
