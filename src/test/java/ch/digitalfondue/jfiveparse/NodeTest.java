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

import java.util.Arrays;
import java.util.EnumSet;

import org.junit.Assert;
import org.junit.Test;

public class NodeTest {

    final Parser parser = new Parser();

    @Test
    public void testGetElementById() {
        Document doc = parser.parse("<div>1</div><div id=myid>2</div><div>3</div>");
        Assert.assertNotNull(doc.getElementById("myid"));
        Assert.assertEquals("2", doc.getElementById("myid").getTextContent());
    }

    @Test
    public void testGetElementByIdNotFound() {
        Document doc = parser.parse("<div>1</div><div id=myid>2</div><div>3</div>");
        Assert.assertNull(doc.getElementById("myid2"));
    }

    @Test
    public void testNodeSibling() {
        Document doc = parser.parse("<div>1</div><div id=myid>2</div><div>3</div>");
        Element e = doc.getElementById("myid");
        Assert.assertNotNull(e);
        Node parent = e.getParentNode();

        Assert.assertEquals(parent.getChildNodes().get(0), e.getPreviousSibling());
        Assert.assertEquals(parent.getChildNodes().get(0), e.getPreviousElementSibling());
        Assert.assertEquals(parent.getChildNodes().get(2), e.getNextSibling());
        Assert.assertEquals(parent.getChildNodes().get(2), e.getNextElementSibling());

        Assert.assertEquals(parent.getChildNodes().get(2), parent.getLastChild());
        Assert.assertEquals(parent.getChildNodes().get(2), parent.getLastElementChild());

        Assert.assertEquals(parent.getChildNodes().get(0), parent.getFirstChild());
        Assert.assertEquals(parent.getChildNodes().get(0), parent.getFirstElementChild());
    }

    @Test
    public void testInnerAndOuterHtml() {
        Document doc = parser.parse("<div>1</div><div id=myid>2</div><div>3</div>");
        Element e = doc.getElementById("myid");
        Assert.assertEquals("2", e.getInnerHTML());
        Assert.assertEquals("<div id=\"myid\">2</div>", e.getOuterHTML());
        Assert.assertEquals("<div id=myid>2</div>", e.getOuterHTML(EnumSet.of(Option.PRINT_ORIGINAL_ATTRIBUTE_QUOTE)));
    }

    @Test
    public void testGetElementsByTagName() {
        Document doc = parser.parse("<div>1</div><span id=myid>2</span><div>3</div>");
        Assert.assertEquals(2, doc.getElementsByTagName("div").size());
        Assert.assertEquals(1, doc.getElementsByTagName("span").size());

        Assert.assertEquals(2, doc.getElementsByTagNameNS("div", Node.NAMESPACE_HTML).size());
        Assert.assertEquals(1, doc.getElementsByTagNameNS("span", Node.NAMESPACE_HTML).size());

        Assert.assertEquals(0, doc.getElementsByTagNameNS("div", Node.NAMESPACE_SVG).size());
        Assert.assertEquals(0, doc.getElementsByTagNameNS("span", Node.NAMESPACE_SVG).size());
    }

    @Test
    public void testReplaceChild() {
        Document doc = parser.parse("<div id=myid>1<span>2</span><div>3</div></div>");
        Element e = doc.getElementById("myid");
        Node last = e.getChildNodes().get(2);
        Node newNode = new Element("span");
        e.replaceChild(newNode, last);

        Assert.assertNull(last.getParentNode());
        Assert.assertEquals(newNode, e.getChildNodes().get(2));
    }

    @Test
    public void testInsertBeforeChild() {
        Document doc = parser.parse("<div id=myid>1<span>2</span><div>3</div></div>");
        Element e = doc.getElementById("myid");
        Node last = e.getChildNodes().get(2);
        Node newNode = new Element("span");
        e.insertBefore(newNode, last);

        Assert.assertEquals(newNode, e.getChildNodes().get(2));
        Assert.assertEquals(last, e.getChildNodes().get(3));
        Assert.assertEquals(e, newNode.getParentNode());
    }

    @Test
    public void testClass() {
        Document doc = parser.parse("<div id=myid class=></div>");
        Element e = doc.getElementById("myid");
        Assert.assertEquals("", e.getClassName());
        Assert.assertTrue(e.getClassList().isEmpty());

        e.getClassList().add("plop");

        Assert.assertEquals("plop", e.getClassName());
        Assert.assertEquals(Arrays.asList("plop"), e.getClassList());

        e.getClassList().remove("plop");
        Assert.assertEquals("", e.getClassName());
        Assert.assertTrue(e.getClassList().isEmpty());

        e.getClassList().add("plop", "hurr");
        Assert.assertEquals("plop hurr", e.getClassName());
        Assert.assertEquals(Arrays.asList("plop", "hurr"), e.getClassList());

        Assert.assertFalse(e.getClassList().toggle("plop"));
        Assert.assertEquals(Arrays.asList("hurr"), e.getClassList());
        Assert.assertTrue(e.getClassList().toggle("plop"));
        Assert.assertEquals(Arrays.asList("hurr", "plop"), e.getClassList());

        Assert.assertFalse(e.getClassList().toggle("plop", false));
        Assert.assertEquals(Arrays.asList("hurr", "plop"), e.getClassList());
        Assert.assertFalse(e.getClassList().toggle("plop", true));
        Assert.assertEquals(Arrays.asList("hurr"), e.getClassList());

        Document doc2 = parser.parse("<div id=myid class=' my class    \n abc '></div>");
        Element e2 = doc2.getElementById("myid");
        Assert.assertEquals("my class abc", e2.getClassName());
        Assert.assertFalse(e2.getClassList().isEmpty());
        Assert.assertEquals(Arrays.asList("my", "class", "abc"), e2.getClassList());
    }

