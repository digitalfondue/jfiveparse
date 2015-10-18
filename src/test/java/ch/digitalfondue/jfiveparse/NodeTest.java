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

}
