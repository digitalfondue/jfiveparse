package ch.digitalfondue.jfiveparse;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class AttributesTest {

    @Test
    void checkEquality() {
        var a = new Attributes(List.of(new AttributeNode("a", "b"), new AttributeNode("b", "c")));
        var b = new Attributes(List.of(new AttributeNode("b", "c"), new AttributeNode("a", "b")));

        Assertions.assertEquals(a, b);
        Assertions.assertEquals(a.hashCode(), b.hashCode());
    }


    @Test
    void htmlNameBasedOperationsAreCaseInsensitive() {
        // browser: element.attributes.getNamedItem("CLASS") finds "class" for HTML elements
        Element div = new Element("div");
        div.setAttribute("CLASS", "x");

        Attributes attributes = div.getAttributes();
        Assertions.assertEquals("x", attributes.getNamedItem("CLASS"));
        Assertions.assertEquals("x", attributes.getNamedItem("cLaSs"));
        Assertions.assertNotNull(attributes.get("CLASS"));
        Assertions.assertTrue(attributes.containsKey("CLASS"));

        attributes.remove("CLASS");
        Assertions.assertTrue(attributes.isEmpty());
    }

    @Test
    void nonHtmlNameBasedOperationsAreCaseSensitive() {
        // browser: SVG/MathML attributes are case sensitive
        Element svg = new Element("svg", Node.NAMESPACE_SVG);
        svg.setAttribute("viewBox", "0 0 1 1");

        Attributes attributes = svg.getAttributes();
        Assertions.assertEquals("0 0 1 1", attributes.getNamedItem("viewBox"));
        Assertions.assertNull(attributes.getNamedItem("viewbox"));
        Assertions.assertNull(attributes.get("viewbox"));
        Assertions.assertFalse(attributes.containsKey("viewbox"));
    }

    @Test
    void copyPreservesHtmlCaseInsensitivity() {
        Element div = new Element("div");
        div.setAttribute("CLASS", "x");
        Attributes copy = div.getAttributes().copy();
        Assertions.assertEquals("x", copy.getNamedItem("CLASS"));

        Element clone = (Element) div.cloneNode(false);
        Assertions.assertEquals("x", clone.getAttributes().getNamedItem("CLASS"));
    }

    @Test
    void attributeNodeEqualsHashCodeContract() {
        // same namespace/localName/value but different prefix must have the same hashCode
        AttributeNode a = new AttributeNode("x:foo", "v", "x", "urn:ns");
        AttributeNode b = new AttributeNode("y:foo", "v", "y", "urn:ns");
        Assertions.assertEquals(a, b);
        Assertions.assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void generalCheck() {
        var a = new Attributes();

        Assertions.assertTrue(a.isEmpty());

        a.put("key", "value");

        Assertions.assertEquals("value", a.getNamedItem("key"));
        Assertions.assertTrue(a.containsKey("key"));
        Assertions.assertFalse(a.containsKey("key1"));

        Assertions.assertFalse(a.isEmpty());

        a.put("key", "value1");
        Assertions.assertFalse(a.containsKey("value"));
        Assertions.assertEquals("value1", a.getNamedItem("key"));


        a.remove("key");
        Assertions.assertTrue(a.isEmpty());
        a.remove("key");
        Assertions.assertTrue(a.isEmpty());
    }
}
