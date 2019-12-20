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

/**
 * Created by mattia on 28/04/16.
 */
public class ElementTest {
    final Parser parser = new Parser();

    //https://developer.mozilla.org/en-US/docs/Web/API/Element/insertAdjacentHTML
    @Test
    public void testInsertAdjacentHTMLBeforeBegin() {
        Element startNode = (Element) parser.parseFragment(new Element("div"), "<div><p id=myid>foo</p></div>").get(0);
        Element myIdElement = startNode.getElementById("myid");
        myIdElement.insertAdjacentHTML("beforebegin", "<!-- beforebegin -->");
        Assert.assertEquals("<div><!-- beforebegin --><p id=\"myid\">foo</p></div>", startNode.getOuterHTML());
    }

    @Test
    public void testInsertAdjacentHTMLAfterBeginMultiple() {
        Element startNode = (Element) parser.parseFragment(new Element("div"), "<div><p id=myid>foo</p></div>").get(0);
        Element myIdElement = startNode.getElementById("myid");
        myIdElement.insertAdjacentHTML("afterbegin", "<h1>1</h1><h2>2</h2>");
        Assert.assertEquals("<div><p id=\"myid\"><h1>1</h1><h2>2</h2>foo</p></div>", startNode.getOuterHTML());
    }

    @Test(expected = IllegalStateException.class)
    public void testBeforeBeginWithoutParent() {
        Element startNode = (Element) parser.parseFragment(new Element("div"), "<div><p id=myid>foo</p></div>").get(0);
        startNode.parentNode = null;
        startNode.insertAdjacentHTML("beforebegin", "<!-- beforebegin -->");
    }

    @Test
    public void testInsertAdjacentHTMLAfterBegin() {
        Element startNode = (Element) parser.parseFragment(new Element("div"), "<div><p id=myid>foo</p></div>").get(0);
        Element myIdElement = startNode.getElementById("myid");
        myIdElement.insertAdjacentHTML("afterbegin", "<!-- afterbegin -->");
        Assert.assertEquals("<div><p id=\"myid\"><!-- afterbegin -->foo</p></div>", startNode.getOuterHTML());
    }

    @Test
    public void testInsertAdjacentHTMLBeforeEnd() {
        Element startNode = (Element) parser.parseFragment(new Element("div"), "<div><p id=myid>foo</p></div>").get(0);
        Element myIdElement = startNode.getElementById("myid");
        myIdElement.insertAdjacentHTML("beforeend", "<!-- beforeend -->");
        Assert.assertEquals("<div><p id=\"myid\">foo<!-- beforeend --></p></div>", startNode.getOuterHTML());
    }

    @Test
    public void testInsertAdjacentHTMLBeforeEndMultiple() {
        Element startNode = (Element) parser.parseFragment(new Element("div"), "<div><p id=myid>foo</p></div>").get(0);
        Element myIdElement = startNode.getElementById("myid");
        myIdElement.insertAdjacentHTML("beforeend", "<h1>1</h1><h2>2</h2>");
        Assert.assertEquals("<div><p id=\"myid\">foo<h1>1</h1><h2>2</h2></p></div>", startNode.getOuterHTML());
    }

    @Test
    public void testInsertAdjacentHTMLAfterEnd() {
        Element startNode = (Element) parser.parseFragment(new Element("div"), "<div><p id=myid>foo</p></div>").get(0);
        Element myIdElement = startNode.getElementById("myid");
        myIdElement.insertAdjacentHTML("afterend", "<!-- afterend -->");
        Assert.assertEquals("<div><p id=\"myid\">foo</p><!-- afterend --></div>", startNode.getOuterHTML());
    }

    @Test(expected = IllegalStateException.class)
    public void testAfterEndWithoutParent() {
        Element startNode = (Element) parser.parseFragment(new Element("div"), "<div><p id=myid>foo</p></div>").get(0);
        startNode.parentNode = null;
        startNode.insertAdjacentHTML("afterend", "<!-- beforebegin -->");
    }

    @Test(expected = IllegalStateException.class)
    public void testWrongPosition() {
        Element startNode = (Element) parser.parseFragment(new Element("div"), "<div><p id=myid>foo</p></div>").get(0);
        startNode.insertAdjacentHTML("plop", "<!-- plop -->");
    }
    
    @Test
    public void testAll() {
        Element startNode = (Element) parser.parseFragment(new Element("div"), "<div><p id=myid>foo</p></div>").get(0);
        Element myIdElement = startNode.getElementById("myid");
        myIdElement.insertAdjacentHTML("beforebegin", "<!-- beforebegin -->");
        myIdElement.insertAdjacentHTML("afterbegin", "<!-- afterbegin -->");
        myIdElement.insertAdjacentHTML("beforeend", "<!-- beforeend -->");
        myIdElement.insertAdjacentHTML("afterend", "<!-- afterend -->");
        Assert.assertEquals("<div><!-- beforebegin --><p id=\"myid\"><!-- afterbegin -->foo<!-- beforeend --></p><!-- afterend --></div>", startNode.getOuterHTML());
    }

    @Test
    public void testInsertAdjacentElementAll() {
        Element startNode = (Element) parser.parseFragment(new Element("div"), "<div><p id=myid>foo</p></div>").get(0);
        Element myIdElement = startNode.getElementById("myid");
        myIdElement.insertAdjacentElement("beforebegin", new Element("beforebegin"));
        myIdElement.insertAdjacentElement("afterbegin", new Element("afterbegin"));
        myIdElement.insertAdjacentElement("beforeend", new Element("beforeend"));
        myIdElement.insertAdjacentElement("afterend", new Element("afterend"));
        Assert.assertEquals("<div><beforebegin></beforebegin><p id=\"myid\"><afterbegin></afterbegin>foo<beforeend></beforeend></p><afterend></afterend></div>", startNode.getOuterHTML());
    }

    @Test(expected = IllegalStateException.class)
    public void testWrongElementPosition() {
        Element startNode = (Element) parser.parseFragment(new Element("div"), "<div><p id=myid>foo</p></div>").get(0);
        startNode.insertAdjacentElement("plop", new Element("plop"));
    }

    @Test(expected = IllegalStateException.class)
    public void testWrongTextPosition() {
        Element startNode = (Element) parser.parseFragment(new Element("div"), "<div><p id=myid>foo</p></div>").get(0);
        startNode.insertAdjacentText("plop", "plop");
    }

    @Test
    public void testInsertAdjacentTextAll() {
        Element startNode = (Element) parser.parseFragment(new Element("div"), "<div><p id=myid>foo</p></div>").get(0);
        Element myIdElement = startNode.getElementById("myid");
        myIdElement.insertAdjacentText("beforebegin", "beforebegin");
        myIdElement.insertAdjacentText("afterbegin", "afterbegin");
        myIdElement.insertAdjacentText("beforeend", "beforeend");
        myIdElement.insertAdjacentText("afterend", "afterend");
        Assert.assertEquals("<div>beforebegin<p id=\"myid\">afterbeginfoobeforeend</p>afterend</div>", startNode.getOuterHTML());
    }

    @Test
    public void testGetTagName() {
        Element div = (Element) parser.parseFragment(new Element("div"), "<div><p id=myid>foo</p></div>").get(0);
        Assert.assertEquals("DIV", div.getTagName());


        Element svg = (Element) parser.parseFragment(new Element("div"), "<svg><path id=myid>foo</path></svg>").get(0);
        Assert.assertEquals("svg", svg.getTagName());
        Assert.assertEquals("path", svg.getFirstElementChild().getTagName());
    }
}
