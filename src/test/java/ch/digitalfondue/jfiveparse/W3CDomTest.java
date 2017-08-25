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

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class W3CDomTest {

    @Test
    public void checkSimpleDomConversion() {
        Parser parser = new Parser();

        Document document = W3CDom.toW3CDocument(parser.parse("<body><p id=42>hello world"));

        Node html = document.getFirstChild();

        Assert.assertEquals("html", html.getNodeName());
        Assert.assertEquals(1, document.getChildNodes().getLength());

        Assert.assertEquals(2, html.getChildNodes().getLength());
        Assert.assertEquals("head", html.getFirstChild().getNodeName());
        Assert.assertEquals("body", html.getLastChild().getNodeName());

        Node body = html.getLastChild();

        Assert.assertEquals(1, body.getChildNodes().getLength());

        Node p = body.getLastChild();

        Assert.assertEquals("p", p.getNodeName());
        Assert.assertEquals("42", p.getAttributes().getNamedItem("id").getNodeValue());

        Assert.assertEquals(1, p.getChildNodes().getLength());

        Node text = p.getLastChild();

        Assert.assertEquals(Node.TEXT_NODE, text.getNodeType());
        Assert.assertEquals("hello world", text.getTextContent());
    }

    @Test
    public void checkSiblingAndNested() {
        Parser parser = new Parser();

        Document document = W3CDom.toW3CDocument(parser.parse("<body><p><span>1</span></p><p><span>2</span></p>"));

        Node body = document.getFirstChild().getLastChild();
        Assert.assertEquals(2, body.getChildNodes().getLength());

        Node p1 = body.getFirstChild();
        Node p2 = p1.getNextSibling();

        Assert.assertEquals("p", p1.getNodeName());
        Assert.assertEquals("p", p2.getNodeName());

        Assert.assertEquals("span", p1.getFirstChild().getNodeName());
        Assert.assertEquals("span", p2.getFirstChild().getNodeName());

        Assert.assertEquals("1", p1.getFirstChild().getFirstChild().getTextContent());
        Assert.assertEquals("2", p2.getFirstChild().getFirstChild().getTextContent());

    }
}
