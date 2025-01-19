/**
 * Copyright © 2019 digitalfondue (info@digitalfondue.ch)
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

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;


class DocumentTest {


    @Test
    void checkDocumentRelatedMethods() {
        Document d1 = new Parser().parse("<!DOCTYPE html><div>Hello World</div>");
        assertEquals("<!DOCTYPE html><html><head></head><body><div>Hello World</div></body></html>", HtmlSerializer.serialize(d1));

        assertEquals("<!DOCTYPE html><html><head></head><body><div>Hello World</div></body></html>", JFiveParse.serialize(JFiveParse.parse("<!DOCTYPE html><div>Hello World</div>")));
        assertEquals("<!DOCTYPE html><html><head></head><body><div>Hello World</div></body></html>", JFiveParse.serialize(JFiveParse.parse(new StringReader("<!DOCTYPE html><div>Hello World</div>"))));

        assertEquals("html", d1.getDocumentElement().getNodeName());
        assertEquals("<head></head>", d1.getHead().getOuterHTML());
        assertEquals("<body><div>Hello World</div></body>", d1.getBody().getOuterHTML());

        Element body = new Element("body");
        body.setId("newBody");

        d1.setBody(body);

        assertEquals("<body id=\"newBody\"></body>", d1.getBody().getOuterHTML());
    }
}