    @Test
    public void testSetInnerHTML() {
        Document doc = parser.parse("<div id=myid class=></div>");
        Element e = doc.getElementById("myid");
        Assert.assertEquals("", e.getInnerHTML());
        e.setInnerHTML("<h1>test</h1>plop <p>test");
        Assert.assertEquals("<h1>test</h1>plop <p>test</p>", e.getInnerHTML());
    }

    @Test
    public void testContains() {
        Document doc = parser.parse("<div id=cont> text<div> bla bla <div id=myid class=>blabla</div>plop</div></div>");
        Element e = doc.getElementById("myid");
        Element cont = doc.getElementById("cont");
        Assert.assertTrue(cont.contains(e));
        Assert.assertFalse(e.contains(cont));
    }
    
    @Test
    public void testMatch() {
        Document doc = parser.parse("<div id=cont> text<div> bla bla <div id=myid class=>blabla</div>plop</div></div>");
        Assert.assertEquals(1, doc.getAllNodesMatching(new NodeMatchers.HasAttribute("id"), true).size());
        Assert.assertEquals(2, doc.getAllNodesMatching(new NodeMatchers.HasAttribute("id"), false).size());
    }

    @Test
    public void nomalize() {

        //same case of https://developer.mozilla.org/en-US/docs/Web/API/Node/normalize

        Element wrapper = new Element("div");
        wrapper.appendChild(new Text("Part 1"));
        wrapper.appendChild(new Text("Part 2"));

        wrapper.normalize();

        Assert.assertEquals(1, wrapper.getChildNodes().size());
        Assert.assertEquals("Part 1Part 2", wrapper.getChildNodes().get(0).getTextContent());

    }
    
    @Test
    public void nomalizeEmpty1() {

        //same case of https://developer.mozilla.org/en-US/docs/Web/API/Node/normalize

        Element wrapper = new Element("div");
        wrapper.appendChild(new Text(""));

        wrapper.normalize();

        Assert.assertEquals(0, wrapper.getChildNodes().size());

    }
    
    @Test
    public void nomalizeEmpty2() {

        //same case of https://developer.mozilla.org/en-US/docs/Web/API/Node/normalize

        Element wrapper = new Element("div");
        wrapper.appendChild(new Text(""));
        wrapper.appendChild(new Text(""));

        wrapper.normalize();

        Assert.assertEquals(0, wrapper.getChildNodes().size());

    }

    @Test
    public void nomalizeNested() {

        //same case of https://developer.mozilla.org/en-US/docs/Web/API/Node/normalize

        Element wrapper = new Element("div");
        wrapper.appendChild(new Text("Part 1"));
        wrapper.appendChild(new Text("Part 2"));
        Element wrapper2 = new Element("div");
        wrapper.appendChild(wrapper2);
        Element wrapper3 = new Element("div");
        wrapper2.appendChild(wrapper3);
        wrapper3.appendChild(new Text("Part 3"));
        wrapper3.appendChild(new Text("Part 4"));

        wrapper.appendChild(new Text("Part 5"));
        wrapper.appendChild(new Text("Part 6"));


        wrapper.normalize();

        Assert.assertEquals(3,wrapper.getChildNodes().size());
        Assert.assertEquals("Part 1Part 2", wrapper.getChildNodes().get(0).getTextContent());
        Assert.assertEquals(1, wrapper3.getChildNodes().size());
        Assert.assertEquals("Part 3Part 4", wrapper3.getChildNodes().get(0).getTextContent());
        Assert.assertEquals("Part 5Part 6", wrapper.getChildNodes().get(2).getTextContent());

    }
    
    @Test
    public void nomalizeEmptyNested() {

        //same case of https://developer.mozilla.org/en-US/docs/Web/API/Node/normalize

        Element wrapper = new Element("div");
        wrapper.appendChild(new Text(""));
        wrapper.appendChild(new Text(""));
        Element wrapper2 = new Element("div");
        wrapper.appendChild(wrapper2);
        Element wrapper3 = new Element("div");
        wrapper2.appendChild(wrapper3);
        wrapper3.appendChild(new Text(""));
        wrapper3.appendChild(new Text(""));

        wrapper.appendChild(new Text(""));
        wrapper.appendChild(new Text(""));


        wrapper.normalize();

        Assert.assertEquals(1,wrapper.getChildNodes().size());
        Assert.assertEquals("<div><div></div></div>", wrapper.getInnerHTML());
        Assert.assertEquals(1, wrapper.getChildNodes().get(0).getChildCount());
        Assert.assertEquals(0, wrapper.getChildNodes().get(0).getChildNodes().get(0).getChildCount());

    }

}
