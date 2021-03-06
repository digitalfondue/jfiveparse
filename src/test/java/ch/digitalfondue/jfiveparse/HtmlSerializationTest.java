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

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class HtmlSerializationTest {

    private static Parser parser(Option... options) {
        return new Parser(new HashSet<>(Arrays.asList(options)));
    }
    
    @Test
    public void testTagNameCase() {
        Parser p = parser();
        Document d = p.parse("<DiV></dIV><sPaN></SPAN>");
        Assert.assertEquals("<html><head></head><body><div></div><span></span></body></html>", d.getFirstElementChild().getOuterHTML());
        Assert.assertEquals("<html><head></head><body><DiV></DiV><sPaN></sPaN></body></html>", d.getFirstElementChild().getOuterHTML(EnumSet.of(Option.PRINT_ORIGINAL_TAG_CASE)));
    }

    @Test
    public void testAttributeCase() {
        Parser p = parser();
        Document d = p.parse("<div AttrIbutE=\"my-attribute\"></div>");
        Assert.assertEquals("<html><head></head><body><div attribute=\"my-attribute\"></div></body></html>", d.getFirstElementChild().getOuterHTML());
        Assert.assertEquals("<html><head></head><body><div AttrIbutE=\"my-attribute\"></div></body></html>", d.getFirstElementChild().getOuterHTML(EnumSet.of(Option.PRINT_ORIGINAL_ATTRIBUTES_CASE)));
    }

    @Test
    public void testAttributeQuote() {
        Parser p = parser();
        Document d = p.parse("<div attr=val attr2='val2' attr3=\"val3\"></div>");
        Assert.assertEquals("<html><head></head><body><div attr=\"val\" attr2=\"val2\" attr3=\"val3\"></div></body></html>", d.getFirstElementChild().getOuterHTML());
        Assert.assertEquals("<html><head></head><body><div attr=val attr2='val2' attr3=\"val3\"></div></body></html>",
                d.getFirstElementChild().getOuterHTML(EnumSet.of(Option.PRINT_ORIGINAL_ATTRIBUTE_QUOTE)));
    }

    @Test
    public void testHideEmptyAttributeValue() {
        Parser p = parser();
        Document d = p.parse("<div attr attr2='' attr3=\"\"></div>");
        Assert.assertEquals("<html><head></head><body><div attr=\"\" attr2=\"\" attr3=\"\"></div></body></html>", d.getFirstElementChild().getOuterHTML());
        Assert.assertEquals("<html><head></head><body><div attr attr2='' attr3=\"\"></div></body></html>", d.getFirstElementChild().getOuterHTML(EnumSet.of(Option.PRINT_ORIGINAL_ATTRIBUTE_QUOTE)));
        Assert.assertEquals("<html><head></head><body><div attr attr2 attr3></div></body></html>", d.getFirstElementChild().getOuterHTML(EnumSet.of(Option.HIDE_EMPTY_ATTRIBUTE_VALUE)));
    }

    @Test
    public void testSerializeToByteArray() throws IOException {
        Parser p = parser();
        Document d = p.parse("<DiV></dIV><sPaN></SPAN>");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Writer osw = new OutputStreamWriter(baos, StandardCharsets.UTF_8);
        HtmlSerializer.serialize(d, osw);
        Assert.assertEquals("<html><head></head><body><div></div><span></span></body></html>", new String(baos.toByteArray(), StandardCharsets.UTF_8));
    }

    @Test
    public void testSimpleApi() {
        Document doc = JFiveParse.parse("<html><body>Hello world!</body></html>");
        Assert.assertEquals("<html><head></head><body>Hello world!</body></html>", JFiveParse.serialize(doc));


        // from reader
        Document doc2 = JFiveParse.parse(new StringReader("<html><body>Hello world!</body></html>"));
        Assert.assertEquals("<html><head></head><body>Hello world!</body></html>", JFiveParse.serialize(doc2));


        // parse fragment
        List<Node> fragment = JFiveParse.parseFragment("<p><span>Hello world</span></p>");
        Assert.assertEquals("<p><span>Hello world</span></p>", JFiveParse.serialize(fragment.get(0)));

        // parse fragment from reader
        List<Node> fragment2 = JFiveParse.parseFragment(new StringReader("<p><span>Hello world</span></p>"));
        Assert.assertEquals("<p><span>Hello world</span></p>", JFiveParse.serialize(fragment2.get(0)));
    }
}
