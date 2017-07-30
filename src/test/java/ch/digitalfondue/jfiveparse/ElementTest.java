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
        Node startNode = parser.parseFragment(new Element("div"), "<div><p id=myid>foo</p></div>").get(0);
        Element myIdElement = startNode.getElementById("myid");
        myIdElement.insertAdjacentHTML("beforebegin", "<!-- beforebegin -->");
        Assert.assertEquals("<div><!-- beforebegin --><p id=\"myid\">foo</p></div>", startNode.getOuterHTML());
    }

    @Test
    public void testInsertAdjacentHTMLAfterBegin() {
    	Node startNode = parser.parseFragment(new Element("div"), "<div><p id=myid>foo</p></div>").get(0);
        Element myIdElement = startNode.getElementById("myid");
        myIdElement.insertAdjacentHTML("afterbegin", "<!-- afterbegin -->");
        Assert.assertEquals("<div><p id=\"myid\"><!-- afterbegin -->foo</p></div>", startNode.getOuterHTML());
    }

    @Test
    public void testInsertAdjacentHTMLBeforeEnd() {
    	Node startNode = parser.parseFragment(new Element("div"), "<div><p id=myid>foo</p></div>").get(0);
        Element myIdElement = startNode.getElementById("myid");
        myIdElement.insertAdjacentHTML("beforeend", "<!-- beforeend -->");
        Assert.assertEquals("<div><p id=\"myid\">foo<!-- beforeend --></p></div>", startNode.getOuterHTML());
    }

    @Test
    public void testInsertAdjacentHTMLAfterEnd() {
    	Node startNode = parser.parseFragment(new Element("div"), "<div><p id=myid>foo</p></div>").get(0);
        Element myIdElement = startNode.getElementById("myid");
        myIdElement.insertAdjacentHTML("afterend", "<!-- afterend -->");
        Assert.assertEquals("<div><p id=\"myid\">foo</p><!-- afterend --></div>", startNode.getOuterHTML());
    }
    
    @Test
    public void testAll() {
    	Node startNode = parser.parseFragment(new Element("div"), "<div><p id=myid>foo</p></div>").get(0);
        Element myIdElement = startNode.getElementById("myid");
        myIdElement.insertAdjacentHTML("beforebegin", "<!-- beforebegin -->");
        myIdElement.insertAdjacentHTML("afterbegin", "<!-- afterbegin -->");
        myIdElement.insertAdjacentHTML("beforeend", "<!-- beforeend -->");
        myIdElement.insertAdjacentHTML("afterend", "<!-- afterend -->");
        Assert.assertEquals("<div><!-- beforebegin --><p id=\"myid\"><!-- afterbegin -->foo<!-- beforeend --></p><!-- afterend --></div>", startNode.getOuterHTML());
    }
}
