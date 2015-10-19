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

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;

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
        Assert.assertEquals("<html><head></head><body><div></div><span></span></body></html>", d.getInnerHTML());
        Assert.assertEquals("<html><head></head><body><DiV></DiV><sPaN></sPaN></body></html>", d.getInnerHTML(EnumSet.of(Option.PRINT_ORIGINAL_TAG_CASE)));
    }

    @Test
    public void testAttributeCase() {
        Parser p = parser();
        Document d = p.parse("<div AttrIbutE=\"my-attribute\"></div>");
        Assert.assertEquals("<html><head></head><body><div attribute=\"my-attribute\"></div></body></html>", d.getInnerHTML());
        Assert.assertEquals("<html><head></head><body><div AttrIbutE=\"my-attribute\"></div></body></html>", d.getInnerHTML(EnumSet.of(Option.PRINT_ORIGINAL_ATTRIBUTES_CASE)));
    }

    @Test
    public void testAttributeQuote() {
        Parser p = parser();
        Document d = p.parse("<div attr=val attr2='val2' attr3=\"val3\"></div>");
        Assert.assertEquals("<html><head></head><body><div attr=\"val\" attr2=\"val2\" attr3=\"val3\"></div></body></html>", d.getInnerHTML());
        Assert.assertEquals("<html><head></head><body><div attr=val attr2='val2' attr3=\"val3\"></div></body></html>",
                d.getInnerHTML(EnumSet.of(Option.PRINT_ORIGINAL_ATTRIBUTE_QUOTE)));
    }

    @Test
    public void testHideEmptyAttributeValue() {
        Parser p = parser();
        Document d = p.parse("<div attr attr2='' attr3=\"\"></div>");
        Assert.assertEquals("<html><head></head><body><div attr=\"\" attr2=\"\" attr3=\"\"></div></body></html>", d.getInnerHTML());
        Assert.assertEquals("<html><head></head><body><div attr attr2='' attr3=\"\"></div></body></html>", d.getInnerHTML(EnumSet.of(Option.PRINT_ORIGINAL_ATTRIBUTE_QUOTE)));
        Assert.assertEquals("<html><head></head><body><div attr attr2 attr3></div></body></html>", d.getInnerHTML(EnumSet.of(Option.HIDE_EMPTY_ATTRIBUTE_VALUE)));
    }
}
